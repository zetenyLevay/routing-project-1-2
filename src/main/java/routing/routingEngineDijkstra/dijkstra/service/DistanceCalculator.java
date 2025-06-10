package routing.routingEngineDijkstra.dijkstra.service;

import routing.routingEngineDijkstra.dijkstra.model.input.DijkstraStop;
import routing.routingEngineDijkstra.dijkstra.model.input.DijkstraCoordinates;

/**
 * Interface for calculating distances between stops and coordinates in meters.
 */
public interface DistanceCalculator {
    //we prepare for the worst and hope for the best

    /**
     * Calculates the distance between two stops.
     *
     * @param from the starting stop
     * @param to   the destination stop
     * @return the distance in meters
     */
    int calculateDistanceMeters(DijkstraStop from, DijkstraStop to);

    /**
     * Calculates the distance between a coordinate and a stop.
     *
     * @param from the starting coordinate
     * @param to   the destination stop
     * @return the distance in meters
     */
    int calculateDistanceMeters(DijkstraCoordinates from, DijkstraStop to);

    /**
     * Calculates the distance between a stop and a coordinate.
     *
     * @param from the starting stop
     * @param to   the destination coordinate
     * @return the distance in meters
     */
    int calculateDistanceMeters(DijkstraStop from, DijkstraCoordinates to);

    /**
     * Calculates the distance between two coordinates.
     *
     * @param from the starting coordinate
     * @param to   the destination coordinate
     * @return the distance in meters
     */
    int calculateDistanceMeters(DijkstraCoordinates from, DijkstraCoordinates to);

    /**
     * Calculates the distance between two points specified by latitude and longitude.
     *
     * @param lat1 the latitude of the first point
     * @param lon1 the longitude of the first point
     * @param lat2 the latitude of the second point
     * @param lon2 the longitude of the second point
     * @return the distance in meters
     */
    int calculateDistanceMeters(double lat1, double lon1, double lat2, double lon2);
}