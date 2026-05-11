import java.util.*;

public class Graph {
    private final Map<Location, List<Edge>> adjacencyList;

    public Graph() {
        this.adjacencyList = new HashMap<>();
    }

    public void addLocation(Location loc) {
        adjacencyList.putIfAbsent(loc, new ArrayList<>());
    }

    public void addRoad(Location from, Location to, double distanceKm, int travelTimeMin) {
        // Ensure both locations exist in the graph
        addLocation(from);
        addLocation(to);

        // Add edge from -> to
        adjacencyList.get(from).add(new Edge(from, to, distanceKm, travelTimeMin));
        // Add edge to -> from (bidirectional)
        adjacencyList.get(to).add(new Edge(to, from, distanceKm, travelTimeMin));
    }

    public List<Edge> getNeighbours(Location loc) {
        return adjacencyList.getOrDefault(loc, new ArrayList<>());
    }

    public Set<Location> getAllLocations() {
        return adjacencyList.keySet();
    }

    public Location getLocationByName(String name) {
        for (Location loc : adjacencyList.keySet()) {
            if (loc.getName().equalsIgnoreCase(name)) {
                return loc;
            }
        }
        return null;
    }

    public void displayMap() {
        System.out.println("--- GPS Map Data ---");
        for (Map.Entry<Location, List<Edge>> entry : adjacencyList.entrySet()) {
            Location loc = entry.getKey();
            List<Edge> edges = entry.getValue();
            System.out.println(loc.getName() + ":");
            if (edges.isEmpty()) {
                System.out.println("  (No connections)");
            } else {
                for (Edge edge : edges) {
                    System.out.println("  " + edge.toString());
                }
            }
        }
    }
}
