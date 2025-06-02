package routing.routingEngineModels.csamodel.CSAAPImodel;

import routing.routingEngineModels.Connection;
import routing.routingEngineModels.Stop.Stop;
// import routing.routingEngineModels.csamodel.pathway.Pathway; // No longer needed for the public interface of transfers

import java.time.LocalTime;
import java.time.Duration;

public class RouteSegmentCSA {
    private final RouteSegmentTypeCSA routeSegmentTypeCSA;
    private final Connection connection; // For TRANSIT type, or the connection PRECEDING a TRANSFER
    // private final Pathway pathway; // Kept private for now if internal structure benefits, but not exposed for walking transfers

    // New fields to store transfer details directly if not using Pathway
    private final Stop transferFromStop;
    private final Stop transferToStop;
    private final String transferType; // e.g., "WALKING"

    private final LocalTime startTime;
    private final Duration duration;

    // Constructor for TRANSIT
    private RouteSegmentCSA(RouteSegmentTypeCSA routeSegmentTypeCSA, Connection connection) {
        this.routeSegmentTypeCSA = routeSegmentTypeCSA;
        this.connection = connection;
        // this.pathway = null; // Explicitly null for transit
        this.transferFromStop = null;
        this.transferToStop = null;
        this.transferType = null;

        if (routeSegmentTypeCSA != RouteSegmentTypeCSA.TRANSIT || connection == null) {
            throw new IllegalArgumentException("Invalid arguments for TRANSIT segment constructor.");
        }
        this.startTime = connection.getDepTime();
        this.duration = Duration.between(connection.getDepTime(), connection.getArrTime());
    }

    // Constructor for WALKING_TRANSFER (or other non-pathway based transfers)
    private RouteSegmentCSA(RouteSegmentTypeCSA routeSegmentTypeCSA, Connection precedingConnection,
                            Stop transferFromStop, Stop transferToStop, int traversalTimeSeconds, String type) {
        this.routeSegmentTypeCSA = routeSegmentTypeCSA;
        this.connection = precedingConnection; // Connection that brought us to the start of this transfer
        // this.pathway = null; // Explicitly null
        this.transferFromStop = transferFromStop;
        this.transferToStop = transferToStop;
        this.transferType = type;


        if (routeSegmentTypeCSA != RouteSegmentTypeCSA.TRANSFER || precedingConnection == null || transferFromStop == null || transferToStop == null) {
            throw new IllegalArgumentException("Invalid arguments for TRANSFER segment constructor.");
        }
        // The start time of a transfer is the arrival time of the preceding connection
        this.startTime = precedingConnection.getArrTime();
        this.duration = Duration.ofSeconds(traversalTimeSeconds);
    }


    public static RouteSegmentCSA forTransit(Connection connection) {
        return new RouteSegmentCSA(RouteSegmentTypeCSA.TRANSIT, connection);
    }

    /**
     * Creates a new RouteSegmentCSA representing a walking (or other calculated) transfer.
     *
     * @param fromStop The stop where the transfer begins.
     * @param toStop The stop where the transfer ends.
     * @param durationSeconds The duration of the transfer in seconds.
     * @param type A string describing the type of transfer (e.g., "WALKING").
     * @param precedingConnection The connection that arrived at the 'fromStop' before this transfer began.
     * @return A new RouteSegmentCSA instance for the transfer.
     */
    public static RouteSegmentCSA forWalkingTransfer(Stop fromStop, Stop toStop, int durationSeconds, String type, Connection precedingConnection) {
        // Note: precedingConnection.getArrStop() should ideally be equal to fromStop.
        // We pass 'precedingConnection' as the 'connection' argument to the constructor,
        // as it's the connection that leads into this transfer segment.
        return new RouteSegmentCSA(RouteSegmentTypeCSA.TRANSFER, precedingConnection, fromStop, toStop, durationSeconds, type);
    }

    public RouteSegmentTypeCSA getRouteSegmentTypeCSA() {
        return routeSegmentTypeCSA;
    }

    /**
     * Gets the connection associated with this segment.
     * For TRANSIT, it's the actual transit connection.
     * For TRANSFER, it's the connection that *preceded* this transfer.
     * @return The connection.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * @deprecated Pathway is no longer directly used for walking transfers. Access transfer details via specific getters.
     * @return null, as Pathway is not used for segments created with forWalkingTransfer.
     */
    @Deprecated
    public Object getPathway() { // Changed return type to Object to avoid Pathway class reference
        return null; // Or throw an UnsupportedOperationException if preferred
    }


    public LocalTime getStartTime() {
        return startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public Stop getFromStop() {
        if (routeSegmentTypeCSA == RouteSegmentTypeCSA.TRANSIT) {
            return connection.getDepStop();
        } else { // TRANSFER
            return transferFromStop;
        }
    }

    public Stop getToStop() {
        if (routeSegmentTypeCSA == RouteSegmentTypeCSA.TRANSIT) {
            return connection.getArrStop();
        } else { // TRANSFER
            return transferToStop;
        }
    }

    /**
     * Gets the type of transfer, e.g., "WALKING".
     * Only applicable if routeSegmentTypeCSA is TRANSFER.
     * @return The transfer type string, or null for TRANSIT segments.
     */
    public String getTransferType() {
        return transferType;
    }
}