package routing.routingEngineModels;

public class Coordinates {
    private final double latitude;
    private final double longitude;

    public Coordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Coordinates(String coord) {
        // you can keep this around if you like, but it will no longer be used.
        String[] parts = coord.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid coordinate format. Expected 'lat,lon'");
        }
        this.latitude = Double.parseDouble(parts[0].trim());
        this.longitude = Double.parseDouble(parts[1].trim());
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    @Override
    public String toString() {
        // nobody else relies on toString(), so you can leave this as is or simplify.
        return "{"
             + "\"lat\":" + latitude
             + ",\"lon\":" + longitude
             + "}";
    }
}
