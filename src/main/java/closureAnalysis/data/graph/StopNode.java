package closureAnalysis.data.graph;

import closureAnalysis.data.enums.TransportType;
import closureAnalysis.data.models.NearbyPOIs;
import routing.routingEngineModels.Coordinates;

import java.util.*;
import java.util.stream.Collectors;

/**
 * the stops in the graph
 */

public class StopNode {

    private String id;
    private Set<StopEdge> edges;

    private List<StopInstance> stopInstances;
    private double closenessCentrality;
    private double betweennessCentrality;

    private  String name;
    private  Coordinates coordinates;
    private TransportType transportType;
    private NearbyPOIs nearbyPOIs;
    private double stopWorth; // Higher the better
    public StopNode(String id) {
        this.id = id;
        edges = new HashSet<>();
        stopInstances = new ArrayList<>();

    }
    public String getId() {
        return id;
    }
    void addEdge( StopEdge e) {
        edges.add(e);
    }
    public void addStopInstance(StopInstance instance) {
        if (!stopInstances.contains(instance)) {
            stopInstances.add(instance);
        }
    }
    public List<StopInstance> getStopInstances() {
        return stopInstances;
    }
    public int getEdgeListSize(){
        return edges.size();
    }
    public Set<StopEdge> getAllEdges() {
        return edges;
    }
    public double getClosenessCentrality() {
        return closenessCentrality;
    }
    public double getBetweennessCentrality() {
        return betweennessCentrality;
    }
    public void setClosenessCentrality(double closenessCentrality) {
        this.closenessCentrality = closenessCentrality;
    }
    public void setBetweennessCentrality(double betweennessCentrality) {
        this.betweennessCentrality = betweennessCentrality;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Coordinates getCoordinates() {
        return coordinates;
    }
    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }
    public TransportType getTransportType() {
        return transportType;
    }
    public void setTransportType(TransportType transportType) {
        this.transportType = transportType;
    }
    public NearbyPOIs getNearbyPOIs() {
        return nearbyPOIs;
    }
    public void setNearbyPOIs(NearbyPOIs nearbyPOIs) {
        this.nearbyPOIs = nearbyPOIs;
    }
    public double getStopWorth() {
        return stopWorth;
    }
    public void setStopWorth(double stopWorth) {
        this.stopWorth = stopWorth;
    }
}