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

import gui.components.MapDisplay;
import gui.components.UserInterfaceBuilder;
import gui.data.BusStopDataLoader;
import gui.data.GeographicBounds;
import gui.data.LocationPoint;
import gui.transform.CoordinateConverter;
import heatmap.StopsCache;
import heatmap.TravelTimeHeatmapAPI;
import routing.api.Router;
import routing.routingEngineDijkstra.api.DijkstraRoutePlanner;
import routing.routingEngineDijkstra.dijkstra.algorithm.DijkstraRouter;
import routing.routingEngineDijkstra.dijkstra.parsers.GTFSDatabaseParser;

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
        TravelTimeHeatmapAPI heatmapAPI = null;
        try {
            DijkstraRouter dijkstraRouter = GTFSDatabaseParser.createRouterFromGTFS(500);
            Router router = new Router(new DijkstraRoutePlanner(dijkstraRouter));
            heatmapAPI = new TravelTimeHeatmapAPI(router);
            System.out.println("Travel-time heatmap system initialized successfully");
            System.out.println("Loaded " + StopsCache.getCacheSize() + " bus stops from database");
        } catch (Exception e) {
            System.err.println("Warning: Could not initialize travel-time heatmap system: " + e.getMessage());
            e.printStackTrace();
        }
        final TravelTimeHeatmapAPI finalHeatmapAPI = heatmapAPI;
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
                JPanel controlPanel;
                if (finalHeatmapAPI != null) {
                    controlPanel = UserInterfaceBuilder.createControlPanel(
                        startCoordinateInput, endCoordinateInput, mapDisplayPanel, finalHeatmapAPI);
                } else {
                    controlPanel = UserInterfaceBuilder.createControlPanelWithoutHeatmap(
                        startCoordinateInput, endCoordinateInput, mapDisplayPanel);
                }
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