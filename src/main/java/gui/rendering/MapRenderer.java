package gui.rendering;

import javax.swing.*;

import gui.data.LocationPoint;
import gui.transform.MapViewTransform;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;

public class MapRenderer {         //draws eveerything on the map
    private final BufferedImage baseMapImage;
    private final BufferedImage heatmapOverlay;
    private final List<LocationPoint> busStopPoints;
    private final MapViewTransform viewTransform;
    private boolean isHeatmapEnabled = false;

    public MapRenderer(BufferedImage baseMapImage, BufferedImage heatmapOverlay, 
                      List<LocationPoint> busStopPoints, MapViewTransform viewTransform) {
        this.baseMapImage = baseMapImage;
        this.heatmapOverlay = heatmapOverlay;
        this.busStopPoints = busStopPoints;
        this.viewTransform = viewTransform;
    }

    public void setHeatmapEnabled(boolean enabled) {
        this.isHeatmapEnabled = enabled;
    }

    public void render(Graphics2D graphics2D) {
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AffineTransform originalTransform = graphics2D.getTransform();
        graphics2D.translate(viewTransform.getHorizontalOffset(), viewTransform.getVerticalOffset());
        graphics2D.scale(viewTransform.getZoomLevel(), viewTransform.getZoomLevel());

        renderBaseMap(graphics2D);
        
        if (isHeatmapEnabled) {
            renderHeatmap(graphics2D);
        }
        
        renderBusStops(graphics2D);

        graphics2D.setTransform(originalTransform);
    }

    private void renderBaseMap(Graphics2D graphics2D) {
        graphics2D.drawImage(baseMapImage, 0, 0, null);
    }

    private void renderHeatmap(Graphics2D graphics2D) {
        graphics2D.drawImage(heatmapOverlay, 0, 0, null);
    }

    private void renderBusStops(Graphics2D graphics2D) {
        graphics2D.setColor(Color.RED);
        for (LocationPoint busStop : busStopPoints) {
            Point2D pixelPosition = viewTransform.convertLocationToPixel(busStop);
            double dotX = pixelPosition.getX() - 4;
            double dotY = pixelPosition.getY() - 4;
            graphics2D.fill(new Ellipse2D.Double(dotX, dotY, 8, 8));
        }
    }
}