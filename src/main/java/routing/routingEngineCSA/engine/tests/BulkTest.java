package routing.routingEngineCSA.engine.tests;

import routing.routingEngineCSA.engine.cache.classloader.StopsCache;
import routing.routingEngineCSA.engine.cache.masterloader.MasterLoader;
import routing.routingEngineCSA.engine.connectionscanalgorithm.CSARouteFinding;
import routing.routingEngineCSA.engine.util.TimeConverter;
import routing.routingEngineModels.Stop.Stop;
import routing.routingEngineModels.csamodel.CSAAPImodel.CSAQuery;
import routing.routingEngineModels.csamodel.CSAAPImodel.ResultantRouteCSA;
import routing.routingEngineModels.csamodel.CSAAPImodel.RouteSegmentCSA;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

public class BulkTest {
    public static void main(String[] args) {
        MasterLoader.initAllCaches();

        List<Stop> allStops = new ArrayList<>(StopsCache.getAllStops());
        Random random = new Random();
        Set<String> attemptedPairs = new HashSet<>();

        int testCount = 0;

        while (testCount < 20) {
            Stop dep = allStops.get(random.nextInt(allStops.size()));
            Stop arr = allStops.get(random.nextInt(allStops.size()));

            if (dep.equals(arr)) continue;

            String pairKey = dep.getStopID() + "_" + arr.getStopID();
            if (attemptedPairs.contains(pairKey)) continue;
            attemptedPairs.add(pairKey);

            CSAQuery query = new CSAQuery(dep, arr, LocalTime.of(8, 0));
            CSARouteFinding engine = new CSARouteFinding(query);

            long startTime = System.nanoTime();
            ResultantRouteCSA result = engine.findRouteViaCSA();
            long endTime = System.nanoTime();

            System.out.println("Test #" + (testCount + 1));
            System.out.println("From: " + dep.getStopName() + " (" + dep.getStopID() + ")");
            System.out.println("To:   " + arr.getStopName() + " (" + arr.getStopID() + ")");
            System.out.println("Route found: " + (result.isFound() ? "YES" : "NO"));

            if (result.isFound()) {
                System.out.println("Arrival Time: " + TimeConverter.formatAsGTFSTime(result.getArrivalTime()));
                for (RouteSegmentCSA segment : result.getSegments()) {
                    System.out.println("  - " + segment.toString());
                }
            }

            long durationMs = Duration.ofNanos(endTime - startTime).toMillis();
            System.out.println("Time taken: " + durationMs + " ms\n");

            testCount++;
        }
    }
}
