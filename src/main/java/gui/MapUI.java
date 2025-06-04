package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

public class MapUI {

    public static void create() {
        List<GeoPosition> stopPositions = loadStopCoordinates("data/stops.csv");

        double mapNorth = convertDMS(47, 31, 35.08);
        double mapSouth = convertDMS(47, 28, 5.16);
        double mapWest  = convertDMS(18, 58, 50.07);
        double mapEast  = convertDMS(19, 7, 26.23);

        SwingUtilities.invokeLater(() -> {
            try {
                OfflineMapPanel mapPanel = new OfflineMapPanel(mapSouth, mapNorth, mapWest, mapEast, stopPositions);

                JTextField startCoordinateInput = new JTextField("Start (lat,lon)", 20);
                JTextField endCoordinateInput = new JTextField("End (lat,lon)", 20);
                JToggleButton heatmapToggleButton = new JToggleButton("Show Heatmap", true);
                heatmapToggleButton.addActionListener(e -> mapPanel.setShowHeatmap(heatmapToggleButton.isSelected()));

                JPanel topControlsPanel = new JPanel(new BorderLayout());
                topControlsPanel.add(startCoordinateInput, BorderLayout.WEST);
                topControlsPanel.add(endCoordinateInput, BorderLayout.CENTER);
                topControlsPanel.add(heatmapToggleButton, BorderLayout.EAST);

                JPanel mainPanel = new JPanel(new BorderLayout());
                mainPanel.add(topControlsPanel, BorderLayout.NORTH);
                mainPanel.add(new JScrollPane(mapPanel), BorderLayout.CENTER);

                JFrame applicationFrame = new JFrame("Offline Map Viewer");
                applicationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                applicationFrame.setContentPane(mainPanel);
                applicationFrame.pack();
                applicationFrame.setLocationRelativeTo(null);
                applicationFrame.setVisible(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static List<GeoPosition> loadStopCoordinates(String csvFilePath) {
        List<GeoPosition> stopList = new ArrayList<>();
        String csvPattern = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(csvPattern, -1);
                if (fields.length < 4) continue;
                try {
                    double latitude = Double.parseDouble(fields[2]);
                    double longitude = Double.parseDouble(fields[3]);
                    stopList.add(new GeoPosition(latitude, longitude));
                } catch (NumberFormatException ignored) {}
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return stopList;
    }

    private static double convertDMS(int degrees, int minutes, double seconds) {
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
        private final BufferedImage mapImage;
        private final List<GeoPosition> stopLocations;
        private final double minLatitude, maxLatitude, minLongitude, maxLongitude;
        private double zoomScale, imageOffsetX, imageOffsetY;
        private Point mouseDragStart;
        private BufferedImage heatmapImage;
        private boolean displayHeatmap = true;

        public OfflineMapPanel(double minLat, double maxLat, double minLon, double maxLon, List<GeoPosition> stops) throws IOException {
            this.minLatitude = minLat;
            this.maxLatitude = maxLat;
            this.minLongitude = minLon;
            this.maxLongitude = maxLon;
            this.stopLocations = stops;
            this.mapImage = loadBaseMapImage();
            this.heatmapImage = createHeatmapOverlay();
            initializeMapView();
            registerMouseInteractions();
        }

        public void setShowHeatmap(boolean show) {
            this.displayHeatmap = show;
            repaint();
        }

        private BufferedImage loadBaseMapImage() throws IOException {
            try (InputStream in = MapUI.class.getResourceAsStream("/mapImage.jpg")) {
                if (in == null) throw new FileNotFoundException("mapImage.jpg not found");
                return ImageIO.read(in);
            }
        }

        private void initializeMapView() {
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            int width = screen.width - 50, height = screen.height - 50;
            double scaleX = (double) width / mapImage.getWidth();
            double scaleY = (double) height / mapImage.getHeight();
            zoomScale = Math.min(1.0, Math.min(scaleX, scaleY));
            setPreferredSize(new Dimension((int)(mapImage.getWidth() * zoomScale), (int)(mapImage.getHeight() * zoomScale)));
            imageOffsetX = imageOffsetY = 0;
        }

        private void registerMouseInteractions() {
            addMouseWheelListener(e -> {
                double previousScale = zoomScale;
                zoomScale *= e.getWheelRotation() < 0 ? 1.1 : 0.9;
                zoomScale = Math.max(previousScale / 4, Math.min(previousScale * 4, zoomScale));
                double mouseX = e.getX(), mouseY = e.getY();
                imageOffsetX = mouseX - (mouseX - imageOffsetX) * (zoomScale / previousScale);
                imageOffsetY = mouseY - (mouseY - imageOffsetY) * (zoomScale / previousScale);
                revalidate(); repaint();
            });
            MouseAdapter adapter = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    mouseDragStart = e.getPoint();
                }
                public void mouseDragged(MouseEvent e) {
                    Point current = e.getPoint();
                    imageOffsetX += current.x - mouseDragStart.x;
                    imageOffsetY += current.y - mouseDragStart.y;
                    mouseDragStart = current;
                    repaint();
                }
            };
            addMouseListener(adapter);
            addMouseMotionListener(adapter);
        }

        private BufferedImage createHeatmapOverlay() {
            int width = mapImage.getWidth(), height = mapImage.getHeight();
            int[][] heatValues = new int[width][height];
            double circleRadius = 120.0;
            int r = (int) Math.ceil(circleRadius);

            for (GeoPosition gp : stopLocations) {
                Point2D center = convertGeoToPixel(gp);
                int cx = (int) Math.round(center.getX());
                int cy = (int) Math.round(center.getY());
                for (int dy = -r; dy <= r; dy++) {
                    for (int dx = -r; dx <= r; dx++) {
                        int x = cx + dx, y = cy + dy;
                        if (x >= 0 && x < width && y >= 0 && y < height) {
                            double distSq = (dx + 0.5) * (dx + 0.5) + (dy + 0.5) * (dy + 0.5);
                            if (distSq <= circleRadius * circleRadius) {
                                heatValues[x][y]++;
                            }
                        }
                    }
                }
            }

            int maxHeat = Arrays.stream(heatValues).flatMapToInt(Arrays::stream).max().orElse(0);
            Color[] colorRamp = new Color[maxHeat + 1];
            for (int i = 1; i <= maxHeat; i++) {
                float hue = 1.0f - Math.min(i, 10) / 10.0f;
                colorRamp[i] = Color.getHSBColor(hue, 1.0f, 1.0f);
            }

            BufferedImage heatmap = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int count = heatValues[x][y];
                    if (count <= 1) continue;
                    Color color = colorRamp[Math.min(count, maxHeat)];
                    int rgba = (color.getRGB() & 0x00FFFFFF) | (180 << 24);
                    heatmap.setRGB(x, y, rgba);
                }
            }
            return heatmap;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            AffineTransform original = g.getTransform();
            g.translate(imageOffsetX, imageOffsetY);
            g.scale(zoomScale, zoomScale);
            g.drawImage(mapImage, 0, 0, null);
            if (displayHeatmap) g.drawImage(heatmapImage, 0, 0, null);
            g.setColor(Color.RED);
            for (GeoPosition gp : stopLocations) {
                Point2D p = convertGeoToPixel(gp);
                g.fill(new Ellipse2D.Double(p.getX() - 2, p.getY() - 2, 4, 4));
            }
            g.setTransform(original);
        }

        private Point2D convertGeoToPixel(GeoPosition gp) {
            double xFraction = (gp.getLongitude() - minLongitude) / (maxLongitude - minLongitude);
            double yFraction = (maxLatitude - gp.getLatitude()) / (maxLatitude - minLatitude);
            return new Point2D.Double(xFraction * mapImage.getWidth(), yFraction * mapImage.getHeight());
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
