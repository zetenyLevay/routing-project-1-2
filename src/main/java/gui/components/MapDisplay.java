package gui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTextField;

import gui.MapLine;
import gui.data.GeographicBounds;
import gui.data.LocationPoint;
import gui.interaction.CoordinateSelectionManager;
import gui.interaction.MapInteractionHandler;
import gui.rendering.HeatmapOverlayRenderer;
import gui.rendering.MapRenderer;
import gui.transform.MapViewTransform;
import gui.util.MapImageLoader;

/**
 * MapDisplay.java
 *
 * This class represents the main display area for the map in the GUI. It
 * handles rendering the base map, bus stops, and optional heatmap overlays. It
 * also manages user interactions such as zooming and selecting coordinates.
 */
public class MapDisplay extends JPanel {

    private final MapRenderer mapRenderer;
    private final MapInteractionHandler interactionHandler;
    private final MapViewTransform viewTransform;
    private final CoordinateSelectionManager selectionManager;
    private final BufferedImage baseMapImage;
    private final List<LocationPoint> busStopPoints;
    private Map<String, Color> heatmapStopColors = new HashMap<>();
    private boolean isHeatmapVisible = false;
    private List<MapLine> routeLines = new ArrayList<>();

    /**
     * MapDisplay constrcutor
     *
     * @param mapBounds Geographic bounds of the map.
     * @param busStops List of bus stop points to be displayed on the map.
     * @param startCoordinateField Text field for start coordinates input.
     * @param endCoordinateField Text field for end coordinates input.
     * @throws IOException If there is an error loading the base map image.
     */
    public MapDisplay(GeographicBounds mapBounds, List<LocationPoint> busStops,
            JTextField startCoordinateField, JTextField endCoordinateField) throws IOException {
        this.baseMapImage = MapImageLoader.load("/mapImage.jpg");
        this.busStopPoints = busStops;
        this.viewTransform = new MapViewTransform(baseMapImage, mapBounds);
        this.selectionManager = new CoordinateSelectionManager(startCoordinateField, endCoordinateField);
        this.mapRenderer = new MapRenderer(baseMapImage, busStopPoints, viewTransform);
        this.interactionHandler = new MapInteractionHandler(viewTransform, selectionManager, this);
        initializeComponent();
    }

    /**
     * Adjusts the zoom level of the map.
     *
     * @param zoomFactor The factor by which to adjust the zoom level.
     */
    public void adjustZoom(double zoomFactor) {
        viewTransform.adjustZoom(zoomFactor, getWidth() / 2.0, getHeight() / 2.0);
        refreshDisplay();
    }

    /**
     * Applies a travel time heatmap to the map display.
     *
     * @param stopColors A map of stop IDs to their corresponding colors for the
     * heatmap.
     */
    public void applyTravelTimeHeatmap(Map<String, Color> stopColors) {
        this.heatmapStopColors = new HashMap<>(stopColors);
        this.isHeatmapVisible = true;
        repaint();
    }

    /**
     * Clears the travel time heatmap from the map display.
     */
    public void clearTravelTimeHeatmap() {
        this.heatmapStopColors.clear();
        this.isHeatmapVisible = false;
        repaint();
    }

    /**
     * Returns the geographic bounds of the map.
     *
     * @return The geographic bounds of the map.
     */
    private void initializeComponent() {
        setPreferredSize(viewTransform.getPreferredSize());
        addMouseWheelListener(interactionHandler);
        addMouseListener(interactionHandler);
        addMouseMotionListener(interactionHandler);
        setFocusable(true);
        requestFocusInWindow();
    }

    /**
     * Returns the geographic bounds of the map.
     *
     * @return The geographic bounds of the map.
     */
    private void refreshDisplay() {
        revalidate();
        repaint();
    }

    /**
     * Returns the geographic bounds of the map.
     *
     * @return The geographic bounds of the map.
     */
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2d = (Graphics2D) graphics.create();

        try {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics;
            mapRenderer.render(g2);
            if (isHeatmapVisible && !heatmapStopColors.isEmpty()) {
                HeatmapOverlayRenderer overlay = new HeatmapOverlayRenderer(viewTransform, busStopPoints, heatmapStopColors);
                overlay.paint(g2, getWidth(), getHeight());
            }
            drawRouteLines(g2d);

            if (!routeLines.isEmpty()) {
                drawLegend(g2d, getWidth(), getHeight());
            }

        } finally {
            g2d.dispose();
        }
    }

    /**
     * Draws the route lines on the map display.
     *
     * @param lines List of MapLine objects representing the route lines to be
     * drawn.
     */
    public void drawRouteLines(List<MapLine> lines) {
        this.routeLines = new ArrayList<>(lines);

        // Debug: Print the lines
        for (int i = 0; i < lines.size(); i++) {
            MapLine line = lines.get(i);
        }
    }

    /**
     * Clears the route lines from the map display.
     */
    public void clearRouteLines() {
        this.routeLines.clear();
    }

    /**
     * Draws the route lines on the map display.
     *
     * @param g2d The Graphics2D object used for drawing.
     */
    private void drawRouteLines(Graphics2D g2d) {
        if (routeLines == null || routeLines.isEmpty()) {
            return;
        }

        // Set line properties
        g2d.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < routeLines.size(); i++) {
            MapLine line = routeLines.get(i);

            // Convert geo coordinates to screen coordinates
            Point startPoint = viewTransform.geoToScreen(line.getSourceLat(), line.getSourceLon());
            Point endPoint = viewTransform.geoToScreen(line.getDestLat(), line.getDestLon());

            if (startPoint != null && endPoint != null) {
                // Set color - make it more visible
                Color lineColor = line.getColour();
                if (lineColor == null) {
                    lineColor = Color.MAGENTA; // Fallback bright color
                }
                g2d.setColor(lineColor);

                // Draw the line
                g2d.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);

                // Draw small circles at endpoints to make them more visible
                g2d.fillOval(startPoint.x - 3, startPoint.y - 3, 6, 6);
                g2d.fillOval(endPoint.x - 3, endPoint.y - 3, 6, 6);

            } else {

            }
        }
    }

    private void drawLegend(Graphics2D g2d, int panelWidth, int panelHeight) {
        int width = 120;
        int height = 70;
        int x = panelWidth - 130;
        int y = 20;
        drawLegendBackground(g2d, x, y, width, height);
        drawLegendTitle(g2d, x, y);
        drawLegendItems(g2d, x, y);
    }

    private void drawLegendBackground(Graphics2D g2d, int x, int y, int width, int height) {
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fillRect(x - 5, y - 5, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x - 5, y - 5, width, height);
    }

    private void drawLegendTitle(Graphics2D g2d, int x, int y) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Route Legend", x, y + 15);
    }

    private void drawLegendItems(Graphics2D g2d, int x, int y) {
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        drawLegendItem(g2d, x, y + 30, Color.RED, "Walk");
        drawLegendItem(g2d, x, y + 50, Color.BLUE, "Ride");
    }

    private void drawLegendItem(Graphics2D g2d, int x, int y, Color color, String label) {
        g2d.setColor(color);
        g2d.fillOval(x, y, 12, 12);
        g2d.setColor(Color.BLACK);
        g2d.drawString(label, x + 20, y + 10);
    }

    /**
     * Returns the geographic bounds of the map.
     *
     * @return The geographic bounds of the map.
     */
    public List<LocationPoint> getBusStopPoints() {
        return busStopPoints;
    }

}
