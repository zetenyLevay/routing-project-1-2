package ClosureAnalysis.Data.Graph;

import ClosureAnalysis.Calculations.EdgeWeightCalculator;

import java.sql.*;
import java.util.*;

public class StopGraph {
    private Set<StopNode> stopNodes;

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
                int distanceTraveled = rs.getInt("shape_dist_traveled");



                StopNode node = stopNodeMap.computeIfAbsent(stopId, k -> {
                    StopNode newNode = new StopNode(stopId);
                    newNode.setDistanceTraveledAtStop(distanceTraveled);
                    newNode.setArrivalTime(arrivalTime);
                    newNode.setDepartureTime(departureTime);
                    return newNode;
                });


                tripStops.computeIfAbsent(tripId, k -> new ArrayList<>()).add(node);

            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (Map.Entry<String, List<StopNode>> entry : tripStops.entrySet()) {
            String tripId = entry.getKey();
            List<StopNode> stops = entry.getValue();

            for (int i = 0; i < stops.size()-1; i++) {

                StopNode from = stops.get(i);
                StopNode to = stops.get(i + 1);

                StopEdge edge = new StopEdge(to);
                double weight = calculator.calculateEdgeWeight(from, to);
                edge.setWeight(weight);

                from.addEdge(tripId, edge);




            }

            if (!stops.isEmpty()) {
                stopGraph.addStopNode(stops.getLast());
            }
        }

        stopGraph.getStopNodes().addAll(stopNodeMap.values());
        for (StopNode node : stopGraph.getStopNodes()) {
            System.out.println("From: " + node.getLabel());
            for (StopEdge edge : node.getAllEdges()){
                System.out.println("To : " + edge.getTo().getLabel() + " weight: " + edge.getWeight());
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
    public static void main(String[] args) throws SQLException {
        StopGraph stopGraph = new StopGraph();
        Connection conn = DriverManager.getConnection("jdbc:sqlite:data/budapest_gtfs.db");
        stopGraph = stopGraph.buildStopGraph(conn);
        List<StopNode> stops = stopGraph.getStopNodes();
        System.out.println(stops.size());
    }
}
