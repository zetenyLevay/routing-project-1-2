package gui.data;

public class GeographicBounds {      //Define boundries
    private final double northLatitude;
    private final double southLatitude;
    private final double westLongitude;
    private final double eastLongitude;

    public GeographicBounds(double southLatitude, double northLatitude, 
                           double westLongitude, double eastLongitude) {
        this.southLatitude = southLatitude;
        this.northLatitude = northLatitude;
        this.westLongitude = westLongitude;
        this.eastLongitude = eastLongitude;
    }

    public double getNorthLatitude() { return northLatitude; }
    public double getSouthLatitude() { return southLatitude; }
    public double getWestLongitude() { return westLongitude; }
    public double getEastLongitude() { return eastLongitude; }
}