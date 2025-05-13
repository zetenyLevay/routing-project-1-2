package routing.routingEngineCSA.engine.cache;

import routing.routingEngineCSA.engine.cache.classloader.ConnectionsCache;
import routing.routingEngineCSA.engine.cache.masterloader.MasterLoader;

public class CachingTest {
    public static void main(String[] args) {
        // Initialize all caches
        MasterLoader.initAllCaches();

        System.out.println(ConnectionsCache.getSortedConnections().size());
    }
}