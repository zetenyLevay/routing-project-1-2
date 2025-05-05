package routing.csa.engine.cache.classloader;

import routing.routingEngineModels.csamodel.Agency;
import routing.routingEngineModels.csamodel.route.Route;
import routing.routingEngineModels.csamodel.route.RouteInfo;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class RoutesCache {
    private static final Map<String, Route> ROUTES = new HashMap<>();

    public static void init() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite::resource:gtfs.db");
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM routes");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("route_id");
                String agencyId = rs.getString("agency_id");
                Agency agency = AgenciesCache.getAgency(agencyId);
                RouteInfo info = RouteInfoCache.getRouteInfo(id);
                ROUTES.put(id, new Route(id, agency, info));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Route getRoute(String id) {
        return ROUTES.get(id);
    }
}
