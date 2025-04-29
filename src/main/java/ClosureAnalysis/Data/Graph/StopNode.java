package ClosureAnalysis.Data.Graph;

import java.util.*;
import java.util.stream.Collectors;

public class StopNode {

    private String label;
    private Set<StopEdge> edges;
    private Map<Integer, String> arrivalTime;
    private Map<Integer, String> departureTime;

    private Map<Integer, Integer> distanceTraveledAtStop;

    private List<Integer> stopSequence;
    private double closenessCentrality;
    private double betweennessCentrality;

    public StopNode(String label) {
        this.label = label;
        edges = new HashSet<>();
        stopSequence = new ArrayList<>();
        arrivalTime = new HashMap<>();
        departureTime = new HashMap<>();
        distanceTraveledAtStop = new HashMap<>();

    }

    public String getLabel() {
        return label;
    }
    void addEdge( StopEdge e) {
        edges.add(e);
    }


    void addArrivalTime(int stopSequence, String arrivalTime) {
        this.arrivalTime.put(stopSequence, arrivalTime);

    }

    void addStopSequence(int stopSequence) {
        this.stopSequence.add(stopSequence);
    }

    void addDepartureTime(int stopSequence, String departureTime) {
        this.departureTime.put(stopSequence, departureTime);
    }

    void addDistanceTraveledAtStop(int stopSequence, int distanceTraveledAtStop) {
        this.distanceTraveledAtStop.put(stopSequence, distanceTraveledAtStop);
    }



    public int getEdgeListSize(){
        return edges.size();
    }

    public List<StopEdge> getAllEdges() {
        return new ArrayList<>(edges);
    }

    public String getArrivalTime(int stopSequence) {
        return arrivalTime.get(stopSequence);
    }
    public String getDepartureTime(int stopSequence) {
        return departureTime.get(stopSequence);
    }
    public int getDistanceTraveledAtStop(int stopSequence) {
        return distanceTraveledAtStop.get(stopSequence);
    }

    public List<Integer> getStopSequence() {
        return stopSequence;
    }

    public double getClosenessCentrality() {
        return closenessCentrality;
    }
    public double getBetweennessCentrality() {
        return betweennessCentrality;
    }

    void setClosenessCentrality(double closenessCentrality) {
        this.closenessCentrality = closenessCentrality;
    }

    void setBetweennessCentrality(double betweennessCentrality) {
        this.betweennessCentrality = betweennessCentrality;
    }


    public List<StopNode> getNeighbors() {
        return edges.stream()
                .map(StopEdge::getTo)
                .collect(Collectors.toList());
    }
}
