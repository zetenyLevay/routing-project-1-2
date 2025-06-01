package routing.routingEngineModels.csamodel.CSAAPImodel;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

public class ResultantRouteCSA {
    private final LocalTime arrivalTime;
    private final List<RouteSegmentCSA> segments;

    private ResultantRouteCSA(LocalTime arrivalTime, List<RouteSegmentCSA> segments) {
        this.arrivalTime = arrivalTime;
        this.segments = segments;
    }

    public static ResultantRouteCSA create(LocalTime arrivalTime, List<RouteSegmentCSA> segments) {
        return new ResultantRouteCSA(arrivalTime, segments);
    }

    public static ResultantRouteCSA notFound() {
        return new ResultantRouteCSA(LocalTime.MAX, Collections.emptyList());
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public List<RouteSegmentCSA> getSegments() {
        return segments;
    }

    public boolean isFound() {
        return !arrivalTime.equals(LocalTime.MAX);
    }
}
