package gui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;

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
                JTextField startCoordinateInput = new JTextField("Start (lat,lon)", 20);
                JTextField endCoordinateInput = new JTextField("End (lat,lon)", 20);

                MapDisplay mapDisplayPanel = new MapDisplay(
                    mapBoundaries, busStopData, startCoordinateInput, endCoordinateInput);

                JPanel controlPanel = UserInterfaceBuilder.createControlPanel(
                    startCoordinateInput, endCoordinateInput, mapDisplayPanel);

                JPanel applicationPanel = new JPanel(new BorderLayout());
                applicationPanel.add(controlPanel, BorderLayout.NORTH);
                applicationPanel.add(new JScrollPane(mapDisplayPanel), BorderLayout.CENTER);

                JFrame mainWindow = UserInterfaceBuilder.createMainWindow(applicationPanel);
                mainWindow.setVisible(true);
                
            } catch (IOException fileError) {
                fileError.printStackTrace();
            }
        });
    }
}