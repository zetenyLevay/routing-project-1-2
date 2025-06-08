package gui.rendering;

import gui.data.LocationPoint;
import gui.transform.MapViewTransform;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;

public class MapRenderer {
    private final BufferedImage baseMapImage;
    private final List<LocationPoint> busStopPoints;
    private final MapViewTransform viewTransform;

    public MapRenderer(BufferedImage baseMapImage, List<LocationPoint> busStopPoints, MapViewTransform viewTransform) {
        this.baseMapImage = baseMapImage;
        this.busStopPoints = busStopPoints;
        this.viewTransform = viewTransform;
    }

    public void render(Graphics2D graphics2D) {
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AffineTransform originalTransform = graphics2D.getTransform();
        graphics2D.translate(viewTransform.getHorizontalOffset(), viewTransform.getVerticalOffset());
        graphics2D.scale(viewTransform.getZoomLevel(), viewTransform.getZoomLevel());

        graphics2D.drawImage(baseMapImage, 0, 0, null);

        graphics2D.setColor(Color.GREEN);
        double dotDiameter = 12;
        double radius = dotDiameter / 2; // = 6
        for (LocationPoint busStop : busStopPoints) {
            Point2D pixelPosition = viewTransform.convertLocationToPixel(busStop);
            double dotX = pixelPosition.getX() - radius;
            double dotY = pixelPosition.getY() - radius;
            graphics2D.fill(new Ellipse2D.Double(dotX, dotY, dotDiameter, dotDiameter));
        }

        graphics2D.setTransform(originalTransform);
    }
}