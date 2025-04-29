package ClosureAnalysis.Data.Graph;

import ClosureAnalysis.Calculations.EdgeWeightCalculator;

import java.sql.*;
import java.util.*;

public class StopGraph {
    private Set<StopNode> stopNodes;
    private Map<String, StopEdge> edgeCache = new HashMap<>();

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


    public StopGraph buildStopGraph(Connection conn) {
        EdgeWeightCalculator calculator = new EdgeWeightCalculator();
        StopGraph stopGraph = new StopGraph();
        String query = "SELECT DISTINCT(trip_id), stop_id, CAST(stop_sequence AS INTEGER) as stop_sequence, " +
                "CAST(shape_dist_traveled AS INTEGER) as shape_dist_traveled, arrival_time, departure_time  " +
                "FROM stop_times_txt " +
                "ORDER BY trip_id, stop_sequence";
        Map<String, List<StopNode>> tripStops = new HashMap<>();
        Map<String, StopNode> stopNodeMap = new HashMap<>();

        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery();
        ) {

            while (rs.next()) {
                String tripId = rs.getString("trip_id");
                String stopId = rs.getString("stop_id");
                String arrivalTime = rs.getString("arrival_time");
                String departureTime = rs.getString("departure_time");
                int stopSequence = rs.getInt("stop_sequence");
                int distanceTraveled = rs.getInt("shape_dist_traveled");


                if (stopNodeMap.containsKey(stopId) && !containsStopNode(stopId)) {
                    StopNode nodeToUpdate = stopNodeMap.get(stopId);
                    if (!nodeToUpdate.getStopSequence().contains(stopSequence)) {
                        nodeToUpdate.addStopSequence(stopSequence);
                        nodeToUpdate.addArrivalTime(stopSequence, arrivalTime);
                        nodeToUpdate.addDepartureTime(stopSequence, departureTime);
                        nodeToUpdate.addDistanceTraveledAtStop(stopSequence, distanceTraveled);
                    }
                    stopNodeMap.put(stopId, nodeToUpdate);
                    tripStops.computeIfAbsent(tripId, k -> new ArrayList<>()).add(nodeToUpdate);
                }
                else{
                    StopNode node = stopNodeMap.computeIfAbsent(stopId, k -> {
                        StopNode newNode = new StopNode(stopId);
                        newNode.addStopSequence(stopSequence);
                        newNode.addDistanceTraveledAtStop(stopSequence,distanceTraveled);
                        newNode.addArrivalTime(stopSequence,arrivalTime);
                        newNode.addDepartureTime(stopSequence,departureTime);
                        return newNode;
                    });
                    tripStops.computeIfAbsent(tripId, k -> new ArrayList<>()).add(node);
                }
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (List<StopNode> nodes : tripStops.values()) {
            for (int node = 0; node < nodes.size()-1; node++) {


                StopNode from = nodes.get(node);
                StopNode to = nodes.get(node+1);

                String edgeKey = from.getLabel() + "->" + to.getLabel();

                StopEdge edge = edgeCache.computeIfAbsent(edgeKey, k -> {
                    StopEdge newEdge = new StopEdge(from, to);
                    double weight = calculator.calculateEdgeWeight(from, to);
                    newEdge.setWeight(weight);
                    return newEdge;
                });

                from.addEdge(edge);

                stopGraph.addStopNode(from);

            }
        }





//        StopNode first = stopGraph.getStopNodes().getFirst();
//       StopEdge edge = first.getAllEdges().getFirst();
//       StopNode to = edge.getTo();
//
//
//       System.out.println(first.getLabel() + " " + first.getArrivalTime());
//       System.out.println(to.getLabel() + " " + to.getArrivalTime());
//        System.out.println(edge.getWeight());



        return stopGraph;
    }

    public boolean containsStopNode(String stopId) {
        for (StopNode stopNode : stopNodes) {
            if (stopNode.getLabel().equals(stopId)) {
                return true;
            }
        }
        return false;
    }

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
    public static void main(String[] args) throws SQLException {
        StopGraph stopGraph = new StopGraph();
        Connection conn = DriverManager.getConnection("jdbc:sqlite:data/budapest_gtfs.db");
        stopGraph = stopGraph.buildStopGraph(conn);
        List<StopNode> stops = stopGraph.getStopNodes();
        System.out.println(stops.size());





    }
}
