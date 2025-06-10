package closureAnalysis.data.graph;

import java.util.Objects;

/**
 * Represents a specific instance of a stop being visited by a particular trip.
 * Contains timing and sequence information for the visit.
 */
public class StopInstance {
    private final int stopSequence;
    private final String arrivalTime;
    private final String departureTime;
    private final int distanceTraveled;
    private final String tripId;

    /**
     * Constructs a new StopInstance.
     * @param tripId The trip ID
     * @param stopSequence The sequence number in the trip
     * @param arrivalTime Arrival time at the stop
     * @param departureTime Departure time from the stop
     * @param distanceTraveled Distance traveled to reach this stop
     */
    public StopInstance(String tripId, int stopSequence, String arrivalTime, String departureTime, int distanceTraveled) {
        this.tripId = tripId;
        this.stopSequence = stopSequence;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.distanceTraveled = distanceTraveled;
    }

    public String getTripId() {
        return tripId;
    }
    public String getArrivalTime() { return arrivalTime; }
    public String getDepartureTime() { return departureTime; }
    public int getDistanceTraveled() { return distanceTraveled; }
    public int getStopSequence() { return stopSequence; }

    /**
     * Custom equals implementation comparing all fields.
     * @param o The object to compare
     * @return true if all fields are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StopInstance that)) return false;
        return distanceTraveled == that.distanceTraveled &&
                stopSequence == that.stopSequence &&
                Objects.equals(arrivalTime, that.arrivalTime) &&
                Objects.equals(departureTime, that.departureTime) &&
                Objects.equals(tripId, that.tripId);
    }

    /**
     * Generates a hash code based on all fields.
     * @return A hash code value for this instance
     */
    @Override
    public int hashCode() {
        return Objects.hash(tripId, stopSequence, arrivalTime, departureTime, distanceTraveled);
    }
}
