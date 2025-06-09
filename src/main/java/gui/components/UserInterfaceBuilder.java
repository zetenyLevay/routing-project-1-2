package gui.components;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import closureAnalysis.StopEvaluator;
import gui.interaction.NLCHandler;
import heatmap.HeatmapData;
import heatmap.StopsCache;
import heatmap.TravelTimeHeatmapAPI;
import javax.swing.Box;
import routing.api.Router;
import routing.routingEngineDijkstra.adiModels.Stop.AdiStop;
import routing.routingEngineDijkstra.api.DijkstraRoutePlanner;

import routing.routingEngineDijkstra.dijkstra.parsers.GTFSDatabaseParser;

public class UserInterfaceBuilder {

    public static JPanel createControlPanel(JTextField startCoordinateField, JTextField endCoordinateField,
                                            MapDisplay mapDisplay, TravelTimeHeatmapAPI heatmapAPI) {
        JButton generateHeatmapButton = createHeatmapButton(startCoordinateField, mapDisplay, heatmapAPI);
        JButton evaluateStopsButton = createStopEvaluatorButton(mapDisplay);
        JButton zoomInButton = createZoomButton("+", 1.5, mapDisplay);
        JButton zoomOutButton = createZoomButton("-", 0.9, mapDisplay);
        return buildControlPanel(startCoordinateField, endCoordinateField,
                zoomInButton, zoomOutButton, generateHeatmapButton,
                null, null, null, evaluateStopsButton, mapDisplay);
    }

    public static JPanel createControlPanelWithoutHeatmap(JTextField startCoordinateField, JTextField endCoordinateField,
                                                          MapDisplay mapDisplay) {
        JButton heatmapButton = createLazyHeatmapButton(startCoordinateField, mapDisplay);
        JButton evaluateStopsButton = createStopEvaluatorButton(mapDisplay);
        JButton zoomInButton = createZoomButton("+", 1.5, mapDisplay);
        JButton zoomOutButton = createZoomButton("-", 0.9, mapDisplay);
        JTextField stopIdField = NLCHandler.createStopIdField();
        JButton nlcButton = NLCHandler.createNLCButton(stopIdField, mapDisplay);
        JButton clearButton = NLCHandler.createClearButton(stopIdField, mapDisplay);
        return buildControlPanel(startCoordinateField, endCoordinateField,
                zoomInButton, zoomOutButton, heatmapButton,
                stopIdField, nlcButton, clearButton, evaluateStopsButton, mapDisplay);
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

    private static JButton createStopEvaluatorButton(MapDisplay mapDisplay) {
        JButton button = new JButton("Evaluate Stops");
        button.addActionListener(e -> runStopEvaluator(mapDisplay, button));
        return button;
    }

    private static JButton createZoomButton(String text, double zoomFactor, MapDisplay mapDisplay) {
        JButton button = new JButton(text);
        button.addActionListener(e -> mapDisplay.adjustZoom(zoomFactor));
        return button;
    }

    private static JPanel buildControlPanel(JTextField startField, JTextField endField,
                                            JButton zoomInButton, JButton zoomOutButton, JButton actionButton,
                                            JTextField stopIdField, JButton nlcButton, JButton clearButton,
                                            JButton evaluateStopsButton, MapDisplay mapDisplay) {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(new JLabel("Start:")); controlPanel.add(startField);
        controlPanel.add(new JLabel("End:"));   controlPanel.add(endField);
        controlPanel.add(zoomInButton);         controlPanel.add(zoomOutButton);
        controlPanel.add(actionButton);         controlPanel.add(evaluateStopsButton);
        if (stopIdField != null) {
            controlPanel.add(Box.createHorizontalStrut(20));
            controlPanel.add(new JLabel("Out of Service:"));
            controlPanel.add(stopIdField);
            controlPanel.add(nlcButton);
            controlPanel.add(clearButton);
        }
        return controlPanel;
    }

    private static void runStopEvaluator(MapDisplay mapDisplay, JButton button) {
        button.setText("Evaluating...");
        button.setEnabled(false);
        SwingWorker<Map<String, Color>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, Color> doInBackground() {
                StopEvaluator evaluator = new StopEvaluator();
                Map<String, Double> rawScores = evaluator.doEverything();
                double min = rawScores.values().stream().mapToDouble(d -> d).min().orElse(0);
                double max = rawScores.values().stream().mapToDouble(d -> d).max().orElse(1);
                return rawScores.entrySet().stream().collect(Collectors.toMap(
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
        };
        worker.execute();
    }

    private static Color interpolateRedGreen(double t) {
        t = Math.max(0, Math.min(1, t));
        int r = (int)((1 - t) * 255);
        int g = (int)(t * 255);
        return new Color(r, g, 0);
    }

    private static void generateHeatmap(JTextField startCoordinateField, MapDisplay mapDisplay,
                                        TravelTimeHeatmapAPI heatmapAPI, JButton button) {
        CoordinateInput ci = parseCoordinateInput(startCoordinateField, mapDisplay);
        if (ci == null) return;
        String id = findNearestStopId(ci.latitude, ci.longitude);
        if (id == null) {
            JOptionPane.showMessageDialog(mapDisplay, "No nearby bus stop found", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        executeHeatmapGeneration(mapDisplay, heatmapAPI, button, id);
    }

    private static void generateHeatmapWithInitialization(JTextField startCoordinateField, MapDisplay mapDisplay,
                                                          JButton button) {
        CoordinateInput ci = parseCoordinateInput(startCoordinateField, mapDisplay);
        if (ci == null) return;
        button.setText("Initializing...");
        button.setEnabled(false);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                TravelTimeHeatmapAPI api = initializeHeatmapSystem();
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
                button.setText("Generate Heatmap");
                button.setEnabled(true);
            }
        }.execute();
    }

    private static CoordinateInput parseCoordinateInput(JTextField f, MapDisplay m) {
        String t = f.getText();
        if (t.trim().isEmpty() || t.contains("lat")) {
            JOptionPane.showMessageDialog(m, "Enter coordinates", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        String[] p = t.split(",");
        if (p.length != 2) {
            JOptionPane.showMessageDialog(m, "Format: lat,lon", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return new CoordinateInput(Double.parseDouble(p[0].trim()), Double.parseDouble(p[1].trim()));
    }

    private static String findNearestStopId(double lat, double lon) {
        return StopsCache.getAllStops().values().stream().min(
            Comparator.comparingDouble(s -> calculateDistance(lat, lon,
                s.getCoordinates().getLatitude(), s.getCoordinates().getLongitude()))
        ).map(AdiStop::getStopID).orElse(null);
    }

    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon/2)*Math.sin(dLon/2);
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private static void executeHeatmapGeneration(MapDisplay d, TravelTimeHeatmapAPI api,
                                                 JButton b, String id) {
        b.setText("Generating...");
        b.setEnabled(false);
        new SwingWorker<HeatmapData, Void>() {
            @Override
            protected HeatmapData doInBackground() throws SQLException {
                return api.generateHeatmap(id);
            }
            @Override
            protected void done() {
                try {
                    HeatmapData data = get();
                    d.applyTravelTimeHeatmap(api.getAllStopColors(data));
                    JOptionPane.showMessageDialog(d,
                        String.format("Heatmap from %.1f to %.1f minutes", data.getMinTime(), data.getMaxTime()));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(d, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    b.setText("Generate Heatmap");
                    b.setEnabled(true);
                }
            }
        }.execute();
    }

    private static TravelTimeHeatmapAPI initializeHeatmapSystem() throws SQLException, IOException {
        return new TravelTimeHeatmapAPI(new Router(new DijkstraRoutePlanner(
            GTFSDatabaseParser.createRouterFromGTFS(500)
        )));
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