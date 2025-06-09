package routing.routingEngineModels;

import java.util.Map;

/**
 * A simple value class representing a geographic coordinate (latitude and longitude).
 * Provides methods to retrieve the raw values as well as to serialize into a JSON-friendly Map.
 */
public class Coordinates {
    private final double latitude;
    private final double longitude;

    /**
     * Constructs a Coordinates instance from numeric latitude and longitude.
     *
     * @param latitude  the latitude value (e.g., 47.497519)
     * @param longitude the longitude value (e.g., 19.071495)
     */
    public Coordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Constructs a Coordinates instance by parsing a single String of the form "lat,lon".
     * Example: new Coordinates("47.497519,19.071495").
     *
     * @param coord a comma-separated string containing latitude and longitude
     * @throws IllegalArgumentException if the input string is not in the form "lat,lon"
     */
    public Coordinates(String coord) {
        String[] parts = coord.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid coordinate format. Expected 'lat,lon'");
        }
        try {
            this.latitude = Double.parseDouble(parts[0].trim());
            this.longitude = Double.parseDouble(parts[1].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric values in coordinate string: " + coord, e);
        }
    }

    /**
     * Returns the latitude component of this coordinate.
     *
     * @return the latitude (in decimal degrees)
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Returns the longitude component of this coordinate.
     *
     * @return the longitude (in decimal degrees)
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Converts this coordinate into a JSON-friendly Map. The resulting map will look like:
     *   { "lat": 47.497519, "lon": 19.071495 }
     *
     * @return a Map<String, Object> representing this coordinate, suitable for feeding into a JSONWriter
     */
    public Map<String, Object> toJSON() {
        return Map.of(
            "lat", latitude,
            "lon", longitude
        );
    }

    /**
     * Returns a string representation in the form of a raw JSON object. This is primarily for debugging
     * or logging. Do NOT use this method if you need a nested object in a JSONWriter, as it will be
     * emitted as a quoted string if passed directly to a JSON library.
     *
     * @return a JSON-like string: "{\"lat\":47.497519,\"lon\":19.071495}"
     */
    @Override
    public String toString() {
        return String.format("{\"lat\":%s,\"lon\":%s}", latitude, longitude);
    }
}
