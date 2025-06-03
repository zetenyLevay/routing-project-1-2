package closureAnalysis.data.graph;

import java.util.Objects;

/**
 * Edges on the graph
 */
public class StopEdge {

    private StopNode from;
    private StopNode to;
    private double weight;
    public double distWeight;
    public double timeWeight;
    public StopEdge(StopNode to, StopNode from) {
        this.to = to;
        this.from = from;
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
    /**
     *
     * we check if in this edge the node we are checking from, is the later node, if it is we return the earlier node
     * else other way around
     * @param from
     * @return connected node
     */
    public StopNode getTo(StopNode from) {
        if (from.equals(this.to)) return this.from;
        if (from.equals(this.from)) return this.to;
        throw new IllegalArgumentException("from: " + from + ", to: " + this.from);
    }
    public double getWeight() {
        return weight;
    }
    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * custom equals function, i did this like 2 months ago no idea why
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StopEdge stopEdge = (StopEdge) o;

        return (Objects.equals(to.getId(), stopEdge.to.getId()) &&
                Objects.equals(from.getId(), stopEdge.from.getId())) ||
                (Objects.equals(to.getId(), stopEdge.from.getId()) &&
                        Objects.equals(from.getId(), stopEdge.to.getId()));
    }

    /**
     * we hash together both the from and to, since we dont want to duplicate the edge
     * if we only did one, then after hashing from and to, hashing to and from would be a different object, giving a new edge (thats bad)
     * @return
     */
    @Override
    public int hashCode() {
        return Objects.hash(from.getId(), to.getId()) +
                Objects.hash(to.getId(), from.getId());
    }
}
