package routing.routingEngineDijkstra.newDijkstra.parsers;

import routing.routingEngineDijkstra.newDijkstra.algorithm.DijkstraRouter;
import routing.routingEngineDijkstra.newDijkstra.model.input.DijkstraConnection;
import routing.routingEngineDijkstra.newDijkstra.model.input.DijkstraRouteInfo;
import routing.routingEngineDijkstra.newDijkstra.model.input.DijkstraStop;

import java.sql.*;
import java.util.*;

public class GTFSDatabaseParser {
    private static final String DB_PATH = "jdbc:sqlite::resource:gtfs.db";
    public Map<String, DijkstraStop> parseStops() throws SQLException {
        Map<String, DijkstraStop> stops = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(DB_PATH)
;
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT stop_id, stop_name, stop_lat, stop_lon FROM stops")) {

            while (rs.next()) {
                String id = rs.getString("stop_id");
                String name = rs.getString("stop_name");
                double lat = rs.getDouble("stop_lat");
                double lon = rs.getDouble("stop_lon");
                stops.put(id, new DijkstraStop(id, name, lat, lon));
            }
        }
        return stops;
    }

    public Map<String, DijkstraRouteInfo> parseRoutes() throws SQLException {
        Map<String, DijkstraRouteInfo> routes = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(DB_PATH)
; Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT route_id, agency_id, route_short_name, route_long_name FROM routes")) {
            while (rs.next()) {
                String routeId = rs.getString("route_id");
                String operator = rs.getString("agency_id");
                String shortName = rs.getString("route_short_name");
                String longName = rs.getString("route_long_name");
                routes.put(routeId, new DijkstraRouteInfo(operator, shortName, longName, longName));
            }
        }
        return routes;
    }
    public Map<String, List<DijkstraConnection>> parseConnections(Map<String, DijkstraStop> stops) throws SQLException {
        Map<String, List<DijkstraConnection>> connections = new HashMap<>();



        String query = "SELECT t.trip_id, t.route_id, t.trip_headsign, " +
                "st1.stop_id as from_stop, st1.departure_time as dep_time, " +
                "st2.stop_id as to_stop, st2.arrival_time as arr_time " +
                "FROM stop_times st1 " +
                "JOIN stop_times st2 ON st1.trip_id = st2.trip_id AND st1.stop_sequence + 1 = st2.stop_sequence " +
                "JOIN trips t ON st1.trip_id = t.trip_id " +
                "ORDER BY st1.trip_id, st1.stop_sequence";


        try (Connection conn = DriverManager.getConnection(DB_PATH)
;

             Statement stmt = conn.createStatement();

             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String tripId = rs.getString("trip_id");
                String routeId = rs.getString("route_id");
                    String headSign = rs.getString("trip_headsign");
                String fromStopId = rs.getString("from_stop");
                String toStopId = rs.getString("to_stop");
                int depTime = parseGTFSTime(rs.getString("dep_time"));
                int arrTime = parseGTFSTime(rs.getString("arr_time"));

                DijkstraStop fromStop = stops.get(fromStopId);
                DijkstraStop toStop = stops.get(toStopId);

                if (fromStop != null && toStop != null) {
                    DijkstraConnection connection = new DijkstraConnection(
                            fromStop, toStop, depTime, arrTime, tripId, routeId, headSign);

                    connections.computeIfAbsent(fromStopId, k -> new ArrayList<>()).add(connection);
                }
            }
        }
        return connections;
    }

    private int parseGTFSTime(String timeStr) {
        String[] parts = timeStr.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        return hours * 3600 + minutes * 60 + seconds;
    }
    public static DijkstraRouter createRouterFromGTFS(int maxWalkingDistanceMeters) throws SQLException {
        GTFSDatabaseParser parser = new GTFSDatabaseParser();
        Map<String, DijkstraStop> stops = parser.parseStops();
        Map<String, DijkstraRouteInfo> routes = parser.parseRoutes();
        Map<String, List<DijkstraConnection>> connections = parser.parseConnections(stops);
        return new DijkstraRouter(stops, connections, routes, maxWalkingDistanceMeters);
    }




}