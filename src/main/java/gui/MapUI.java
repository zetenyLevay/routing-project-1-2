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
import javax.swing.JLayeredPane;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactory;

import eventHandlers.ZoomHandler;

public class MapUI {

    /**
     * Creates the MapUI and draws
     */
    public static void create() {
        JXMapViewer mapViewer = new JXMapViewer();
        TileFactory tileFactory = new DefaultTileFactory(new OSMTileFactoryInfo());
        mapViewer.setTileFactory(tileFactory);
        mapViewer.setZoom(5);
        mapViewer.setAddressLocation(new GeoPosition(47.4979, 19.0402));    //Budapest location

        ZoomHandler zoomHandler = new ZoomHandler(mapViewer);

        mapViewer.addMouseWheelListener(e -> {
            int rotation = e.getWheelRotation();
            if (rotation < 0) {
                zoomHandler.zoomIn();
            } else if (rotation > 0) {
                zoomHandler.zoomOut();
            }
        });

        //so we can drag around the map
        PanMouseInputListener panListener = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(panListener);
        mapViewer.addMouseMotionListener(panListener);

        //load and display stops
        //TODO: put this in a seperate class
        List<GeoPosition> stops = new ArrayList<>();
        String filePath = "data/stops.csv";
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                try {
                    double lat = Double.parseDouble(parts[2]);
                    double lon = Double.parseDouble(parts[3]);
                    stops.add(new GeoPosition(lat, lon));
                } catch (Exception e) {
                    System.out.println("Skipping invalid line: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading stops: " + e.getMessage());
        }

        mapViewer.setOverlayPainter((Graphics2D g, JXMapViewer map, int w, int h) -> {
            g.setColor(Color.BLUE);

            for (GeoPosition stop : stops) {
                Point point = new Point((int) map.convertGeoPositionToPoint(stop).getX(),
                        (int) map.convertGeoPositionToPoint(stop).getY());
                g.fillOval(point.x - 5, point.y - 5, 8, 8);
            }
        });

        //make layered pane and sets up map
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(800, 600));
        mapViewer.setBounds(0, 0, 800, 600);
        layeredPane.add(mapViewer, JLayeredPane.DEFAULT_LAYER);

        //create adn add control panel - this will also handle all buttons and control panel items
        ControlPanel controlPanel = ControlPanel.create(mapViewer);
        controlPanel.setBounds(0, 0, 800, 600);
        layeredPane.add(controlPanel, JLayeredPane.PALETTE_LAYER);

        //create frame
        JFrame frame = new JFrame("Map Viewer");
        frame.add(layeredPane);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

    }

}
