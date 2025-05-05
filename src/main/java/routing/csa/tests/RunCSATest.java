package routing.csa.tests;

import routing.csa.engine.cache.classloader.StopsCache;
import routing.csa.engine.cache.masterloader.MasterLoader;
import routing.csa.engine.connectionscanalgorithm.CSARouteFinding;
import routing.routingEngineModels.csamodel.CSAAPImodel.CSAQuery;
import routing.routingEngineModels.csamodel.Stop;

import java.time.*;
import java.util.*;
import java.util.stream.IntStream;

public class RunCSATest {
    public static void main(String[] args) {
        printMemoryUsage("Before loading cache: ");
        Instant startCaching = Instant.now();
        MasterLoader.initAllCaches();
        printMemoryUsage("After loading cache: ");
        System.out.println("Time to taken load master cache: " + Duration.between(startCaching, Instant.now()).toMillis());
        List<String> stops = StopsCache.getAllStops().stream()
                .map(Stop::getStopID)
                .toList();
        Random random = new Random();
        LocalTime depTime = LocalTime.of(8, 0);

        long totalTime = IntStream.range(0, 1000)
                .mapToLong(i -> {
                    String from = stops.get(random.nextInt(stops.size()));
                    String to;
                    do { to = stops.get(random.nextInt(stops.size())); }
                    while (to.equals(from));

                    Instant start = Instant.now();
                    new CSARouteFinding(new CSAQuery(
                            StopsCache.getStop(from),
                            StopsCache.getStop(to),
                            depTime
                    )).findRouteViaCSA();
                    printMemoryUsage("Per request: ");
                    return Duration.between(start, Instant.now()).toMillis();
                })
                .sum();

        System.out.printf("Average runtime: %.3f ms%n", totalTime / 1000.0);

        printMemoryUsage("After all test: ");
    }

    private static void printMemoryUsage(String phase) {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        System.out.printf("[MEMORY] %s - Used: %.2f MB / Max: %.2f MB%n",
                phase,
                usedMemory / (1024.0 * 1024.0),
                maxMemory / (1024.0 * 1024.0)
        );
    }
}