package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

import java.awt.Point;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactory;

public class mainUI {
    public static void main(String[] args) {  
        JXMapViewer mapViewer = new JXMapViewer();
        TileFactory tileFactory = new DefaultTileFactory(new OSMTileFactoryInfo());;
        mapViewer.setTileFactory(tileFactory);
        mapViewer.setZoom(5);                                               // default zoom
        mapViewer.setAddressLocation(new GeoPosition(47.4979, 19.0402));    //Budapest location


        PanMouseInputListener panListener = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(panListener);
        mapViewer.addMouseMotionListener(panListener);


        JButton zoomInBtn = new JButton("+");
        zoomInBtn.addActionListener(e -> mapViewer.setZoom(mapViewer.getZoom() - 1)); // Zoom in
        
        JButton zoomOutBtn = new JButton("-");
        zoomOutBtn.addActionListener(e -> mapViewer.setZoom(mapViewer.getZoom() + 1)); // Zoom out

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        
        buttonPanel.add(zoomInBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        buttonPanel.add(zoomOutBtn);



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

        
        
        JLayeredPane layeredPane = new JLayeredPane();
            layeredPane.setPreferredSize(new Dimension(800, 600));
            
            mapViewer.setBounds(0, 0, 800, 600);
            buttonPanel.setOpaque(false);
            buttonPanel.setBounds(700,10,50,90);
            
            layeredPane.add(mapViewer, JLayeredPane.DEFAULT_LAYER);
            layeredPane.add(buttonPanel, JLayeredPane.PALETTE_LAYER);

        JFrame frame = new JFrame("Map Viewer");
        frame.add(layeredPane);

        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
