package ClosureAnalysis.Data.Models;

import ClosureAnalysis.Data.Enums.POIType;

public class PointOfInterest {
    private String id;
    private POIType type;
    private Coordinate coordinates;

    public PointOfInterest(String id, POIType type, Coordinate coordinates) {
        this.id = id;
        this.type = type;
        this.coordinates = coordinates;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public POIType getType() {
        return type;
    }

    public void setType(POIType type) {
        this.type = type;
    }

    public Coordinate getCoordinates() {
        return coordinates;
    }
    public void setCoordinates(Coordinate coordinates) {
        this.coordinates = coordinates;
    }
}
