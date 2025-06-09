package gui.components;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import closureAnalysis.StopEvaluator;
import gui.MapUI;
import gui.data.BusStopDataLoader;
import gui.data.GeographicBounds;
import gui.data.LocationPoint;
import gui.interaction.NLCHandler;
import gui.transform.CoordinateConverter;
import heatmap.HeatmapData;
import heatmap.StopsCache;
import heatmap.TravelTimeHeatmapAPI;
import javax.swing.Box;
import routing.api.Router;
import routing.routingEngineDijkstra.adiModels.Stop.AdiStop;
import routing.routingEngineDijkstra.api.DijkstraRoutePlanner;

import routing.routingEngineDijkstra.dijkstra.parsers.GTFSDatabaseParser;

public class UserInterfaceBuilder {
    public static JPanel createControlPanel(
            JTextField startField,
            JTextField endField,
            MapDisplay mapDisplay,
            TravelTimeHeatmapAPI heatmapAPI
    ) {
        JButton heatmapButton = heatmapAPI != null
                ? createHeatmapButton(startField, mapDisplay, heatmapAPI)
                : createLazyHeatmapButton(startField, mapDisplay);
        JButton zoomIn = createZoomButton("+", 1.5, mapDisplay);
        JButton zoomOut = createZoomButton("-", 0.9, mapDisplay);
        JButton evaluateStops = createStopEvaluatorButton(mapDisplay);
        JTextField stopIdField = null;
        JButton nlcButton = null;
        JButton clearButton = null;
        if (heatmapAPI == null) {
            stopIdField = NLCHandler.createStopIdField();
            nlcButton = NLCHandler.createNLCButton(stopIdField, mapDisplay);
            clearButton = NLCHandler.createClearButton(stopIdField, mapDisplay);
        }
        return buildControlPanel(
                startField,
                endField,
                zoomIn,
                zoomOut,
                heatmapButton,
                stopIdField,
                nlcButton,
                clearButton,
                evaluateStops,
                mapDisplay
        );
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

    private static JButton createZoomButton(
            String text,
            double factor,
            MapDisplay mapDisplay
    ) {
        JButton button = new JButton(text);
        button.addActionListener(e -> mapDisplay.adjustZoom(factor));
        return button;
    }

    private static JPanel buildControlPanel(
            JTextField startField,
            JTextField endField,
            JButton zoomIn,
            JButton zoomOut,
            JButton actionButton,
            JTextField stopIdField,
            JButton nlcButton,
            JButton clearButton,
            JButton evaluateStops,
            MapDisplay mapDisplay
    ) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Start:")); panel.add(startField);
        panel.add(new JLabel("End:")); panel.add(endField);
        panel.add(zoomIn); panel.add(zoomOut);
        panel.add(actionButton); panel.add(evaluateStops);
        if (stopIdField != null) {
            panel.add(Box.createHorizontalStrut(20));
            panel.add(new JLabel("Out of Service:"));
            panel.add(stopIdField);
            panel.add(nlcButton); panel.add(clearButton);
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
        if (ci == null) return;
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
                            String.format("Heatmap from %.1f to %.1f minutes", data.getMinTime(), data.getMaxTime()));
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