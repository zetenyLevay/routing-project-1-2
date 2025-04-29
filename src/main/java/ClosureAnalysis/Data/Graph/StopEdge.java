package ClosureAnalysis.Data.Graph;

import java.util.Objects;

public class StopEdge {

    private StopNode to;
    private StopNode from;
    private double weight;

    public StopEdge(StopNode from, StopNode to) {
        this.from = from;
        this.to = to;

    }

    public StopNode getTo() {
        return to;
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
        return Objects.equals(from.getLabel(), stopEdge.from.getLabel()) &&
                Objects.equals(to.getLabel(), stopEdge.to.getLabel());
    }

    @Override
    public int hashCode() {
        return Objects.hash(from.getLabel(), to.getLabel());
    }
}
