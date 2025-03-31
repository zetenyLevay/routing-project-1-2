package gui;

import org.jxmapviewer.*;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactory;
import javax.swing.*;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;
import java.awt.BorderLayout;

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


        JButton zoomInBtn = new JButton("Zoom out");
        zoomInBtn.addActionListener(e -> mapViewer.setZoom(mapViewer.getZoom() + 1)); // Zoom in
        
        JButton zoomOutBtn = new JButton("Zoom in");
        zoomOutBtn.addActionListener(e -> mapViewer.setZoom(mapViewer.getZoom() - 1)); // Zoom out

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(zoomOutBtn);
        buttonPanel.add(zoomInBtn);


        JFrame frame = new JFrame("Map Viewer");
        frame.getContentPane().add(mapViewer);
        frame.setLayout(new BorderLayout());
        frame.add(buttonPanel, BorderLayout.NORTH);
        frame.add(mapViewer, BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
