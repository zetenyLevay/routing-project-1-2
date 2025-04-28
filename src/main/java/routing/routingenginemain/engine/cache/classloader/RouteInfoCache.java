package routing.routingenginemain.engine.cache.classloader;

import routing.routingenginemain.model.route.RouteInfo;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class RouteInfoCache {
    private static final Map<String, RouteInfo> ROUTE_INFO = new HashMap<>();

    public static void init() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite::resource:gtfs.db");
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM routes");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("route_id");
                String shortName = rs.getString("route_short_name");
                String desc = rs.getString("route_desc");
                int type = rs.getInt("route_type");
                RouteInfo info = new RouteInfo(shortName, desc, type);
                ROUTE_INFO.put(id, info);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static RouteInfo getRouteInfo(String routeId) {
        return ROUTE_INFO.get(routeId);
    }
}
