package routingenginemain.engine;

import routingenginemain.model.Connection;

import java.util.List;

public class ConnectionScanner {
    private final List<Connection> connections;

    public ConnectionScanner() {
        this.connections = ConnectionsCache.getSortedConnections();
    }

//    public LocalTime getEarliestArrivalTime(Request request) {
//        Map<Stop, Integer> earliestArrival = new HashMap<>();
//        Map<Stop, Connection> parentConnection = new HashMap<>();
//        Map<Stop, Pathway> parentFootpath = new HashMap<>();
//
//
//    }

//    public void setStopsToInfinity() {
//        for (Stop stop: )
//    }
}
