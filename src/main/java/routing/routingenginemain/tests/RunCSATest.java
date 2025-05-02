package routing.routingenginemain.tests;

import routing.routingenginemain.engine.cache.classloader.StopsCache;
import routing.routingenginemain.engine.cache.masterloader.MasterLoader;
import routing.routingenginemain.engine.connectionscanalgorithm.CSARouteFinding;
import routing.routingenginemain.model.CSAAPImodel.CSAQuery;
import routing.routingenginemain.model.Stop;

import java.time.*;
import java.util.*;
import java.util.stream.IntStream;

public class RunCSATest {
    public static void main(String[] args) {
        Instant startCaching = Instant.now();
        MasterLoader.initAllCaches();
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
                    return Duration.between(start, Instant.now()).toMillis();
                })
                .sum();

        System.out.printf("Average runtime: %.3f ms%n", totalTime / 1000.0);
    }
}