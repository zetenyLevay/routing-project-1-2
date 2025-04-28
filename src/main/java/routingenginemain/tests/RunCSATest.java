package routingenginemain.tests;

import routingenginemain.engine.cache.classloader.StopsCache;
import routingenginemain.engine.cache.masterloader.MasterLoader;
import routingenginemain.engine.connectionscanalgorithm.CSARouteFinding;
import routingenginemain.model.CSAAPImodel.CSAQuery;
import routingenginemain.model.Stop;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;

public class RunCSATest {
    public static void main(String[] args) {
        MasterLoader.initAllCaches();
        Stop departureStop = StopsCache.getStop("002133");
        Stop arrivalStop = StopsCache.getStop("007883");
        CSAQuery csaQuery = new CSAQuery(departureStop, arrivalStop, LocalTime.parse("08:03:00"));
        CSARouteFinding csaRouteFinding = new CSARouteFinding(csaQuery);
        Instant start = Instant.now();
        csaRouteFinding.findRouteViaCSA();
        Instant end = Instant.now();

        Duration duration = Duration.between(start, end);
        System.out.println("Route finding completed in: " + duration.toMillis() + " milliseconds");


    }
}
