package gui;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;


class HeatmapGenerator { 
    private final BufferedImage baseMapImage;
    private final GeographicBounds mapBounds;
    private final List<LocationPoint> dataPoints;

    public HeatmapGenerator(BufferedImage baseMapImage, GeographicBounds mapBounds, 
                           List<LocationPoint> dataPoints) {
        this.baseMapImage = baseMapImage;
        this.mapBounds = mapBounds;
        this.dataPoints = dataPoints;
    }

    public BufferedImage generateHeatmap() {
        int imageWidth = baseMapImage.getWidth();
        int imageHeight = baseMapImage.getHeight();
        int[][] intensityGrid = new int[imageWidth][imageHeight];
        double influenceRadius = 120.0;
        int radiusPixels = (int) Math.ceil(influenceRadius);

        for (LocationPoint point : dataPoints) {
            Point2D pixelPosition = convertLocationToPixel(point);
            int centerX = (int) Math.round(pixelPosition.getX());
            int centerY = (int) Math.round(pixelPosition.getY());

            for (int deltaY = -radiusPixels; deltaY <= radiusPixels; deltaY++) {
                for (int deltaX = -radiusPixels; deltaX <= radiusPixels; deltaX++) {
                    int pixelX = centerX + deltaX;
                    int pixelY = centerY + deltaY;

                    if (pixelX >= 0 && pixelX < imageWidth && pixelY >= 0 && pixelY < imageHeight) {
                        double distanceSquared = (deltaX + 0.5) * (deltaX + 0.5) + (deltaY + 0.5) * (deltaY + 0.5);
                        if (distanceSquared <= influenceRadius * influenceRadius) {
                            intensityGrid[pixelX][pixelY]++;
                        }
                    }
                }
            }
        }

        return createHeatmapImage(intensityGrid, imageWidth, imageHeight);
    }

    private BufferedImage createHeatmapImage(int[][] intensityGrid, int imageWidth, int imageHeight) {
        int maxIntensity = findMaxIntensity(intensityGrid, imageWidth, imageHeight);
        Color[] intensityColors = createColorPalette(maxIntensity);

        BufferedImage heatmapImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                int intensity = intensityGrid[x][y];
                if (intensity <= 1) continue;
                
                Color pixelColor = intensityColors[Math.min(intensity, maxIntensity)];
                int colorWithAlpha = (pixelColor.getRGB() & 0x00FFFFFF) | (180 << 24);
                heatmapImage.setRGB(x, y, colorWithAlpha);
            }
        }

        return heatmapImage;
    }

    private int findMaxIntensity(int[][] intensityGrid, int width, int height) {
        int maxValue = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (intensityGrid[x][y] > maxValue) {
                    maxValue = intensityGrid[x][y];
                }
            }
        }
        return maxValue;
    }

    private Color[] createColorPalette(int maxIntensity) {
        Color[] colors = new Color[maxIntensity + 1];
        for (int intensity = 1; intensity <= maxIntensity; intensity++) {
            float hue = 1.0f - Math.min(intensity, 10) / 10.0f;
            colors[intensity] = Color.getHSBColor(hue, 1.0f, 1.0f);
        }
        return colors;
    }

    private Point2D convertLocationToPixel(LocationPoint location) {
        double horizontalRatio = (location.getLongitude() - mapBounds.getWestLongitude()) / 
                               (mapBounds.getEastLongitude() - mapBounds.getWestLongitude());
        double verticalRatio = (mapBounds.getNorthLatitude() - location.getLatitude()) / 
                             (mapBounds.getNorthLatitude() - mapBounds.getSouthLatitude());
        
        double pixelX = horizontalRatio * baseMapImage.getWidth();
        double pixelY = verticalRatio * baseMapImage.getHeight();
        return new Point2D.Double(pixelX, pixelY);
    }
}