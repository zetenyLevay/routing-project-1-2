package closureAnalysis.data.graph;

import java.util.Objects;

public class StopEdge {

    private StopNode node1;
    private StopNode node2;
    private double weight;

    public double distWeight;
    public double timeWeight;

    public StopEdge(StopNode node2, StopNode node1) {
        this.node2 = node2;
        this.node1 = node1;

    }

    public double getTimeWeight() {
        return timeWeight;
    }
    public void setTimeWeight(double timeWeight) {
        this.timeWeight = timeWeight;
    }

    public double getDistWeight() {
        return distWeight;
    }
    public void setDistWeight(double distWeight) {
        this.distWeight = distWeight;
    }

    public StopNode getTo(StopNode from) {
        if (from.equals(this.node2)) return this.node1;
        if (from.equals(this.node1)) return this.node2;
        throw new IllegalArgumentException("from: " + from + ", to: " + node1);
    }

    public boolean containsTo(StopNode node) {
        return this.node1.equals(node) || this.node2.equals(node);
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StopEdge stopEdge = (StopEdge) o;

        return (Objects.equals(node2.getLabel(), stopEdge.node2.getLabel()) &&
                Objects.equals(node1.getLabel(), stopEdge.node1.getLabel())) ||
                (Objects.equals(node2.getLabel(), stopEdge.node1.getLabel()) &&
                        Objects.equals(node1.getLabel(), stopEdge.node2.getLabel()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(node1.getLabel(), node2.getLabel()) +
                Objects.hash(node2.getLabel(), node1.getLabel());
    }
}
