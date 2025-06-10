package routing.routingEngineModels.aStarModel;

import routing.routingEngineModels.Stop.Stop;
import routing.routingEngineModels.utils.TimeAndGeoUtils;

/**
 * A single state in the time‐expanded network: - stop: which stop we are at -
 * timeSec: arrival time at that stop (seconds since midnight)
 */
public class AStarNode {

    private final Stop stop;
    private final int timeSec;

    /**
     * Constructor for AStarNode.
     *
     * @param stop the stop at which this node is located
     * @param timeSec the time in seconds since midnight when this node is
     * reached
     */
    public AStarNode(Stop stop, int timeSec) {
        this.stop = stop;
        this.timeSec = timeSec;
    }

    /**
     * Checks if this AStarNode is equal to another object.
     * Two AStarNodes are considered equal if they have the same stop ID and
     * @param o the object to compare with
     * @return true if the object is an AStarNode with the same stop ID and
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AStarNode)) {
            return false;
        }
        AStarNode other = (AStarNode) o;
        return this.timeSec == other.timeSec
                && this.stop.getStopID().equals(other.stop.getStopID());
    }

    /**
     * Computes the hash code for this AStarNode.
     * The hash code is computed based on the stop ID and time in seconds.
     *
     * @return the hash code of this AStarNode
     */
    @Override
    public int hashCode() {
        // Combine stopID.hashCode() and timeSec
        int result = stop.getStopID().hashCode();
        result = 31 * result + timeSec;
        return result;
    }

    /**
     * Returns a string representation of this AStarNode.
     * The format is "(stopID @ time)", where time is formatted as HH:MM:SS.
     *
     * @return a string representation of this AStarNode
     */
    @Override
    public String toString() {
        return String.format("(%s @ %s)",
                stop.getStopID(),
                TimeAndGeoUtils.secondsToTimeString(timeSec));
    }

    // Getters
    public Stop getStop() {
        return stop;
    }

    public int getTimeSec() {
        return timeSec;
    }
}
