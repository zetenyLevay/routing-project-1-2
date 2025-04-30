package ClosureAnalysis.Calculations;

import ClosureAnalysis.Data.Graph.StopGraph;
import ClosureAnalysis.Data.Graph.StopNode;
import ClosureAnalysis.Dijkstra;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

public class CentralityCalculator {

    public void calculateClosenessCentrality(StopGraph graph) {
        Dijkstra dijkstra = new Dijkstra();

        for (StopNode node : graph.getStopNodes()) {
            Map<StopNode, Double> dist = dijkstra.dijkstra(graph, node);

            double totalDistance = dist.values().stream()
                    .filter(d -> d != Double.MAX_VALUE)
                    .mapToDouble(Double::doubleValue)
                    .sum();

            long reachableNodes = dist.values().stream()
                    .filter(d -> d != Double.MAX_VALUE)
                    .count();

            double result = totalDistance / reachableNodes;
            DecimalFormat df = new DecimalFormat("#.##");


            if (totalDistance > 0 && reachableNodes > 0) {
                node.setClosenessCentrality(Double.valueOf(df.format(result)));
            }
            else {
                node.setClosenessCentrality(0.0);
            }
        }

    }

    public void calculateBetweennessCentrality(StopGraph graph) {
        Dijkstra dijkstra = new Dijkstra();
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        StopGraph stopGraph = new StopGraph();
        CentralityCalculator calculator = new CentralityCalculator();
        Connection conn = DriverManager.getConnection("jdbc:sqlite:data/budapest_gtfs.db");
        stopGraph = stopGraph.buildStopGraph(conn);
        calculator.calculateClosenessCentrality(stopGraph);

        for (StopNode node : stopGraph.getStopNodes()) {
            System.out.println(node.getClosenessCentrality());
        }




    }
}
