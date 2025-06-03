package routing.routingEngineDijkstra.newDijkstra.model.input;

public class DijkstraConnection {
    public final DijkstraStop from;
    public final DijkstraStop to;
    public final int departureTime;
    public final int arrivalTime;
    public final String tripId;
    public final String routeId;
    public final String headSign;

    public DijkstraConnection(DijkstraStop from, DijkstraStop to, int departureTime, int arrivalTime,
                      String tripId, String routeId, String headSign) {
        this.from = from;
        this.to = to;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.tripId = tripId;
        this.routeId = routeId;
        this.headSign = headSign;
    }
}
