package gui.components;

import java.awt.*;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

    private Map<String, Color> heatmapStopColors = new HashMap<>();
    private boolean isHeatmapVisible = false;

    public MapDisplay(GeographicBounds mapBounds, List<LocationPoint> busStops,
                      JTextField startCoordinateField, JTextField endCoordinateField) throws IOException {
        this.baseMapImage = loadMapImageFromResources();
        this.busStopPoints = busStops;
        this.viewTransform = new MapViewTransform(baseMapImage, mapBounds);
        this.selectionManager = new CoordinateSelectionManager(startCoordinateField, endCoordinateField);
        this.mapRenderer = new MapRenderer(baseMapImage, busStopPoints, viewTransform);
        this.interactionHandler = new MapInteractionHandler(viewTransform, selectionManager, this);
        initializeComponent();
    }

    public void adjustZoom(double zoomFactor) {
        viewTransform.adjustZoom(zoomFactor, getWidth() / 2.0, getHeight() / 2.0);
        refreshDisplay();
    }

    public void applyTravelTimeHeatmap(Map<String, Color> stopColors) {
        this.heatmapStopColors = new HashMap<>(stopColors);
        this.isHeatmapVisible = true;
        repaint();
    }

    public void clearTravelTimeHeatmap() {
        this.heatmapStopColors.clear();
        this.isHeatmapVisible = false;
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

    private void initializeComponent() {
        setPreferredSize(viewTransform.getPreferredSize());
        addMouseWheelListener(interactionHandler);
        addMouseListener(interactionHandler);
        addMouseMotionListener(interactionHandler);
        setFocusable(true);
        requestFocusInWindow();
    }

    private void refreshDisplay() {
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D graphics2D = (Graphics2D) graphics;
        
        mapRenderer.render(graphics2D);
        
        if (isHeatmapVisible && !heatmapStopColors.isEmpty()) {
            renderHeatmapOverlay(graphics2D);
        }
    }

    private void renderHeatmapOverlay(Graphics2D graphics2D) {
        enableAntialiasing(graphics2D);
        drawHeatmapStops(graphics2D);
        drawHeatmapLegend(graphics2D);
    }

    private void enableAntialiasing(Graphics2D graphics2D) {
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    private void drawHeatmapStops(Graphics2D graphics2D) {
        for (LocationPoint busStop : busStopPoints) {
            String stopId = busStop.getStopId();
            Color stopColor = heatmapStopColors.get(stopId);
            
            if (stopColor != null) {
                Point screenPosition = viewTransform.geoToScreen(
                    busStop.getLatitude(),
                    busStop.getLongitude()
                );
                
                if (screenPosition != null && isPointOnScreen(screenPosition)) {
                    drawColoredStop(graphics2D, screenPosition, stopColor);
                }
            }
        }
    }

    private boolean isPointOnScreen(Point point) {
        return point.x >= 0 && point.x < getWidth() &&
               point.y >= 0 && point.y < getHeight();
    }

    private void drawColoredStop(Graphics2D graphics2D, Point screenPosition, Color stopColor) {
        int stopRadius = 8;
        int stopDiameter = stopRadius * 2;
        
        graphics2D.setColor(stopColor);
        graphics2D.fillOval(screenPosition.x - stopRadius, screenPosition.y - stopRadius,
                           stopDiameter, stopDiameter);
        
        graphics2D.setColor(Color.BLACK);
        graphics2D.setStroke(new BasicStroke(1));
        graphics2D.drawOval(screenPosition.x - stopRadius, screenPosition.y - stopRadius,
                           stopDiameter, stopDiameter);
    }

    private void drawHeatmapLegend(Graphics2D graphics2D) {
        if (!isHeatmapVisible || heatmapStopColors.isEmpty()) {
            return;
        }

        int legendWidth = 140;
        int legendHeight = 80;
        int legendX = getWidth() - 150;
        int legendY = 20;
        
        drawLegendBackground(graphics2D, legendX, legendY, legendWidth, legendHeight);
        drawLegendTitle(graphics2D, legendX, legendY);
        drawLegendItems(graphics2D, legendX, legendY);
    }

    private void drawLegendBackground(Graphics2D graphics2D, int x, int y, int width, int height) {
        graphics2D.setColor(new Color(255, 255, 255, 200));
        graphics2D.fillRect(x - 5, y - 5, width, height);
        graphics2D.setColor(Color.BLACK);
        graphics2D.drawRect(x - 5, y - 5, width, height);
    }

    private void drawLegendTitle(Graphics2D graphics2D, int x, int y) {
        graphics2D.setColor(Color.BLACK);
        graphics2D.setFont(new Font("Arial", Font.BOLD, 12));
        graphics2D.drawString("Travel Time", x, y + 15);
    }

    private void drawLegendItems(Graphics2D graphics2D, int x, int y) {
        graphics2D.setFont(new Font("Arial", Font.PLAIN, 10));
        
        drawLegendItem(graphics2D, x, y + 25, Color.GREEN, "Short");
        drawLegendItem(graphics2D, x, y + 45, Color.YELLOW, "Medium");
        drawLegendItem(graphics2D, x, y + 65, Color.RED, "Long");
    }

    private void drawLegendItem(Graphics2D graphics2D, int x, int y, Color color, String label) {
        graphics2D.setColor(color);
        graphics2D.fillOval(x, y, 12, 12);
        graphics2D.setColor(Color.BLACK);
        graphics2D.drawString(label, x + 20, y + 10);
    }

    public List<LocationPoint> getBusStopPoints() {
        return busStopPoints;
    }
}