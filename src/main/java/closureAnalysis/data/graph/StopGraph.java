package closureAnalysis.data.graph;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import closureAnalysis.calculations.EdgeWeightCalculator;
import com.sun.javafx.geom.Edge;
import javafx.util.Pair;

/**
 * big daddy graph itself
 */
public class StopGraph {

    private Set<StopNode> stopNodes;
    private Map<String, StopEdge> edgeCache = new HashMap<>();
    public Map<String, StopNode> stopNodeMap = new HashMap<>();

    public StopGraph() {
        stopNodes = new LinkedHashSet<>();
    }
    public List<StopNode> getStopNodes() {
        return new ArrayList<>(stopNodes);
    }
    void addStopNode(StopNode n) {
        stopNodes.add(n);
    }
    public int getSize() {
        return stopNodes.size();
    }
    public StopNode getStopNode(String id) {
        for (StopNode n : stopNodes) {
            if (n.getId().equals(id)) {
                return n;
            }
        }
        return null;
    }

    /**
     * iterates through every single trip in database, creates a StopNode for each unique stop in table
     * since a stop can have different routes going through it, we create a new StopInstance for each (by checking if its a different sequence number)
     *
     * @param conn db connection
     * @return returns fully built graph
     */
    public StopGraph buildStopGraph(Connection conn) {

        EdgeWeightCalculator calculator = new EdgeWeightCalculator();
        StopGraph stopGraph = new StopGraph();
        String query = "SELECT trip_id, stop_id, CAST(stop_sequence AS INTEGER) as stop_sequence, "
                + "CAST(shape_dist_traveled AS INTEGER) as shape_dist_traveled, arrival_time, departure_time "
                + "FROM stop_times "
                + "ORDER BY trip_id, stop_sequence";
        List<Pair<Integer, StopNode>> tripToProcess = new ArrayList<>(); // a trip has number (stop_sequence) connected to a node, so we know which node comes after which

        String previousTrip = null;



        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery();) {

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
                } else if (!previousTrip.equals(tripId)) {
                    processTrip(tripToProcess, calculator, stopGraph);
                    previousTrip = tripId;
                    tripToProcess.clear();
                }

                tripToProcess.add(new Pair<>(stopSequence, node));
            }
            // make sure last trip is processed
            if (!tripToProcess.isEmpty()) {
                processTrip(tripToProcess, calculator, stopGraph);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        stopNodes.addAll(stopNodeMap.values());

        System.out.println(stopGraph.getSize());
        return stopGraph;
    }

    /**
     * starting from 0, we check each sequence number, based on that we create an edge using a unique key for each
     *
     * @param tripStops  a trip has number (stop_sequence) connected to a node, so we know which node comes after which
     * @param calculator
     * @param stopGraph
     */

    private void processTrip(List<Pair<Integer, StopNode>> tripStops, EdgeWeightCalculator calculator, StopGraph stopGraph) {
        tripStops.sort(Comparator.comparingInt(Pair::getKey)); // make sure nodes are in order

        for (int i = 0; i < tripStops.size() - 1; i++) {
            StopNode from = tripStops.get(i).getValue();
            StopNode to = tripStops.get(i + 1).getValue();

            String edgeKey = generateEdgeKey(from, to);

            StopEdge edge = edgeCache.computeIfAbsent(edgeKey, k -> {
                StopEdge newEdge = new StopEdge(from, to);
                double weight = calculator.calculateEdgeWeight(from, to);
                newEdge.setWeight(weight);
                return newEdge;
            });

            // both nodes get the edge (bidirectional)
            from.addEdge(edge);
            to.addEdge(edge);

            // so the last stops dont get skipped
            stopGraph.addStopNode(from);
            if (i == tripStops.size() - 2) {
                stopGraph.addStopNode(to);
            }
        }
    }

    /**
     * simple edge key
     * @param from
     * @param to
     * @return edge key
     */
    private String generateEdgeKey(StopNode from, StopNode to) {
        return from.getId().compareTo(to.getId()) < 0
                ? from.getId() + "--" + to.getId()
                : to.getId() + "--" + from.getId();
    }


    public static void main(String[] args) throws SQLException {
        StopGraph stopGraph = new StopGraph();
        Connection conn = DriverManager.getConnection("jdbc:sqlite:data/budapest_gtfs.db");
        stopGraph = stopGraph.buildStopGraph(conn);
        List<StopNode> stops = stopGraph.getStopNodes();
        System.out.println(stops.size());

    }
}
