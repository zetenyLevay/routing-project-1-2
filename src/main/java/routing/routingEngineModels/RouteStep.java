package routing.routingEngineModels;

import routing.routingEngineModels.Stop.Stop;

public class RouteStep {
    private String modeOfTransport;
    private Coordinates toCoord;
    private Stop toStop; // Add this field
    private double numOfMinutes;
    private String startTime;
    private String departureTime; // Add this field
    private String arrivalTime;   // Add this field
    private String stopStr;

    // Constructor for transit steps
    public RouteStep(String modeOfTransport, Stop toStop, double numOfMinutes, 
                     String departureTime, String arrivalTime, String stopStr) {
        this.modeOfTransport = modeOfTransport;
        this.toStop = toStop;
        this.toCoord = new Coordinates(toStop.getLatitude(), toStop.getLongitude());
        this.numOfMinutes = numOfMinutes;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.startTime = departureTime; // Keep for backward compatibility
        this.stopStr = stopStr;
    }

    // Constructor for walking steps
    public RouteStep(String modeOfTransport, Stop toStop, String startTime, int walkingSeconds) {
        this.modeOfTransport = modeOfTransport;
        this.toStop = toStop;
        this.toCoord = new Coordinates(toStop.getLatitude(), toStop.getLongitude());
        this.startTime = startTime;
        this.departureTime = startTime;
        this.arrivalTime = addSecondsToTime(startTime, walkingSeconds);
        this.numOfMinutes = walkingSeconds / 60.0;
        this.stopStr = toStop.getStopName() + " (" + toStop.getStopID() + ")";
    }

    // Getters
    public String getModeOfTransport() {
        return modeOfTransport;
    }

    public Coordinates getToCoord() {
        return toCoord;
    }

    public Stop getToStop() {
        return toStop;
    }

    public double getNumOfMinutes() {
        return numOfMinutes;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public String getStopStr() {
        return stopStr;
    }

    // Helper method to add seconds to time string
    private String addSecondsToTime(String timeStr, int secondsToAdd) {
        try {
            String[] parts = timeStr.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int seconds = Integer.parseInt(parts[2]);
            
            int totalSeconds = hours * 3600 + minutes * 60 + seconds + secondsToAdd;
            
            int newHours = totalSeconds / 3600;
            int newMinutes = (totalSeconds % 3600) / 60;
            int newSecs = totalSeconds % 60;
            
            return String.format("%02d:%02d:%02d", newHours, newMinutes, newSecs);
            
        } catch (Exception e) {
            return timeStr;
        }
    }

    @Override
    public String toString() {
        return String.format("%s to %s (dep: %s, arr: %s)", 
                           modeOfTransport, stopStr, departureTime, arrivalTime);
    }
}