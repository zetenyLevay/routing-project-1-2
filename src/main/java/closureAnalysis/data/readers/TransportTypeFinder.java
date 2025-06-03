package closureAnalysis.data.readers;

import closureAnalysis.data.graph.StopNode;
import closureAnalysis.data.enums.TransportType;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class TransportTypeFinder implements Finder {
    private Map<String, TransportType> transportData;  // each stop id gets a transport type

    /**
     * due to large query we seperate this from main query as not to cause too much delay
     * @param conn
     */
    public void preload(Connection conn) {
        transportData = new HashMap<>();
        String query = "SELECT s.stop_id, r.route_type\n " +
                "FROM stops s\n" +
                "JOIN stop_times st USING(stop_id)\n" +
                "JOIN trips t using(trip_id)\n" +
                "JOIN routes r using(route_id)\n" +
                "GROUP BY s.stop_id, r.route_type\n" +
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

    @Override
    public void find(StopNode input) {
        TransportType type = transportData.get(input.getId());
        if (type != null) {
            input.setTransportType(type);
        }
    }
}
