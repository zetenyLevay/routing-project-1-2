package routing.routingEngineCSA.tests;

import routing.routingEngineCSA.engine.cache.classloader.StopsCache;
import routing.routingEngineCSA.engine.cache.masterloader.MasterLoader;
import routing.routingEngineCSA.engine.connectionscanalgorithm.CSARouteFinding;
import routing.routingEngineModels.Stop.Stop;
import routing.routingEngineModels.csamodel.CSAAPImodel.CSAQuery;
import routing.routingEngineModels.csamodel.CSAAPImodel.ResultantRouteCSA;
import routing.routingEngineModels.csamodel.CSAAPImodel.RouteSegmentCSA;
import routing.routingEngineModels.csamodel.CSAAPImodel.RouteSegmentTypeCSA;

import java.time.LocalTime;
import java.util.List;
import java.util.Scanner;

public class SingleTest {

    public static void main(String[] args) {
        loadCache();
        Scanner scanner = new Scanner(System.in);

        System.out.println("CSA Route Finder (enter 'exit' to quit)");
        System.out.println("---------------------------------------");

        while (true) {
            System.out.println("\nEnter departure stop ID (or 'exit' to quit):");
            String departureStopId = scanner.nextLine().trim();

            if (departureStopId.equalsIgnoreCase("exit")) {
                break;
            }

            System.out.println("Enter arrival stop ID (or 'exit' to quit):");
            String arrivalStopId = scanner.nextLine().trim();

            if (arrivalStopId.equalsIgnoreCase("exit")) {
                break;
            }

            Stop departureStop = getStop(departureStopId);
            Stop arrivalStop = getStop(arrivalStopId);

            if (departureStop == null || arrivalStop == null) {
                System.out.println("Error: One or both stops not found");
                continue;
            }

            System.out.println("\nFinding route from " + departureStop.getStopName() +
                    " (" + departureStopId + ") to " +
                    arrivalStop.getStopName() + " (" + arrivalStopId + ")");
            System.out.println("Departure time: " + inputTime());

            ResultantRouteCSA resultantRoute = new CSARouteFinding(
                    new CSAQuery(departureStop, arrivalStop, inputTime())
            ).findRouteViaCSA();

            printResultantRoute(resultantRoute);
        }

        scanner.close();
        System.out.println("\nExiting CSA Route Finder. Goodbye!");
    }

    private static void loadCache() {
        MasterLoader.initAllCaches();
    }

    private static LocalTime inputTime() {
        return LocalTime.of(16, 0); // Default departure time 08:00
    }

    private static Stop getStop(String stopID) {
        return StopsCache.getStop(stopID);
    }

    private static void printResultantRoute(ResultantRouteCSA resultantRoute) {
        System.out.println("\n=== Route Result ===");

        if (!resultantRoute.isFound()) {
            System.out.println("No route found between the specified stops");
            return;
        }

        System.out.println("Total journey time: " + formatDuration(resultantRoute.getArrivalTime()));
        System.out.println("Number of segments: " + resultantRoute.getSegments().size());
        System.out.println("\nJourney segments:");

        List<RouteSegmentCSA> segments = resultantRoute.getSegments();
        for (int i = 0; i < segments.size(); i++) {
            RouteSegmentCSA segment = segments.get(i);
            System.out.println("\nSegment #" + (i + 1) + ": " + segment.getRouteSegmentTypeCSA().name());

            if (segment.getRouteSegmentTypeCSA() == RouteSegmentTypeCSA.TRANSIT) {
                printTransitSegment(segment);
            } else {
                printTransferSegment(segment);
            }
        }
    }

    private static void printTransitSegment(RouteSegmentCSA segment) {
        System.out.println("  Transit type: " +
                segment.getConnection().getTrip().getRoute().getRouteInfo().getRouteTypeName());
        System.out.println("  From: " + segment.getFromStop().getStopName() +
                " (" + segment.getFromStop().getStopID() + ")");
        System.out.println("  To: " + segment.getToStop().getStopName() +
                " (" + segment.getToStop().getStopID() + ")");
        System.out.println("  Departure: " + formatTime(segment.getStartTime()));
        System.out.println("  Arrival: " + formatTime(segment.getStartTime() + segment.getDuration()));
        System.out.println("  Duration: " + formatDuration(segment.getDuration()));
        System.out.println("  Route: " +
                segment.getConnection().getTrip().getRoute().getRouteInfo().getRouteShortName() +
                " (" + segment.getConnection().getTrip().getRoute().getAgency().agencyName + ")");
        System.out.println("  Headsign: " + segment.getConnection().getTrip().getHeadSign());
    }

    private static void printTransferSegment(RouteSegmentCSA segment) {
        System.out.println("  Transfer type: " +
                segment.getPathway().getPathwayTypeName());
        System.out.println("  From: " + segment.getFromStop().getStopName() +
                " (" + segment.getFromStop().getStopID() + ")");
        System.out.println("  To: " + segment.getToStop().getStopName() +
                " (" + segment.getToStop().getStopID() + ")");
        System.out.println("  Start: " + formatTime(segment.getStartTime()));
        System.out.println("  Duration: " + formatDuration(segment.getDuration()));
    }

    private static String formatTime(int secondsSinceMidnight) {
        int hours = secondsSinceMidnight / 3600;
        int minutes = (secondsSinceMidnight % 3600) / 60;
        int seconds = secondsSinceMidnight % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private static String formatDuration(int durationInSeconds) {
        if (durationInSeconds < 60) {
            return durationInSeconds + " seconds";
        }

        int minutes = durationInSeconds / 60;
        int seconds = durationInSeconds % 60;

        if (minutes < 60) {
            return String.format("%d min %d sec", minutes, seconds);
        }

        int hours = minutes / 60;
        minutes = minutes % 60;
        return String.format("%d hr %d min %d sec", hours, minutes, seconds);
    }
}