package ClosureAnalysis;

import ClosureAnalysis.Data.Graph.StopEdge;
import ClosureAnalysis.Data.Graph.StopGraph;
import ClosureAnalysis.Data.Graph.StopNode;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public class Dijkstra {

    private int count = 0;
    public Map<StopNode, Double> dijkstra(StopGraph graph, StopNode start) {


        Map<StopNode, Double> dist = new HashMap<>();
        for (StopNode node : graph.getStopNodes()) {
            dist.put(node, Double.valueOf(Double.MAX_VALUE));
        }
        dist.put(start, Double.valueOf(0.0));

        PriorityQueue<StopNode> pq = new PriorityQueue<>(
                Comparator.comparingDouble(dist::get)
        );
        pq.offer(start);



        Set<StopNode> visited = new HashSet<>();
        while (!pq.isEmpty()) {
            StopNode current = pq.poll();

            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);
//            for (StopEdge edge : current.getEdges()) {
//                StopNode neighbour = edge.getTo();
//                double newDist = dist.get(current) + edge.getWeight();
//                if (newDist < dist.get(neighbour)) {
//                    dist.put(neighbour, Double.valueOf(newDist));
//                    pq.offer(neighbour);
//                }
//            }
        }
        return dist;
    }

    public static void main(String[] args) throws SQLException {
        StopGraph graph = new StopGraph();
        Connection conn = DriverManager.getConnection("jdbc:sqlite:data/budapest_gtfs.db");
        graph = graph.buildStopGraph(conn);
        List<StopNode> nodes = graph.getStopNodes();
        Dijkstra dijkstra = new Dijkstra();
        Map<StopNode, Double> dist;


        for (StopNode node : nodes) {

            dist = dijkstra.dijkstra(graph, node);
            System.out.println("At node: " + node.getLabel() + " dist: " + dist.get(node));
        }


    }
}
