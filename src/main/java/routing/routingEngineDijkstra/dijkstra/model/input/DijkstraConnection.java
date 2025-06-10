package routing.routingEngineDijkstra.dijkstra.model.input;

/**
 * Represents a connection between two stops in the routing system, including departure and arrival times, trip, and route details.
 */
public class DijkstraConnection {
    /** The starting stop of the connection. */
    public final DijkstraStop from;
    /** The destination stop of the connection. */
    public final DijkstraStop to;
    /** The departure time in seconds since midnight. */
    public final int departureTime;
    /** The arrival time in seconds since midnight. */
    public final int arrivalTime;
    /** The unique identifier of the trip, or null for walking connections. */
    public final String tripId;
    /** The identifier of the route, or "WALK" for walking connections. */
    public final String routeId;
    /** The head sign displayed for the route, or a description for walking connections. */
    public final String headSign;

    /**
     * Constructs a DijkstraConnection with the specified details.
     *
     * @param from          the starting stop
     * @param to            the destination stop
     * @param departureTime the departure time in seconds since midnight
     * @param arrivalTime   the arrival time in seconds since midnight
     * @param tripId        the unique trip identifier, or null for walking
     * @param routeId       the route identifier, or "WALK" for walking
     * @param headSign      the head sign or description of the route
     */
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

    /**
     * Calculates the duration of the connection.
     *
     * @return the duration in seconds
     */
    public int getDuration() {
        return arrivalTime - departureTime;
    }

    /**
     * Creates a new DijkstraConnection with an updated departure time, adjusting the arrival time accordingly.
     *
     * @param newDepartureTime the new departure time in seconds since midnight
     * @return a new DijkstraConnection with updated times
     */
    public DijkstraConnection withUpdatedTimes(int newDepartureTime) {
        return new DijkstraConnection(
                from, to, newDepartureTime, newDepartureTime + getDuration(),
                tripId, routeId, headSign
        );
    }
}