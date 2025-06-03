package routing.routingEngineDijkstra.newDijkstra.tests;

import routing.routingEngineDijkstra.newDijkstra.model.output.FinalRoute;
import routing.routingEngineDijkstra.newDijkstra.model.output.RouteStep;
import routing.routingEngineDijkstra.newDijkstra.model.input.DijkstraRouteInfo;

import java.text.DecimalFormat;
import java.util.List;

public class FinalRoutePrinter {
    private static final DecimalFormat df = new DecimalFormat("#.##");

    public static void print(FinalRoute route) {
        if (route == null) {
            System.out.println("No route found.");
            return;
        }

        List<RouteStep> steps = route.getRouteSteps();
        System.out.println("----- Route Details -----");
        for (int i = 0; i < steps.size(); i++) {
            RouteStep step = steps.get(i);
            String mode = step.getModeOfTransport();
            String start = coordsToString(step.getStartCoord());
            String end = coordsToString(step.getEndCoord());
            String time = df.format(step.getTime()) + "s";
            System.out.print((i + 1) + ". " + mode + " from " + start + " to " + end + " (" + time + ")");

            if (!step.isWalkingStep()) {
                String stopName = step.getStopName();
                DijkstraRouteInfo info = step.getRouteInfo();
                if (stopName != null) {
                    System.out.print(", end at stop: " + stopName);
                }
                if (info != null) {
                    System.out.print(" [Route Info: " + info + "]");
                }
            }
            System.out.println();
        }
        System.out.println("Total distance: " + df.format(route.getTotalDistance()) + " meters");
        System.out.println("Total time: " + df.format(route.getTotalTime()) + " seconds");
        System.out.println("-------------------------");
    }

    private static String coordsToString(routing.routingEngineDijkstra.newDijkstra.model.input.Coordinates coord) {
        return "(" + df.format(coord.getLatitude()) + ", " + df.format(coord.getLongitude()) + ")";
    }
}
