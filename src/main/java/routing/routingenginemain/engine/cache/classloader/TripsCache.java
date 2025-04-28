package routing.routingenginemain.engine.cache.classloader;

import routing.routingenginemain.model.Trip;
import routing.routingenginemain.model.route.Route;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TripsCache {
    private static final Map<String, Trip> TRIPS = new HashMap<>();

    public static void init() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite::resource:gtfs.db");
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM trips");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("trip_id");
                String routeId = rs.getString("route_id");
                String headsign = rs.getString("trip_headsign");
                Route route = RoutesCache.getRoute(routeId);
                TRIPS.put(id, new Trip(id, route, new ArrayList<>(), headsign));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Trip getTrip(String id) {
        return TRIPS.get(id);
    }
}
