package routing.routingEngineModels;

import java.util.HashMap;
import java.util.Map;

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
    private RouteInfo routeInfo;

    // Constructor for transit
    public RouteStep(String modeOfTransport, Stop toStop, double numOfMinutes,
            String departureTime, String arrivalTime, String stopStr, RouteInfo routeInfo) {
        this.modeOfTransport = modeOfTransport;
        this.toStop = toStop;
        this.toCoord = new Coordinates(toStop.getLatitude(), toStop.getLongitude());
        this.numOfMinutes = numOfMinutes;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.startTime = departureTime; // Keep for backward compatibility
        this.stopStr = stopStr;
        this.routeInfo = routeInfo;
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
        this.routeInfo = null; // No route info for walking steps
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
        if (modeOfTransport.equals("walk")) {
            return String.format(
                    "{\"mode\":\"%s\",\"to\":\"%s\",\"duration\":\"%.2f\",\"startTime\":\"%s\"}",
                    modeOfTransport, toCoord, numOfMinutes, startTime.substring(0, 5)
            );
        } else {
            return String.format(
                    "{\"mode\":\"ride\",\"to\":\"%s\",\"duration\":\"%.2f\",\"startTime\":\"%s\",\"stop\":%s,\"route\":\"%s\"}",
                    toCoord, numOfMinutes, departureTime.substring(0, 5), stopStr, routeInfo.toString()
            );
        }
    }

    public Map<String, Object> toJSON() {
        Map<String, Object> json = new HashMap<>();

        if (modeOfTransport.equals("walk")) {
            json.put("mode", modeOfTransport);
        }
        else {
            json.put("mode", "ride");
        }
        
        json.put("to", toCoord.toJSON());

        String twoDecimals = String.format("%.2f", numOfMinutes);
        json.put("duration", twoDecimals);
        json.put("startTime", startTime.substring(0, 5));

        if (!modeOfTransport.equals("walk")) {
            json.put("stop", stopStr);
            json.put("route", routeInfo.toJSON());
        }
        return json;
    }

}
