package routing.routingEngineModels.csamodel;

import routing.routingEngineModels.csamodel.pathway.Pathway;

import java.util.List;

public class Stop {
    private final String stopID;
    private final String stopName;
    private final Coordinates stopCoordinates;
    private final int minimumTransferTime = 120;
    private final List<Pathway> footpaths;

    public Stop(String stopID, String stopName, Coordinates stopCoordinates, List<Pathway> footpaths) {
        this.stopID = stopID;
        this.stopName = stopName;
        this.stopCoordinates = stopCoordinates;
        this.footpaths = footpaths;
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


    public int getMinimumTransferTime() {
        return minimumTransferTime;
    }

    public List<Pathway> getFootpaths() {
        return footpaths;
    }
}
