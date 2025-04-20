package routingenginemain.engine.cache.masterloader;

import routingenginemain.engine.cache.classloader.*;

public class MasterLoader {
    public static void initAllCaches() {
        AgenciesCache.init();
        RouteInfoCache.init();
        RoutesCache.init();
        StopsCache.init();
        PathwaysCache.init();
        TripsCache.init();
        ConnectionsCache.init();
    }
}
