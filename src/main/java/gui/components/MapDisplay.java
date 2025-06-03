package gui.components;

import java.awt.*;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

import gui.MapUI;
import gui.data.GeographicBounds;
import gui.data.LocationPoint;
import gui.interaction.CoordinateSelectionManager;
import gui.interaction.MapInteractionHandler;
import gui.rendering.HeatmapGenerator;
import gui.rendering.MapRenderer;
import gui.transform.MapViewTransform;

public class MapDisplay extends JPanel {    //show the map on the screen
    private final MapRenderer mapRenderer;
    private final MapInteractionHandler interactionHandler;
    private final MapViewTransform viewTransform;
    private final CoordinateSelectionManager selectionManager;
    private final BufferedImage baseMapImage;
    private final BufferedImage heatmapOverlay;
    private final List<LocationPoint> busStopPoints;

    public MapDisplay(GeographicBounds bounds, List<LocationPoint> busStops, 
                      JTextField startField, JTextField endField) throws IOException {
        this.baseMapImage = loadMapImageFromResources();
        this.busStopPoints = busStops;
        
        HeatmapGenerator heatmapGenerator = new HeatmapGenerator(baseMapImage, bounds, busStops);
        this.heatmapOverlay = heatmapGenerator.generateHeatmap();
        
        this.viewTransform = new MapViewTransform(baseMapImage, bounds);
        this.selectionManager = new CoordinateSelectionManager(startField, endField);
        this.mapRenderer = new MapRenderer(baseMapImage, heatmapOverlay, busStopPoints, viewTransform);
        this.interactionHandler = new MapInteractionHandler(viewTransform, selectionManager, this);
        
        setupComponent();
    }

    public void toggleHeatmapVisibility(boolean isVisible) {
        mapRenderer.setHeatmapEnabled(isVisible);
        repaint();
    }

    public void adjustZoom(double zoomMultiplier) {
        viewTransform.adjustZoom(zoomMultiplier, getWidth() / 2.0, getHeight() / 2.0);
        revalidate();
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
    }
}