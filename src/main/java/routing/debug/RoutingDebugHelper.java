package routing.debug;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import routing.db.DBConnectionManager;
import routing.routingEngineAstar.RoutingEngineAstar;

/**
 * Debug helper class to troubleshoot the A* routing engine
 */
public class RoutingDebugHelper {
    
    private final DBConnectionManager dbManager;
    
    public RoutingDebugHelper(DBConnectionManager dbManager) {
        this.dbManager = dbManager;
    }
    
    /**
     * Test database connection and basic queries
     */
    public void testDatabaseConnection() {
        System.out.println("=== Testing Database Connection ===");
        
        try (Connection conn = dbManager.getConnection()) {
            System.out.println("✓ Database connection successful");
            
            // Test stops table
            String countQuery = "SELECT COUNT(*) as count FROM stops";
            try (PreparedStatement stmt = conn.prepareStatement(countQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("count");
                    System.out.println("✓ Stops table contains " + count + " records");
                }
            }
            
            // Show first few stops
            String sampleQuery = "SELECT stop_id, stop_name, stop_lat, stop_lon FROM stops LIMIT 5";
            try (PreparedStatement stmt = conn.prepareStatement(sampleQuery);
                 ResultSet rs = stmt.executeQuery()) {
                System.out.println("Sample stops:");
                while (rs.next()) {
                    System.out.printf("  %s: %s (%.6f, %.6f)%n", 
                        rs.getString("stop_id"),
                        rs.getString("stop_name"),
                        rs.getDouble("stop_lat"),
                        rs.getDouble("stop_lon"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Database connection failed: " + e.getMessage());
        }
    }
    
    /**
     * Test finding nearby stops for given coordinates
     */
    public void testFindNearbyStops(double lat, double lon, double radiusM) {
        System.out.println("\n=== Testing Find Nearby Stops ===");
        System.out.printf("Searching near (%.6f, %.6f) within %.0fm%n", lat, lon, radiusM);
        
        RoutingEngineAstar router = new RoutingEngineAstar(dbManager);
        
        // We need to make findNearbyStops public or create a test method
        // For now, let's test manually with SQL
        testNearbyStopsWithSQL(lat, lon, radiusM);
    }
    
    /**
     * Test nearby stops using direct SQL query
     */
    private void testNearbyStopsWithSQL(double lat, double lon, double radiusM) {
        String query = """
            SELECT stop_id, stop_name, stop_lat, stop_lon,
                   (6371000 * 2 * ASIN(SQRT(
                       POWER(SIN((RADIANS(stop_lat) - RADIANS(?)) / 2), 2) +
                       COS(RADIANS(?)) * COS(RADIANS(stop_lat)) *
                       POWER(SIN((RADIANS(stop_lon) - RADIANS(?)) / 2), 2)
                   ))) as distance_m
            FROM stops
            HAVING distance_m <= ?
            ORDER BY distance_m
            LIMIT 10
            """;
            
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setDouble(1, lat);
            stmt.setDouble(2, lat);
            stmt.setDouble(3, lon);
            stmt.setDouble(4, radiusM);
            
            try (ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.printf("  %s: %s (%.6f, %.6f) - %.0fm away%n",
                        rs.getString("stop_id"),
                        rs.getString("stop_name"),
                        rs.getDouble("stop_lat"),
                        rs.getDouble("stop_lon"),
                        rs.getDouble("distance_m"));
                }
                
                if (count == 0) {
                    System.out.println("✗ No stops found within radius");
                    
                    // Try finding the closest stops regardless of distance
                    findClosestStops(lat, lon, 5);
                } else {
                    System.out.println("✓ Found " + count + " stops within radius");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Error finding nearby stops: " + e.getMessage());
        }
    }
    
    /**
     * Find the closest N stops regardless of distance
     */
    private void findClosestStops(double lat, double lon, int limit) {
        System.out.println("\nFinding closest stops regardless of distance:");
        
        String query = """
            SELECT stop_id, stop_name, stop_lat, stop_lon,
                   (6371000 * 2 * ASIN(SQRT(
                       POWER(SIN((RADIANS(stop_lat) - RADIANS(?)) / 2), 2) +
                       COS(RADIANS(?)) * COS(RADIANS(stop_lat)) *
                       POWER(SIN((RADIANS(stop_lon) - RADIANS(?)) / 2), 2)
                   ))) as distance_m
            FROM stops
            ORDER BY distance_m
            LIMIT ?
            """;
            
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setDouble(1, lat);
            stmt.setDouble(2, lat);
            stmt.setDouble(3, lon);
            stmt.setInt(4, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("  %s: %s (%.6f, %.6f) - %.0fm away%n",
                        rs.getString("stop_id"),
                        rs.getString("stop_name"),
                        rs.getDouble("stop_lat"),
                        rs.getDouble("stop_lon"),
                        rs.getDouble("distance_m"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Error finding closest stops: " + e.getMessage());
        }
    }
    
    /**
     * Test the routing with realistic Budapest coordinates
     */
    public void testBudapestRouting() {
        System.out.println("\n=== Testing Budapest Routing ===");
        
        // Using coordinates from your data (around Vörösmarty tér area)
        double sourceLat = 47.498808;
        double sourceLon = 19.050563;
        double destLat = 47.463408;
        double destLon = 19.149165;
        String startTime = "08:00:00";
        
        System.out.printf("Route from (%.6f, %.6f) to (%.6f, %.6f)%n", 
            sourceLat, sourceLon, destLat, destLon);
        
        // First test finding nearby stops
        testFindNearbyStops(sourceLat, sourceLon, 1000.0);
        testFindNearbyStops(destLat, destLon, 1000.0);
        
        // Then test routing
        RoutingEngineAstar router = new RoutingEngineAstar(dbManager);
        List<routing.routingEngineModels.RouteStep> route = router.findRoute(
            sourceLat, sourceLon, destLat, destLon, startTime);
        
        System.out.println("Route result: " + route.size() + " steps");
        if (!route.isEmpty()) {
            for (int i = 0; i < route.size(); i++) {
                System.out.println("Step " + (i+1) + ": " + route.get(i));
            }
        }
    }
    
    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        // Update this path to match your database location
        String dbPath = "jdbc:sqlite:budapest_gtfs.db";
        
        DBConnectionManager dbManager = new DBConnectionManager(dbPath);
        RoutingDebugHelper debugHelper = new RoutingDebugHelper(dbManager);
        
        // Run all tests
        debugHelper.testDatabaseConnection();
        debugHelper.testBudapestRouting();
    }
}