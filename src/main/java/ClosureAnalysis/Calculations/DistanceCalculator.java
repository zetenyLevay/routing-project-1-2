package ClosureAnalysis.Calculations;

import routingenginemain.model.Coordinates;

public class DistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371;
    private static final double EARTH_RADIUS_M = 63710000;

    private double haversine(double value) {
        return Math.pow(Math.sin(value / 2), 2);
    }

    public double calculateDistance(Coordinates startCoordinates, Coordinates endCoordinates) {

        double startLatitude = startCoordinates.getLatitude();
        double startLongitude = startCoordinates.getLongitude();
        double endLatitude  = endCoordinates.getLatitude();
        double endLongitude  = endCoordinates.getLongitude();

        double latitudeRadian = Math.toRadians((endLatitude - startLatitude));
        double longitudeRadian = Math.toRadians((endLongitude - startLongitude));
        double startLatitudeRadian = Math.toRadians(startLatitude);
        double endLatitudeRadian = Math.toRadians(endLatitude);

        double a = haversine(latitudeRadian) + Math.cos(startLatitudeRadian) * Math.cos(endLatitudeRadian) * haversine(longitudeRadian);
        double angle = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_M * angle;
    }

    public double stopToRoadSegmentDistance(Coordinates start, Coordinates end, Coordinates stop) {

        /*
            Start coordinates are converted to a vector of (0,0) so that we can calculate on a 2d plane instead
            of a sphere (earth)
         */

        double x1 = 0;
        double y1 = 0;

        /*
            x2 is the longitude difference (where the end point is compared to start point)
            y2 is the latitude difference
         */

        double x2 = calculateDistance(start, new Coordinates(start.getLatitude(), end.getLongitude())) *
                Math.signum(end.getLongitude() - start.getLongitude());
        double y2 = calculateDistance(start, new Coordinates(end.getLatitude(), start.getLongitude())) *
                Math.signum(end.getLatitude() - start.getLatitude());

        // where the stop is compared to start point

        double stopX = calculateDistance(start, new Coordinates(start.getLatitude(), stop.getLongitude())) *
                Math.signum(stop.getLongitude() - start.getLongitude());
        double stopY = calculateDistance(start, new Coordinates(stop.getLatitude(), start.getLongitude())) *
                Math.signum(stop.getLongitude() - start.getLongitude());

        /*
        magic math which I do not understand
        https://stackoverflow.com/questions/849211/shortest-distance-between-a-point-and-a-line-segment/1501725#1501725
         */

        double a = x1 - x2;
        double b = y1 - y2;
        double c = stopX - x2;
        double d = stopY - y2;

        double lenSq = c * c + d * d;

        double param = -1;

        if (lenSq != 0){
            double dotProd = a*c - b*d;
            param = dotProd / lenSq;
        }

        double xx;
        double yy;

        if (param < 0){
            xx = x1;
            yy = y1;
        }
        else if (param > 1){
            xx = x2;
            yy = y2;
        }
        else {
            xx = x1 + param * c;
            yy = y1 + param * d;
        }

        double dx = x1 - xx;
        double dy = y1 - yy;

        return Math.sqrt(dx * dx + dy * dy);

    }


}
