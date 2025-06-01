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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BooleanTest {

    public static void main(String[] args) {
        loadCaches();

        Collection<Stop> allStops = StopsCache.getAllStops();
        List<Stop> stopList = new ArrayList<>(allStops);
        int totalStops = stopList.size();
        int foundCount = 0;
        int testCount = 0;

        System.out.println("Beginning systematic testing of " + totalStops + " stops...");

        for (int i = 0; i < totalStops; i++) {
            Stop departureStop = stopList.get(i);

            for (int j = 0; j < totalStops; j++) {
                if (i == j) continue;

                testCount++;
                Stop arrivalStop = stopList.get(j);

                ResultantRouteCSA route = new CSARouteFinding(
                        new CSAQuery(departureStop, arrivalStop, LocalTime.of(16, 0))
                ).findRouteViaCSA();

                if (route.isFound()) {
                    foundCount++;
                    printRoute(departureStop, arrivalStop, route);
                }
            }
        }

        System.out.println("\nTesting complete.");
        System.out.println("Total stop pairs tested: " + (totalStops * (totalStops - 1)));
        System.out.println("Routes found: " + foundCount);
    }

    private static void loadCaches() {
        MasterLoader.initAllCaches();
    }

    private static void printRoute(Stop from, Stop to, ResultantRouteCSA route) {
        System.out.println("\nRoute found: " + from.getStopID() + " → " + to.getStopID());
        System.out.println("Total duration: " + formatMinutes(route.getArrivalTime() / 60.0));

        System.out.println("Path:");
        for (RouteSegmentCSA segment : route.getSegments()) {
            if (segment.getRouteSegmentTypeCSA() == RouteSegmentTypeCSA.TRANSIT) {
                System.out.printf("  Take %s at %s (%s → %s) %s-%s\n",
                        segment.getConnection().getTrip().getRoute().getRouteInfo().getRouteShortName(),
                        segment.getFromStop().getStopID(),
                        formatTime(segment.getStartTime()),
                        formatTime(segment.getStartTime() + segment.getDuration()),
                        segment.getFromStop().getStopID(),
                        segment.getToStop().getStopID());
            } else {
                System.out.printf("  Transfer at %s (%s → %s) %s\n",
                        segment.getFromStop().getStopID(),
                        formatTime(segment.getStartTime()),
                        formatTime(segment.getStartTime() + segment.getDuration()),
                        formatDuration(segment.getDuration()));
            }
        }
    }

    private static String formatTime(int seconds) {
        return String.format("%02d:%02d", seconds / 3600, (seconds % 3600) / 60);
    }

    private static String formatDuration(int seconds) {
        return String.format("%dm %02ds", seconds / 60, seconds % 60);
    }

    private static String formatMinutes(double minutes) {
        return String.format("%.1f minutes", minutes);
    }
}