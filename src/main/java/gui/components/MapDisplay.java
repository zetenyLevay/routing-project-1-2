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
import gui.rendering.HeatmapOverlayRenderer;
import gui.rendering.MapRenderer;
import gui.transform.MapViewTransform;
import gui.util.MapImageLoader;

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
        this.baseMapImage = MapImageLoader.load("/mapImage.jpg");
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
        Graphics2D g2 = (Graphics2D) graphics;
        mapRenderer.render(g2);
        if (isHeatmapVisible && !heatmapStopColors.isEmpty()) {
            HeatmapOverlayRenderer overlay = new HeatmapOverlayRenderer(viewTransform, busStopPoints, heatmapStopColors);
            overlay.paint(g2, getWidth(), getHeight());
        }
    }

    public List<LocationPoint> getBusStopPoints() {
        return busStopPoints;
    }
}