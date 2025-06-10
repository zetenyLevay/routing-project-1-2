package closureAnalysis;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;
import closureAnalysis.data.graph.StopEdge;
import closureAnalysis.data.graph.StopGraph;
import closureAnalysis.data.graph.StopNode;

/**
 * Implements Dijkstra's algorithm for finding the shortest paths in a graph,
 * with additional functionality for Brandes' algorithm to support betweenness centrality calculations.
 *
 * <p>This class provides:
 * <ul>
 *   <li>Shortest path calculations between nodes</li>
 *   <li>Tracking of predecessor nodes and path counts</li>
 *   <li>Support for betweenness centrality calculations through Brandes' algorithm</li>
 * </ul>
 *
 * <p>The algorithm maintains:
 * <ul>
 *   <li>Distance maps for each node</li>
 *   <li>Counts of shortest paths (sigma)</li>
 *   <li>Predecessor lists for path reconstruction</li>
 * </ul>
 */
public class Dijkstra {


    /**
     * Executes Dijkstra's algorithm from a given start node.
     * @param graph The graph to analyze
     * @param startNode The starting node for path calculations
     * @return A DijkstraResult containing distances, path counts, predecessors, and traversal order
     */
    public DijkstraResult dijkstra(StopGraph graph, StopNode startNode) {

        Map<StopNode, List<StopNode>> pred = new HashMap<>();
        Map<StopNode, Integer> sigma = new HashMap<>();
        Stack<StopNode> stack = new Stack<>();
        Map<StopNode, Double> distances = new HashMap<>();
        for (StopNode node : graph.getStopNodes()) {
            distances.put(node, Double.MAX_VALUE);
            sigma.put(node, 0);
            pred.put(node, new ArrayList<>());
        }

        distances.put(startNode, 0.0);
        sigma.put(startNode, 1);

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

            stack.push(current);

            for (StopEdge edge : current.getAllEdges()) {
                StopNode neighbor = edge.getTo(current);
                double edgeWeight = edge.getWeight();
                double newDistance = distances.get(current) + edgeWeight;


                if (newDistance < distances.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    distances.put(neighbor, newDistance);
                    sigma.put(neighbor, 0);
                    pred.get(neighbor);
                    queue.add(neighbor);
                }
                if (distances.get(neighbor).equals(newDistance) ) {
                    sigma.put(neighbor, sigma.get(neighbor) + sigma.get(current));
                    pred.get(neighbor).add(current);
                }
            }
        }
        return new DijkstraResult(distances, sigma, pred, stack);
    }
    /**
     * Represents the results of a Dijkstra algorithm execution.
     * @param dist Map of nodes to their distances from the start node
     * @param sigma Map of nodes to their number of shortest paths from start
     * @param pred Map of nodes to their predecessor nodes in the shortest paths
     * @param stack The traversal order of nodes (LIFO)
     */
    public record DijkstraResult(Map<StopNode, Double> dist, Map<StopNode, Integer> sigma, Map<StopNode, List<StopNode>> pred,
                          Stack<StopNode> stack) {
    }
}
