package closureAnalysis.data.graph;

import closureAnalysis.calculations.EdgeWeightCalculator;
import javafx.util.Pair;

import java.sql.*;
import java.util.*;

public class StopGraph {
    private Set<StopNode> stopNodes;
    private Map<String, StopEdge> edgeCache = new HashMap<>();
    Map<String, StopNode> stopNodeMap = new HashMap<>();

    public StopGraph() {
        stopNodes = new LinkedHashSet<>();
    }

    public List<StopNode> getStopNodes() {
        return new ArrayList<>(stopNodes);
    }

//    public List<StopNode> getNeighbours(StopNode s) {
//        List<StopNode> neighbours = new ArrayList<>();
//        for (StopEdge e : s.getEdges()) {
//            neighbours.add(e.getTo());
//        }
//        return neighbours;
//    }

    void addStopNode(StopNode n) {
        stopNodes.add(n);
    }

    public int getSize(){
        return stopNodes.size();
    }

    public StopNode getStopNode(String id) {
        for (StopNode n : stopNodes) {
            if (n.getLabel().equals(id)) {
                return n;
            }
        }
        return null;
    }


    public StopGraph buildStopGraph(Connection conn) {
        EdgeWeightCalculator calculator = new EdgeWeightCalculator();
        StopGraph stopGraph = new StopGraph();
        String query = "SELECT DISTINCT(trip_id), stop_id, CAST(stop_sequence AS INTEGER) as stop_sequence, " +
                "CAST(shape_dist_traveled AS INTEGER) as shape_dist_traveled, arrival_time, departure_time  " +
                "FROM stop_times_txt " +
                "ORDER BY trip_id, stop_sequence";
        List<Pair<Integer, StopNode>> tripToProcess = new ArrayList<>();

        String previousTrip = null;

        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery();
        ) {
            printMemoryUsage("Before scanning trips");
            while (rs.next()) {
                String tripId = rs.getString("trip_id");
                String stopId = rs.getString("stop_id");
                String arrivalTime = rs.getString("arrival_time");
                String departureTime = rs.getString("departure_time");
                int stopSequence = rs.getInt("stop_sequence");
                int distanceTraveled = rs.getInt("shape_dist_traveled");


                StopNode node = stopNodeMap.computeIfAbsent(stopId, k -> new StopNode(stopId));
                StopInstance inst = new StopInstance(tripId, stopSequence, arrivalTime, departureTime, distanceTraveled);
                node.addStopInstance(inst);


                if (previousTrip == null) {
                    previousTrip = tripId;
                }
                else if (!previousTrip.equals(tripId)) {
                    processTrip(tripToProcess, calculator, stopGraph);
                    previousTrip = tripId;
                    tripToProcess.clear();
                }

                tripToProcess.add(new Pair<>(stopSequence, node));
            }
            printMemoryUsage("After scanning trips");
            if (!tripToProcess.isEmpty()) {
                processTrip(tripToProcess, calculator, stopGraph);
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        stopNodes.addAll(stopNodeMap.values());
        printMemoryUsage("End");
        return stopGraph;
    }

    private void processTrip(List<Pair<Integer, StopNode>> tripStops, EdgeWeightCalculator calculator, StopGraph stopGraph) {
        tripStops.sort(Comparator.comparingInt(Pair::getKey));

        for (int i = 0; i < tripStops.size() - 1; i++ ) {
            StopNode from = tripStops.get(i).getValue();
            StopNode to = tripStops.get(i+1).getValue();

            String edgeKey = from.getLabel().compareTo(to.getLabel()) < 0 ?
                    from.getLabel() + "--" + to.getLabel() :
                    to.getLabel() + "--" + from.getLabel();

            StopEdge edge = edgeCache.computeIfAbsent(edgeKey, k -> {
                StopEdge newEdge = new StopEdge(from, to);
                double weight = calculator.calculateEdgeWeight(from, to);
                newEdge.setWeight(weight);
                return newEdge;
                });
            /*
            if (edge.getWeight() > 0) {

            }
            */

            from.addEdge(edge);
            to.addEdge(edge);

            stopGraph.addStopNode(from);
            if (i == tripStops.size() - 2) {
                stopGraph.addStopNode(to);
            }
        }
    }

    public boolean containsStopNode(String stopId) {
        for (StopNode stopNode : stopNodes) {
            if (stopNode.getLabel().equals(stopId)) {
                return true;
            }
        }
        return false;
    }
/*
    public void printAllNeighbors() {
        for (StopNode node : stopNodes) {
            System.out.println("\nNode: " + node.getLabel());
            System.out.println("Neighbors:");

            List<StopNode> neighbors = node.getNeighbors();
            if (neighbors.isEmpty()) {
                System.out.println("  (No neighbors)");
            } else {
                for (StopNode neighbor : neighbors) {
                    System.out.println("  - " + neighbor.getLabel());

                    node.getAllEdges().stream()
                            .filter(e -> e.getTo().equals(neighbor))
                            .findFirst()
                            .ifPresent(e -> System.out.println("    Edge weight: " + e.getWeight()));
                }
            }
        }
    }
    */

    private static void printMemoryUsage(String phase) {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        System.out.printf("[MEMORY] %s - Used: %.2f MB / Max: %.2f MB%n",
                phase,
                usedMemory / (1024.0 * 1024.0),
                maxMemory / (1024.0 * 1024.0));
    }

    public static void main(String[] args) throws SQLException {
        StopGraph stopGraph = new StopGraph();
        Connection conn = DriverManager.getConnection("jdbc:sqlite:data/budapest_gtfs.db");
        stopGraph = stopGraph.buildStopGraph(conn);
        List<StopNode> stops = stopGraph.getStopNodes();
        System.out.println(stops.size());






    }
}
