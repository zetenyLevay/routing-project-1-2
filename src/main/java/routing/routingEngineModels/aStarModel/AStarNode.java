package routing.routingEngineModels.aStarModel;

import routing.routingEngineModels.Stop.Stop;
import routing.routingEngineModels.utils.TimeAndGeoUtils;

/**
 * A single state in the time‐expanded network:
 *   - stop: which stop we are at
 *   - timeSec: arrival time at that stop (seconds since midnight)
 */
public class AStarNode {
    private final Stop stop;
    private final int timeSec;

    public AStarNode(Stop stop, int timeSec) {
        this.stop = stop;
        this.timeSec = timeSec;
    }

    public Stop getStop() {
        return stop;
    }

    public int getTimeSec() {
        return timeSec;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AStarNode)) return false;
        AStarNode other = (AStarNode) o;
        return this.timeSec == other.timeSec
            && this.stop.getStopID().equals(other.stop.getStopID());
    }

    @Override
    public int hashCode() {
        // Combine stopID.hashCode() and timeSec
        int result = stop.getStopID().hashCode();
        result = 31 * result + timeSec;
        return result;
    }

    @Override
    public String toString() {
        return String.format("(%s @ %s)", 
            stop.getStopID(), 
            TimeAndGeoUtils.secondsToTimeString(timeSec));
    }
}
