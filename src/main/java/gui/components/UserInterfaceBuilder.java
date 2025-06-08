package gui.components;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import heatmap.HeatmapData;
import heatmap.StopsCache;
import heatmap.TravelTimeHeatmapAPI;
import routing.api.Router;
import routing.routingEngineDijkstra.adiModels.Stop.AdiStop;
import routing.routingEngineDijkstra.api.DijkstraRoutePlanner;
import routing.routingEngineDijkstra.dijkstra.algorithm.DijkstraRouter;
import routing.routingEngineDijkstra.dijkstra.parsers.GTFSDatabaseParser;
import routing.routingEngineModels.Coordinates;
import routing.routingEngineDijkstra.adiModels.Stop.AdiStop;
import routing.routingEngineDijkstra.adiModels.*;


public class UserInterfaceBuilder {

    public static JPanel createControlPanel(JTextField startCoordinateField, JTextField endCoordinateField,
                                            MapDisplay mapDisplay, TravelTimeHeatmapAPI heatmapAPI) {
        JButton generateHeatmapButton = createHeatmapButton(startCoordinateField, mapDisplay, heatmapAPI);
        JButton zoomInButton = createZoomButton("+", 1.5, mapDisplay);
        JButton zoomOutButton = createZoomButton("-", 0.9, mapDisplay);

        return buildControlPanel(startCoordinateField, endCoordinateField, 
                               zoomInButton, zoomOutButton, generateHeatmapButton);
    }

    public static JPanel createControlPanelWithoutHeatmap(JTextField startCoordinateField, JTextField endCoordinateField,
                                                          MapDisplay mapDisplay) {
        JButton heatmapButton = createLazyHeatmapButton(startCoordinateField, mapDisplay);
        JButton zoomInButton = createZoomButton("+", 1.5, mapDisplay);
        JButton zoomOutButton = createZoomButton("-", 0.9, mapDisplay);

        return buildControlPanel(startCoordinateField, endCoordinateField, 
                               zoomInButton, zoomOutButton, heatmapButton);
    }

    private static JButton createHeatmapButton(JTextField startCoordinateField, MapDisplay mapDisplay, 
                                             TravelTimeHeatmapAPI heatmapAPI) {
        JButton button = new JButton("Generate Heatmap");
        button.addActionListener(e -> generateHeatmap(startCoordinateField, mapDisplay, heatmapAPI, button));
        return button;
    }

    private static JButton createLazyHeatmapButton(JTextField startCoordinateField, MapDisplay mapDisplay) {
        JButton button = new JButton("Generate Heatmap");
        button.addActionListener(e -> generateHeatmapWithInitialization(startCoordinateField, mapDisplay, button));
        return button;
    }

    private static JButton createZoomButton(String text, double zoomFactor, MapDisplay mapDisplay) {
        JButton button = new JButton(text);
        button.addActionListener(e -> mapDisplay.adjustZoom(zoomFactor));
        return button;
    }

    private static JPanel buildControlPanel(JTextField startField, JTextField endField,
                                          JButton zoomInButton, JButton zoomOutButton, JButton actionButton) {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(new JLabel("Start:"));
        controlPanel.add(startField);
        controlPanel.add(new JLabel("End:"));
        controlPanel.add(endField);
        controlPanel.add(zoomInButton);
        controlPanel.add(zoomOutButton);
        controlPanel.add(actionButton);
        return controlPanel;
    }

    private static void generateHeatmap(JTextField startCoordinateField, MapDisplay mapDisplay, 
                                      TravelTimeHeatmapAPI heatmapAPI, JButton button) {
        try {
            CoordinateInput coordinateInput = parseCoordinateInput(startCoordinateField, mapDisplay);
            if (coordinateInput == null) return;

            String nearestStopId = findNearestStopId(coordinateInput.latitude, coordinateInput.longitude);
            if (nearestStopId == null) {
                showErrorMessage(mapDisplay, "No nearby bus stop found for the selected coordinates");
                return;
            }

            executeHeatmapGeneration(mapDisplay, heatmapAPI, button, nearestStopId);

        } catch (NumberFormatException ex) {
            showErrorMessage(mapDisplay, "Invalid coordinate format. Please enter numbers only.");
        } catch (Exception ex) {
            showErrorMessage(mapDisplay, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static void generateHeatmapWithInitialization(JTextField startCoordinateField, MapDisplay mapDisplay, 
                                                        JButton button) {
        try {
            CoordinateInput coordinateInput = parseCoordinateInput(startCoordinateField, mapDisplay);
            if (coordinateInput == null) return;

            button.setText("Initializing...");
            button.setEnabled(false);

            SwingWorker<Void, Void> initializationWorker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    TravelTimeHeatmapAPI heatmapAPI = initializeHeatmapSystem();
                    String nearestStopId = findNearestStopId(coordinateInput.latitude, coordinateInput.longitude);
                    
                    if (nearestStopId == null) {
                        throw new Exception("No nearby bus stop found for the selected coordinates");
                    }

                    HeatmapData heatmapData = heatmapAPI.generateHeatmap(nearestStopId);
                    Map<String, Color> stopColors = heatmapAPI.getAllStopColors(heatmapData);
                    
                    SwingUtilities.invokeLater(() -> {
                        mapDisplay.applyTravelTimeHeatmap(stopColors);
                        showSuccessMessage(mapDisplay, heatmapData);
                    });
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                    } catch (Exception ex) {
                        showErrorMessage(mapDisplay, "Error: " + ex.getMessage());
                        ex.printStackTrace();
                    } finally {
                        resetButton(button);
                    }
                }
            };
            initializationWorker.execute();

        } catch (NumberFormatException ex) {
            showErrorMessage(mapDisplay, "Invalid coordinate format. Please enter numbers only.");
        } catch (Exception ex) {
            showErrorMessage(mapDisplay, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static CoordinateInput parseCoordinateInput(JTextField startCoordinateField, MapDisplay mapDisplay) {
        String coordinateText = startCoordinateField.getText();
        
        if (coordinateText.equals("Start (lat,lon)") || coordinateText.trim().isEmpty()) {
            showErrorMessage(mapDisplay, "Please select a start point on the map or enter coordinates");
            return null;
        }

        String[] coordinates = coordinateText.split(",");
        if (coordinates.length != 2) {
            showErrorMessage(mapDisplay, "Invalid coordinate format. Use: lat,lon");
            return null;
        }

        double latitude = Double.parseDouble(coordinates[0].trim());
        double longitude = Double.parseDouble(coordinates[1].trim());
        return new CoordinateInput(latitude, longitude);
    }

    private static TravelTimeHeatmapAPI initializeHeatmapSystem() throws Exception {
        DijkstraRouter dijkstraRouter = GTFSDatabaseParser.createRouterFromGTFS(500);
        Router router = new Router(new DijkstraRoutePlanner(dijkstraRouter));
        return new TravelTimeHeatmapAPI(router);
    }

    private static void executeHeatmapGeneration(MapDisplay mapDisplay, TravelTimeHeatmapAPI heatmapAPI, 
                                               JButton button, String nearestStopId) {
        button.setText("Generating...");
        button.setEnabled(false);

        SwingWorker<HeatmapData, Void> generationWorker = new SwingWorker<HeatmapData, Void>() {
            @Override
            protected HeatmapData doInBackground() throws Exception {
                return heatmapAPI.generateHeatmap(nearestStopId);
            }

            @Override
            protected void done() {
                try {
                    HeatmapData heatmapData = get();
                    Map<String, Color> stopColors = heatmapAPI.getAllStopColors(heatmapData);
                    mapDisplay.applyTravelTimeHeatmap(stopColors);
                    showSuccessMessage(mapDisplay, heatmapData);
                } catch (Exception ex) {
                    showErrorMessage(mapDisplay, "Error generating heatmap: " + ex.getMessage());
                    ex.printStackTrace();
                } finally {
                    resetButton(button);
                }
            }
        };
        generationWorker.execute();
    }

    private static void showSuccessMessage(MapDisplay mapDisplay, HeatmapData heatmapData) {
        JOptionPane.showMessageDialog(mapDisplay,
            String.format("Heatmap generated! Travel times from %.1f to %.1f minutes",
                heatmapData.getMinTime(), heatmapData.getMaxTime()));
    }

    private static void showErrorMessage(MapDisplay mapDisplay, String message) {
        JOptionPane.showMessageDialog(mapDisplay, message);
    }

    private static void resetButton(JButton button) {
        button.setText("Generate Heatmap");
        button.setEnabled(true);
    }

    private static String findNearestStopId(double latitude, double longitude) {
        Map<String, AdiStop> allStops = StopsCache.getAllStops();
        String nearestStopId = null;
        double minimumDistance = Double.MAX_VALUE;
        
        for (AdiStop stop : allStops.values()) {
            Coordinates stopCoordinates = stop.getCoordinates();
            double distance = calculateDistance(latitude, longitude,
                                              stopCoordinates.getLatitude(),
                                              stopCoordinates.getLongitude());
            if (distance < minimumDistance) {
                minimumDistance = distance;
                nearestStopId = stop.getStopID();
            }
        }
        return nearestStopId;
    }

    private static double calculateDistance(double latitude1, double longitude1, double latitude2, double longitude2) {
        double earthRadiusKm = 6371;
        double latitudeDifference = Math.toRadians(latitude2 - latitude1);
        double longitudeDifference = Math.toRadians(longitude2 - longitude1);
        
        double a = Math.sin(latitudeDifference / 2) * Math.sin(latitudeDifference / 2) +
                   Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2)) *
                   Math.sin(longitudeDifference / 2) * Math.sin(longitudeDifference / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    public static JFrame createMainWindow(JPanel contentPanel) {
        JFrame mainWindow = new JFrame("Offline Map Viewer");
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindow.setContentPane(contentPanel);
        mainWindow.pack();
        mainWindow.setLocationRelativeTo(null);
        return mainWindow;
    }

    private static class CoordinateInput {
        final double latitude;
        final double longitude;

        CoordinateInput(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}