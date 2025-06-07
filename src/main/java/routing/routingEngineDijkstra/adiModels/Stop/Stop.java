package routing.routingEngineDijkstra.adiModels.Stop;

import routing.routingEngineModels.Coordinates;

public class Stop {
    private final String stopID;
    private final String stopName;
    private final Coordinates stopCoordinates;
//    private final int minimumTransferTime = 120;
//    private final StopType stopType;
//    private final String parentStationID;
//    private final List<Pathway> footpaths;

    public Stop(String stopID, String stopName, Coordinates stopCoordinates) {
        this.stopID = stopID;
        this.stopName = stopName;
        this.stopCoordinates = stopCoordinates;
//        this.footpaths = footpaths;
//        this.stopType = StopType.getNameFromCode(code);
//        this.parentStationID = parentStationID;
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


//    public int getMinimumTransferTime() {
//        return minimumTransferTime;
//    }

//    public Station getParentStation() {
//        return parentStation;
//    }

//    public List<Pathway> getFootpaths() {
//        return footpaths;
//    }


}
