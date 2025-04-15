package ClosureAnalysis.Calculations;

import routingenginemain.model.Coordinates;

public class DistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371;
    private static final double EARTH_RADIUS_M = 6_371_000;


    public double calculateDistance(Coordinates startCoordinates, Coordinates endCoordinates) {

        double startLatitudeRadian = Math.toRadians(startCoordinates.getLatitude());
        double endLatitudeRadian = Math.toRadians(endCoordinates.getLatitude());
        double deltaLatitude = Math.toRadians(endCoordinates.getLatitude() - startCoordinates.getLatitude());
        double deltaLongitude = Math.toRadians(endCoordinates.getLongitude() - startCoordinates.getLongitude());

        double a =  Math.sin(deltaLatitude/2) * Math.sin(deltaLatitude/2) +
                Math.cos(startLatitudeRadian) * Math.cos(endLatitudeRadian) *
                        Math.sin(deltaLongitude/2) * Math.sin(deltaLongitude/2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return EARTH_RADIUS_M * c;

    }

    public double stopToRoadDistance(Coordinates startCoordinates, Coordinates endCoordinates, Coordinates stopCoordinates) {

        double roadDist = calculateDistance(startCoordinates, endCoordinates);
        double startToStopDist = calculateDistance(startCoordinates, stopCoordinates);
        double endToStopDist = calculateDistance(endCoordinates, stopCoordinates);


        // projection is where along the segment the stop is (if < 0 or > roadDist then it is outside the segment)
        double projection = (startToStopDist * startToStopDist + roadDist * roadDist - endToStopDist * endToStopDist)
                / (2* roadDist);

        if (projection <= 0 || projection >= roadDist)
            return Double.POSITIVE_INFINITY;
        else
            return Math.sqrt(startToStopDist * startToStopDist - projection * projection);
    }

    public static void main(String[] args) {
        DistanceCalculator calculator = new DistanceCalculator();
        Coordinates endCoordinates = new Coordinates(47.5013360673781, 19.0834875092641);
        Coordinates startCoordinates = new Coordinates(47.5009585313839, 19.0826709483695);
        Coordinates stopCoord = new Coordinates(47.501104,19.082712);

        System.out.println(calculator.stopToRoadDistance(startCoordinates, endCoordinates,stopCoord));
    }


    /*
    public double stopToRoadSegmentDistance(Coordinates start, Coordinates end, Coordinates stop) {

        /*
            Start coordinates are converted to a vector of (0,0) so that we can calculate on a 2d plane instead
            of a sphere (earth)


        double x1 = 0;
        double y1 = 0;

        /*
            x2 is the longitude difference (where the end point is compared to start point)
            y2 is the latitude difference


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
    */


}
