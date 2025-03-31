package gui;

import org.jxmapviewer.*;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactory;
import javax.swing.*;

public class mainUI {
    public static void main(String[] args) {  
        JXMapViewer mapViewer = new JXMapViewer();
        TileFactory tileFactory = new DefaultTileFactory(new OSMTileFactoryInfo());;
        mapViewer.setTileFactory(tileFactory);
        mapViewer.setZoom(5);
        mapViewer.setAddressLocation(new GeoPosition(47.4979, 19.0402));


        JFrame frame = new JFrame("Map Viewer");
        frame.getContentPane().add(mapViewer);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
