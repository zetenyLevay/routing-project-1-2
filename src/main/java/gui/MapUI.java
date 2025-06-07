package gui;

import java.io.IOException;
import java.util.List;
import java.awt.*;
import javax.swing.*;

import gui.components.MapDisplay;
import gui.components.UserInterfaceBuilder;
import gui.data.BusStopDataLoader;
import gui.data.GeographicBounds;
import gui.data.LocationPoint;
import gui.transform.CoordinateConverter;

public class MapUI {
    public static void main(String[] args) throws IOException {
        MapUI.create();
    }

    public static void create() {
        List<LocationPoint> busStopData = BusStopDataLoader.loadFromCsvFile("data/stops.csv");
        double northBoundary = CoordinateConverter.degreesMinutesSecondsToDecimal(47, 31, 35.08);
        double southBoundary = CoordinateConverter.degreesMinutesSecondsToDecimal(47, 28, 5.16);
        double westBoundary = CoordinateConverter.degreesMinutesSecondsToDecimal(18, 58, 50.07);
        double eastBoundary = CoordinateConverter.degreesMinutesSecondsToDecimal(19, 7, 26.23);
        GeographicBounds mapBoundaries = new GeographicBounds(
            southBoundary, northBoundary, westBoundary, eastBoundary);
        
        SwingUtilities.invokeLater(() -> {
            try {
                JTextField startCoordinateInput = new JTextField("(lat,lon)", 20);
                JTextField endCoordinateInput = new JTextField("(lat,lon)", 20);
                MapDisplay mapDisplayPanel = new MapDisplay(
                    mapBoundaries, busStopData, startCoordinateInput, endCoordinateInput);
                JScrollPane scrollPane = new JScrollPane(mapDisplayPanel);
                scrollPane.getViewport().addMouseWheelListener(e -> {
                    mapDisplayPanel.dispatchEvent(
                        SwingUtilities.convertMouseEvent(scrollPane.getViewport(), e, mapDisplayPanel)
                    );
                });
                JPanel controlPanel = UserInterfaceBuilder.createControlPanelWithoutHeatmap(
                    startCoordinateInput, endCoordinateInput, mapDisplayPanel);
                
                JPanel applicationPanel = new JPanel(new BorderLayout());
                applicationPanel.add(controlPanel, BorderLayout.NORTH);
                applicationPanel.add(scrollPane, BorderLayout.CENTER);
                JFrame mainWindow = UserInterfaceBuilder.createMainWindow(applicationPanel);
                mainWindow.setVisible(true);
            } catch (IOException fileError) {
                fileError.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Error loading map: " + fileError.getMessage(),
                    "Map Loading Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}