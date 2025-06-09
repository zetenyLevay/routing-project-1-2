package routing.routingEngineModels.Stop;

import routing.routingEngineModels.Coordinates;

public class Stop {
    private final String stopID;
    private final String stopName;
    private final Coordinates stopCoordinates;
//    private final int minimumTransferTime = 120;
    private StopType stopType;
    private final String parentStationID;
//    private final List<Pathway> footpaths;

    public Stop(String stopID, String stopName, Coordinates stopCoordinates, int code, String parentStationID) {
        this.stopID = stopID;
        this.stopName = stopName;
        this.stopCoordinates = stopCoordinates;
        this.stopType = StopType.getNameFromCode(code);
        this.parentStationID = parentStationID;
    }

    public Stop(String stopId2, String stopName2, Coordinates coordinates) {
        this.stopID = stopId2;
        this.stopName = stopName2;
        this.stopCoordinates = coordinates;
        this.stopType = null;
        this.parentStationID = null;
    }

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

    public boolean isPlatform() {
        return stopType.isPlatform(parentStationID);
    }

}