package closureAnalysis.calculations;

import routing.routingEngineModels.Coordinates;

public class DistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371;
    private static final double EARTH_RADIUS_M = 6_371_000;

    /**
     *
     * @param startCoordinates
     * @param endCoordinates
     * @return distance in meters
     */
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

}
