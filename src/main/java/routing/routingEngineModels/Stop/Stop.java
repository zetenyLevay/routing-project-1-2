package routing.routingEngineModels.Stop;

import routing.routingEngineModels.Coordinates;

/**
 * Stop.java
 *
 * Represents a stop in the routing engine, which can be a bus stop, train station,
 * or any other type of transit stop. This class encapsulates the details of the stop,
 * including its ID, name, coordinates, type, and parent station ID.
 */
public class Stop {
    private final String stopID;
    private final String stopName;
    private final Coordinates stopCoordinates;
    private StopType stopType;
    private final String parentStationID;


    /**
     * Constructor for Stop.
     *
     * @param stopID The unique identifier for the stop.
     * @param stopName The name of the stop.
     * @param stopCoordinates The coordinates of the stop.
     * @param code The type code of the stop, used to determine its type.
     * @param parentStationID The ID of the parent station, if applicable.
     */
    public Stop(String stopID, String stopName, Coordinates stopCoordinates, int code, String parentStationID) {
        this.stopID = stopID;
        this.stopName = stopName;
        this.stopCoordinates = stopCoordinates;
        this.stopType = StopType.getNameFromCode(code);
        this.parentStationID = parentStationID;
    }

    /**
     * Constructor for Stop without a parent station ID.
     *
     * @param stopId2 The unique identifier for the stop.
     * @param stopName2 The name of the stop.
     * @param coordinates The coordinates of the stop.
     */
    public Stop(String stopId2, String stopName2, Coordinates coordinates) {
        this.stopID = stopId2;
        this.stopName = stopName2;
        this.stopCoordinates = coordinates;
        this.stopType = null;
        this.parentStationID = null;
    }

    //getters
    public double getLatitude() {
        return this.stopCoordinates.getLatitude();
    }

    public double getLongitude() {
        return this.stopCoordinates.getLongitude();
    }

    public String getStopName() {
        return this.stopName;
    }

    public String getStopID() {
        return this.stopID;
    }

    public StopType getStopType() {
        return stopType;
    }

    public String getParentStationID() {
        return parentStationID;
    }

    /**
     * Returns a string representation of the Stop object in a JSON-like format.
     * This is useful for debugging or logging purposes.
     *
     * @return A string representation of the Stop object.
     */
    public boolean isPlatform() {
        return stopType.isPlatform(parentStationID);
    }

}