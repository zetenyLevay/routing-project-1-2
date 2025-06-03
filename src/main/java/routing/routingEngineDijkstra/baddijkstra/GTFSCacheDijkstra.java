package routing.routingEngineDijkstra.baddijkstra;

import routing.routingEngineModels.Coordinates;

import java.sql.*;
import java.util.*;

public class GTFSCacheDijkstra {
    private static final Map<String, StopDijkstra> STOPS = new HashMap<>();
    private static final Map<String, List<DijkstraRouter.Connection>> CONNECTIONS = new HashMap<>();

    public static void init() {
        loadStops();
        loadConnections();
    }

    private static void loadStops() {
        String sql = "SELECT stop_id, stop_name, stop_lat, stop_lon, location_type, parent_station FROM stops";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite::resource:gtfs.db");
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("stop_id");
                String name = rs.getString("stop_name");
                double lat = rs.getDouble("stop_lat");
                double lon = rs.getDouble("stop_lon");
                int locationType = rs.getInt("location_type");
                String parentStation = rs.getString("parent_station");

                Coordinates coord = new Coordinates(lat, lon);
                StopDijkstra stop = new StopDijkstra(id, name, coord, locationType, parentStation);
                STOPS.put(id, stop);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void loadConnections() {
        String sql =
                "SELECT st1.trip_id, st1.stop_id AS from_id, st1.departure_time, " +
                        "       st2.stop_id AS to_id, st2.arrival_time " +
                        "FROM stop_times st1 " +
                        "JOIN stop_times st2 " +
                        "  ON st1.trip_id = st2.trip_id " +
                        " AND st1.stop_sequence + 1 = st2.stop_sequence " +
                        "ORDER BY st1.trip_id, st1.stop_sequence";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite::resource:gtfs.db");
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String tripId = rs.getString("trip_id");
                String fromId = rs.getString("from_id");
                String toId = rs.getString("to_id");
                int depTime = parseTimeToSeconds(rs.getString("departure_time"));
                int arrTime = parseTimeToSeconds(rs.getString("arrival_time"));

                DijkstraRouter.Connection connection =
                        new DijkstraRouter.Connection(fromId, toId, tripId, depTime, arrTime);

                CONNECTIONS
                        .computeIfAbsent(fromId, k -> new ArrayList<>())
                        .add(connection);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static int parseTimeToSeconds(String timeStr) {
        String[] parts = timeStr.split(":");
        int hours = Integer.parseInt(parts[0]) % 24;
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        return hours * 3600 + minutes * 60 + seconds;
    }

    public static Map<String, StopDijkstra> getStopsMap() {
        return Collections.unmodifiableMap(STOPS);
    }

    public static Collection<StopDijkstra> getAllStops() {
        return Collections.unmodifiableCollection(STOPS.values());
    }

    public static Map<String, List<DijkstraRouter.Connection>> getConnectionsMap() {
        return Collections.unmodifiableMap(CONNECTIONS);
    }

    public static StopDijkstra getStop(String stopId) {
        return STOPS.get(stopId);
    }
}