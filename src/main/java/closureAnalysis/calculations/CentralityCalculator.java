package closureAnalysis.calculations;



import closureAnalysis.Dijkstra;
import closureAnalysis.data.graph.StopGraph;
import closureAnalysis.data.graph.StopNode;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public class CentralityCalculator {

    public void calculateClosenessCentrality(StopGraph graph) {
        Dijkstra dijkstra = new Dijkstra();

        for (StopNode node : graph.getStopNodes()) {
            Map<StopNode, Double> dist = dijkstra.dijkstra(graph, node).dist();

            double totalDistance = dist.values().stream()
                    .filter(d -> d != Double.MAX_VALUE)
                    .mapToDouble(Double::doubleValue)
                    .sum();

            long reachableNodes = dist.values().stream()
                    .filter(d -> d != Double.MAX_VALUE)
                    .count();

            if (reachableNodes > 0) {
                double result = 1 / totalDistance;
                node.setClosenessCentrality(result*100);
            }
            else {
                node.setClosenessCentrality(0.0);
            }
        }

        System.out.println(dijkstra.totalTime);
    }

    public void calculateBetweennessCentrality(StopGraph graph) {

        Dijkstra dijkstra = new Dijkstra();


        Map<StopNode, Double> betweenness = new HashMap<>();
        for (StopNode node : graph.getStopNodes()) {
            betweenness.put(node, 0.0);
        }

        for (StopNode node : graph.getStopNodes()) {
            Dijkstra.DijkstraResult result = dijkstra.dijkstra(graph, node);
            Stack<StopNode> stack = result.stack();
            Map<StopNode, List<StopNode>> predecessors = result.pred();
            Map<StopNode, Integer> sigma = result.sigma();
            Map<StopNode, Double> dist = result.dist();

            Map<StopNode, Double> delta = new HashMap<>();
            for (StopNode n : graph.getStopNodes()) {
                delta.put(n, 0.0);
            }

            while (!stack.isEmpty()) {
                StopNode n = stack.pop();
                for (StopNode pred : predecessors.get(n)) {
                    double coefficient = ((double) sigma.get(pred) / sigma.get(n)) * (1 +delta.get(n));
                    delta.put(pred, delta.get(pred) + coefficient);
                }
                if (!n.equals(node)) {
                    betweenness.put(n, betweenness.get(n) + delta.get(n));
                }
            }
        }



        for (StopNode node : graph.getStopNodes()) {
            node.setBetweennessCentrality(betweenness.get(node));

        }



    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        StopGraph stopGraph = new StopGraph();
        CentralityCalculator calculator = new CentralityCalculator();
        Connection conn = DriverManager.getConnection("jdbc:sqlite:data/budapest_gtfs.db");
        stopGraph = stopGraph.buildStopGraph(conn);
        calculator.calculateBetweennessCentrality(stopGraph);
        calculator.calculateClosenessCentrality(stopGraph);
        List<StopNode> list = new ArrayList<>();

        StopNode test = stopGraph.getStopNode("007884");

        System.out.println(test.getClosenessCentrality());
        System.out.println(test.getBetweennessCentrality());



        /*
        for (StopNode node : stopGraph.getStopNodes()) {
            if (node.getBetweennessCentrality() == 0.0)
            {
                list.add(node);
            }
            System.out.println(node.getBetweennessCentrality());

        }



        for (StopNode node : list) {
            List<StopNode> neighbors = node.getNeighbors();

            // Skip if node and its only neighbor only point to each other
            if (neighbors.size() == 1) {
                StopNode neighbor = neighbors.get(0);
                List<StopNode> neighborNeighbors = neighbor.getNeighbors();
                if (neighborNeighbors.size() == 1 && neighborNeighbors.get(0).equals(node)) {
                    continue; // Skip this trivial pair
                }
            }

            System.out.printf("Node: %s\n", node.getLabel());
            System.out.printf("  Betweenness Centrality: %.6f\n", node.getBetweennessCentrality());

            if (neighbors.isEmpty()) {
                System.out.println("  Neighbors: (none)");
            } else {
                System.out.print("  Neighbors: ");
                for (int i = 0; i < neighbors.size(); i++) {
                    System.out.print(neighbors.get(i).getLabel());
                    if (i < neighbors.size() - 1) System.out.print(", ");
                }
                System.out.println();
            }
        }



        stopGraph.getStopNodes().stream()
                .sorted(Comparator.comparingDouble(StopNode::getBetweennessCentrality).reversed())
                .limit(10)
                .forEach(node -> System.out.println("Node : " + node.getBetweennessCentrality() + "Label: " + node.getLabel()));

        */

        stopGraph.getStopNodes().stream()
                .sorted(Comparator.comparingDouble(StopNode::getClosenessCentrality).reversed())
                .limit(1000)
                .forEach(node -> System.out.println("Node : " + node.getClosenessCentrality() + " Label: " + node.getLabel()));







    }
}
