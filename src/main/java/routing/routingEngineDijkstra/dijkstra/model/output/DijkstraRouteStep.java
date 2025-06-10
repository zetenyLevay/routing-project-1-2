package routing.routingEngineDijkstra.dijkstra.model.output;

import routing.routingEngineDijkstra.dijkstra.model.input.DijkstraCoordinates;
import routing.routingEngineDijkstra.dijkstra.model.input.DijkstraRouteInfo;

import java.util.Objects;

/**
 * Represents a single step in a route, including mode of transport, coordinates, time, and optional stop and route information.
 */
public class DijkstraRouteStep {
    private final String modeOfTransport;
    private final DijkstraCoordinates startCoord;
    private final DijkstraCoordinates endCoord;
    private final double time;
    private final String stopName;
    private final DijkstraRouteInfo routeInfo;

    /**
     * Constructs a DijkstraRouteStep for a walking step with no stop or route information.
     *
     * @param mode      the mode of transport (e.g., "WALK")
     * @param start     the starting coordinates
     * @param end       the destination coordinates
     * @param time      the duration of the step in seconds
     * @throws NullPointerException     if mode, start, or end is null
     * @throws IllegalArgumentException if time is negative
     */
    public DijkstraRouteStep(String mode, DijkstraCoordinates start, DijkstraCoordinates end, double time) {
        this(mode, start, end, time, null, null);
    }

    /**
     * Constructs a DijkstraRouteStep with full details, including stop and route information.
     *
     * @param mode        the mode of transport (e.g., "WALK", or route ID)
     * @param start       the starting coordinates
     * @param end         the destination coordinates
     * @param time        the duration of the step in seconds
     * @param stopName    the name of the stop, or null if not applicable
     * @param routeInfo   the route information, or null if not applicable
     * @throws NullPointerException     if mode, start, or end is null
     * @throws IllegalArgumentException if time is negative
     */
    public DijkstraRouteStep(String mode, DijkstraCoordinates start, DijkstraCoordinates end, double time, String stopName, DijkstraRouteInfo routeInfo) {
        this.modeOfTransport = Objects.requireNonNull(mode, "Mode of transport cannot be null");
        this.startCoord = Objects.requireNonNull(start, "Start coordinate cannot be null");
        this.endCoord = Objects.requireNonNull(end, "End coordinate cannot be null");
        if (time < 0) throw new IllegalArgumentException("Time cannot be negative");
        this.time = time;
        this.stopName = stopName;
        this.routeInfo = routeInfo;
    }

    /**
     * Retrieves the mode of transport for this step.
     *
     * @return the mode of transport as a String
     */
    public String getModeOfTransport() {
        return modeOfTransport;
    }

    /**
     * Retrieves the starting coordinates of this step.
     *
     * @return the start DijkstraCoordinates
     */
    public DijkstraCoordinates getStartCoord() {
        return startCoord;
    }

    /**
     * Retrieves the destination coordinates of this step.
     *
     * @return the end DijkstraCoordinates
     */
    public DijkstraCoordinates getEndCoord() {
        return endCoord;
    }

    /**
     * Retrieves the duration of this step.
     *
     * @return the time in seconds
     */
    public double getTime() {
        return time;
    }

    /**
     * Retrieves the name of the stop for this step, if applicable.
     *
     * @return the stop name as a String, or null if not applicable
     */
    public String getStopName() {
        return stopName;
    }

    /**
     * Retrieves the route information for this step, if applicable.
     *
     * @return the DijkstraRouteInfo, or null if not applicable
     */
    public DijkstraRouteInfo getRouteInfo() {
        return routeInfo;
    }

    /**
     * Checks if this step is a walking step.
     *
     * @return true if the mode of transport is "WALK", false otherwise
     */
    public boolean isWalkingStep() {
        return "WALK".equalsIgnoreCase(modeOfTransport);
    }

    /**
     * Checks if this step is equal to another object.
     *
     * @param obj the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DijkstraRouteStep routeStep = (DijkstraRouteStep) obj;
        return Double.compare(routeStep.time, time) == 0 &&
                Objects.equals(modeOfTransport, routeStep.modeOfTransport) &&
                Objects.equals(startCoord, routeStep.startCoord) &&
                Objects.equals(endCoord, routeStep.endCoord);
    }

    /**
     * Generates a hash code for this step.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(modeOfTransport, startCoord, endCoord, time);
    }
}