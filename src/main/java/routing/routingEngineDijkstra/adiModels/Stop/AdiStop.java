package routing.routingEngineDijkstra.adiModels.Stop;

import routing.routingEngineModels.Coordinates;
import routing.routingEngineModels.*;
import routing.routingEngineModels.Stop.StopType;

/**
 * Represents a stop in the routing system, containing information such as stop ID, name, coordinates, and parent station.
 */
public class AdiStop {
    private final String stopID;
    private final String stopName;
    private final Coordinates stopCoordinates;
    //    private final int minimumTransferTime = 120;
//    private final StopType stopType;
//    private final String parentStationID;
//    private final List<Pathway> footpaths;
    private final Object parentStationID;

    /**
     * Constructs an AdiStop with specified details including stop type and parent station ID.
     *
     * @param stopID           the unique identifier of the stop
     * @param stopName         the name of the stop
     * @param stopCoordinates  the geographical coordinates of the stop
     * @param stopType         the type of stop (e.g., platform, station)
     * @param parentStationID  the ID of the parent station, or null if none
     */
    public AdiStop(String stopID, String stopName, Coordinates stopCoordinates, StopType stopType, Object parentStationID) {
        this.stopID = stopID;
        this.stopName = stopName;
        this.stopCoordinates = stopCoordinates;
        this.parentStationID = parentStationID;
    }

    /**
     * Constructs an AdiStop with basic details, setting parent station ID to null.
     *
     * @param stopId2       the unique identifier of the stop
     * @param stopName2     the name of the stop
     * @param coordinates   the geographical coordinates of the stop
     */
    public AdiStop(String stopId2, String stopName2, Coordinates coordinates) {
        this.stopID = stopId2;
        this.stopName = stopName2;
        this.stopCoordinates = coordinates;
        this.parentStationID = null;
    }

    /**
     * Retrieves the latitude of the stop's coordinates.
     *
     * @return the latitude as a double
     */
    public double getLatitude() {
        return this.stopCoordinates.getLatitude();
    }

    /**
     * Retrieves the longitude of the stop's coordinates.
     *
     * @return the longitude as a double
     */
    public double getLongitude() {
        return this.stopCoordinates.getLongitude();
    }

    /**
     * Retrieves the name of the stop.
     *
     * @return the stop name as a String
     */
    public String getStopName() {
        return this.stopName;
    }

    /**
     * Retrieves the unique identifier of the stop.
     *
     * @return the stop ID as a String
     */
    public String getStopID() {
        return this.stopID;
    }

    /**
     * Retrieves the coordinates of the stop.
     *
     * @return the Coordinates object representing the stop's location
     */
    public Coordinates getCoordinates() {
        return this.stopCoordinates;
    }

//    /**
//     * Retrieves the type of the stop.
//     *
//     * @return the StopType of the stop
//     */
//    public StopType getStopType() {
//        return stopType;
//    }

//    /**
//     * Retrieves the ID of the parent station.
//     *
//     * @return the parent station ID as a String, or null if none
//     */
//    public String getParentStationID() {
//        return parentStationID;
//    }

//    /**
//     * Checks if the stop is a platform based on its type and parent station ID.
//     *
//     * @return true if the stop is a platform, false otherwise
//     */
//    public boolean isPlatform() {
//        return stopType.isPlatform(parentStationID);
//    }
}