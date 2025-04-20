package routingenginemain.engine.connectionspanalgorithm;

import routingenginemain.engine.api.Request;
import routingenginemain.engine.cache.classloader.ConnectionsCache;
import routingenginemain.engine.cache.classloader.StopsCache;
import routingenginemain.model.Connection;
import routingenginemain.model.Stop;
import routingenginemain.model.pathway.Pathway;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionScanner {
    private final Map<Stop, Integer> earliestArrival;
    private final Map<Stop, Connection> parentConnection;
    private final Map<Stop, Pathway> parentFootpath;
    private final List<Connection> sortedConnections;

    public ConnectionScanner() {
        this.earliestArrival = new HashMap<>();
        this.parentConnection = new HashMap<>();
        this.parentFootpath = new HashMap<>();
        this.sortedConnections = ConnectionsCache.getSortedConnections();

    }

    private void initializeAllDataStructures(Stop origin, int departureTime) {
        for (Stop stop: StopsCache.getAllStops()) {
            earliestArrival.put(stop, Integer.MAX_VALUE);
            parentConnection.put(stop, null);
            parentFootpath.put(stop, null);

        }

        earliestArrival.put(origin, departureTime);
    }




}
