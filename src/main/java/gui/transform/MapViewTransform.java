package gui.transform;
import javax.swing.*;

import gui.data.GeographicBounds;
import gui.data.LocationPoint;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class MapViewTransform {      //Zoom and move the map
    private final BufferedImage baseMapImage;
    private final GeographicBounds mapBounds;
    private double zoomLevel;
    private double horizontalOffset;
    private double verticalOffset;
    private final double minimumZoom;

    public MapViewTransform(BufferedImage baseMapImage, GeographicBounds bounds) {
        this.baseMapImage = baseMapImage;
        this.mapBounds = bounds;
        initializeViewSettings();
        this.minimumZoom = Math.max(0.3, this.zoomLevel * 0.5);
    }

    private void initializeViewSettings() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int availableWidth = screenSize.width - 50;
        int availableHeight = screenSize.height - 50;

        double widthScale = (double) availableWidth / baseMapImage.getWidth();
        double heightScale = (double) availableHeight / baseMapImage.getHeight();
        zoomLevel = Math.min(1.0, Math.min(widthScale, heightScale));

        horizontalOffset = 0;
        verticalOffset = 0;
    }

    public void adjustZoom(double zoomMultiplier, double centerX, double centerY) {
        double previousZoom = zoomLevel;
        zoomLevel *= zoomMultiplier;
        zoomLevel = Math.max(minimumZoom, Math.min(zoomLevel, 10));

        horizontalOffset = centerX - (centerX - horizontalOffset) * (zoomLevel / previousZoom);
        verticalOffset = centerY - (centerY - verticalOffset) * (zoomLevel / previousZoom);
    }

    public void zoomAtPoint(double zoomMultiplier, double mouseX, double mouseY) {
        double previousZoom = zoomLevel;
        zoomLevel *= zoomMultiplier;
        zoomLevel = Math.max(minimumZoom, Math.min(previousZoom * 4, zoomLevel));

        horizontalOffset = mouseX - (mouseX - horizontalOffset) * (zoomLevel / previousZoom);
        verticalOffset = mouseY - (mouseY - verticalOffset) * (zoomLevel / previousZoom);
    }

    public void pan(double deltaX, double deltaY) {
        horizontalOffset += deltaX;
        verticalOffset += deltaY;
    }

    public LocationPoint convertPixelToLocation(Point screenPixel) {
        try {
            double mapX = (screenPixel.x - horizontalOffset) / zoomLevel;
            double mapY = (screenPixel.y - verticalOffset) / zoomLevel;

            if (mapX < 0 || mapX >= baseMapImage.getWidth() || 
                mapY < 0 || mapY >= baseMapImage.getHeight()) {
                return null;
            }

            double horizontalRatio = mapX / baseMapImage.getWidth();
            double verticalRatio = mapY / baseMapImage.getHeight();

            double longitude = mapBounds.getWestLongitude() + horizontalRatio * 
                             (mapBounds.getEastLongitude() - mapBounds.getWestLongitude());
            double latitude = mapBounds.getNorthLatitude() - verticalRatio * 
                            (mapBounds.getNorthLatitude() - mapBounds.getSouthLatitude());

            return new LocationPoint(latitude, longitude);
        } catch (Exception ignored) {
            return null;
        }
    }

    public Point2D convertLocationToPixel(LocationPoint location) {
        double horizontalRatio = (location.getLongitude() - mapBounds.getWestLongitude()) / 
                               (mapBounds.getEastLongitude() - mapBounds.getWestLongitude());
        double verticalRatio = (mapBounds.getNorthLatitude() - location.getLatitude()) / 
                             (mapBounds.getNorthLatitude() - mapBounds.getSouthLatitude());

        double pixelX = horizontalRatio * baseMapImage.getWidth();
        double pixelY = verticalRatio * baseMapImage.getHeight();
        return new Point2D.Double(pixelX, pixelY);
    }

    public Dimension getPreferredSize() {
        int panelWidth = (int)(baseMapImage.getWidth() * zoomLevel);
        int panelHeight = (int)(baseMapImage.getHeight() * zoomLevel);
        return new Dimension(panelWidth, panelHeight);
    }

    public double getZoomLevel() { return zoomLevel; }
    public double getHorizontalOffset() { return horizontalOffset; }
    public double getVerticalOffset() { return verticalOffset; }
}
