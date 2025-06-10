package closureAnalysis.calculations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import closureAnalysis.Dijkstra;
import closureAnalysis.data.graph.StopGraph;
import closureAnalysis.data.graph.StopNode;
/**
 * Calculates centrality measures for nodes in a transportation network graph.
 * Provides methods for computing both closeness and betweenness centrality.
 */
public class CentralityCalculator {
    /**
     * Calculates closeness centrality using the formula:
     * C_C(u) = (n-1) / ∑_{v=1}^{n-1} d(u,v)
     * Where:
     *   n = number of reachable nodes
     *   d(u,v) = shortest-path distance between u and v
     *   (n-1) = normalization factor (for connected graphs)
     *
     * <p>Measures how close a stop is to all other stops. Higher values indicate
     * more central and reachable nodes in the network.
     *
     * @param graph The StopGraph containing all nodes to analyze
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
     * Calculates betweenness centrality using the formula:
     * C_B(v) = ∑_{s≠v≠t} (σ_{st}(v) / σ_{st})
     * Where:
     *   σ_{st} = total number of shortest paths from s to t
     *   σ_{st}(v) = number of those paths passing through v
     *
     * <p>Measures how often a stop is on the shortest path between other stops.
     * Higher values indicate nodes that are more critical for the flow of public transport.
     *
     * @param graph The StopGraph containing all nodes to analyze
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
}
