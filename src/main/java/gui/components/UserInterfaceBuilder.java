package gui.components;

import java.awt.Color;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import closureAnalysis.StopEvaluator;
import gui.MapLine;
import gui.MapUI;
import gui.interaction.NLCHandler;
import heatmap.HeatmapData;
import heatmap.StopsCache;
import heatmap.TravelTimeHeatmapAPI;
import routing.api.Router;
import routing.db.DBConnectionManager;
import routing.routingEngineAstar.RoutingEngineAstar;
import routing.routingEngineDijkstra.adiModels.Stop.AdiStop;
import routing.routingEngineModels.RouteStep;

public class UserInterfaceBuilder {

    private static final String DB_URL = "jdbc:sqlite:budapest_gtfs.db";
    private final static DBConnectionManager dbManager = new DBConnectionManager(DB_URL);
    private static final RoutingEngineAstar routingEngine = new RoutingEngineAstar(dbManager);
    private static JTextField startField;
    private static JTextField endField;

    public static JPanel createControlPanel(JTextField startField, JTextField endField, MapDisplay mapDisplay, TravelTimeHeatmapAPI heatmapAPI) {
        JButton heatmapButton = heatmapAPI != null
                ? createHeatmapButton(startField, mapDisplay, heatmapAPI)
                : createLazyHeatmapButton(startField, mapDisplay);
        JButton zoomIn = createZoomButton("+", 1.5, mapDisplay);
        JButton zoomOut = createZoomButton("-", 0.9, mapDisplay);
        JButton evaluateStops = createStopEvaluatorButton(mapDisplay);
        JButton showRouteButton = createShowRouteButton(startField, endField, mapDisplay);
        JTextField stopIdField = null;
        JButton nlcButton = null;
        JButton clearButton = null;
        if (heatmapAPI == null) {
            stopIdField = NLCHandler.createStopIdField();
            nlcButton = NLCHandler.createNLCButton(stopIdField, mapDisplay);
            clearButton = NLCHandler.createClearButton(stopIdField, mapDisplay);
        }
        return buildControlPanel(startField, endField, zoomIn, zoomOut, heatmapButton, stopIdField, nlcButton, clearButton, evaluateStops, showRouteButton, mapDisplay);
    }

    private static JButton createHeatmapButton(
            JTextField startField,
            MapDisplay mapDisplay,
            TravelTimeHeatmapAPI heatmapAPI
    ) {
        JButton button = new JButton("Generate Heatmap");
        button.addActionListener(e -> generateHeatmap(startField, mapDisplay, heatmapAPI, button));
        return button;
    }

    private static JButton createLazyHeatmapButton(
            JTextField startField,
            MapDisplay mapDisplay
    ) {
        JButton button = new JButton("Generate Heatmap");
        button.addActionListener(e -> generateHeatmapWithSharedRouter(startField, mapDisplay, button));
        return button;
    }

    private static JButton createStopEvaluatorButton(MapDisplay mapDisplay) {
        JButton button = new JButton("Evaluate Stops");
        button.addActionListener(e -> runStopEvaluator(mapDisplay, button));
        return button;
    }

    private static JButton createShowRouteButton(JTextField startField, JTextField endField, MapDisplay mapDisplay) {
        JButton button = new JButton("Show Route");
        button.addActionListener(e -> {
            try {
                String startText = startField.getText();
                String endText = endField.getText();

                if (startText.trim().isEmpty() || endText.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(mapDisplay,
                            "Please enter both start and end coordinates",
                            "Input Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Parse coordinates
                String[] startParts = startText.split(",");
                String[] endParts = endText.split(",");

                if (startParts.length != 2 || endParts.length != 2) {
                    JOptionPane.showMessageDialog(mapDisplay,
                            "Coordinates must be in format: lat,lon",
                            "Format Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double sourceLat = Double.parseDouble(startParts[0].trim());
                double sourceLon = Double.parseDouble(startParts[1].trim());
                double targetLat = Double.parseDouble(endParts[0].trim());
                double targetLon = Double.parseDouble(endParts[1].trim());

                String startTime = "08:00:00"; //TODO: change in a but
                System.out.println("Finding route from " + sourceLat + "," + sourceLon
                        + " to " + targetLat + "," + targetLon + " at " + startTime);
                List<RouteStep> route = routingEngine.findRoute(sourceLat, sourceLon, targetLat, targetLon, startTime);
                System.out.println(route);

                if (route.isEmpty()) {
                    JOptionPane.showMessageDialog(mapDisplay,
                            "No route found between the specified coordinates",
                            "Route Not Found",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                List<MapLine> mapLines = MapLine.routeToLine(route, sourceLat, sourceLon);

                mapDisplay.drawRouteLines(mapLines);
                mapDisplay.repaint(); 

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mapDisplay,
                        "Error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        return button;
    }

    private static JButton createZoomButton(
            String text,
            double factor,
            MapDisplay mapDisplay
    ) {
        JButton button = new JButton(text);
        button.addActionListener(e -> mapDisplay.adjustZoom(factor));
        return button;
    }

    private static JPanel buildControlPanel(JTextField startField, JTextField endField, JButton zoomIn, JButton zoomOut, JButton actionButton, JTextField stopIdField, JButton nlcButton, JButton clearButton, JButton evaluateStops, JButton showRouteButton, MapDisplay mapDisplay) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Start:"));
        panel.add(startField);
        panel.add(new JLabel("End:"));
        panel.add(endField);
        panel.add(zoomIn);
        panel.add(zoomOut);
        panel.add(showRouteButton);
        panel.add(actionButton);
        panel.add(evaluateStops);
        if (stopIdField != null) {
            panel.add(Box.createHorizontalStrut(20));
            panel.add(new JLabel("Out of Service:"));
            panel.add(stopIdField);
            panel.add(nlcButton);
            panel.add(clearButton);
        }
        return panel;
    }

    private static void runStopEvaluator(MapDisplay mapDisplay, JButton button) {
        button.setText("Evaluating...");
        button.setEnabled(false);
        new SwingWorker<Map<String, Color>, Void>() {
            @Override
            protected Map<String, Color> doInBackground() {
                StopEvaluator evaluator = new StopEvaluator();
                Map<String, Double> scores = evaluator.doEverything();
                double min = scores.values().stream().mapToDouble(d -> d).min().orElse(0);
                double max = scores.values().stream().mapToDouble(d -> d).max().orElse(1);
                return scores.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> interpolateRedGreen((e.getValue() - min) / (max - min))
                        ));
            }

            @Override
            protected void done() {
                try {
                    mapDisplay.applyTravelTimeHeatmap(get());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mapDisplay, "Error during stop evaluation:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                } finally {
                    button.setText("Evaluate Stops");
                    button.setEnabled(true);
                }
            }
        }.execute();
    }

    private static Color interpolateRedGreen(double t) {
        t = Math.max(0, Math.min(1, t));
        int r = (int) ((1 - t) * 255);
        int g = (int) (t * 255);
        return new Color(r, g, 0);
    }

    private static void generateHeatmap(
            JTextField startField,
            MapDisplay mapDisplay,
            TravelTimeHeatmapAPI heatmapAPI,
            JButton button
    ) {
        CoordinateInput ci = parseCoordinateInput(startField, mapDisplay);
        if (ci == null) {
            return;
        }
        String id = findNearestStopId(ci.latitude, ci.longitude);
        if (id == null) {
            JOptionPane.showMessageDialog(mapDisplay, "No nearby bus stop found", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        executeHeatmapGeneration(mapDisplay, heatmapAPI, button, id);
    }

    private static void generateHeatmapWithSharedRouter(
            JTextField startField,
            MapDisplay mapDisplay,
            JButton button
    ) {
        CoordinateInput ci = parseCoordinateInput(startField, mapDisplay);
        if (ci == null) return;
        
        button.setText("Generating...");
        button.setEnabled(false);
        
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Router sharedRouter = MapUI.getSharedRouter();
                if (sharedRouter == null) {
                    throw new Exception("Router not initialized");
                }
                
                TravelTimeHeatmapAPI api = new TravelTimeHeatmapAPI(sharedRouter);
                String id = findNearestStopId(ci.latitude, ci.longitude);
                if (id == null) throw new Exception("No nearby bus stop found");
                
                HeatmapData data = api.generateHeatmap(id);
                Map<String, Color> colors = api.getAllStopColors(data);
                
                SwingUtilities.invokeLater(() -> {
                    mapDisplay.applyTravelTimeHeatmap(colors);
                    JOptionPane.showMessageDialog(mapDisplay,
                            String.format("Heatmap from %.1f to %.1f seconds", data.getMinTime(), data.getMaxTime()));
                });
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mapDisplay, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                } finally {
                    button.setText("Generate Heatmap");
                    button.setEnabled(true);
                }
            }
        }.execute();
    }

    private static CoordinateInput parseCoordinateInput(JTextField field, MapDisplay mapDisplay) {
        String text = field.getText();
        if (text.trim().isEmpty() || text.contains("lat")) {
            JOptionPane.showMessageDialog(mapDisplay, "Enter coordinates", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        String[] parts = text.split(",");
        if (parts.length != 2) {
            JOptionPane.showMessageDialog(mapDisplay, "Format: lat,lon", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return new CoordinateInput(
                Double.parseDouble(parts[0].trim()),
                Double.parseDouble(parts[1].trim())
        );
    }

    private static String findNearestStopId(double lat, double lon) {
        return StopsCache.getAllStops().values().stream()
                .min(Comparator.comparingDouble(s -> calculateDistance(lat, lon,
                s.getCoordinates().getLatitude(), s.getCoordinates().getLongitude())))
                .map(AdiStop::getStopID)
                .orElse(null);
    }

    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private static void executeHeatmapGeneration(
            MapDisplay mapDisplay,
            TravelTimeHeatmapAPI api,
            JButton button,
            String stopId
    ) {
        button.setText("Generating...");
        button.setEnabled(false);
        new SwingWorker<HeatmapData, Void>() {
            @Override
            protected HeatmapData doInBackground() throws SQLException {
                return api.generateHeatmap(stopId);
            }

            @Override
            protected void done() {
                try {
                    HeatmapData data = get();
                    mapDisplay.applyTravelTimeHeatmap(api.getAllStopColors(data));
                    JOptionPane.showMessageDialog(mapDisplay,
                            String.format("Heatmap from %.1f to %.1f minutes", data.getMinTime(), data.getMaxTime()));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(mapDisplay, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    button.setText("Generate Heatmap");
                    button.setEnabled(true);
                }
            }
        }.execute();
    }


    public static JFrame createMainWindow(JPanel contentPanel) {
        JFrame frame = new JFrame("Offline Map Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(contentPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        return frame;
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
