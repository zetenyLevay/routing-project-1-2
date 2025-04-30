package routing.routingenginemain.tests;

import routing.routingenginemain.engine.cache.classloader.StopsCache;
import routing.routingenginemain.engine.cache.masterloader.MasterLoader;
import routing.routingenginemain.engine.connectionscanalgorithm.CSARouteFinding;
import routing.routingenginemain.model.CSAAPImodel.CSAQuery;
import routing.routingenginemain.model.Stop;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;

public class RunCSATest {
    public static void main(String[] args) {
        Instant startLoadingCache = Instant.now();
        MasterLoader.initAllCaches();
        Instant endLoadingCaches = Instant.now();

        Instant startFirstTest = Instant.now();
        Stop departureStop = StopsCache.getStop("002133");
        Stop arrivalStop = StopsCache.getStop("MERGED_F03810");
        CSAQuery csaQuery = new CSAQuery(departureStop, arrivalStop, LocalTime.parse("08:03:00"));
        CSARouteFinding csaRouteFinding = new CSARouteFinding(csaQuery);
        csaRouteFinding.findRouteViaCSA();
        Instant endFirstTest = Instant.now();

        Instant startSecondTest = Instant.now();
        Stop departureStop2 = StopsCache.getStop("004952");
        Stop arrivalStop2 = StopsCache.getStop("F00179");
        CSAQuery csaQuery2 = new CSAQuery(departureStop2, arrivalStop2, LocalTime.parse("18:43:01"));
        CSARouteFinding csaRouteFinding2 = new CSARouteFinding(csaQuery2);
        csaRouteFinding2.findRouteViaCSA();
        Instant endSecondTest = Instant.now();

        Duration durationLoading = Duration.between(startLoadingCache, endLoadingCaches);
        System.out.println("Loading completed in: " + durationLoading.toMillis() + " milliseconds");

        Duration durationFirstTest = Duration.between(startFirstTest, endFirstTest);
        System.out.println("First test took " + durationFirstTest.toMillis() + "milliseconds");

        Duration durationSecondTest = Duration.between(startSecondTest,endSecondTest);
        System.out.println("Second test took " + durationSecondTest.toMillis() + "milliseconds");



    }
}
