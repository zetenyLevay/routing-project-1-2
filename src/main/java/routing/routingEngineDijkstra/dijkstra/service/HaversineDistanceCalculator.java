package routing.routingEngineDijkstra.dijkstra.service;

import routing.routingEngineDijkstra.dijkstra.model.input.DijkstraStop;
import routing.routingEngineDijkstra.dijkstra.model.input.DijkstraCoordinates;

/**
 * Implements the DistanceCalculator interface using the Haversine formula to calculate distances between geographical points.
 */
public class HaversineDistanceCalculator implements DistanceCalculator {
    private static final int EARTH_RADIUS_M = 6_371_000;

    /**
     * Provides a fast approximation of the distance between two points for initial filtering.
     *
     * @param lat1 the latitude of the first point
     * @param lon1 the longitude of the first point
     * @param lat2 the latitude of the second point
     * @param lon2 the longitude of the second point
     * @return the approximate distance in meters
     */
    public int estimateQuickDistance(double lat1, double lon1, double lat2, double lon2) {
        double x = (lon2 - lon1) * Math.cos(Math.toRadians((lat1 + lat2) / 2));
        double y = (lat2 - lat1);
        return (int) (Math.sqrt(x*x + y*y) * 111319.491); // meters per degree
    }

    /**
     * Calculates the distance between two stops using the Haversine formula.
     *
     * @param from the starting stop
     * @param to   the destination stop
     * @return the distance in meters
     */
    @Override
    public int calculateDistanceMeters(DijkstraStop from, DijkstraStop to) {
        return calculateDistanceMeters(from.lat, from.lon, to.lat, to.lon);
    }

    /**
     * Calculates the distance between a coordinate and a stop using the Haversine formula.
     *
     * @param from the starting coordinate
     * @param to   the destination stop
     * @return the distance in meters
     */
    @Override
    public int calculateDistanceMeters(DijkstraCoordinates from, DijkstraStop to) {
        return calculateDistanceMeters(from.getLatitude(), from.getLongitude(), to.lat, to.lon);
    }

    /**
     * Calculates the distance between a stop and a coordinate using the Haversine formula.
     *
     * @param from the starting stop
     * @param to   the destination coordinate
     * @return the distance in meters
     */
    @Override
    public int calculateDistanceMeters(DijkstraStop from, DijkstraCoordinates to) {
        return calculateDistanceMeters(from.lat, from.lon, to.getLatitude(), to.getLongitude());
    }

    /**
     * Calculates the distance between two coordinates using the Haversine formula.
     *
     * @param from the starting coordinate
     * @param to   the destination coordinate
     * @return the distance in meters
     */
    @Override
    public int calculateDistanceMeters(DijkstraCoordinates from, DijkstraCoordinates to) {
        return calculateDistanceMeters(from.getLatitude(), from.getLongitude(), to.getLatitude(), to.getLongitude());
    }

    /**
     * Calculates the distance between two points specified by latitude and longitude using the Haversine formula.
     *
     * @param lat1 the latitude of the first point
     * @param lon1 the longitude of the first point
     * @param lat2 the latitude of the second point
     * @param lon2 the longitude of the second point
     * @return the distance in meters
     */
    @Override
    public int calculateDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (int) Math.round(EARTH_RADIUS_M * c);
    }
}