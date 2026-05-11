import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GPSApp {
    private static final Graph graph = new Graph();
    private static final RouteCalculator calculator = new RouteCalculator();
    private static final RouteStorage storage = new RouteStorage();

    public static void main(String[] args) throws IOException {
        buildSampleMap();

        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Serve index.html at root
        server.createContext("/", new StaticFileHandler());

        // API endpoints
        server.createContext("/api/map", new MapHandler());
        server.createContext("/api/route", new RouteHandler());
        server.createContext("/api/history", new HistoryHandler());
        server.createContext("/api/clear", new ClearHandler());

        server.setExecutor(null); // creates a default executor
        System.out.println("Server started on http://localhost:" + port);
        server.start();
    }

    private static void buildSampleMap() {
        Location delhi = new Location("Delhi", 28.6139, 77.2090);
        Location jaipur = new Location("Jaipur", 26.9124, 75.7873);
        Location ahmedabad = new Location("Ahmedabad", 23.0225, 72.5714);
        Location mumbai = new Location("Mumbai", 19.0760, 72.8777);
        Location pune = new Location("Pune", 18.5204, 73.8567);
        Location hyderabad = new Location("Hyderabad", 17.3850, 78.4867);
        Location bengaluru = new Location("Bengaluru", 12.9716, 77.5946);
        Location chennai = new Location("Chennai", 13.0827, 80.2707);

        graph.addRoad(delhi, jaipur, 280, 240);
        graph.addRoad(jaipur, ahmedabad, 650, 540);
        graph.addRoad(ahmedabad, mumbai, 530, 420);
        graph.addRoad(mumbai, pune, 150, 180);
        graph.addRoad(pune, hyderabad, 560, 480);
        graph.addRoad(hyderabad, bengaluru, 570, 480);
        graph.addRoad(bengaluru, chennai, 350, 300);
        graph.addRoad(delhi, mumbai, 1400, 900);
        graph.addRoad(hyderabad, chennai, 630, 540);
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            File file = new File("index.html");
            if (!file.exists()) {
                String response = "404 (Not Found)";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            exchange.sendResponseHeaders(200, file.length());
            try (OutputStream os = exchange.getResponseBody();
                 FileInputStream fs = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int count;
                while ((count = fs.read(buffer)) >= 0) {
                    os.write(buffer, 0, count);
                }
            }
        }
    }

    static class MapHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder json = new StringBuilder("{");
            Set<Location> locations = graph.getAllLocations();
            int locCount = 0;
            for (Location loc : locations) {
                json.append("\"").append(loc.getName()).append("\": [");
                List<Edge> edges = graph.getNeighbours(loc);
                for (int i = 0; i < edges.size(); i++) {
                    json.append("\"").append(edges.get(i).toString().replace("\"", "\\\"")).append("\"");
                    if (i < edges.size() - 1) json.append(", ");
                }
                json.append("]");
                if (++locCount < locations.size()) json.append(", ");
            }
            json.append("}");

            sendJsonResponse(exchange, json.toString());
        }
    }

    static class RouteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
            String sourceName = params.get("source");
            String destName = params.get("dest");

            Location source = graph.getLocationByName(sourceName);
            Location dest = graph.getLocationByName(destName);

            StringBuilder json = new StringBuilder("{");
            if (source == null || dest == null) {
                json.append("\"found\": false, \"message\": \"City not found\"}");
            } else {
                RouteCalculator.RouteResult result = calculator.findShortestPath(graph, source, dest);
                if (result.found) {
                    storage.saveRoute(result, source.getName(), dest.getName());
                    json.append("\"found\": true, ");
                    json.append("\"totalDistance\": ").append(result.totalDistance).append(", ");
                    json.append("\"totalTime\": ").append(result.totalTime).append(", ");
                    json.append("\"path\": [");
                    for (int i = 0; i < result.path.size(); i++) {
                        json.append("\"").append(result.path.get(i).getName()).append("\"");
                        if (i < result.path.size() - 1) json.append(", ");
                    }
                    json.append("]");
                } else {
                    json.append("\"found\": false");
                }
                json.append("}");
            }

            sendJsonResponse(exchange, json.toString());
        }
    }

    static class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<String> history = storage.loadAllRoutes();
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < history.size(); i++) {
                json.append("\"").append(history.get(i).replace("\"", "\\\"")).append("\"");
                if (i < history.size() - 1) json.append(", ");
            }
            json.append("]");

            sendJsonResponse(exchange, json.toString());
        }
    }

    static class ClearHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            storage.clearHistory();
            sendJsonResponse(exchange, "{\"success\": true}");
        }
    }

    private static void sendJsonResponse(HttpExchange exchange, String json) throws IOException {
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(200, response.length);
        OutputStream os = exchange.getResponseBody();
        os.write(response);
        os.close();
    }

    private static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) return result;
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(URLDecoder.decode(entry[0], StandardCharsets.UTF_8), 
                           URLDecoder.decode(entry[1], StandardCharsets.UTF_8));
            } else {
                result.put(URLDecoder.decode(entry[0], StandardCharsets.UTF_8), "");
            }
        }
        return result;
    }
}
