package routing.routingEngineDijkstra.badAStar;

import routing.routingEngineModels.FinalRoute;
import routing.routingEngineModels.RouteStep;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class RealAStarUsage {
    public static void main(String[] args) {
        GTFSCacheAStar.init();

        Map<String, StopAStar> stops = GTFSCacheAStar.getStopsMap();
        Map<String, List<AStarRouter.Connection>> connections = GTFSCacheAStar.getConnectionsMap();

        StopAStar start = stops.get("004521");
        StopAStar end = stops.get("F04785");

        if (start == null || end == null) {
            System.err.println("Start or end stop not found. Check your stop IDs.");
            return;
        }

        LocalTime departure = LocalTime.of(8, 0, 0);
        FinalRoute route = AStarRouter.findShortestPath(stops, connections, start, end, departure);

        if (route == null) {
            System.out.println("NO_ROUTE_FOUND|" + start.getStopID() + "|" + end.getStopID());
        } else {
            System.out.println("ROUTE_FOUND|" +
                    start.getStopID() + "|" + end.getStopID() + "|" +
                    String.format("%.2f|%.2f", route.getTotalTime(), route.getTotalDistance()));

            for (RouteStep step : route.getRouteSteps()) {
                System.out.println(
                        step.getModeOfTransport() + "|" +
                                step.getStartCoord().getLatitude() + "," + step.getStartCoord().getLongitude() + "|" +
                                step.getEndCoord().getLatitude() + "," + step.getEndCoord().getLongitude() + "|" +
                                String.format("%.2f", step.getTime())
                );
            }
        }
    }
}
