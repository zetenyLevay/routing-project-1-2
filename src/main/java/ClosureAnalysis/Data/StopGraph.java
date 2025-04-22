package ClosureAnalysis.Data;

import java.sql.*;
import java.util.*;

public class StopGraph {
    private Map<String, Set<String>> adjList = new HashMap<>();

    public void addEdge(String src, String dest) {
        adjList.putIfAbsent(src, new HashSet<>());
        adjList.get(src).add(dest);
    }

    public void addStop(String src) {
        adjList.putIfAbsent(src, new HashSet<>());
    }

    public Set<String> getNeighbors(String stop) {
        return adjList.getOrDefault(stop, Collections.emptySet());
    }

    public Set<String> getAllStops() {
        return adjList.keySet();
    }

    public StopGraph buildStopGraph(Connection conn) {

        StopGraph stopGraph = new StopGraph();
        String query = "SELECT trip_id, stop_id, stop_sequence FROM stop_times_txt ORDER BY trip_id, stop_sequence";
        Map<String, List<String>> tripStops = new HashMap<>();

        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery();
        ) {

            while (rs.next()) {
                String tripId = rs.getString("trip_id");
                String stopId = rs.getString("stop_id");

                tripStops.computeIfAbsent(tripId, k -> new ArrayList<>()).add(stopId);

            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (List<String> stops : tripStops.values()) {
            for (int i = 0; i < stops.size(); i++) {

                String from = stops.get(i);
                stopGraph.addStop(from);

                if (i < stops.size() - 1) {
                    String to = stops.get(i + 1);
                    stopGraph.addStop(to);
                    stopGraph.addEdge(from, to);
                }
            }
        }
        return stopGraph;
    }
    public static void main(String[] args) throws SQLException {
        StopGraph stopGraph = new StopGraph();
        Connection conn = DriverManager.getConnection("jdbc:sqlite:data/budapest_gtfs.db");
        stopGraph = stopGraph.buildStopGraph(conn);
        Set<String> stops = stopGraph.getAllStops();
        System.out.println(stops.size());
    }
}
