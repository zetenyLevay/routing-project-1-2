package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

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
