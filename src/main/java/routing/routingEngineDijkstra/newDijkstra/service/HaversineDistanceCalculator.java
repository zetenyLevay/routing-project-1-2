package routing.routingEngineDijkstra.newDijkstra.service;

import routing.routingEngineDijkstra.newDijkstra.model.input.DijkstraStop;
import routing.routingEngineDijkstra.newDijkstra.model.input.Coordinates;

public class HaversineDistanceCalculator implements DistanceCalculator {
    private static final int EARTH_RADIUS_M = 6_371_000;

    @Override
    public int calculateDistanceMeters(DijkstraStop from, DijkstraStop to) {
        return calculateDistanceMeters(from.lat, from.lon, to.lat, to.lon);
    }

    @Override
    public int calculateDistanceMeters(Coordinates from, DijkstraStop to) {
        return calculateDistanceMeters(from.getLatitude(), from.getLongitude(), to.lat, to.lon);
    }

    @Override
    public int calculateDistanceMeters(DijkstraStop from, Coordinates to) {
        return calculateDistanceMeters(from.lat, from.lon, to.getLatitude(), to.getLongitude());
    }

    @Override
    public int calculateDistanceMeters(Coordinates from, Coordinates to) {
        return calculateDistanceMeters(from.getLatitude(), from.getLongitude(), to.getLatitude(), to.getLongitude());
    }

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
