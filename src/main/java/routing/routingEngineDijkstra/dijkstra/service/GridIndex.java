package routing.routingEngineDijkstra.dijkstra.service;

import routing.routingEngineDijkstra.dijkstra.model.input.DijkstraStop;

import java.util.*;

/**
 * Implements a spatial index using a grid to efficiently find nearby stops for walking transfers.
 */
public class GridIndex {
    private final Map<String, DijkstraStop[]> gridCells = new HashMap<>();
    private final double cellSizeDegrees;
    private final int maxWalkingDistanceMeters;
    private final Map<String, String> stopToCellMap = new HashMap<>();

    /**
     * Constructs a GridIndex for the given stops and maximum walking distance.
     *
     * @param stops                  the collection of stops to index
     * @param maxWalkingDistanceMeters the maximum walking distance in meters
     */
    public GridIndex(Collection<DijkstraStop> stops, int maxWalkingDistanceMeters) {
        this.maxWalkingDistanceMeters = maxWalkingDistanceMeters;
        this.cellSizeDegrees = (maxWalkingDistanceMeters * 2) / 111320.0;

        Map<String, List<DijkstraStop>> tempCells = new HashMap<>();
        for (DijkstraStop stop : stops) {
            String cellKey = getCellKey(stop.lat, stop.lon);
            tempCells.computeIfAbsent(cellKey, k -> new ArrayList<>()).add(stop);
            stopToCellMap.put(stop.id, cellKey);
        }

        tempCells.forEach((key, list) ->
                gridCells.put(key, list.toArray(new DijkstraStop[0])));
    }

    /**
     * Generates a cell key for a given latitude and longitude.
     *
     * @param lat the latitude
     * @param lon the longitude
     * @return a string key representing the grid cell
     */
    private String getCellKey(double lat, double lon) {
        int latCell = (int) (lat / cellSizeDegrees);
        int lonCell = (int) (lon / cellSizeDegrees);
        return latCell + "," + lonCell;
    }

    /**
     * Checks if two stops are in nearby grid cells.
     *
     * @param stop1 the first stop
     * @param stop2 the second stop
     * @return true if the stops are in the same or adjacent cells, false otherwise
     */
    public boolean areStopsNearby(DijkstraStop stop1, DijkstraStop stop2) {
        String cell1 = stopToCellMap.get(stop1.id);
        String cell2 = stopToCellMap.get(stop2.id);

        if (cell1.equals(cell2)) return true;

        String[] parts1 = cell1.split(",");
        String[] parts2 = cell2.split(",");
        int lat1 = Integer.parseInt(parts1[0]);
        int lon1 = Integer.parseInt(parts1[1]);
        int lat2 = Integer.parseInt(parts2[0]);
        int lon2 = Integer.parseInt(parts2[1]);

        return Math.abs(lat1 - lat2) <= 1 && Math.abs(lon1 - lon2) <= 1;
    }

    /**
     * Retrieves all stops in the same or adjacent grid cells as the given stop.
     *
     * @param stop the reference stop
     * @return a list of nearby DijkstraStop objects
     */
    public List<DijkstraStop> getNearbyStops(DijkstraStop stop) {
        List<DijkstraStop> nearbyStops = new ArrayList<>();
        String baseCellKey = stopToCellMap.get(stop.id);

        if (baseCellKey != null) {
            String[] parts = baseCellKey.split(",");
            int baseLat = Integer.parseInt(parts[0]);
            int baseLon = Integer.parseInt(parts[1]);

            for (int latOffset = -1; latOffset <= 1; latOffset++) {
                for (int lonOffset = -1; lonOffset <= 1; lonOffset++) {
                    String cellKey = (baseLat + latOffset) + "," + (baseLon + lonOffset);
                    DijkstraStop[] cellStops = gridCells.get(cellKey);
                    if (cellStops != null) {
                        Collections.addAll(nearbyStops, cellStops);
                    }
                }
            }
        }
        return nearbyStops;
    }
}