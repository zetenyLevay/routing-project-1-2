package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import javax.swing.JFrame;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class MapUI {

    public static void create() {
        List<LocationPoint> busStopLocations = loadBusStopLocationsFromFile("data/stops.csv");

        double mapTopLatitude = convertDegreesMinutesSeconds(47, 31, 35.08);
        double mapBottomLatitude = convertDegreesMinutesSeconds(47, 28, 5.16);
        double mapLeftLongitude = convertDegreesMinutesSeconds(18, 58, 50.07);
        double mapRightLongitude = convertDegreesMinutesSeconds(19, 7, 26.23);

        SwingUtilities.invokeLater(() -> {
            try {
                MapPanel mainMapPanel = new MapPanel(
                        mapBottomLatitude, mapTopLatitude,
                        mapLeftLongitude, mapRightLongitude,
                        busStopLocations
                );

                JTextField startAddressField = new JTextField("Start (lat,lon)", 20);
                JTextField endAddressField = new JTextField("End (lat,lon)", 20);

                JToggleButton showHeatmapButton = new JToggleButton("Show Heatmap", true);
                showHeatmapButton.addActionListener(buttonClick ->
                        mainMapPanel.setHeatmapVisible(showHeatmapButton.isSelected()));

                JButton zoomInButton = new JButton("+");
                JButton zoomOutButton = new JButton("-");

                zoomInButton.addActionListener(buttonClick -> mainMapPanel.changeZoom(1.5));
                zoomOutButton.addActionListener(buttonClick -> mainMapPanel.changeZoom(0.9));

                JPanel topControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                topControlPanel.add(new JLabel("Start:"));
                topControlPanel.add(startAddressField);
                topControlPanel.add(new JLabel("End:"));
                topControlPanel.add(endAddressField);
                topControlPanel.add(zoomInButton);
                topControlPanel.add(zoomOutButton);
                topControlPanel.add(showHeatmapButton);

                JPanel wholeApplicationPanel = new JPanel(new BorderLayout());
                wholeApplicationPanel.add(topControlPanel, BorderLayout.NORTH);
                wholeApplicationPanel.add(new JScrollPane(mainMapPanel), BorderLayout.CENTER);

                JFrame mainWindow = new JFrame("Offline Map Viewer");
                mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                mainWindow.setContentPane(wholeApplicationPanel);
                mainWindow.pack();
                mainWindow.setLocationRelativeTo(null);
                mainWindow.setVisible(true);
                
            } catch (IOException fileError) {
                fileError.printStackTrace();
            }
        });
    }

    private static List<LocationPoint> loadBusStopLocationsFromFile(String csvFileName) {
        List<LocationPoint> busStopLocations = new ArrayList<>();
        String csvSeparatorPattern = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

        try (BufferedReader fileReader = new BufferedReader(new FileReader(csvFileName))) {
            fileReader.readLine();
            
            String currentLine;
            while ((currentLine = fileReader.readLine()) != null) {
                String[] csvColumns = currentLine.split(csvSeparatorPattern, -1);
                
                if (csvColumns.length < 4) continue;
                
                try {
                    double busStopLatitude = Double.parseDouble(csvColumns[2]);
                    double busStopLongitude = Double.parseDouble(csvColumns[3]);
                    busStopLocations.add(new LocationPoint(busStopLatitude, busStopLongitude));
                } catch (NumberFormatException numberError) {
                }
            }
        } catch (IOException fileError) {
            System.err.println("Error reading CSV file: " + fileError.getMessage());
        }

        return busStopLocations;
    }

    private static double convertDegreesMinutesSeconds(int degrees, int minutes, double seconds) {
        return degrees + minutes / 60.0 + seconds / 3600.0;
    }

    public static class LocationPoint {
        private final double latitude;
        private final double longitude;

        public LocationPoint(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }

    public static class MapPanel extends JPanel {
        private final BufferedImage mapBackgroundImage;
        private final List<LocationPoint> busStopLocations;
        private final double mapMinimumLatitude;
        private final double mapMaximumLatitude;
        private final double mapMinimumLongitude;
        private final double mapMaximumLongitude;
        private double currentZoomLevel;
        private double mapHorizontalOffset;
        private double mapVerticalOffset;
        private Point mouseLastPosition;
        private BufferedImage heatmapImage;
        private boolean isHeatmapVisible = true;
        private final double minimumAllowedZoom;

        public MapPanel(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude, 
                       List<LocationPoint> busStops) throws IOException {
            this.mapMinimumLatitude = minLatitude;
            this.mapMaximumLatitude = maxLatitude;
            this.mapMinimumLongitude = minLongitude;
            this.mapMaximumLongitude = maxLongitude;
            this.busStopLocations = busStops;
            this.mapBackgroundImage = loadMapImageFromResources();
            this.heatmapImage = createHeatmapFromBusStops();
            setupInitialViewSettings();
            this.minimumAllowedZoom = Math.max(0.3, this.currentZoomLevel * 0.5);
            setupMouseControlsForMapNavigation();
        }

        public void setHeatmapVisible(boolean shouldShowHeatmap) {
            this.isHeatmapVisible = shouldShowHeatmap;
            repaint();
        }

        public void changeZoom(double zoomMultiplier) {
            double oldZoomLevel = currentZoomLevel;
            currentZoomLevel *= zoomMultiplier;
            currentZoomLevel = Math.max(minimumAllowedZoom, Math.min(currentZoomLevel, 10));
            double panelCenterX = getWidth() / 2.0;
            double panelCenterY = getHeight() / 2.0;
            mapHorizontalOffset = panelCenterX - (panelCenterX - mapHorizontalOffset) * (currentZoomLevel / oldZoomLevel);
            mapVerticalOffset = panelCenterY - (panelCenterY - mapVerticalOffset) * (currentZoomLevel / oldZoomLevel);
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

        private void setupInitialViewSettings() {
            Dimension screenDimensions = Toolkit.getDefaultToolkit().getScreenSize();
            int availableScreenWidth = screenDimensions.width - 50;
            int availableScreenHeight = screenDimensions.height - 50;
            double widthScaleFactor = (double) availableScreenWidth / mapBackgroundImage.getWidth();
            double heightScaleFactor = (double) availableScreenHeight / mapBackgroundImage.getHeight();
            currentZoomLevel = Math.min(1.0, Math.min(widthScaleFactor, heightScaleFactor));
            int panelWidth = (int)(mapBackgroundImage.getWidth() * currentZoomLevel);
            int panelHeight = (int)(mapBackgroundImage.getHeight() * currentZoomLevel);
            setPreferredSize(new Dimension(panelWidth, panelHeight));
            mapHorizontalOffset = 0;
            mapVerticalOffset = 0;
        }

        private void setupMouseControlsForMapNavigation() {
            addMouseWheelListener(wheelEvent -> {
                double oldZoomLevel = currentZoomLevel;
                if (wheelEvent.getWheelRotation() < 0) {
                    currentZoomLevel *= 1.1;
                } else {
                    currentZoomLevel *= 0.9;
                }
                currentZoomLevel = Math.max(minimumAllowedZoom, Math.min(oldZoomLevel * 4, currentZoomLevel));
                double mouseX = wheelEvent.getX();
                double mouseY = wheelEvent.getY();
                mapHorizontalOffset = mouseX - (mouseX - mapHorizontalOffset) * (currentZoomLevel / oldZoomLevel);
                mapVerticalOffset = mouseY - (mouseY - mapVerticalOffset) * (currentZoomLevel / oldZoomLevel);
                revalidate();
                repaint();
            });

            MouseAdapter panningMouseHandler = new MouseAdapter() {
                public void mousePressed(MouseEvent mouseEvent) {
                    mouseLastPosition = mouseEvent.getPoint();
                }

                public void mouseDragged(MouseEvent mouseEvent) {
                    Point currentMousePosition = mouseEvent.getPoint();
                    mapHorizontalOffset += currentMousePosition.x - mouseLastPosition.x;
                    mapVerticalOffset += currentMousePosition.y - mouseLastPosition.y;
                    mouseLastPosition = currentMousePosition;
                    repaint();
                }
            };

            addMouseListener(panningMouseHandler);
            addMouseMotionListener(panningMouseHandler);
        }

        private BufferedImage createHeatmapFromBusStops() {
            int imageWidth = mapBackgroundImage.getWidth();
            int imageHeight = mapBackgroundImage.getHeight();
            int[][] heatIntensityGrid = new int[imageWidth][imageHeight];
            double heatInfluenceRadius = 120.0;
            int radiusInPixels = (int) Math.ceil(heatInfluenceRadius);

            for (LocationPoint busStopLocation : busStopLocations) {
                Point2D pixelPosition = convertLocationToPixelPosition(busStopLocation);
                int centerPixelX = (int) Math.round(pixelPosition.getX());
                int centerPixelY = (int) Math.round(pixelPosition.getY());

                for (int deltaY = -radiusInPixels; deltaY <= radiusInPixels; deltaY++) {
                    for (int deltaX = -radiusInPixels; deltaX <= radiusInPixels; deltaX++) {
                        int pixelX = centerPixelX + deltaX;
                        int pixelY = centerPixelY + deltaY;

                        if (pixelX >= 0 && pixelX < imageWidth && pixelY >= 0 && pixelY < imageHeight) {
                            double distanceSquared = (deltaX + 0.5) * (deltaX + 0.5) + (deltaY + 0.5) * (deltaY + 0.5);
                            if (distanceSquared <= heatInfluenceRadius * heatInfluenceRadius) {
                                heatIntensityGrid[pixelX][pixelY]++;
                            }
                        }
                    }
                }
            }

            int maximumHeatValue = 0;
            for (int x = 0; x < imageWidth; x++) {
                for (int y = 0; y < imageHeight; y++) {
                    if (heatIntensityGrid[x][y] > maximumHeatValue) {
                        maximumHeatValue = heatIntensityGrid[x][y];
                    }
                }
            }

            Color[] heatLevelColors = new Color[maximumHeatValue + 1];
            for (int heatLevel = 1; heatLevel <= maximumHeatValue; heatLevel++) {
                float colorHue = 1.0f - Math.min(heatLevel, 10) / 10.0f;
                heatLevelColors[heatLevel] = Color.getHSBColor(colorHue, 1.0f, 1.0f);
            }

            BufferedImage heatmapResult = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < imageHeight; y++) {
                for (int x = 0; x < imageWidth; x++) {
                    int heatValue = heatIntensityGrid[x][y];
                    if (heatValue <= 1) continue;
                    Color pixelColor = heatLevelColors[Math.min(heatValue, maximumHeatValue)];
                    int colorWithTransparency = (pixelColor.getRGB() & 0x00FFFFFF) | (180 << 24);
                    heatmapResult.setRGB(x, y, colorWithTransparency);
                }
            }

            return heatmapResult;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D graphics2D = (Graphics2D) graphics;
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            AffineTransform originalTransform = graphics2D.getTransform();
            graphics2D.translate(mapHorizontalOffset, mapVerticalOffset);
            graphics2D.scale(currentZoomLevel, currentZoomLevel);
            graphics2D.drawImage(mapBackgroundImage, 0, 0, null);
            if (isHeatmapVisible) {
                graphics2D.drawImage(heatmapImage, 0, 0, null);
            }
            graphics2D.setColor(Color.RED);
            for (LocationPoint busStopLocation : busStopLocations) {
                Point2D pixelPosition = convertLocationToPixelPosition(busStopLocation);
                double dotX = pixelPosition.getX() - 2;
                double dotY = pixelPosition.getY() - 2;
                graphics2D.fill(new Ellipse2D.Double(dotX, dotY, 4, 4));
            }
            graphics2D.setTransform(originalTransform);
        }

        private Point2D convertLocationToPixelPosition(LocationPoint location) {
            double horizontalRatio = (location.getLongitude() - mapMinimumLongitude) / 
                                   (mapMaximumLongitude - mapMinimumLongitude);
            double verticalRatio = (mapMaximumLatitude - location.getLatitude()) / 
                                 (mapMaximumLatitude - mapMinimumLatitude);
            double pixelX = horizontalRatio * mapBackgroundImage.getWidth();
            double pixelY = verticalRatio * mapBackgroundImage.getHeight();
            return new Point2D.Double(pixelX, pixelY);
        }
    }
}
