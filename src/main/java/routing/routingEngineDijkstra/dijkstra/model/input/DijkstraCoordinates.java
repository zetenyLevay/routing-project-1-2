package routing.routingEngineDijkstra.dijkstra.model.input;

import java.util.Objects;

/**
 * Represents geographical coordinates with latitude and longitude, including validation and string parsing.
 */
public class DijkstraCoordinates {
    private final double latitude;
    private final double longitude;

    /**
     * Constructs a DijkstraCoordinates object with the specified latitude and longitude.
     *
     * @param latitude  the latitude in degrees
     * @param longitude the longitude in degrees
     * @throws IllegalArgumentException if latitude is not between -90 and 90 or longitude is not between -180 and 180
     */
    public DijkstraCoordinates(double latitude, double longitude) {
        validateCoordinates(latitude, longitude);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Constructs a DijkstraCoordinates object from a string in the format "lat,lon".
     *
     * @param coord the coordinate string in "lat,lon" format
     * @throws IllegalArgumentException if the format is invalid or coordinates are out of valid ranges
     */
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

    /**
     * Validates that the provided coordinates are within acceptable ranges.
     *
     * @param latitude  the latitude to validate
     * @param longitude the longitude to validate
     * @throws IllegalArgumentException if latitude or longitude is out of range
     */
    private void validateCoordinates(double latitude, double longitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees");
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees");
        }
    }

    /**
     * Retrieves the latitude of the coordinates.
     *
     * @return the latitude in degrees
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Retrieves the longitude of the coordinates.
     *
     * @return the longitude in degrees
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Checks if this coordinate is equal to another object.
     *
     * @param obj the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DijkstraCoordinates that = (DijkstraCoordinates) obj;
        return Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0;
    }

    /**
     * Generates a hash code for this coordinate.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    /**
     * Returns a string representation of the coordinates.
     *
     * @return a string in the format "Coordinates{latitude=x, longitude=y}"
     */
    @Override
    public String toString() {
        return String.format("Coordinates{latitude=%.6f, longitude=%.6f}", latitude, longitude);
    }
}