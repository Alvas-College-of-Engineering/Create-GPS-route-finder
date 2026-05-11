import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RouteStorage {
    private static final String ROUTES_FILE = "routes.txt";

    public void saveRoute(RouteCalculator.RouteResult result, String source, String destination) {
        if (result == null || !result.found) return;

        StringBuilder pathBuilder = new StringBuilder();
        for (int i = 0; i < result.path.size(); i++) {
            pathBuilder.append(result.path.get(i).getName());
            if (i < result.path.size() - 1) {
                pathBuilder.append("->");
            }
        }

        String line = String.format("%s|%s|%.1f|%d|%s%n",
                source, destination, result.totalDistance, result.totalTime, pathBuilder.toString());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ROUTES_FILE, true))) {
            writer.write(line);
        } catch (IOException e) {
            System.err.println("Error saving route to file: " + e.getMessage());
        }
    }

    public List<String> loadAllRoutes() {
        List<String> summaries = new ArrayList<>();
        File file = new File(ROUTES_FILE);
        
        if (!file.exists()) return summaries;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 5) {
                    summaries.add(String.format("From: %s to %s | %s km | %s min | Path: %s",
                            parts[0], parts[1], parts[2], parts[3], parts[4]));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading routes from file: " + e.getMessage());
        }
        return summaries;
    }

    public void searchRouteHistory(String source, String destination) {
        File file = new File(ROUTES_FILE);
        if (!file.exists()) {
            System.out.println("No route history found.");
            return;
        }

        boolean found = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 5) {
                    if (parts[0].equalsIgnoreCase(source) && parts[1].equalsIgnoreCase(destination)) {
                        System.out.println("Found in History: " + String.format("From: %s to %s | %s km | %s min | Path: %s",
                                parts[0], parts[1], parts[2], parts[3], parts[4]));
                        found = true;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error searching route history: " + e.getMessage());
        }

        if (!found) {
            System.out.println("No matching route found in history for: " + source + " to " + destination);
        }
    }

    public void clearHistory() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ROUTES_FILE, false))) {
            writer.write("");
            System.out.println("Route history cleared.");
        } catch (IOException e) {
            System.err.println("Error clearing route history: " + e.getMessage());
        }
    }
}
