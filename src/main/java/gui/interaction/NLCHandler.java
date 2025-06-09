package gui.interaction;

import java.awt.Color;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import gui.MapUI;
import gui.components.MapDisplay;
import nlc.NLCHeatmapAPI;
import nlc.NLCHeatmapData;
import routing.api.Router;

public class NLCHandler {
    public static JTextField createStopIdField() {
        return new JTextField("(stop ID)", 15);
    }

    public static JButton createNLCButton(JTextField stopIdField, MapDisplay mapDisplay) {
        JButton button = new JButton("Show Impact");
        button.addActionListener(e -> generateNLCHeatmap(stopIdField, mapDisplay, button));
        return button;
    }

    public static JButton createClearButton(JTextField stopIdField, MapDisplay mapDisplay) {
        JButton clearButton = new JButton("Clear Everything");
        clearButton.addActionListener(e -> {
            mapDisplay.clearTravelTimeHeatmap();
            stopIdField.setText("(stop ID)");
        });
        return clearButton;
    }

    private static void generateNLCHeatmap(JTextField stopIdField, MapDisplay mapDisplay, JButton button) {
        try {
            String stopId = stopIdField.getText().trim();
            if (stopId.equals("(stop ID)") || stopId.isEmpty()) {
                showErrorMessage(mapDisplay, "Please enter a stop ID");
                return;
            }
            button.setText("Analyzing...");
            button.setEnabled(false);
            new SwingWorker<NLCHeatmapData, Void>() {
                @Override
                protected NLCHeatmapData doInBackground() throws Exception {
                    NLCHeatmapAPI nlcAPI = new NLCHeatmapAPI();
                    return nlcAPI.generateHeatmap(stopId);
                }
                @Override
                protected void done() {
                    try {
                        NLCHeatmapData heatmapData = get();
                        NLCHeatmapAPI nlcAPI = new NLCHeatmapAPI();
                        Map<String, Color> stopColors = nlcAPI.getAllStopColors(heatmapData);
                        Map<String, Integer> nlcValues = nlcAPI.getAllNLCValues(heatmapData);
                        mapDisplay.applyTravelTimeHeatmap(stopColors);
                        showNLCSuccessMessage(mapDisplay, heatmapData, nlcValues);
                    } catch (Exception ex) {
                        String msg = ex.getMessage();
                        if (msg != null && msg.contains("Stop not found")) {
                            showErrorMessage(mapDisplay, "Stop ID not found: " + stopIdField.getText().trim());
                        } else {
                            showErrorMessage(mapDisplay, "Error analyzing stop impact: " + msg);
                        }
                        ex.printStackTrace();
                    } finally {
                        button.setText("Show Impact");
                        button.setEnabled(true);
                    }
                }
            }.execute();
        } catch (Exception ex) {
            showErrorMessage(mapDisplay, "Error: " + ex.getMessage());
            button.setText("Show Impact");
            button.setEnabled(true);
            ex.printStackTrace();
        }
    }

    private static void showNLCSuccessMessage(MapDisplay mapDisplay, NLCHeatmapData heatmapData, Map<String, Integer> nlcValues) {
        int maxImpact = nlcValues.values().stream().mapToInt(i -> i).max().orElse(0);
        int affectedStops = (int) nlcValues.values().stream().filter(v -> v > 0).count();
        String message = String.format(
            "Impact Analysis Complete!\n" +
            "Closed Stop: %s\n" +
            "Affected Stops: %d\n" +
            "Maximum Impact: %d neighbors lost\n\n" +
            "Red areas show higher impact from closure.",
            heatmapData.getClosedStop().getStopID(),
            affectedStops,
            maxImpact
        );
        JOptionPane.showMessageDialog(mapDisplay, message, "Stop Closure Impact", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void showErrorMessage(MapDisplay mapDisplay, String message) {
        JOptionPane.showMessageDialog(mapDisplay, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}