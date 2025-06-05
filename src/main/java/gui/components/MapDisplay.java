package gui.components;

import java.awt.*;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.*;

import gui.MapUI;
import gui.data.GeographicBounds;
import gui.data.LocationPoint;
import gui.interaction.CoordinateSelectionManager;
import gui.interaction.MapInteractionHandler;
import gui.rendering.MapRenderer;
import gui.transform.MapViewTransform;

public class MapDisplay extends JPanel {
    private final MapRenderer mapRenderer;
    private final MapInteractionHandler interactionHandler;
    private final MapViewTransform viewTransform;
    private final CoordinateSelectionManager selectionManager;
    private final BufferedImage baseMapImage;
    private final List<LocationPoint> busStopPoints;

    private Map<String, Color> travelTimeHeatmapColors = new HashMap<>();
    private boolean travelTimeHeatmapEnabled = false;

    public MapDisplay(GeographicBounds bounds, List<LocationPoint> busStops,
                      JTextField startField, JTextField endField) throws IOException {
        this.baseMapImage = loadMapImageFromResources();
        this.busStopPoints = busStops;
        this.viewTransform = new MapViewTransform(baseMapImage, bounds);
        this.selectionManager = new CoordinateSelectionManager(startField, endField);
        this.mapRenderer = new MapRenderer(baseMapImage, busStopPoints, viewTransform);
        this.interactionHandler = new MapInteractionHandler(viewTransform, selectionManager, this);
        setupComponent();
        setFocusable(true);
        requestFocusInWindow();
    }

    public void adjustZoom(double zoomMultiplier) {
        viewTransform.adjustZoom(zoomMultiplier, getWidth() / 2.0, getHeight() / 2.0);
        revalidate();
        repaint();
    }

    public void applyTravelTimeHeatmap(Map<String, Color> stopColors) {
        this.travelTimeHeatmapColors = new HashMap<>(stopColors);
        this.travelTimeHeatmapEnabled = true;
        repaint();
    }

    public void clearTravelTimeHeatmap() {
        this.travelTimeHeatmapColors.clear();
        this.travelTimeHeatmapEnabled = false;
        repaint();
    }

    private BufferedImage loadMapImageFromResources() throws IOException {
        try (InputStream imageStream = MapUI.class.getResourceAsStream("/mapImage.jpg")) {
            if (imageStream == null) {
                throw new FileNotFoundException("Map image file 'mapImage.jpg' not found in resources");
            }
            return ImageIO.read(imageStream);
        }
    }

    private void setupComponent() {
        setPreferredSize(viewTransform.getPreferredSize());
        addMouseWheelListener(interactionHandler);
        addMouseListener(interactionHandler);
        addMouseMotionListener(interactionHandler);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        mapRenderer.render((Graphics2D) graphics);
        if (travelTimeHeatmapEnabled && !travelTimeHeatmapColors.isEmpty()) {
            renderTravelTimeHeatmap((Graphics2D) graphics);
        }
    }

    private void renderTravelTimeHeatmap(Graphics2D g2d) {
        RenderingHints originalHints = g2d.getRenderingHints();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (LocationPoint busStop : busStopPoints) {
            String stopId = busStop.getStopId();
            Color heatmapColor = travelTimeHeatmapColors.get(stopId);
            if (heatmapColor != null) {
                Point screenPoint = viewTransform.geoToScreen(
                    busStop.getLatitude(),
                    busStop.getLongitude()
                );
                if (screenPoint != null && isPointVisible(screenPoint)) {
                    g2d.setColor(heatmapColor);
                    int radius = 8;
                    g2d.fillOval(screenPoint.x - radius, screenPoint.y - radius,
                                 radius * 2, radius * 2);
                    g2d.setColor(Color.BLACK);
                    g2d.setStroke(new BasicStroke(1));
                    g2d.drawOval(screenPoint.x - radius, screenPoint.y - radius,
                                 radius * 2, radius * 2);
                }
            }
        }
        g2d.setRenderingHints(originalHints);
        drawHeatmapLegend(g2d);
    }

    private boolean isPointVisible(Point point) {
        return point.x >= 0 && point.x < getWidth() &&
               point.y >= 0 && point.y < getHeight();
    }

    private void drawHeatmapLegend(Graphics2D g2d) {
        if (!travelTimeHeatmapEnabled || travelTimeHeatmapColors.isEmpty()) {
            return;
        }
        int legendX = getWidth() - 150;
        int legendY = 20;
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fillRect(legendX - 5, legendY - 5, 140, 80);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(legendX - 5, legendY - 5, 140, 80);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Travel Time", legendX, legendY + 15);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.setColor(Color.GREEN);
        g2d.fillOval(legendX, legendY + 25, 12, 12);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Short", legendX + 20, legendY + 35);
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(legendX, legendY + 45, 12, 12);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Medium", legendX + 20, legendY + 55);
        g2d.setColor(Color.RED);
        g2d.fillOval(legendX, legendY + 65, 12, 12);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Long", legendX + 20, legendY + 75);
    }

    public List<LocationPoint> getBusStopPoints() {
        return busStopPoints;
    }
}