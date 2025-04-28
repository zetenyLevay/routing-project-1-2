package routing.routingenginemain.engine.cache.classloader;

import routing.routingenginemain.model.Coordinates;
import routing.routingenginemain.model.Stop;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StopsCache {
    private static final Map<String, Stop> STOPS = new HashMap<>();

    public static void init() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite::resource:gtfs.db");
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM stops");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("stop_id");
                String name = rs.getString("stop_name");
                double lat = rs.getDouble("stop_lat");
                double lon = rs.getDouble("stop_lon");
                Coordinates coord = new Coordinates(lat, lon);
                STOPS.put(id, new Stop(id, name, coord, new ArrayList<>()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Stop getStop(String id) {
        return STOPS.get(id);
    }

    public static Collection<Stop> getAllStops() {
        return STOPS.values();
    }
}
