package ClosureAnalysis.Data.Graph;

public class StopEdge {

    private StopNode to;
    private double weight;

    public StopEdge(StopNode to) {
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
}
