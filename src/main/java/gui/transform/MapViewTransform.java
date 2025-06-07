package gui.transform;

import gui.data.GeographicBounds;
import gui.data.LocationPoint;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class MapViewTransform {
    private final BufferedImage baseMapImage;
    private final GeographicBounds mapBounds;
    private double currentZoomLevel;
    private double horizontalOffset;
    private double verticalOffset;
    private final double minimumZoomLevel;
    private final double maximumZoomLevel = 10.0;

    public MapViewTransform(BufferedImage baseMapImage, GeographicBounds mapBounds) {
        this.baseMapImage = baseMapImage;
        this.mapBounds = mapBounds;
        initializeDefaultViewSettings();
        this.minimumZoomLevel = Math.max(0.3, this.currentZoomLevel * 0.5);
    }

    private void initializeDefaultViewSettings() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int availableScreenWidth = screenSize.width - 50;
        int availableScreenHeight = screenSize.height - 50;

        double widthScaleFactor = (double) availableScreenWidth / baseMapImage.getWidth();
        double heightScaleFactor = (double) availableScreenHeight / baseMapImage.getHeight();
        currentZoomLevel = Math.min(1.0, Math.min(widthScaleFactor, heightScaleFactor));

        horizontalOffset = 0;
        verticalOffset = 0;
    }

    public void adjustZoom(double zoomMultiplier, double centerX, double centerY) {
        double previousZoomLevel = currentZoomLevel;
        currentZoomLevel = calculateNewZoomLevel(currentZoomLevel * zoomMultiplier);
        adjustOffsetsForZoom(previousZoomLevel, centerX, centerY);
    }

    public void zoomAtPoint(double zoomMultiplier, double mouseX, double mouseY) {
        double previousZoomLevel = currentZoomLevel;
        double proposedZoomLevel = currentZoomLevel * zoomMultiplier;
        double maxAllowedZoom = Math.min(maximumZoomLevel, previousZoomLevel * 4);
        currentZoomLevel = Math.max(minimumZoomLevel, Math.min(maxAllowedZoom, proposedZoomLevel));
        adjustOffsetsForZoom(previousZoomLevel, mouseX, mouseY);
    }

    private double calculateNewZoomLevel(double proposedZoomLevel) {
        return Math.max(minimumZoomLevel, Math.min(proposedZoomLevel, maximumZoomLevel));
    }

    private void adjustOffsetsForZoom(double previousZoomLevel, double centerX, double centerY) {
        double zoomRatio = currentZoomLevel / previousZoomLevel;
        horizontalOffset = centerX - (centerX - horizontalOffset) * zoomRatio;
        verticalOffset = centerY - (centerY - verticalOffset) * zoomRatio;
    }

    public void pan(double deltaX, double deltaY) {
        horizontalOffset += deltaX;
        verticalOffset += deltaY;
    }

    public LocationPoint convertPixelToLocation(Point screenPixel) {
        try {
            double mapX = (screenPixel.x - horizontalOffset) / currentZoomLevel;
            double mapY = (screenPixel.y - verticalOffset) / currentZoomLevel;

            if (!isPixelWithinMapBounds(mapX, mapY)) {
                return null;
            }

            return convertMapPixelToGeographicLocation(mapX, mapY);
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isPixelWithinMapBounds(double mapX, double mapY) {
        return mapX >= 0 && mapX < baseMapImage.getWidth() && 
               mapY >= 0 && mapY < baseMapImage.getHeight();
    }

    private LocationPoint convertMapPixelToGeographicLocation(double mapX, double mapY) {
        double horizontalRatio = mapX / baseMapImage.getWidth();
        double verticalRatio = mapY / baseMapImage.getHeight();

        double longitude = calculateLongitudeFromRatio(horizontalRatio);
        double latitude = calculateLatitudeFromRatio(verticalRatio);

        return new LocationPoint(latitude, longitude);
    }

    private double calculateLongitudeFromRatio(double horizontalRatio) {
        return mapBounds.getWestLongitude() + horizontalRatio * 
               (mapBounds.getEastLongitude() - mapBounds.getWestLongitude());
    }

    private double calculateLatitudeFromRatio(double verticalRatio) {
        return mapBounds.getNorthLatitude() - verticalRatio * 
               (mapBounds.getNorthLatitude() - mapBounds.getSouthLatitude());
    }

    public Point2D convertLocationToPixel(LocationPoint location) {
        double horizontalRatio = calculateHorizontalRatio(location.getLongitude());
        double verticalRatio = calculateVerticalRatio(location.getLatitude());

        double pixelX = horizontalRatio * baseMapImage.getWidth();
        double pixelY = verticalRatio * baseMapImage.getHeight();
        return new Point2D.Double(pixelX, pixelY);
    }

    public Point geoToScreen(double latitude, double longitude) {
        double horizontalRatio = calculateHorizontalRatio(longitude);
        double verticalRatio = calculateVerticalRatio(latitude);

        double mapX = horizontalRatio * baseMapImage.getWidth();
        double mapY = verticalRatio * baseMapImage.getHeight();

        int screenX = (int)(mapX * currentZoomLevel + horizontalOffset);
        int screenY = (int)(mapY * currentZoomLevel + verticalOffset);

        return new Point(screenX, screenY);
    }

    private double calculateHorizontalRatio(double longitude) {
        return (longitude - mapBounds.getWestLongitude()) / 
               (mapBounds.getEastLongitude() - mapBounds.getWestLongitude());
    }

    private double calculateVerticalRatio(double latitude) {
        return (mapBounds.getNorthLatitude() - latitude) / 
               (mapBounds.getNorthLatitude() - mapBounds.getSouthLatitude());
    }

    public Dimension getPreferredSize() {
        int preferredWidth = (int)(baseMapImage.getWidth() * currentZoomLevel);
        int preferredHeight = (int)(baseMapImage.getHeight() * currentZoomLevel);
        return new Dimension(preferredWidth, preferredHeight);
    }

    public double getZoomLevel() { 
        return currentZoomLevel; 
    }
    
    public double getHorizontalOffset() { 
        return horizontalOffset; 
    }
    
    public double getVerticalOffset() { 
        return verticalOffset; 
    }
}