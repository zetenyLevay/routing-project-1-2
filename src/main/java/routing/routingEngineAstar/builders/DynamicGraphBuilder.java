package routing.routingEngineAstar.builders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import routing.db.DBConnectionManager;
import routing.routingEngineAstar.finders.StopConnectionFinder;
import routing.routingEngineAstar.validators.TimeConstraintValidator;
import routing.routingEngineModels.Coordinates;
import routing.routingEngineModels.RouteStep;
import routing.routingEngineModels.Stop.Stop;

/**
 * Builds the graph dynamically during A* search with time constraints
 */
public class DynamicGraphBuilder {
    
    private final DBConnectionManager dbManager;
    private final StopConnectionFinder connectionFinder;
    private final FootpathConnectionBuilder footpathBuilder;
    private final TimeConstraintValidator timeValidator;
    
    // Cache to avoid rebuilding connections for the same stop-time combination
    private final Map<String, List<RouteStep>> connectionCache;
    
    public DynamicGraphBuilder(DBConnectionManager dbManager) {
        this.dbManager = dbManager;
        this.connectionFinder = new StopConnectionFinder(dbManager);
        this.footpathBuilder = new FootpathConnectionBuilder(dbManager);
        this.timeValidator = new TimeConstraintValidator();
        this.connectionCache = new HashMap<>();
    }
    
    /**
     * Gets all valid route steps from a stop at a given time
     */
    public List<RouteStep> getValidRouteSteps(Stop fromStop, String currentTime) {
        String cacheKey = fromStop.getStopID() + "_" + currentTime;
        
        // Check cache first
        if (connectionCache.containsKey(cacheKey)) {
            return connectionCache.get(cacheKey);
        }
        
        List<RouteStep> validSteps = new ArrayList<>();
        
        // Get transit connections
        List<RouteStep> transitSteps = connectionFinder.findValidConnections(fromStop, currentTime);
        validSteps.addAll(transitSteps);
        
        // Get walking connections (footpaths)
        Map<String, Integer> footpaths = footpathBuilder.findFootpathConnections(fromStop);
        for (Map.Entry<String, Integer> footpath : footpaths.entrySet()) {
            // Create a walking RouteStep
            RouteStep walkingStep = createWalkingRouteStep(
                fromStop, 
                footpath.getKey(), 
                footpath.getValue(),
                currentTime
            );
            if (walkingStep != null) {
                validSteps.add(walkingStep);
            }
        }
        
        // Cache the result
        connectionCache.put(cacheKey, validSteps);
        return validSteps;
    }
    
    /**
     * Creates a walking route step
     */
    private RouteStep createWalkingRouteStep(Stop fromStop, String toStopId, 
                                           int walkingSeconds, String startTime) {
        // Get destination stop
        Stop toStop = getStopById(toStopId);
        if (toStop == null) {
            return null;
        }
        
        // Use the walking constructor that takes Stop object and walking time
        return new RouteStep("walk", toStop, startTime, walkingSeconds);
    }
    
    /**
     * Gets a stop by ID from the database
     */
    private Stop getStopById(String stopId) {
        String query = "SELECT stop_id, stop_name, stop_lat, stop_lon FROM stops WHERE stop_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, stopId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String stopName = rs.getString("stop_name");
                    double lat = rs.getDouble("stop_lat");
                    double lon = rs.getDouble("stop_lon");
                    Coordinates coordinates = new Coordinates(lat, lon);
                    return new Stop(stopId, stopName, coordinates);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting stop by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Clears the connection cache (useful for memory management)
     */
    public void clearCache() {
        connectionCache.clear();
    }
}