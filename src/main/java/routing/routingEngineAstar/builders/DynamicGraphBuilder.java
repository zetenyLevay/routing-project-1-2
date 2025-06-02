package routing.routingEngineAstar.builders;

import graphStructure.Graph;
import routing.db.DBConnectionManager;
import routing.routingEngineModels.Stop.Stop;
import routing.routingEngineModels.RouteStep;
import routing.routingEngineAstar.finders.StopConnectionFinder;
import routing.routingEngineAstar.validators.TimeConstraintValidator;

import java.sql.SQLException;
import java.util.*;

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
            // Create a walking RouteStep using the walking constructor
            RouteStep walkingStep = createWalkingRouteStep(
                fromStop.getStopID(), 
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
    private RouteStep createWalkingRouteStep(String fromStopId, String toStopId, 
                                           int walkingSeconds, String startTime) {
        // Get destination stop coordinates
        Stop toStop = getStopById(toStopId);
        if (toStop == null) {
            return null;
        }
        
        Coordinates toCoord = new Coordinates(toStop.getLatitude(), toStop.getLongitude());
        
        // Use the walking constructor (modeOfTransport, toCoord, startTime)
        return new RouteStep("WALK", toCoord, startTime);
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
                    return new Stop(stopId, stopName, lat, lon);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting stop by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Adds seconds to a time string
     */
    private String addSecondsToTime(String timeStr, int secondsToAdd) {
        try {
            String[] parts = timeStr.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int seconds = Integer.parseInt(parts[2]);
            
            int totalSeconds = hours * 3600 + minutes * 60 + seconds + secondsToAdd;
            
            int newHours = (totalSeconds / 3600) % 24;
            int newMinutes = (totalSeconds % 3600) / 60;
            int newSecs = totalSeconds % 60;
            
            return String.format("%02d:%02d:%02d", newHours, newMinutes, newSecs);
            
        } catch (Exception e) {
            return timeStr; // Return original time if parsing fails
        }
    }
    
    /**
     * Clears the connection cache (useful for memory management)
     */
    public void clearCache() {
        connectionCache.clear();
    }
}
