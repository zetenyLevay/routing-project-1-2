package routing.routingEngineDijkstra.newDijkstra.service;

import routing.routingEngineDijkstra.newDijkstra.model.input.DijkstraStop;
import routing.routingEngineDijkstra.newDijkstra.model.input.Coordinates;

public interface DistanceCalculator {
    int calculateDistanceMeters(DijkstraStop from, DijkstraStop to);
    int calculateDistanceMeters(Coordinates from, DijkstraStop to);
    int calculateDistanceMeters(DijkstraStop from, Coordinates to);
    int calculateDistanceMeters(Coordinates from, Coordinates to);
    int calculateDistanceMeters(double lat1, double lon1, double lat2, double lon2);
}
