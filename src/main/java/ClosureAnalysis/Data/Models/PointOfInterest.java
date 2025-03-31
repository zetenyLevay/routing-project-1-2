package ClosureAnalysis.Data.Models;

import ClosureAnalysis.Data.Enums.POIType;

public class PointOfInterest {
    private String id;
    private POIType type;
    private double latitude;
    private double longitude;

    public PointOfInterest(String id, POIType type, double latitude,
                           double longitude) {
        this.id = id;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
