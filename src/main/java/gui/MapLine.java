package gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import routing.routingEngineModels.RouteStep;

/**
 * MapLine.java
 *
 * Represents a line on a map between two geographical coordinates. Each line
 * has a source and destination latitude and longitude, and a color. The color
 * is determined based on the mode of transport for the route step.
 */
public class MapLine {

    Double sourceLat;
    Double sourceLon;
    Double destLat;
    Double destLon;
    Color colour;

    /**
     * MapLine contructor
     *
     * @param sourceLat Latitude of the source point.
     * @param sourceLon Longitude of the source point.
     * @param destLat Latitude of the destination point.
     * @param destLon Longitude of the destination point.
     * @param colour Color of the line, determined by the mode of transport.
     */
    public MapLine(Double sourceLat, Double sourceLon, Double destLat, Double destLon, Color colour) {
        this.sourceLat = sourceLat;
        this.sourceLon = sourceLon;
        this.destLat = destLat;
        this.destLon = destLon;
        this.colour = colour;

        // draw on map 
    }

    /**
     * Converts a list of RouteStep objects into a list of MapLine objects.
     * Each MapLine represents a segment of the route with its source and
     * destination coordinates and color based on the mode of transport.
     *
     * @param route List of RouteStep objects representing the route.
     * @param sourceLat Latitude of the starting point.
     * @param sourceLon Longitude of the starting point.
     * @return List of MapLine objects representing the route.
     */
    public static List<MapLine> routeToLine(List<RouteStep> route, double sourceLat, double sourceLon) {
        List<MapLine> lines = new ArrayList<>();

        double currentLat = sourceLat;
        double currentLon = sourceLon;

        for (RouteStep step : route) {
            double destLat = step.getToCoord().getLatitude();
            double destLon = step.getToCoord().getLongitude();

            // Debug: Print the mode of transport
            String mode = step.getModeOfTransport();

            // Set color based on mode - RED for walking, BLUE for everything else
            Color colour;
            if ("WALK".equalsIgnoreCase(mode) || "WALKING".equalsIgnoreCase(mode)) {
                colour = Color.RED;
            } else {
                colour = Color.BLUE;
            }

            lines.add(new MapLine(currentLat, currentLon, destLat, destLon, colour));

            currentLat = destLat;
            currentLon = destLon;
        }

        return lines;
    }


    /*
     * Getters for the MapLine properties
     */
    public Double getSourceLat() {
        return sourceLat;
    }

    public Double getSourceLon() {
        return sourceLon;
    }

    public Double getDestLat() {
        return destLat;
    }

    public Double getDestLon() {
        return destLon;
    }

    public Color getColour() {
        return colour;
    }

}
