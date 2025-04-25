package ClosureAnalysis.Data.Graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StopNode {

    private String label;
    private Set<StopEdge> edges;
    private String arrivalTime;
    private String departureTime;
    private int distanceTraveledAtStop;
    private double closenessCentrality;
    private double betweennessCentrality;

    public StopNode(String label) {
        this.label = label;
        edges = new HashSet<>();
    }

    String getLabel() {
        return label;
    }
    void addEdge(StopEdge e) {
        edges.add(e);
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

    public List<StopEdge> getEdges() {
        return new ArrayList<>(edges);
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
