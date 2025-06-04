package routing.routingEngineDijkstra.dijkstra.model.input;

import java.util.Objects;

public class DijkstraCoordinates {
    private final double latitude;
    private final double longitude;

    public DijkstraCoordinates(double latitude, double longitude) {
        validateCoordinates(latitude, longitude);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public DijkstraCoordinates(String coord) {
        String[] parts = coord.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid coordinate format. Expected 'lat,lon'");
        }
        double lat = Double.parseDouble(parts[0].trim());
        double lon = Double.parseDouble(parts[1].trim());
        validateCoordinates(lat, lon);
        this.latitude = lat;
        this.longitude = lon;
    }

    private void validateCoordinates(double latitude, double longitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees");
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees");
        }
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DijkstraCoordinates that = (DijkstraCoordinates) obj;
        return Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return String.format("Coordinates{latitude=%.6f, longitude=%.6f}", latitude, longitude);
    }
}
