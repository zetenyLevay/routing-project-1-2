package routing.routingEngineDijkstra.adiModels;

import java.time.LocalTime;

import routing.routingEngineModels.Coordinates;

public class AdiRouteStep {
    private final String mode;
    private final Coordinates to;
    private final double duration;
    private final LocalTime startTime;
    private final String stop;
    private final AdiRouteInfo route;

    public AdiRouteStep(String mode, Coordinates to, double duration, LocalTime startTime) {
        this(mode, to, duration, startTime, null, null);
    }

    public AdiRouteStep(String mode, Coordinates to, double duration, LocalTime startTime, String stop, AdiRouteInfo route) {
        this.mode = mode;
        this.to = to;
        this.duration = duration;
        this.startTime = startTime;
        this.stop = stop;
        this.route = route;
    }

    public String getMode() { return mode; }
    public Coordinates getTo() { return to; }
    public double getDuration() { return duration; }
    public LocalTime getStartTime() { return startTime; }
    public String getStop() { return stop; }
    public AdiRouteInfo getRoute() { return route; }
}