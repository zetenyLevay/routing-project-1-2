package ClosureAnalysis;

import ClosureAnalysis.Data.Graph.StopEdge;
import ClosureAnalysis.Data.Graph.StopGraph;
import ClosureAnalysis.Data.Graph.StopNode;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public class Dijkstra {


    public Map<StopNode, Double> dijkstra(StopGraph graph, StopNode startNode) {

        Map<StopNode, Double> distances = new HashMap<>();
        for (StopNode node : graph.getStopNodes()) {
            distances.put(node, Double.MAX_VALUE);
        }
        distances.put(startNode, 0.0);


        PriorityQueue<StopNode> queue = new PriorityQueue<>(
                Comparator.comparingDouble(node -> distances.getOrDefault(node, Double.MAX_VALUE))
        );
        queue.add(startNode);

        Set<StopNode> visited = new HashSet<>();

        while (!queue.isEmpty()) {
            StopNode current = queue.poll();

            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);


            for (StopEdge edge : current.getAllEdges()) {
                StopNode neighbor = edge.getTo(current);
                double edgeWeight = edge.getWeight();
                double newDistance = distances.get(current) + edgeWeight;


                if (newDistance < distances.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    distances.put(neighbor, newDistance);
                    queue.add(neighbor);
                }
            }
        }

        return distances;
    }

    public static void main(String[] args) throws SQLException {
        StopGraph graph = new StopGraph();
        Connection conn = DriverManager.getConnection("jdbc:sqlite:data/budapest_gtfs.db");
        graph = graph.buildStopGraph(conn);

        Dijkstra dijkstra = new Dijkstra();

        System.out.println(graph.getStopNodes().size());

        StopNode testNode = graph.getStopNodes().get(100);
        System.out.println(testNode.getLabel());




        Map<StopNode, Double> dist = dijkstra.dijkstra(graph, testNode);
        for (StopNode node : graph.getStopNodes()) {
            System.out.println(dist.get(node));
        }


    }
}
