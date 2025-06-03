package routing.routingEngineDijkstra.baddijkstra;

import routing.routingEngineModels.FinalRoute;
import routing.routingEngineModels.RouteStep;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class RealDijkstraUsage {
    public static void main(String[] args) {
        GTFSCacheDijkstra.init();

        Map<String, StopDijkstra> stops = GTFSCacheDijkstra.getStopsMap();
        Map<String, List<DijkstraRouter.Connection>> connections = GTFSCacheDijkstra.getConnectionsMap();

        StopDijkstra start = stops.get("004521");
        StopDijkstra end = stops.get("F04785");

        if (start == null || end == null) {
            System.err.println("Start or end stop not found. Check your stop IDs.");
            return;
        }

        LocalTime departure = LocalTime.of(8, 0, 0);
        FinalRoute route = DijkstraRouter.findShortestPath(stops, connections, start, end, departure);

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