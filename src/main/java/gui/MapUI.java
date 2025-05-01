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
        List<GeoPosition> stops = loadStops("data/stops.csv");
        double maxLat = dmsToDecimal(47, 32, 48.7);
        double minLat = dmsToDecimal(47, 24, 47.8);
        double minLon = dmsToDecimal(18, 57, 44.6);
        double maxLon = dmsToDecimal(19, 17, 30.9);

        SwingUtilities.invokeLater(() -> {
            try {
                OfflineMapPanel panel = new OfflineMapPanel(minLat, maxLat, minLon, maxLon, stops);
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

    private static List<GeoPosition> loadStops(String filePath) {
        List<GeoPosition> list = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new FileReader(filePath))) {
            r.readLine();
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split(",");
                try {
                    list.add(new GeoPosition(
                        Double.parseDouble(p[2]), Double.parseDouble(p[3])
                    ));
                } catch (Exception ex) { }
            }
        } catch (IOException e) {
            System.out.println("Error loading stops: " + e.getMessage());
        }
        return list;
    }

    private static double dmsToDecimal(int deg, int min, double sec) {
        return deg + min / 60.0 + sec / 3600.0;
    }

    public static class GeoPosition {
        private final double lat, lon;
        public GeoPosition(double lat, double lon) { this.lat = lat; this.lon = lon; }
        public double getLatitude() { return lat; }
        public double getLongitude() { return lon; }
    }

    public static class OfflineMapPanel extends JPanel {
        private BufferedImage img;
        private final double minLat, maxLat, minLon, maxLon;
        private final java.util.List<GeoPosition> stops;
        private double scale = 1.0;
        private double offsetX = 0, offsetY = 0;
        private Point lastDrag;

        public OfflineMapPanel(double minLat, double maxLat, double minLon, double maxLon, java.util.List<GeoPosition> stops) throws IOException {
            this.minLat = minLat; this.maxLat = maxLat;
            this.minLon = minLon; this.maxLon = maxLon;
            this.stops = stops;
            try (InputStream in = MapUI.class.getResourceAsStream("/mapImage.png")) {
                if (in == null) throw new FileNotFoundException("mapImage.png not found");
                img = ImageIO.read(in);
            }
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            int maxW = screen.width - 50, maxH = screen.height - 50;
            double sx = (double)maxW / img.getWidth(), sy = (double)maxH / img.getHeight();
            double init = Math.min(1.0, Math.min(sx, sy));
            scale = init;
            setPreferredSize(new Dimension((int)(img.getWidth()*init), (int)(img.getHeight()*init)));
            addMouseWheelListener(e -> {
                double old = scale;
                scale *= (e.getWheelRotation() < 0 ? 1.1 : 0.9);
                scale = Math.max(init/4, Math.min(scale, init*4));
                double mx = e.getX(), my = e.getY();
                offsetX = mx - (mx - offsetX) * (scale/old);
                offsetY = my - (my - offsetY) * (scale/old);
                revalidate(); repaint();
            });
            MouseAdapter ma = new MouseAdapter() {
                public void mousePressed(MouseEvent e) { lastDrag = e.getPoint(); }
                public void mouseDragged(MouseEvent e) {
                    Point p = e.getPoint();
                    offsetX += p.x - lastDrag.x;
                    offsetY += p.y - lastDrag.y;
                    lastDrag = p;
                    repaint();
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            AffineTransform at = new AffineTransform();
            at.translate(offsetX, offsetY);
            at.scale(scale, scale);
            g2.drawImage(img, at, null);
            g2.setColor(Color.RED);
            for (GeoPosition stop : stops) {
                Point pt = geoToPoint(stop);
                Point2D p2 = at.transform(pt, null);
                g2.fillOval((int)p2.getX()-4, (int)p2.getY()-4, 8, 8);
            }
        }

        private Point geoToPoint(GeoPosition gp) {
            double xFrac = (gp.getLongitude() - minLon)/(maxLon - minLon);
            double yFrac = (maxLat - gp.getLatitude())/(maxLat - minLat);
            int x = (int)(xFrac * img.getWidth());
            int y = (int)(yFrac * img.getHeight());
            return new Point(x, y);
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
