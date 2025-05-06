package closureAnalysis.data.graph;

import java.util.*;
import java.util.stream.Collectors;

public class StopNode {

    private String label;
    private Set<StopEdge> edges;

    private List<StopInstance> stopInstances;
    private double closenessCentrality;
    private double betweennessCentrality;

    public StopNode(String label) {
        this.label = label;
        edges = new HashSet<>();
        stopInstances = new ArrayList<>();

    }

    public String getLabel() {
        return label;
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

    public List<StopEdge> getAllEdges() {
        return new ArrayList<>(edges);
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


    public List<StopNode> getNeighbors() {
        return edges.stream()
                .flatMap(edge -> {
                    List<StopNode> nodes = new ArrayList<>();
                    if (!edge.getTo(this).equals(this)) { // avoid self-loops
                        nodes.add(edge.getTo(this));
                    }
                    return nodes.stream();
                })
                .distinct()
                .collect(Collectors.toList());
    }
}
