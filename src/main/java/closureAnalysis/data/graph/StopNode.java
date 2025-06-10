package closureAnalysis.data.graph;

import closureAnalysis.data.enums.TransportType;
import closureAnalysis.data.models.NearbyPOIs;
import routing.routingEngineModels.Coordinates;

import java.util.*;

/**
 * Represents a stop node in the transportation network graph.
 * Contains information about the stop, its connections, and various metrics
 * used for network analysis including centrality measures and worth calculations.
 */
public class StopNode {

    private final String id;
    private final Set<StopEdge> edges;

    private final List<StopInstance> stopInstances;
    private double closenessCentrality;
    private double betweennessCentrality;

    private  String name;
    private  Coordinates coordinates;
    private TransportType transportType;
    private NearbyPOIs nearbyPOIs;
    private double transportWorth;
    private double poiWorth;
    public double centralityWorth;
    private double stopWorth; // Higher the better
    /**
     * Constructs a StopNode with the given ID.
     * @param id The unique identifier for this stop
     */
    public StopNode(String id) {
        this.id = id;
        edges = new HashSet<>();
        stopInstances = new ArrayList<>();

    }
    // getter and setters
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
    public double getPoiWorth() {
        return poiWorth;
    }
    public void setPoiWorth(double poiWorth) {
        this.poiWorth = poiWorth;
    }
    public double getTransportWorth() {
        return transportWorth;
    }
    public void setTransportWorth(double transportWorth) {
        this.transportWorth = transportWorth;
    }
    public double getCentralityWorth() {
        return centralityWorth;
    }
    public void setCentralityWorth(double centralityWorth) {
        this.centralityWorth = centralityWorth;
    }
}