package ClosureAnalysis.Data.Graph;

import java.util.Objects;

public class StopInstance {
    private final int stopSequence;
    private final String arrivalTime;
    private final String departureTime;
    private final int distanceTraveled;
    private final String tripId;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StopInstance)) return false;
        StopInstance that = (StopInstance) o;
        return distanceTraveled == that.distanceTraveled &&
                stopSequence == that.stopSequence &&
                Objects.equals(arrivalTime, that.arrivalTime) &&
                Objects.equals(departureTime, that.departureTime) &&
                Objects.equals(tripId, that.tripId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tripId, stopSequence, arrivalTime, departureTime, distanceTraveled);
    }
}
