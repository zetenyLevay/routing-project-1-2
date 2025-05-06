package routing.routingEngineCSA.engine.cache.masterloader;

import routing.routingEngineCSA.engine.cache.classloader.*;

public class MasterLoader {
    public static void initAllCaches() {
        displayLoading("agency");
        AgenciesCache.init();
        displayLoaded("agency");

        displayLoading("routeinfo");
        RouteInfoCache.init();
        displayLoaded("routeinfo");

        displayLoading("route");
        RoutesCache.init();
        displayLoaded("route");

        displayLoading("stops");
        StopsCache.init();
        displayLoaded("stops");

        displayLoading("pathways");
        PathwaysCache.init();
        displayLoaded("pathways");

        displayLoading("trips");
        TripsCache.init();
        displayLoaded("trips");

        displayLoading("connections");
        ConnectionsCache.init();
        displayLoaded("connections");


        System.out.println("All loaded.");
    }

    private static void displayLoading(String string) {
        System.out.println("Loading:" + string);
    }

    private static void displayLoaded(String string) {
        System.out.println("Loaded:" + string);
    }
}
