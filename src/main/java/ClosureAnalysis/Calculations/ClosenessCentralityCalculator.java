package ClosureAnalysis.Calculations;

import ClosureAnalysis.Data.StopGraph;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public class ClosenessCentralityCalculator {

    public Map<String, Double> closenessCentrality(StopGraph stopGraph) {
        Map<String, Double> closenessCentrality = new HashMap<>();
        for (String stop : stopGraph.getAllStops()){
            Map<String, Integer> distances = bfs(stopGraph, stop);
            int totalDistance = distances.values().stream().mapToInt(Integer::intValue).sum();
            if (totalDistance > 0){
                closenessCentrality.put(stop, (stopGraph.getAllStops().size() - 1) / (double) totalDistance);
            }
            else
                closenessCentrality.put(stop, 0.0); // unreachable stop
        }

        return closenessCentrality;
    }
    private Map<String, Integer> bfs(StopGraph stopGraph, String start) {
        Map<String, Integer> distance = new HashMap<>();
        Queue<String> queue = new LinkedList<>();

        distance.put(start, 0);
        queue.add(start);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            int currentDistance = distance.get(current);

            for (String neighbor : stopGraph.getNeighbors(current)) {
                if (!distance.containsKey(neighbor)) {
                    distance.put(neighbor, currentDistance + 1);
                    queue.add(neighbor);
                }
            }
        }
        return distance;
    }

    public static void main(String[] args) throws SQLException {
        StopGraph stopGraph = new StopGraph();
        Connection conn = DriverManager.getConnection("jdbc:sqlite:data/budapest_gtfs.db");
        stopGraph = stopGraph.buildStopGraph(conn);
        ClosenessCentralityCalculator closenessCentralityCalculator = new ClosenessCentralityCalculator();
        Map<String, Double> result = closenessCentralityCalculator.closenessCentrality(stopGraph);

        int count = 0;




        for (Map.Entry<String, Double> entry : result.entrySet()) {

                if (entry.getValue() == 0.0){
                    System.out.println(entry.getKey() + ": " + entry.getValue());
                    count++;
                }
        }
        System.out.println(count);
    }
}
