package heatmap;

import routing.routingEngineDijkstra.adiModels.Stop.AdiStop;
import routing.routingEngineModels.Coordinates;
import java.sql.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton cache for storing stop data loaded from a GTFS database.
 */
public class StopsCache {
    private static StopsCache instance;
    private final Map<String, AdiStop> stopsMap;
    private static final String DB_PATH = "jdbc:sqlite:budapest_gtfs.db";

    /**
     * Constructs a StopsCache and loads stops from the database.
     */
    private StopsCache() {
        this.stopsMap = Collections.synchronizedMap(new HashMap<>());
        loadStopsFromDatabase();
    }

    /**
     * Retrieves the singleton instance of StopsCache, initializing it if necessary.
     *
     * @return the StopsCache instance
     */
    public static synchronized StopsCache getInstance() {
        if (instance == null) {
            instance = new StopsCache();
        }
        return instance;
    }

    /**
     * Loads stop data from the GTFS database into the cache.
     *
     * @throws RuntimeException if a database error occurs
     */
    private void loadStopsFromDatabase() {
        String query = "SELECT stop_id, stop_name, stop_lat, stop_lon FROM stops";

        try (Connection conn = DriverManager.getConnection(DB_PATH);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String id = rs.getString("stop_id");
                String name = rs.getString("stop_name");
                double lat = rs.getDouble("stop_lat");
                double lon = rs.getDouble("stop_lon");
                AdiStop stop = new AdiStop(id, name, new Coordinates(lat, lon));
                stopsMap.put(id, stop);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load stops from database", e);
        }
    }

    /**
     * Retrieves a stop by its ID.
     *
     * @param stopId the ID of the stop
     * @return the AdiStop object, or null if not found
     */
    public static AdiStop getStop(String stopId) {
        String lowerCaseId = stopId.toLowerCase();
        return getInstance().stopsMap.values().stream()
                .filter(stop -> stop.getStopID().toLowerCase().equals(lowerCaseId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Retrieves all stops in the cache.
     *
     * @return an unmodifiable map of stop IDs to AdiStop objects
     */
    public static Map<String, AdiStop> getAllStops() {
        return Collections.unmodifiableMap(getInstance().stopsMap);
    }

    /**
     * Initializes the stops cache by creating the singleton instance.
     */
    public static void init() {
        getInstance();
    }

    /**
     * Clears all stops from the cache.
     */
    public static void clearCache() {
        getInstance().stopsMap.clear();
    }

    /**
     * Reloads the cache by clearing existing data and reloading from the database.
     */
    public static void reload() {
        synchronized (StopsCache.class) {
            clearCache();
            getInstance().loadStopsFromDatabase();
        }
    }
}