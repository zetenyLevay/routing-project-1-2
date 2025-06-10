package closureAnalysis.data.graph;

import java.util.Objects;

/**
 * Represents a connection between two stop nodes in the transportation network.
 * Contains weight information for both distance and time factors.
 */
public class StopEdge {

    private final StopNode from;
    private final StopNode to;
    private double weight;
    /**
     * Constructs a new StopEdge between two nodes.
     * @param to The destination node
     * @param from The origin node
     */
    public StopEdge(StopNode to, StopNode from) {
        this.to = to;
        this.from = from;
    }
    /**
     * Gets the connected node based on the given origin.
     * @param from The node to check connection from
     * @return The connected node
     * @throws IllegalArgumentException if the given node is not part of this edge
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
     * Custom equals implementation that considers edges bidirectional.
     * @param o The object to compare
     * @return true if edges connect the same nodes (in any direction)
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
     * Custom hashCode implementation that ensures bidirectional equality.
     * @return A hash code value for this edge
     */
    @Override
    public int hashCode() {
        return Objects.hash(from.getId(), to.getId()) +
                Objects.hash(to.getId(), from.getId());
    }
}
