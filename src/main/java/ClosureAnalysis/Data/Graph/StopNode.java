package ClosureAnalysis.Data.Graph;

import java.util.*;
import java.util.stream.Collectors;

public class StopNode {

    private String label;
    private Map<String, Set<StopEdge>> edges;
    private String arrivalTime;
    private String departureTime;
    private int distanceTraveledAtStop;
    private double closenessCentrality;
    private double betweennessCentrality;

    public StopNode(String label) {
        this.label = label;
        edges = new HashMap<>();
    }

    public String getLabel() {
        return label;
    }
    void addEdge(String tripId, StopEdge e) {
        edges.computeIfAbsent(tripId, k -> new HashSet<>()).add(e);
    }


    void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    void setDistanceTraveledAtStop(int distanceTraveledAtStop) {
        this.distanceTraveledAtStop = distanceTraveledAtStop;
    }

    public List<StopEdge> getTripIDEdges(String tripId) {
        return new ArrayList<>(edges.getOrDefault(tripId, Collections.emptySet()));
    }

    public List<StopEdge> getAllEdges() {
        return edges.values().stream().flatMap(Set::stream).collect(Collectors.toList());
    }

    public String getArrivalTime() {
        return arrivalTime;
    }
    public String getDepartureTime() {
        return departureTime;
    }
    public int getDistanceTraveledAtStop() {
        return distanceTraveledAtStop;
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


}
