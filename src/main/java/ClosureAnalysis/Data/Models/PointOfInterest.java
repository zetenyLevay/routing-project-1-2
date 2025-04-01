package ClosureAnalysis.Data.Models;

import ClosureAnalysis.Data.Enums.POIType;

import java.util.List;

public class PointOfInterest {
    private String id;
    private POIType type;
    private List<Double> coordinates;

    public PointOfInterest(String id, POIType type, List<Double> coordinates) {
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

    public List<Double> getCoordinates() {
        return coordinates;
    }

    public double getLatitude(){
        return coordinates.getFirst();
    }

    public double getLongitude(){
        return coordinates.getLast();
    }
    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }
}
