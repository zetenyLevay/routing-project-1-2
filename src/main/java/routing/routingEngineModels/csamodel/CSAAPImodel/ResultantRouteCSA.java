package routing.routingEngineModels.csamodel.CSAAPImodel;

import java.util.Collections;
import java.util.List;

public class ResultantRouteCSA {
    private final int arrivalTime;
    private final List<RouteSegmentCSA> segments;

    private ResultantRouteCSA(int arrivalTime, List<RouteSegmentCSA> segments) {
        this.arrivalTime = arrivalTime;
        this.segments = segments;


    }

    public static ResultantRouteCSA create(int arrivalTime, List<RouteSegmentCSA> segments) {
        return new ResultantRouteCSA(arrivalTime, segments);
    }

    public static ResultantRouteCSA notFound() {
        return new ResultantRouteCSA(Integer.MAX_VALUE, Collections.emptyList());
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public List<RouteSegmentCSA> getSegments() {
        return segments;
    }

    public boolean isFound() {
        return arrivalTime != Integer.MAX_VALUE;
    }


}
