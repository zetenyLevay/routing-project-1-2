package routing.routingEngineModels;

import java.time.LocalTime;

public class RouteStep {
    private final String mode;
    private final Coordinates to;
    private final double duration;
    private final LocalTime startTime;
    private final String stop;
    private final RouteInfo route;

    public RouteStep(String mode, Coordinates to, double duration, LocalTime startTime) {
        this(mode, to, duration, startTime, null, null);
    }

    public RouteStep(String mode, Coordinates to, double duration, LocalTime startTime, String stop, RouteInfo route) {
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
    public RouteInfo getRoute() { return route; }
}