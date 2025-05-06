package closureAnalysis;




import closureAnalysis.data.graph.StopEdge;
import closureAnalysis.data.graph.StopGraph;
import closureAnalysis.data.graph.StopNode;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Dijkstra {

    public long totalTime;


    public DijkstraResult dijkstra(StopGraph graph, StopNode startNode) {

        Instant start = Instant.now();
        //System.out.println("Dijkstra start: " + start);

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
                    pred.get(neighbor).clear();
                    queue.add(neighbor);
                }
                if (distances.get(neighbor).equals(newDistance) ) {
                    sigma.put(neighbor, sigma.get(neighbor) + sigma.get(current));
                    pred.get(neighbor).add(current);
                }
            }
        }

        Instant finish = Instant.now();
        totalTime +=  Duration.between(start,finish).toMillis();
        //System.out.println("Dijkstra finished");
        //System.out.println("Dijkstra time: " + Duration.between(start, finish).toMillis());


        return new DijkstraResult(distances, sigma, pred, stack);
    }

    public static void main(String[] args) throws SQLException {
        StopGraph graph = new StopGraph();
        Connection conn = DriverManager.getConnection("jdbc:sqlite:data/budapest_gtfs.db");
        graph = graph.buildStopGraph(conn);

        Dijkstra dijkstra = new Dijkstra();

        System.out.println(graph.getStopNodes().size());

        StopNode testNode = graph.getStopNode("007884");
        System.out.println(testNode.getLabel());


        DijkstraResult dijkstraResult = dijkstra.dijkstra(graph, testNode);
        Map<StopNode, List<StopNode>> pred = dijkstraResult.pred;
        Map<StopNode, Double> dist = dijkstraResult.dist;
        List<StopNode> list = pred.get(testNode);

        System.out.println(list.size());

        for (StopNode node : pred.get(testNode)) {
            System.out.println("To: " + node.getLabel() + " | "+ dist.get(node) + " Meters");
        }


        /*

        for (StopNode node : graph.getStopNodes()) {
            System.out.println("To: " + node.getLabel() + " | "+ dist.get(node) + " Meters");
        }

         */



    }

    public record DijkstraResult(Map<StopNode, Double> dist, Map<StopNode, Integer> sigma, Map<StopNode, List<StopNode>> pred,
                          Stack<StopNode> stack) {
    }


}
