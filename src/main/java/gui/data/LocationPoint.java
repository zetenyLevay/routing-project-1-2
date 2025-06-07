package gui.data;

import java.util.Objects;

public class LocationPoint {
    private final double latitude;
    private final double longitude;
    private final String stopId;
    private final String stopName;

    public LocationPoint(double latitude, double longitude, String stopId) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.stopId = stopId;
        this.stopName = null;
    }
    
    public LocationPoint(double latitude, double longitude, String stopId, String stopName) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.stopId = stopId;
        this.stopName = stopName;
    }
    
    public LocationPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.stopId = "unknown_" + System.currentTimeMillis();
        this.stopName = null;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
    
    public String getStopId() {
        return stopId;
    }
    
    public String getStopName() {
        return stopName;
    }
    
    public boolean hasValidStopId() {
        return stopId != null && !stopId.startsWith("unknown_");
    }
    
    @Override
    public String toString() {
        return String.format("LocationPoint{lat=%.6f, lon=%.6f, stopId='%s', name='%s'}", 
                           latitude, longitude, stopId, stopName);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        LocationPoint that = (LocationPoint) obj;
        return Double.compare(that.latitude, latitude) == 0 &&
               Double.compare(that.longitude, longitude) == 0 &&
               Objects.equals(stopId, that.stopId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, stopId);
    }
}