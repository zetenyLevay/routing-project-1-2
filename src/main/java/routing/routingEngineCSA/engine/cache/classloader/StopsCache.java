package routing.routingEngineCSA.engine.cache.classloader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import routing.routingEngineModels.Coordinates;
import routing.routingEngineModels.Stop.Stop;

/**
 * StopsCache.java
 *
 * This class is responsible for loading and caching stop data from a GTFS database.
 * It provides methods to initialize the cache, retrieve a specific stop by ID,
 * and get all stops in the cache.
 */
public class StopsCache {
    private static final Map<String, Stop> STOPS = new HashMap<>();

    /**
     * Initializes the StopsCache by loading stop data from the GTFS database.
     * This method connects to the database, retrieves all stops, and stores them
     * in a map for quick access.
     */
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
                // int locationType = rs.getInt("location_type");
                // String parentStation = rs.getString("parent_station");
                STOPS.put(id, new Stop(id, name, coord));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Getters
    public static Stop getStop(String id) {
        return STOPS.get(id);
    }

    public static Collection<Stop> getAllStops() {
        return STOPS.values();
    }
}