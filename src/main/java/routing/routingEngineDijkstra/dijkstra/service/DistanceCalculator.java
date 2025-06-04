package routing.routingEngineDijkstra.dijkstra.service;

import routing.routingEngineDijkstra.dijkstra.model.input.DijkstraStop;
import routing.routingEngineDijkstra.dijkstra.model.input.DijkstraCoordinates;

public interface DistanceCalculator {
    int calculateDistanceMeters(DijkstraStop from, DijkstraStop to);
    int calculateDistanceMeters(DijkstraCoordinates from, DijkstraStop to);
    int calculateDistanceMeters(DijkstraStop from, DijkstraCoordinates to);
    int calculateDistanceMeters(DijkstraCoordinates from, DijkstraCoordinates to);
    int calculateDistanceMeters(double lat1, double lon1, double lat2, double lon2);
}
