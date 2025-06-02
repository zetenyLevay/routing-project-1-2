package routing.routingEngineAstar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import routing.db.DBConnectionManager;
import routing.routingEngineAstar.builders.DynamicGraphBuilder;
import routing.routingEngineModels.Coordinates;
import routing.routingEngineModels.RouteStep;
import routing.routingEngineModels.Stop.Stop;
import routing.routingEngineModels.utils.TimeAndGeoUtils;

/**
 * AStarRouter with dynamic graph building and time constraints
 */
public class RoutingEngineAstar {

    private static final int MAX_WAIT_SECONDS = 3600; // 1 hour
    private static final double INITIAL_WALK_RADIUS_M = 1000.0; // 1 km

    private final DBConnectionManager dbManager;
    private final Map<String, Stop> allStops;
    private final DynamicGraphBuilder graphBuilder;

    public RoutingEngineAstar(DBConnectionManager dbManager) {
        this.dbManager = dbManager;
        this.allStops = loadAllStops();
        this.graphBuilder = new DynamicGraphBuilder(dbManager);
    }
    
    /**
     * Main routing method - finds route from source to destination
     */
    public List<RouteStep> findRoute(double sourceLat, double sourceLon, 
                                   double destLat, double destLon, String startTime) {
        
        // Find nearby stops for start and end
        List<Stop> startStops = findNearbyStops(sourceLat, sourceLon, INITIAL_WALK_RADIUS_M);
        List<Stop> endStops = findNearbyStops(destLat, destLon, INITIAL_WALK_RADIUS_M);
        
        if (startStops.isEmpty() || endStops.isEmpty()) {
            return Collections.emptyList(); // No route possible
        }
        
        // Run A* algorithm with dynamic graph building
        return runAStar(startStops, endStops, startTime);
    }
    
    /**
     * Gets valid connections for a stop at a given time using dynamic graph builder
     */
    public List<RouteStep> getConnectionsForStop(Stop stop, String currentTime) {
        return graphBuilder.getValidRouteSteps(stop, currentTime);
    }
    
    private Map<String, Stop> loadAllStops() {
        Map<String, Stop> stops = new HashMap<>();
        
        String query = """
            SELECT stop_id, stop_name, stop_lat, stop_lon, 
                   location_type, parent_station 
            FROM stops
            """;
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String stopId = rs.getString("stop_id");
                String stopName = rs.getString("stop_name");
                double lat = rs.getDouble("stop_lat");
                double lon = rs.getDouble("stop_lon");
                int locationType = rs.getInt("location_type");
                String parentStation = rs.getString("parent_station");
                
                Coordinates coordinates = new Coordinates(lat, lon);
                Stop stop = new Stop(stopId, stopName, coordinates, locationType, parentStation);
                stops.put(stopId, stop);
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading stops: " + e.getMessage());
        }
        
        return stops;
    }
    
    private List<Stop> findNearbyStops(double lat, double lon, double radiusM) {
        List<Stop> nearbyStops = new ArrayList<>();
        
        for (Stop stop : allStops.values()) {
            double distance = TimeAndGeoUtils.haversineMeters(
                lat, lon, stop.getLatitude(), stop.getLongitude()
            );
            
            if (distance <= radiusM) {
                nearbyStops.add(stop);
            }
        }
        
        return nearbyStops;
    }
    
    private List<RouteStep> runAStar(List<Stop> startStops, List<Stop> endStops, String startTime) {
        // Implement your A* algorithm here
        // Use graphBuilder.getValidRouteSteps(stop, currentTime) to get connections
        // This is where you'll implement the actual A* search logic
        
        // Placeholder return
        return Collections.emptyList();
    }
}