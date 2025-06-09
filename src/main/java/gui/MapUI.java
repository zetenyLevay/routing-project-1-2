package gui;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.awt.*;
import javax.swing.*;

import gui.components.MapDisplay;
import gui.components.UserInterfaceBuilder;
import gui.data.BusStopDataLoader;
import gui.data.GeographicBounds;
import gui.data.LocationPoint;
import gui.transform.CoordinateConverter;
import routing.api.Router;
import routing.routingEngineDijkstra.api.DijkstraRoutePlanner;
import routing.routingEngineDijkstra.dijkstra.parsers.GTFSDatabaseParser;

public class MapUI {
    private static Router sharedRouter;
    
    public static void main(String[] args) throws IOException {
        create();
    }

    public static void create() {
        List<LocationPoint> busStopData = BusStopDataLoader.loadFromCsvFile("data/stops.csv");
        double northBoundary = CoordinateConverter.degreesMinutesSecondsToDecimal(47, 39, 9.36);
        double southBoundary = CoordinateConverter.degreesMinutesSecondsToDecimal(47, 20, 8.50);
        double westBoundary = CoordinateConverter.degreesMinutesSecondsToDecimal(18, 41, 15.29);
        double eastBoundary = CoordinateConverter.degreesMinutesSecondsToDecimal(19, 28, 30.60);
        GeographicBounds mapBoundaries = new GeographicBounds(
                southBoundary, northBoundary, westBoundary, eastBoundary
        );

        SwingUtilities.invokeLater(() -> {
            try {
                initializeSharedRouter();
                
                JTextField startCoordinateInput = new JTextField("(lat,lon)", 20);
                JTextField endCoordinateInput = new JTextField("(lat,lon)", 20);
                MapDisplay mapDisplayPanel = new MapDisplay(
                        mapBoundaries, busStopData, startCoordinateInput, endCoordinateInput
                );
                JScrollPane scrollPane = new JScrollPane(mapDisplayPanel);
                scrollPane.getViewport().addMouseWheelListener(e -> mapDisplayPanel.dispatchEvent(
                        SwingUtilities.convertMouseEvent(scrollPane.getViewport(), e, mapDisplayPanel)
                ));
                JPanel controlPanel = UserInterfaceBuilder.createControlPanel(
                        startCoordinateInput,
                        endCoordinateInput,
                        mapDisplayPanel,
                        null
                );

                JPanel applicationPanel = new JPanel(new BorderLayout());
                applicationPanel.add(controlPanel, BorderLayout.NORTH);
                applicationPanel.add(scrollPane, BorderLayout.CENTER);
                JFrame mainWindow = UserInterfaceBuilder.createMainWindow(applicationPanel);
                mainWindow.setVisible(true);
            } catch (Exception fileError) {
                fileError.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Error loading map: " + fileError.getMessage(),
                        "Map Loading Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
    
    private static void initializeSharedRouter() throws SQLException, IOException {
        sharedRouter = new Router(new DijkstraRoutePlanner(
                GTFSDatabaseParser.createRouterFromGTFS(500)
        ));
    }
    
    public static Router getSharedRouter() {
        return sharedRouter;
    }
}