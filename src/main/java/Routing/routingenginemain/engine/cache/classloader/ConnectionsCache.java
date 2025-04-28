package Routing.routingenginemain.engine.cache.classloader;

import Routing.routingenginemain.engine.util.TimeConverter;
import Routing.routingenginemain.model.Connection;
import Routing.routingenginemain.model.Stop;
import Routing.routingenginemain.model.Trip;
import Routing.routingenginemain.engine.connectionscanalgorithm.ConnectionSort;

import java.sql.*;
import java.util.*;

public class ConnectionsCache {
    private static final List<Connection> SORTED_CONNECTIONS = new ArrayList<>();

    public static void init() {
        List<Connection> connections = new ArrayList<>();

        try (java.sql.Connection conn = DriverManager.getConnection("jdbc:sqlite::resource:gtfs.db");
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM stop_times ORDER BY departure_time");
             ResultSet rs = stmt.executeQuery()) {

            String lastTripId = null;
            Trip currentTrip = null;
            Stop lastStop = null;
            int lastDep = -1;

            while (rs.next()) {
                String tripId = rs.getString("trip_id");
                String stopId = rs.getString("stop_id");
                String arrStr = rs.getString("arrival_time");
                String depStr = rs.getString("departure_time");
                int arr = TimeConverter.timeToSeconds(arrStr);
                int dep = TimeConverter.timeToSeconds(depStr);


                Stop stop = StopsCache.getStop(stopId);

                if (!tripId.equals(lastTripId)) {
                    lastTripId = tripId;
                    currentTrip = TripsCache.getTrip(tripId);
                    lastStop = stop;
                    lastDep = dep;
                } else {
                    Connection c = new Connection(currentTrip, lastStop, stop, lastDep, arr);
                    connections.add(c);
                    currentTrip.getConnections().add(c);
                    lastStop = stop;
                    lastDep = dep;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        SORTED_CONNECTIONS.addAll(ConnectionSort.sortConnections(connections));
    }

    public static List<Connection> getSortedConnections() {
        return SORTED_CONNECTIONS;
    }
}
