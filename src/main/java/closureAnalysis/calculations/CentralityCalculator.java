package closureAnalysis.calculations;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import closureAnalysis.Dijkstra;
import closureAnalysis.data.graph.StopGraph;
import closureAnalysis.data.graph.StopNode;

public class CentralityCalculator {
    /**
    * Calculates closeness centrality with formula :
    * C_C(u) = (n-1) / ∑_{v=1}^{n-1} d(u,v)
    * Where:
    *   n = number of reachable nodes
    *   d(u,v) = shortest-path distance between u and v
    *   (n-1) = normalization factor (for connected graphs)
    * What this is:
    *   Measures how close a stop is to all other stops
    *   Higher the value, more central and reachable
    *
    * @param graph the whole stop graph
     */
    public void calculateClosenessCentrality(StopGraph graph) {
        Dijkstra dijkstra = new Dijkstra();

        graph.getStopNodes().parallelStream().forEach(node -> {
            Map<StopNode, Double> dist = dijkstra.dijkstra(graph, node).dist();

            double totalDistance = dist.values().stream()
                    .filter(d -> d != Double.MAX_VALUE)
                    .mapToDouble(Double::doubleValue)
                    .sum();

            long reachableNodes = dist.values().stream()
                    .filter(d -> d != Double.MAX_VALUE)
                    .count();

            double result = (reachableNodes-1) / totalDistance;



            if (reachableNodes > 0) {

                node.setClosenessCentrality(result);
            }
            else {
                node.setClosenessCentrality(0.0);
            }
        });
    }
    /**
    * Calculates betweeness centrality using formula:
    * C_B(v) = ∑_{s≠v≠t} (σ_{st}(v) / σ_{st})
    * Where:
    * σ_{st} = total number of shortest paths from s to t
    * σ_{st}(v) = number of those paths passing through v
    * What this is:
    *   Measures how often a stop is on a shortest path
    *   Higher value means more critical for flow of public transport
     *   @param graph the whole stop graph
     */
    public void calculateBetweennessCentrality(StopGraph graph) {
        Dijkstra dijkstra = new Dijkstra();

        Map<StopNode, Double> betweenness = new HashMap<>();
        for (StopNode node : graph.getStopNodes()) {
            betweenness.put(node, 0.0);
        }
        graph.getStopNodes().parallelStream().forEach(node -> {
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
        });
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

        stopGraph.getStopNodes().stream()
                .sorted(Comparator.comparingDouble(StopNode::getBetweennessCentrality))
                .limit(10)
                .forEach(node -> System.out.println("Node : " + node.getBetweennessCentrality() + "Label: " + node.getId()));

        stopGraph.getStopNodes().stream()
                .sorted(Comparator.comparingDouble(StopNode::getClosenessCentrality).reversed())
                .limit(1000)
                .forEach(node -> System.out.println("Node : " + node.getClosenessCentrality() + " Label: " + node.getId()));
    }
}
