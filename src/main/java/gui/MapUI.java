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
        double a = dmsToDecimal(47, 32, 48.7);
        double b = dmsToDecimal(47, 24, 47.8);
        double c = dmsToDecimal(18, 57, 44.6);
        double d = dmsToDecimal(19, 17, 30.9);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    OfflineMapPanel panel = new OfflineMapPanel(a, b, c, d, list);
                    JFrame frame = new JFrame("Offline Map Viewer");
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.add(new JScrollPane(panel));
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static List<GeoPosition> loadStops(String f) {
        List<GeoPosition> list = new ArrayList<GeoPosition>();
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(f));
            r.readLine();
            String l = null;
            while ((l = r.readLine()) != null) {
                String[] p = l.split(",");
                try {
                    double la = Double.parseDouble(p[2]);
                    double lo = Double.parseDouble(p[3]);
                    list.add(new GeoPosition(la, lo));
                } catch (Exception ex) {
                }
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            if (r != null) {
                try { r.close(); } catch (IOException ex) { }
            }
        }
        return list;
    }

    private static double dmsToDecimal(int deg, int min, double sec) {
        return deg + (min / 60.0) + (sec / 3600.0);
    }

    public static class GeoPosition {
        private final double la;
        private final double lo;
        public GeoPosition(double la, double lo) {
            this.la = la;
            this.lo = lo;
        }
        public double getLatitude() { return la; }
        public double getLongitude() { return lo; }
    }

    public static class OfflineMapPanel extends JPanel {
        private BufferedImage image;
        private double minA, maxA, minB, maxB;
        private List<GeoPosition> list;
        private double s;
        private double oX = 0, oY = 0;
        private Point lp;

        public OfflineMapPanel(double minA, double maxA, double minB, double maxB, List<GeoPosition> list) throws IOException {
            this.minA = minA; this.maxA = maxA;
            this.minB = minB; this.maxB = maxB;
            this.list = list;

            InputStream in = MapUI.class.getResourceAsStream("/mapImage.png");
            if (in == null) throw new FileNotFoundException("mapImage.png not found");
            image = ImageIO.read(in);
            in.close();

            Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
            int w = scr.width - 50;
            int h = scr.height - 50;
            double sx = (double) w / image.getWidth();
            double sy = (double) h / image.getHeight();
            double init = Math.min(1.0, Math.min(sx, sy));
            s = init;
            setPreferredSize(new Dimension((int)(image.getWidth() * init), (int)(image.getHeight() * init)));

            addMouseWheelListener(new MouseWheelListener() {
                public void mouseWheelMoved(MouseWheelEvent e) {
                    double old = s;
                    if (e.getWheelRotation() < 0) s = s * 1.1;
                    else s = s * 0.9;
                    if (s < init / 4) s = init / 4;
                    if (s > init * 4) s = init * 4;
                    double x = e.getX(), y = e.getY();
                    oX = x - (x - oX) * (s / old);
                    oY = y - (y - oY) * (s / old);
                    revalidate(); repaint();
                }
            });

            MouseAdapter ma = new MouseAdapter() {
                public void mousePressed(MouseEvent e) { lp = e.getPoint(); }
                public void mouseDragged(MouseEvent e) {
                    Point p = e.getPoint();
                    oX += p.x - lp.x;
                    oY += p.y - lp.y;
                    lp = p;
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
            at.translate(oX, oY);
            at.scale(s, s);
            g2.drawImage(image, at, null);
            g2.setColor(Color.RED);
            for (int i = 0; i < list.size(); i++) {
                GeoPosition gp = list.get(i);
                Point pt = geoToPoint(gp);
                Point2D p2 = at.transform(pt, null);
                g2.fillOval((int)p2.getX() - 4, (int)p2.getY() - 4, 8, 8);
            }
        }

        private Point geoToPoint(GeoPosition gp) {
            double xf = (gp.getLongitude() - minB) / (maxB - minB);
            double yf = (maxA - gp.getLatitude()) / (maxA - minA);
            int x = (int)(xf * image.getWidth());
            int y = (int)(yf * image.getHeight());
            return new Point(x, y);
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
