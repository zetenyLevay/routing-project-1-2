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
        List<GeoPosition> list = loadStops("data/stops.csv");
        double north = dmsToDecimal(47, 31, 35.08);
        double south = dmsToDecimal(47, 28, 5.16);
        double west  = dmsToDecimal(18, 58, 50.07);
        double east  = dmsToDecimal(19, 7, 26.23);

        SwingUtilities.invokeLater(() -> {
            try {
                OfflineMapPanel panel = new OfflineMapPanel(south, north, west, east, list);
                JFrame frame = new JFrame("Offline Map Viewer");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.add(new JScrollPane(panel));
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static List<GeoPosition> loadStops(String path) {
        List<GeoPosition> list = new ArrayList<>();
        String pattern = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
        try (BufferedReader r = new BufferedReader(new FileReader(path))) {
            r.readLine();
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split(pattern, -1);
                if (p.length < 4) continue;
                try {
                    double lat = Double.parseDouble(p[2]);
                    double lon = Double.parseDouble(p[3]);
                    list.add(new GeoPosition(lat, lon));
                } catch (NumberFormatException ignore) {}
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return list;
    }

    private static double dmsToDecimal(int deg, int min, double sec) {
        return deg + min / 60.0 + sec / 3600.0;
    }

    public static class GeoPosition {
        private final double latitude, longitude;
        public GeoPosition(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
        public double getLatitude()  { return latitude; }
        public double getLongitude() { return longitude; }
    }

    public static class OfflineMapPanel extends JPanel {
        private final BufferedImage image;
        private final double minLat, maxLat, minLon, maxLon;
        private final List<GeoPosition> stops;
        private double scale, offsetX, offsetY;
        private Point lastDragPoint;

        public OfflineMapPanel(double minLat, double maxLat, double minLon, double maxLon, List<GeoPosition> stops) throws IOException {
            this.minLat = minLat;
            this.maxLat = maxLat;
            this.minLon = minLon;
            this.maxLon = maxLon;
            this.stops  = stops;
            try (InputStream in = MapUI.class.getResourceAsStream("/mapImage.jpg")) {
                if (in == null) throw new FileNotFoundException("mapImage.jpg not found");
                image = ImageIO.read(in);
            }
            Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
            int w = scr.width - 50, h = scr.height - 50;
            double sx = (double) w / image.getWidth();
            double sy = (double) h / image.getHeight();
            scale = Math.min(1.0, Math.min(sx, sy));
            setPreferredSize(new Dimension((int)(image.getWidth()*scale), (int)(image.getHeight()*scale)));
            offsetX = offsetY = 0;
            addMouseWheelListener(e -> {
                double old = scale;
                scale *= e.getWheelRotation() < 0 ? 1.1 : 0.9;
                scale = Math.max(old/4, Math.min(old*4, scale));
                double mx = e.getX(), my = e.getY();
                offsetX = mx - (mx - offsetX)*(scale/old);
                offsetY = my - (my - offsetY)*(scale/old);
                revalidate(); repaint();
            });
            MouseAdapter ma = new MouseAdapter() {
                public void mousePressed(MouseEvent e) { lastDragPoint = e.getPoint(); }
                public void mouseDragged(MouseEvent e) {
                    Point p = e.getPoint();
                    offsetX += p.x - lastDragPoint.x;
                    offsetY += p.y - lastDragPoint.y;
                    lastDragPoint = p;
                    repaint();
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
            AffineTransform orig = g.getTransform();
            g.translate(offsetX, offsetY);
            g.scale(scale, scale);
            g.drawImage(image, 0, 0, null);
            double r = 4.0;
            g.setColor(Color.RED);
            for (GeoPosition gp : stops) {
                Point2D p = geoToPoint(gp);
                g.fill(new Ellipse2D.Double(p.getX()-r, p.getY()-r, 2*r, 2*r));
            }
            g.setTransform(orig);
        }

        private Point2D geoToPoint(GeoPosition gp) {
            double xFrac = (gp.getLongitude() - minLon) / (maxLon - minLon);
            double yFrac = (maxLat - gp.getLatitude()) / (maxLat - minLat);
            return new Point2D.Double(xFrac * image.getWidth(), yFrac * image.getHeight());
        }
    }

    public static void main(String[] args) {
        create();
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
