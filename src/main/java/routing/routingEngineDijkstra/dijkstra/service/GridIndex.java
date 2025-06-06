package routing.routingEngineDijkstra.dijkstra.service;

import routing.routingEngineDijkstra.dijkstra.model.input.DijkstraStop;

import java.util.*;

public class GridIndex {
    private final Map<String, List<DijkstraStop>> gridCells = new HashMap<>();
    private final double cellSizeDegrees;
    private final int maxWalkingDistanceMeters;

    public GridIndex(Collection<DijkstraStop> stops, int maxWalkingDistanceMeters) {
        this.maxWalkingDistanceMeters = maxWalkingDistanceMeters;
        this.cellSizeDegrees = (maxWalkingDistanceMeters * 2) / 111320.0;

        for (DijkstraStop stop : stops) {
            String cellKey = getCellKey(stop.lat, stop.lon);
            gridCells.computeIfAbsent(cellKey, k -> new ArrayList<>()).add(stop);
        }
    }

    private String getCellKey(double lat, double lon) {
        int latCell = (int) (lat / cellSizeDegrees);
        int lonCell = (int) (lon / cellSizeDegrees);
        return latCell + "," + lonCell;
    }

    public List<DijkstraStop> getNearbyStops(DijkstraStop stop) {
        List<DijkstraStop> nearbyStops = new ArrayList<>();
        int searchRadius = 1;

        for (int latOffset = -searchRadius; latOffset <= searchRadius; latOffset++) {
            for (int lonOffset = -searchRadius; lonOffset <= searchRadius; lonOffset++) {
                String cellKey = getCellKey(
                        stop.lat + (latOffset * cellSizeDegrees),
                        stop.lon + (lonOffset * cellSizeDegrees)
                );
                if (gridCells.containsKey(cellKey)) {
                    nearbyStops.addAll(gridCells.get(cellKey));
                }
            }
        }
        return nearbyStops;
    }
}