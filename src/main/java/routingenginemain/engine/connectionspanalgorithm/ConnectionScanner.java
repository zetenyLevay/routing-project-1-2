package routingenginemain.engine.connectionspanalgorithm;

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
    private final int requestedDepartureTime;
    private final Stop departureStop;


    public ConnectionScanner(int requestedDepartureTime, Stop departureStop) {
        this.earliestArrival = new HashMap<>();
        this.parentConnection = new HashMap<>();
        this.parentFootpath = new HashMap<>();
        this.sortedConnections = ConnectionsCache.getSortedConnections();
        this.requestedDepartureTime = requestedDepartureTime;
        this.departureStop = departureStop;


    }

    private void initializeAllDataStructures() {
        for (Stop stop: StopsCache.getAllStops()) {
            earliestArrival.put(stop, Integer.MAX_VALUE);
            parentConnection.put(stop, null);
            parentFootpath.put(stop, null);

        }

        earliestArrival.put(this.departureStop, this.requestedDepartureTime);
    }

    private int findFirstRelevantConnectionIndex() {
        return binarySearch(this.sortedConnections, this.requestedDepartureTime);
    }

    private int binarySearch(List<Connection> connections, int departureTime) {
        int low = 0;
        int high = connections.size() - 1;
        int result = connections.size();

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int depTime = connections.get(mid).getDepTime();

            if (depTime >= departureTime) {
                result = mid;
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return result;
    }

    public






}
