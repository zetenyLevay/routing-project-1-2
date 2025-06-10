package closureAnalysis.data.readers;

import closureAnalysis.data.graph.StopNode;
import closureAnalysis.data.enums.TransportType;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Determines and attaches transport types to StopNodes based on GTFS route data.
 *
 * <p>This implementation:
 * <ul>
 *   <li>Preloads transport type information by joining multiple GTFS tables</li>
 *   <li>Maps GTFS route_type integers to TransportType enum values</li>
 *   <li>Provides efficient lookup after the initial preloading</li>
 * </ul>
 *
 * <p>Separated from main graph building to optimize query performance.
 */
public class TransportTypeFinder implements Finder {
    private Map<String, TransportType> transportData;  // each stop id gets a transport type

    /**
     * Preloads transport type data by querying the GTFS database.
     * Joins stops, stop_times, trips, and routes tables to determine
     * the primary transport type for each stop.
     * @param conn Active database connection to the GTFS data
     */
    public void preload(Connection conn) {
        transportData = new HashMap<>();
        String query = "SELECT s.stop_id, r.route_type " +
                "FROM stops s " +
                "JOIN stop_times st USING(stop_id) " +
                "JOIN trips t using(trip_id) " +
                "JOIN routes r using(route_id) " +
                "GROUP BY s.stop_id, r.route_type " +
                "ORDER BY s.stop_id, r.route_type;";
        try (PreparedStatement ps = conn.prepareStatement(query)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String stopId = rs.getString("stop_id");
                int routeType = rs.getInt("route_type");


                transportData.putIfAbsent(stopId, TransportType.fromIntType(routeType));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Finds and sets the transport type for the given stop node.
     * @param input The StopNode whose transport type needs to be set
     */
    @Override
    public void find(StopNode input) {
        TransportType type = transportData.get(input.getId());
        if (type != null) {
            input.setTransportType(type);
        }
    }
}
