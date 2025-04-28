package ClosureAnalysis.Data.Models;

import ClosureAnalysis.Data.Enums.POIType;
import Routing.routingenginemain.model.Coordinates;

public class PointOfInterest {
    private String id;
    private POIType type;
    private Coordinates coordinates;

    public PointOfInterest(String id, POIType type, Coordinates coordinates) {
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

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public double getLatitude(){
        return coordinates.getLatitude();
    }

    public double getLongitude(){
        return coordinates.getLongitude();
    }
    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }
}
