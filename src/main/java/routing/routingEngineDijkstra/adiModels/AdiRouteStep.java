package routing.routingEngineDijkstra.adiModels;

import java.time.LocalTime;

import routing.routingEngineModels.Coordinates;

/**
 * Represents a single step in a route, including mode of transport, destination, duration, and associated stop or route information.
 */
public class AdiRouteStep {
    private final String mode;
    private final Coordinates to;
    private final double duration;
    private final LocalTime startTime;
    private final String stop;
    private final AdiRouteInfo route;

    /**
     * Constructs an AdiRouteStep for a walking step with no stop or route information.
     *
     * @param mode       the mode of transport (e.g., "walk")
     * @param to         the destination coordinates
     * @param duration   the duration of the step in seconds
     * @param startTime  the start time of the step
     */
    public AdiRouteStep(String mode, Coordinates to, double duration, LocalTime startTime) {
        this(mode, to, duration, startTime, null, null);
    }

    /**
     * Constructs an AdiRouteStep with full details, including stop and route information.
     *
     * @param mode       the mode of transport (e.g., "walk", "ride")
     * @param to         the destination coordinates
     * @param duration   the duration of the step in seconds
     * @param startTime  the start time of the step
     * @param stop       the name of the stop, or null if not applicable
     * @param route      the route information, or null if not applicable
     */
    public AdiRouteStep(String mode, Coordinates to, double duration, LocalTime startTime, String stop, AdiRouteInfo route) {
        this.mode = mode;
        this.to = to;
        this.duration = duration;
        this.startTime = startTime;
        this.stop = stop;
        this.route = route;
    }

    /**
     * Retrieves the mode of transport for this step.
     *
     * @return the mode as a String
     */
    public String getMode() {
        return mode;
    }

    /**
     * Retrieves the destination coordinates of this step.
     *
     * @return the Coordinates object representing the destination
     */
    public Coordinates getTo() {
        return to;
    }

    /**
     * Retrieves the duration of this step.
     *
     * @return the duration in seconds as a double
     */
    public double getDuration() {
        return duration;
    }

    /**
     * Retrieves the start time of this step.
     *
     * @return the start time as a LocalTime
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * Retrieves the name of the stop for this step, if applicable.
     *
     * @return the stop name as a String, or null if not applicable
     */
    public String getStop() {
        return stop;
    }

    /**
     * Retrieves the route information for this step, if applicable.
     *
     * @return the AdiRouteInfo object, or null if not applicable
     */
    public AdiRouteInfo getRoute() {
        return route;
    }
}