package gui;

import java.awt.*;
import java.awt.geom.Point2D; 
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

class MapDisplay extends JPanel {       //Main map rendering and interaction panel. Manages zooming, mouse interactions, coordinate selection and display the map
    private final BufferedImage baseMapImage;
    private final BufferedImage heatmapOverlay;
    private final List<LocationPoint> busStopPoints;
    private final GeographicBounds mapBounds;
    private final JTextField startCoordinateField;
    private final JTextField endCoordinateField;

    private double zoomLevel;
    private double horizontalOffset;
    private double verticalOffset;
    private final double minimumZoom;
    private Point lastMousePosition;
    private boolean isHeatmapEnabled = true;
    private boolean isSelectingStartPoint = true;
    private boolean isDraggingMap = false;

    public MapDisplay(GeographicBounds bounds, List<LocationPoint> busStops, 
                     JTextField startField, JTextField endField) throws IOException {
        this.mapBounds = bounds;
        this.busStopPoints = busStops;
        this.startCoordinateField = startField;
        this.endCoordinateField = endField;
        this.baseMapImage = loadMapImageFromResources();
        
        HeatmapGenerator heatmapGenerator = new HeatmapGenerator(baseMapImage, bounds, busStops);
        this.heatmapOverlay = heatmapGenerator.generateHeatmap();
        
        initializeViewSettings();
        this.minimumZoom = Math.max(0.3, this.zoomLevel * 0.5);
        setupMouseInteractions();
    }

    public void toggleHeatmapVisibility(boolean isVisible) {
        this.isHeatmapEnabled = isVisible;
        repaint();
    }

    public void adjustZoom(double zoomMultiplier) {
        double previousZoom = zoomLevel;
        zoomLevel *= zoomMultiplier;
        zoomLevel = Math.max(minimumZoom, Math.min(zoomLevel, 10));
        
        double panelCenterX = getWidth() / 2.0;
        double panelCenterY = getHeight() / 2.0;
        horizontalOffset = panelCenterX - (panelCenterX - horizontalOffset) * (zoomLevel / previousZoom);
        verticalOffset = panelCenterY - (panelCenterY - verticalOffset) * (zoomLevel / previousZoom);
        
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

    private void initializeViewSettings() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int availableWidth = screenSize.width - 50;
        int availableHeight = screenSize.height - 50;
        
        double widthScale = (double) availableWidth / baseMapImage.getWidth();
        double heightScale = (double) availableHeight / baseMapImage.getHeight();
        zoomLevel = Math.min(1.0, Math.min(widthScale, heightScale));
        
        int panelWidth = (int)(baseMapImage.getWidth() * zoomLevel);
        int panelHeight = (int)(baseMapImage.getHeight() * zoomLevel);
        setPreferredSize(new Dimension(panelWidth, panelHeight));
        
        horizontalOffset = 0;
        verticalOffset = 0;
    }

    private void setupMouseInteractions() {
        addMouseWheelListener(wheelEvent -> {
            double previousZoom = zoomLevel;
            if (wheelEvent.getWheelRotation() < 0) {
                zoomLevel *= 1.1;
            } else {
                zoomLevel *= 0.9;
            }
            zoomLevel = Math.max(minimumZoom, Math.min(previousZoom * 4, zoomLevel));
            
            double mouseX = wheelEvent.getX();
            double mouseY = wheelEvent.getY();
            horizontalOffset = mouseX - (mouseX - horizontalOffset) * (zoomLevel / previousZoom);
            verticalOffset = mouseY - (mouseY - verticalOffset) * (zoomLevel / previousZoom);
            
            revalidate();
            repaint();
        });

        MouseAdapter mapInteractionHandler = new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                lastMousePosition = mouseEvent.getPoint();
                isDraggingMap = false;
            }

            public void mouseDragged(MouseEvent mouseEvent) {
                Point currentMousePosition = mouseEvent.getPoint();
                horizontalOffset += currentMousePosition.x - lastMousePosition.x;
                verticalOffset += currentMousePosition.y - lastMousePosition.y;
                lastMousePosition = currentMousePosition;
                isDraggingMap = true;
                repaint();
            }

            public void mouseReleased(MouseEvent mouseEvent) {
                if (!isDraggingMap) {
                    handleMapClick(mouseEvent);
                }
            }
        };

        addMouseListener(mapInteractionHandler);
        addMouseMotionListener(mapInteractionHandler);
    }

    private void handleMapClick(MouseEvent mouseEvent) {
        LocationPoint clickedLocation = convertPixelToLocation(mouseEvent.getPoint());
        
        if (clickedLocation != null) {
            String coordinateString = String.format("%.6f,%.6f", 
                clickedLocation.getLatitude(), clickedLocation.getLongitude());

            if (isSelectingStartPoint) {
                startCoordinateField.setText(coordinateString);
                startCoordinateField.setBackground(Color.LIGHT_GRAY);
                endCoordinateField.setBackground(Color.WHITE);
            } else {
                endCoordinateField.setText(coordinateString);
                endCoordinateField.setBackground(Color.LIGHT_GRAY);
                startCoordinateField.setBackground(Color.WHITE);
            }

            isSelectingStartPoint = !isSelectingStartPoint;
            
            Timer highlightTimer = new Timer(300, e -> {
                startCoordinateField.setBackground(Color.WHITE);
                endCoordinateField.setBackground(Color.WHITE);
            });
            highlightTimer.setRepeats(false);
            highlightTimer.start();
        }
    }

    private LocationPoint convertPixelToLocation(Point screenPixel) {
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

    private Point2D convertLocationToPixel(LocationPoint location) {
        double horizontalRatio = (location.getLongitude() - mapBounds.getWestLongitude()) / 
                               (mapBounds.getEastLongitude() - mapBounds.getWestLongitude());
        double verticalRatio = (mapBounds.getNorthLatitude() - location.getLatitude()) / 
                             (mapBounds.getNorthLatitude() - mapBounds.getSouthLatitude());
        
        double pixelX = horizontalRatio * baseMapImage.getWidth();
        double pixelY = verticalRatio * baseMapImage.getHeight();
        return new Point2D.Double(pixelX, pixelY);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D graphics2D = (Graphics2D) graphics;
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        AffineTransform originalTransform = graphics2D.getTransform();
        graphics2D.translate(horizontalOffset, verticalOffset);
        graphics2D.scale(zoomLevel, zoomLevel);
        
        graphics2D.drawImage(baseMapImage, 0, 0, null);
        
        if (isHeatmapEnabled) {
            graphics2D.drawImage(heatmapOverlay, 0, 0, null);
        }
        
        graphics2D.setColor(Color.RED);
        for (LocationPoint busStop : busStopPoints) {
            Point2D pixelPosition = convertLocationToPixel(busStop);
            double dotX = pixelPosition.getX() - 2;
            double dotY = pixelPosition.getY() - 2;
            graphics2D.fill(new Ellipse2D.Double(dotX, dotY, 4, 4));
        }
        
        graphics2D.setTransform(originalTransform);
    }
}