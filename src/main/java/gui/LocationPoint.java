package gui;

public class LocationPoint {    //class for geographic points
    private final double latitude;
    private final double longitude;

    public LocationPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
