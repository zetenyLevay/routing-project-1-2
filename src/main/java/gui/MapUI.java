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
        List<GeoPosition> stopCoordinates = loadStopCoordinatesFromCSV("data/stops.csv");

        double northernLatitude = convertDegreesMinutesSeconds(47, 31, 35.08);
        double southernLatitude = convertDegreesMinutesSeconds(47, 28, 5.16);
        double westernLongitude = convertDegreesMinutesSeconds(18, 58, 50.07);
        double easternLongitude = convertDegreesMinutesSeconds(19, 7, 26.23);

        SwingUtilities.invokeLater(() -> {
            try {
                OfflineMapPanel mapPanel = new OfflineMapPanel(
                        southernLatitude, northernLatitude,
                        westernLongitude, easternLongitude,
                        stopCoordinates
                );

                JTextField startCoordinatesField = new JTextField("Start (lat,lon)", 20);
                JTextField endCoordinatesField = new JTextField("End (lat,lon)", 20);

                JToggleButton heatmapToggleButton = new JToggleButton("Show Heatmap", true);
                heatmapToggleButton.addActionListener(event ->
                        mapPanel.setShowHeatmap(heatmapToggleButton.isSelected()));

                JPanel inputPanel = new JPanel(new BorderLayout());
                inputPanel.add(startCoordinatesField, BorderLayout.WEST);
                inputPanel.add(endCoordinatesField, BorderLayout.CENTER);
                inputPanel.add(heatmapToggleButton, BorderLayout.EAST);

                JPanel mainPanel = new JPanel(new BorderLayout());
                mainPanel.add(inputPanel, BorderLayout.NORTH);
                mainPanel.add(new JScrollPane(mapPanel), BorderLayout.CENTER);

                JFrame mapFrame = new JFrame("Offline Map Viewer");
                mapFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                mapFrame.setContentPane(mainPanel);
                mapFrame.pack();
                mapFrame.setLocationRelativeTo(null);
                mapFrame.setVisible(true);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });
    }

    private static List<GeoPosition> loadStopCoordinatesFromCSV(String csvFilePath) {
        List<GeoPosition> stopCoordinates = new ArrayList<>();
        String csvDelimiterPattern = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

        try (BufferedReader fileReader = new BufferedReader(new FileReader(csvFilePath))) {
            fileReader.readLine();
            String line;
            while ((line = fileReader.readLine()) != null) {
                String[] columns = line.split(csvDelimiterPattern, -1);
                if (columns.length < 4) continue;
                try {
                    double latitude = Double.parseDouble(columns[2]);
                    double longitude = Double.parseDouble(columns[3]);
                    stopCoordinates.add(new GeoPosition(latitude, longitude));
                } catch (NumberFormatException ignored) {}
            }
        } catch (IOException exception) {
            System.err.println(exception.getMessage());
        }

        return stopCoordinates;
    }

    private static double convertDegreesMinutesSeconds(int degrees, int minutes, double seconds) {
        return degrees + minutes / 60.0 + seconds / 3600.0;
    }

    public static class GeoPosition {
        private final double latitude;
        private final double longitude;

        public GeoPosition(double latitude, double longitude) {
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

    public static class OfflineMapPanel extends JPanel {
        private final BufferedImage baseMapImage;
        private final List<GeoPosition> stopCoordinates;
        private final double minimumLatitude;
        private final double maximumLatitude;
        private final double minimumLongitude;
        private final double maximumLongitude;
        private double zoomFactor;
        private double offsetX;
        private double offsetY;
        private Point dragStartPoint;
        private BufferedImage heatmapOverlayImage;
        private boolean showHeatmap = true;

        public OfflineMapPanel(double minLat, double maxLat, double minLon, double maxLon, List<GeoPosition> stops) throws IOException {
            this.minimumLatitude = minLat;
            this.maximumLatitude = maxLat;
            this.minimumLongitude = minLon;
            this.maximumLongitude = maxLon;
            this.stopCoordinates = stops;
            this.baseMapImage = loadMapImage();
            this.heatmapOverlayImage = generateHeatmap();
            setupInitialView();
            setupMouseInteractions();
        }

        public void setShowHeatmap(boolean shouldDisplayHeatmap) {
            this.showHeatmap = shouldDisplayHeatmap;
            repaint();
        }

        private BufferedImage loadMapImage() throws IOException {
            try (InputStream inputStream = MapUI.class.getResourceAsStream("/mapImage.jpg")) {
                if (inputStream == null) throw new FileNotFoundException("mapImage.jpg not found");
                return ImageIO.read(inputStream);
            }
        }

        private void setupInitialView() {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int screenWidth = screenSize.width - 50;
            int screenHeight = screenSize.height - 50;

            double scaleWidth = (double) screenWidth / baseMapImage.getWidth();
            double scaleHeight = (double) screenHeight / baseMapImage.getHeight();

            zoomFactor = Math.min(1.0, Math.min(scaleWidth, scaleHeight));
            setPreferredSize(new Dimension((int)(baseMapImage.getWidth() * zoomFactor), (int)(baseMapImage.getHeight() * zoomFactor)));

            offsetX = offsetY = 0;
        }

        private void setupMouseInteractions() {
            addMouseWheelListener(event -> {
                double previousZoom = zoomFactor;
                zoomFactor *= event.getWheelRotation() < 0 ? 1.1 : 0.9;
                zoomFactor = Math.max(previousZoom / 4, Math.min(previousZoom * 4, zoomFactor));

                double mouseX = event.getX();
                double mouseY = event.getY();

                offsetX = mouseX - (mouseX - offsetX) * (zoomFactor / previousZoom);
                offsetY = mouseY - (mouseY - offsetY) * (zoomFactor / previousZoom);

                revalidate();
                repaint();
            });

            MouseAdapter dragAdapter = new MouseAdapter() {
                public void mousePressed(MouseEvent event) {
                    dragStartPoint = event.getPoint();
                }

                public void mouseDragged(MouseEvent event) {
                    Point currentPoint = event.getPoint();
                    offsetX += currentPoint.x - dragStartPoint.x;
                    offsetY += currentPoint.y - dragStartPoint.y;
                    dragStartPoint = currentPoint;
                    repaint();
                }
            };

            addMouseListener(dragAdapter);
            addMouseMotionListener(dragAdapter);
        }

        private BufferedImage generateHeatmap() {
            int imageWidth = baseMapImage.getWidth();
            int imageHeight = baseMapImage.getHeight();
            int[][] heatValues = new int[imageWidth][imageHeight];
            double influenceRadius = 120.0;
            int radiusPixels = (int) Math.ceil(influenceRadius);

            for (GeoPosition position : stopCoordinates) {
                Point2D pixel = convertGeoToPixel(position);
                int centerX = (int) Math.round(pixel.getX());
                int centerY = (int) Math.round(pixel.getY());

                for (int dy = -radiusPixels; dy <= radiusPixels; dy++) {
                    for (int dx = -radiusPixels; dx <= radiusPixels; dx++) {
                        int x = centerX + dx;
                        int y = centerY + dy;
                        if (x >= 0 && x < imageWidth && y >= 0 && y < imageHeight) {
                            double distanceSquared = (dx + 0.5) * (dx + 0.5) + (dy + 0.5) * (dy + 0.5);
                            if (distanceSquared <= influenceRadius * influenceRadius) {
                                heatValues[x][y]++;
                            }
                        }
                    }
                }
            }

            int maxHeatValue = Arrays.stream(heatValues).flatMapToInt(Arrays::stream).max().orElse(0);
            Color[] heatmapColors = new Color[maxHeatValue + 1];
            for (int i = 1; i <= maxHeatValue; i++) {
                float hue = 1.0f - Math.min(i, 10) / 10.0f;
                heatmapColors[i] = Color.getHSBColor(hue, 1.0f, 1.0f);
            }

            BufferedImage heatmap = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < imageHeight; y++) {
                for (int x = 0; x < imageWidth; x++) {
                    int value = heatValues[x][y];
                    if (value <= 1) continue;
                    Color color = heatmapColors[Math.min(value, maxHeatValue)];
                    int rgba = (color.getRGB() & 0x00FFFFFF) | (180 << 24);
                    heatmap.setRGB(x, y, rgba);
                }
            }

            return heatmap;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            AffineTransform previousTransform = g2d.getTransform();
            g2d.translate(offsetX, offsetY);
            g2d.scale(zoomFactor, zoomFactor);

            g2d.drawImage(baseMapImage, 0, 0, null);
            if (showHeatmap) g2d.drawImage(heatmapOverlayImage, 0, 0, null);

            g2d.setColor(Color.RED);
            for (GeoPosition position : stopCoordinates) {
                Point2D pixel = convertGeoToPixel(position);
                g2d.fill(new Ellipse2D.Double(pixel.getX() - 2, pixel.getY() - 2, 4, 4));
            }

            g2d.setTransform(previousTransform);
        }

        private Point2D convertGeoToPixel(GeoPosition geoPosition) {
            double xRatio = (geoPosition.getLongitude() - minimumLongitude) / (maximumLongitude - minimumLongitude);
            double yRatio = (maximumLatitude - geoPosition.getLatitude()) / (maximumLatitude - minimumLatitude);
            return new Point2D.Double(xRatio * baseMapImage.getWidth(), yRatio * baseMapImage.getHeight());
        }
    }
}



//Old imlemetation. Here just for reference.

// public class MapUI {

//     /**
//      * Creates the MapUI and draws
//      */
//     public static void create() {
//         JXMapViewer mapViewer = new JXMapViewer();
//         TileFactory tileFactory = new DefaultTileFactory(new OSMTileFactoryInfo());
//         mapViewer.setTileFactory(tileFactory);
//         mapViewer.setZoom(5);
//         mapViewer.setAddressLocation(new GeoPosition(47.4979, 19.0402));    //Budapest location

//         ZoomHandler zoomHandler = new ZoomHandler(mapViewer);

//         mapViewer.addMouseWheelListener(e -> {
//             int rotation = e.getWheelRotation();
//             if (rotation < 0) {
//                 zoomHandler.zoomIn();
//             } else if (rotation > 0) {
//                 zoomHandler.zoomOut();
//             }
//         });

//         //so we can drag around the map
//         PanMouseInputListener panListener = new PanMouseInputListener(mapViewer);
//         mapViewer.addMouseListener(panListener);
//         mapViewer.addMouseMotionListener(panListener);

//         //load and display stops
//         //TODO: put this in a seperate class
//         List<GeoPosition> stops = new ArrayList<>();
//         String filePath = "data/stops.csv";
//         try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
//             reader.readLine();

//             String line;
//             while ((line = reader.readLine()) != null) {
//                 String[] parts = line.split(",");
//                 try {
//                     double lat = Double.parseDouble(parts[2]);
//                     double lon = Double.parseDouble(parts[3]);
//                     stops.add(new GeoPosition(lat, lon));
//                 } catch (Exception e) {
//                     System.out.println("Skipping invalid line: " + line);
//                 }
//             }
//         } catch (IOException e) {
//             System.out.println("Error loading stops: " + e.getMessage());
//         }

//         mapViewer.setOverlayPainter((Graphics2D g, JXMapViewer map, int w, int h) -> {
//             g.setColor(Color.BLUE);

//             for (GeoPosition stop : stops) {
//                 Point point = new Point((int) map.convertGeoPositionToPoint(stop).getX(),
//                         (int) map.convertGeoPositionToPoint(stop).getY());
//                 g.fillOval(point.x - 5, point.y - 5, 8, 8);
//             }
//         });

//         //make layered pane and sets up map
//         JLayeredPane layeredPane = new JLayeredPane();
//         layeredPane.setPreferredSize(new Dimension(800, 600));
//         mapViewer.setBounds(0, 0, 800, 600);
//         layeredPane.add(mapViewer, JLayeredPane.DEFAULT_LAYER);

//         //create adn add control panel - this will also handle all buttons and control panel items
//         ControlPanel controlPanel = ControlPanel.create(mapViewer);
//         controlPanel.setBounds(0, 0, 800, 600);
//         layeredPane.add(controlPanel, JLayeredPane.PALETTE_LAYER);

//         //create frame
//         JFrame frame = new JFrame("Map Viewer");
//         frame.add(layeredPane);
//         frame.setSize(800, 600);
//         frame.setLocationRelativeTo(null);
//         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//         frame.setVisible(true);

//     }

// }
