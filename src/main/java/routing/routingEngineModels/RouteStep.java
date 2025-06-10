package routing.routingEngineModels;

import java.util.HashMap;
import java.util.Map;

import routing.routingEngineModels.Stop.Stop;

/**
 * RouteStep.java
 *
 * Represents a step in a route, which can either be a ride or a walk. Each step
 * contains information about the mode of transport, destination coordinates,
 * stop information, duration, and timing details.
 */
public class RouteStep {

    private String modeOfTransport;
    private Coordinates toCoord;
    private Stop toStop;
    private double numOfMinutes;
    private String startTime;
    private String departureTime;
    private String arrivalTime;
    private String stopStr;
    private RouteInfo routeInfo;

    /**
     * Constructor for a route step that a step in the final route
     *
     * @param modeOfTransport the mode of transport (e.g., "bus", "train")
     * @param toStop the destination stop
     * @param numOfMinutes the duration of the step in minutes
     * @param departureTime the departure time in HH:mm:ss format
     * @param arrivalTime the arrival time in HH:mm:ss format
     * @param stopStr a string representation of the stop (e.g., "Stop Name
     * (Stop ID)")
     * @param routeInfo the route information associated with this step
     */
    public RouteStep(String modeOfTransport, Stop toStop, double numOfMinutes,
            String departureTime, String arrivalTime, String stopStr, RouteInfo routeInfo) {
        this.modeOfTransport = modeOfTransport;
        this.toStop = toStop;
        this.toCoord = new Coordinates(toStop.getLatitude(), toStop.getLongitude());
        this.numOfMinutes = numOfMinutes;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.startTime = departureTime;
        this.stopStr = stopStr;
        this.routeInfo = routeInfo;
    }

    /**
     * Constructor for a walking step in the route
     *
     * @param modeOfTransport the mode of transport (should be "walk")
     * @param toStop the destination stop
     * @param startTime the start time in HH:mm:ss format
     * @param walkingSeconds the duration of the walk in seconds
     */
    public RouteStep(String modeOfTransport, Stop toStop, String startTime, int walkingSeconds) {
        this.modeOfTransport = modeOfTransport;
        this.toStop = toStop;
        this.toCoord = new Coordinates(toStop.getLatitude(), toStop.getLongitude());
        this.startTime = startTime;
        this.departureTime = startTime;
        this.arrivalTime = addSecondsToTime(startTime, walkingSeconds);
        this.numOfMinutes = walkingSeconds / 60.0;
        this.stopStr = toStop.getStopName() + " (" + toStop.getStopID() + ")";
        this.routeInfo = null;
    }

    /**
     * Getters for the RouteStep properties
     */
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

    /**
     * Adds seconds to a time string in HH:mm:ss format.
     *
     * @param timeStr the time string in HH:mm:ss format
     * @param secondsToAdd the number of seconds to add
     * @return the new time string in HH:mm:ss format
     */
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

    /**
     * Returns the route information associated with this step.
     *
     * @return the RouteInfo object
     */
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

    /**
     * Converts the RouteStep to a JSON-like Map representation.
     *
     * @return a Map representing the RouteStep in JSON format
     */
    public Map<String, Object> toJSON() {
        Map<String, Object> json = new HashMap<>();

        if (modeOfTransport.equals("walk")) {
            json.put("mode", modeOfTransport);
        } else {
            json.put("mode", "ride");
        }

        json.put("to", toCoord.toJSON());

        // String twoDecimals = String.format("%.2f", numOfMinutes);
        json.put("duration", numOfMinutes);
        json.put("startTime", startTime.substring(0, 5));

        if (!modeOfTransport.equals("walk")) {
            json.put("stop", stopStr);
            json.put("route", routeInfo.toJSON());
        }
        return json;
    }

}
