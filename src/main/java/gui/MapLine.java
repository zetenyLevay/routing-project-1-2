package gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import routing.routingEngineModels.RouteStep;

public class MapLine {

    Double sourceLat;
    Double sourceLon;
    Double destLat;
    Double destLon;
    Color colour;

    public MapLine(Double sourceLat, Double sourceLon, Double destLat, Double destLon, Color colour) {
        this.sourceLat = sourceLat;
        this.sourceLon = sourceLon;
        this.destLat = destLat;
        this.destLon = destLon;
        this.colour = colour;

        // draw on map 
    }

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
