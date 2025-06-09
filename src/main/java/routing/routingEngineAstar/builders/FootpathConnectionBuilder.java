package routing.routingEngineAstar.builders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import routing.db.DBConnectionManager;
import routing.routingEngineModels.Stop.Stop;
import routing.routingEngineModels.utils.TimeAndGeoUtils;

/**
 * Builds footpath connections between nearby stops
 */
public class FootpathConnectionBuilder {
    
    private static final double FOOTPATH_RADIUS_M = 500.0; // 0.5 km
    private static final double WALKING_SPEED_MS = 1.4; // 1.4 m/s (average walking speed)
    
    private final DBConnectionManager dbManager;
    
    public FootpathConnectionBuilder(DBConnectionManager dbManager) {
        this.dbManager = dbManager;
    }
    
    /**
     * Finds all walkable connections from a given stop
     */
    public Map<String, Integer> findFootpathConnections(Stop fromStop) {
        Map<String, Integer> footpaths = new HashMap<>();
        
        String query = """
            SELECT stop_id, stop_lat, stop_lon, stop_name
            FROM stops
            WHERE stop_id != ?
            """;
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, fromStop.getStopID());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String toStopId = rs.getString("stop_id");
                    double toLat = rs.getDouble("stop_lat");
                    double toLon = rs.getDouble("stop_lon");
                    
                    double distance = TimeAndGeoUtils.haversineMeters(
                        fromStop.getLatitude(), fromStop.getLongitude(),
                        toLat, toLon
                    );
                    
                    if (distance <= FOOTPATH_RADIUS_M) {
                        int walkingTime = (int) Math.ceil(distance / WALKING_SPEED_MS);
                        footpaths.put(toStopId, walkingTime);
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding footpath connections: " + e.getMessage());
        }
        
        return footpaths;
    }
}
