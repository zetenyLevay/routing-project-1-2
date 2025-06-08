package routing.routingEngineAstar.finders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import routing.db.DBConnectionManager;
import routing.routingEngineModels.Coordinates;
import routing.routingEngineModels.Stop.Stop;
import routing.routingEngineModels.utils.TimeAndGeoUtils;

public class StopService {
    
    private final Map<String, Stop> allStops;
    private final DBConnectionManager dbManager;

    public StopService(DBConnectionManager dbManager) {
        this.dbManager = dbManager;
        this.allStops = loadAllStops();
    }

     /**
     * Loads all stops from the GTFS `stops` table into a HashMap, keyed by
     * stop_id.
     */
    private Map<String, Stop> loadAllStops() {
        Map<String, Stop> stops = new HashMap<>();

        String query = """
            SELECT stop_id, stop_name, stop_lat, stop_lon
            FROM stops
            """;

        try (
                Connection conn = dbManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String stopId = rs.getString("stop_id");
                String stopName = rs.getString("stop_name");
                double lat = rs.getDouble("stop_lat");
                double lon = rs.getDouble("stop_lon");

                Coordinates coordinates = new Coordinates(lat, lon);
                Stop stop = new Stop(stopId, stopName, coordinates);
                stops.put(stopId, stop);
            }
        } catch (SQLException e) {
            System.err.println("Error loading stops: " + e.getMessage());
            e.printStackTrace();
        }

        return stops;
    }

     /**
     * Finds all stops within `radiusM` meters of (lat, lon), using
     * straight‐line (haversine) distance.
     */
    public List<Stop> findNearbyStops(double lat, double lon, double radiusM) {
        List<Stop> nearbyStops = new ArrayList<>();

        for (Stop stop : allStops.values()) {
            double distance = TimeAndGeoUtils.haversineMeters(
                    lat, lon,
                    stop.getLatitude(), stop.getLongitude()
            );
            if (distance <= radiusM) {
                nearbyStops.add(stop);

            }
        }

        return nearbyStops;
    }

    public Map<String, Stop> getAllStops() {
        return allStops;
    }
}
