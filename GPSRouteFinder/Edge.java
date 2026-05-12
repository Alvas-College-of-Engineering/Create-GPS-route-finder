public class Edge {
    private final Location from;
    private final Location to;
    private final double distanceKm;
    private final int travelTimeMin;

    public Edge(Location from, Location to, double distanceKm, int travelTimeMin) {
        this.from = from;
        this.to = to;
        this.distanceKm = distanceKm;
        this.travelTimeMin = travelTimeMin;
    }

    public Location getFrom() {
        return from;
    }

    public Location getTo() {
        return to;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public int getTravelTimeMin() {
        return travelTimeMin;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s | %.1fkm | %dmin", from.getName(), to.getName(), distanceKm, travelTimeMin);
    }
}
