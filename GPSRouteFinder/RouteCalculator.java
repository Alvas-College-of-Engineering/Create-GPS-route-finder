import java.util.*;

public class RouteCalculator {

    public static class RouteResult {
        public List<Location> path;
        public double totalDistance;
        public int totalTime;
        public boolean found;

        public RouteResult(List<Location> path, double totalDistance, int totalTime, boolean found) {
            this.path = path;
            this.totalDistance = totalDistance;
            this.totalTime = totalTime;
            this.found = found;
        }
    }

    private static class NodeDistance implements Comparable<NodeDistance> {
        Location location;
        double distance;

        NodeDistance(Location location, double distance) {
            this.location = location;
            this.distance = distance;
        }

        @Override
        public int compareTo(NodeDistance other) {
            return Double.compare(this.distance, other.distance);
        }
    }

    public RouteResult findShortestPath(Graph graph, Location source, Location destination) {
        if (source == null || destination == null) {
            return new RouteResult(null, 0, 0, false);
        }

        Map<Location, Double> distances = new HashMap<>();
        Map<Location, Location> previousNodes = new HashMap<>();
        PriorityQueue<NodeDistance> pq = new PriorityQueue<>();

        for (Location loc : graph.getAllLocations()) {
            distances.put(loc, Double.MAX_VALUE);
        }

        distances.put(source, 0.0);
        pq.add(new NodeDistance(source, 0.0));

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            Location u = current.location;

            if (u.equals(destination)) break;

            if (current.distance > distances.get(u)) continue;

            for (Edge edge : graph.getNeighbours(u)) {
                Location v = edge.getTo();
                double weight = edge.getDistanceKm();
                double newDist = distances.get(u) + weight;

                if (newDist < distances.get(v)) {
                    distances.put(v, newDist);
                    previousNodes.put(v, u);
                    pq.add(new NodeDistance(v, newDist));
                }
            }
        }

        if (!previousNodes.containsKey(destination) && !source.equals(destination)) {
            return new RouteResult(null, 0, 0, false);
        }

        List<Location> path = reconstructPath(previousNodes, destination, source);
        
        // Calculate total distance and time from the final path
        double totalDistance = 0;
        int totalTime = 0;
        
        for (int i = 0; i < path.size() - 1; i++) {
            Location from = path.get(i);
            Location to = path.get(i + 1);
            for (Edge edge : graph.getNeighbours(from)) {
                if (edge.getTo().equals(to)) {
                    totalDistance += edge.getDistanceKm();
                    totalTime += edge.getTravelTimeMin();
                    break;
                }
            }
        }

        return new RouteResult(path, totalDistance, totalTime, true);
    }

    private List<Location> reconstructPath(Map<Location, Location> previousNodes, Location destination, Location source) {
        List<Location> path = new LinkedList<>();
        Location current = destination;
        
        while (current != null) {
            path.add(0, current);
            current = previousNodes.get(current);
        }
        
        // Ensure path starts with source if it was found
        if (!path.isEmpty() && !path.get(0).equals(source)) {
            return new ArrayList<>(); // Should not happen if logic is correct
        }
        
        return path;
    }

    public void displayRoute(RouteResult result) {
        if (result == null || !result.found) {
            System.out.println("No route found between source and destination.");
            return;
        }

        System.out.print("Route: ");
        for (int i = 0; i < result.path.size(); i++) {
            System.out.print(result.path.get(i).getName());
            if (i < result.path.size() - 1) {
                System.out.print(" -> ");
            }
        }
        System.out.println();
        System.out.printf("Total Distance: %.1f km%n", result.totalDistance);
        System.out.println("Estimated Time: " + result.totalTime + " minutes");
    }
}
