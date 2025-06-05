package heatmap;

import routing.routingEngineModels.Coordinates;
import routing.routingEngineModels.Stop.Stop;
import java.sql.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StopsCache {
    private static StopsCache instance;
    private final Map<String, Stop> stopsMap;
    private static final String DB_PATH = "jdbc:sqlite::resource:gtfs.db";

    // Private constructor for singleton pattern
    private StopsCache() {
        this.stopsMap = Collections.synchronizedMap(new HashMap<>());
        loadStopsFromDatabase();
    }

    public static synchronized StopsCache getInstance() {
        if (instance == null) {
            instance = new StopsCache();
        }
        return instance;
    }

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

                // Create Stop with just the required parameters
                Stop stop = new Stop(id, name, new Coordinates(lat, lon));
                stopsMap.put(id, stop);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load stops from database", e);
        }
    }

    public static Stop getStop(String stopId) {
        return getInstance().stopsMap.get(stopId);
    }

    public static Map<String, Stop> getAllStops() {
        return Collections.unmodifiableMap(getInstance().stopsMap);
    }

    public static void init() {
        // Trigger initialization if not already done
        getInstance();
    }

    public static int getCacheSize() {
        return getInstance().stopsMap.size();
    }

    public static void clearCache() {
        getInstance().stopsMap.clear();
    }

    public static void reload() {
        synchronized (StopsCache.class) {
            clearCache();
            getInstance().loadStopsFromDatabase();
        }
    }
}