package routingenginemain.engine.cache.classloader;

import routingenginemain.model.Connection;
import routingenginemain.model.Stop;
import routingenginemain.model.Trip;
import routingenginemain.engine.ConnectionSort;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ConnectionsCache {
    private static final List<Connection> SORTED_CONNECTIONS = new ArrayList<>();

    public static void init() {
        List<Connection> connections = new ArrayList<>();

        try (java.sql.Connection conn = DriverManager.getConnection("jdbc:sqlite:gtfs.db");
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM stop_times ORDER BY departure_time");
             ResultSet rs = stmt.executeQuery()) {

            String lastTripId = null;
            Trip currentTrip = null;
            Stop lastStop = null;
            int lastDep = -1;

            while (rs.next()) {
                String tripId = rs.getString("trip_id");
                String stopId = rs.getString("stop_id");
                int arr = rs.getInt("arrival_time");
                int dep = rs.getInt("departure_time");

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
