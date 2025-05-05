package RoutingEngineModels;

public class Coordinate {
    private final double latitude;
    private final double longitude;

    public Coordinate(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    public Coordinate(String coord) {
        String[] parts = (coord).split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid coordinate format. Expected 'lat,lon'");
        }
        String lat = parts[0].trim();
        String lon = parts[1].trim();
        this.latitude = Double.parseDouble(lat);
        this.longitude = Double.parseDouble(lon);
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }



    public String toString() {
        return "Coordinate{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
