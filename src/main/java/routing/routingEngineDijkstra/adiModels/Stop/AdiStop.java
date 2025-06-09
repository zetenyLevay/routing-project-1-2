package routing.routingEngineDijkstra.adiModels.Stop;

import routing.routingEngineModels.Coordinates;
import routing.routingEngineModels.*;
import routing.routingEngineModels.Stop.StopType;

public class AdiStop {
    private final String stopID;
    private final String stopName;
    private final Coordinates stopCoordinates;
//    private final int minimumTransferTime = 120;
//    private final StopType stopType;
//    private final String parentStationID;
//    private final List<Pathway> footpaths;
    private final Object parentStationID;

    public AdiStop(String stopID, String stopName, Coordinates stopCoordinates, StopType stopType, Object parentStationID) {
        this.stopID = stopID;
        this.stopName = stopName;
        this.stopCoordinates = stopCoordinates;
        this.parentStationID = parentStationID;
    }

        public AdiStop(String stopId2, String stopName2, Coordinates coordinates) {
        this.stopID = stopId2;
        this.stopName = stopName2;
        this.stopCoordinates = coordinates;
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

    public Coordinates getCoordinates() {return  this.stopCoordinates;}

//    public StopType getStopType() {
//        return stopType;
//    }

//    public String getParentStationID() {
//        return parentStationID;
//    }

//    public boolean isPlatform() {
//        return stopType.isPlatform(parentStationID);
//    }

}
