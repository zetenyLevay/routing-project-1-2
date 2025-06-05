package gui.components;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import closureAnalysis.data.models.Stop;
import heatmap.HeatmapData;
import heatmap.StopsCache;
import heatmap.TravelTimeHeatmapAPI;
import routing.routingEngineModels.Coordinates;


public class UserInterfaceBuilder {
    public static JPanel createControlPanel(JTextField startField, JTextField endField,
                                            MapDisplay mapDisplay, TravelTimeHeatmapAPI heatmapAPI) {
        JButton generateHeatmapButton = new JButton("Generate Heatmap");
        generateHeatmapButton.addActionListener(e -> {
            try {
                String startCoords = startField.getText();
                if (startCoords.equals("Start (lat,lon)") || startCoords.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(mapDisplay,
                        "Please select a start point on the map or enter coordinates");
                    return;
                }
                String[] coords = startCoords.split(",");
                if (coords.length != 2) {
                    JOptionPane.showMessageDialog(mapDisplay,
                        "Invalid coordinate format. Use: lat,lon");
                    return;
                }
                double lat = Double.parseDouble(coords[0].trim());
                double lon = Double.parseDouble(coords[1].trim());
                String nearestStopId = findNearestStopId(lat, lon);
                if (nearestStopId == null) {
                    JOptionPane.showMessageDialog(mapDisplay,
                        "No nearby bus stop found for the selected coordinates");
                    return;
                }
                generateHeatmapButton.setText("Generating...");
                generateHeatmapButton.setEnabled(false);
                SwingWorker<HeatmapData, Void> worker = new SwingWorker<HeatmapData, Void>() {
                    @Override
                    protected HeatmapData doInBackground() throws Exception {
                        return heatmapAPI.generateHeatmap(nearestStopId);
                    }
                    @Override
                    protected void done() {
                        try {
                            HeatmapData heatmap = get();
                            Map<String, Color> stopColors = heatmapAPI.getAllStopColors(heatmap);
                            mapDisplay.applyTravelTimeHeatmap(stopColors);
                            JOptionPane.showMessageDialog(mapDisplay,
                                String.format("Heatmap generated! Travel times from %.1f to %.1f minutes",
                                    heatmap.getMinTime(), heatmap.getMaxTime()));
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(mapDisplay,
                                "Error generating heatmap: " + ex.getMessage());
                            ex.printStackTrace();
                        } finally {
                            generateHeatmapButton.setText("Generate Heatmap");
                            generateHeatmapButton.setEnabled(true);
                        }
                    }
                };
                worker.execute();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(mapDisplay,
                    "Invalid coordinate format. Please enter numbers only.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mapDisplay,
                    "Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        JButton zoomInButton = new JButton("+");
        JButton zoomOutButton = new JButton("-");
        zoomInButton.addActionListener(e -> mapDisplay.adjustZoom(1.5));
        zoomOutButton.addActionListener(e -> mapDisplay.adjustZoom(0.9));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(new JLabel("Start:"));
        controlPanel.add(startField);
        controlPanel.add(new JLabel("End:"));
        controlPanel.add(endField);
        controlPanel.add(zoomInButton);
        controlPanel.add(zoomOutButton);
        controlPanel.add(generateHeatmapButton);

        return controlPanel;
    }

    public static JPanel createControlPanelWithoutHeatmap(JTextField startField, JTextField endField,
                                                          MapDisplay mapDisplay) {
        JButton zoomInButton = new JButton("+");
        JButton zoomOutButton = new JButton("-");
        zoomInButton.addActionListener(e -> mapDisplay.adjustZoom(1.5));
        zoomOutButton.addActionListener(e -> mapDisplay.adjustZoom(0.9));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(new JLabel("Start:"));
        controlPanel.add(startField);
        controlPanel.add(new JLabel("End:"));
        controlPanel.add(endField);
        controlPanel.add(zoomInButton);
        controlPanel.add(zoomOutButton);

        JButton disabledHeatmapButton = new JButton("Travel Heatmap (Unavailable)");
        disabledHeatmapButton.setEnabled(false);
        disabledHeatmapButton.setToolTipText("Travel-time heatmap system failed to initialize");
        controlPanel.add(disabledHeatmapButton);

        return controlPanel;
    }

    public static JFrame createMainWindow(JPanel contentPanel) {
        JFrame mainWindow = new JFrame("Offline Map Viewer");
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindow.setContentPane(contentPanel);
        mainWindow.pack();
        mainWindow.setLocationRelativeTo(null);
        return mainWindow;
    }

    private static String findNearestStopId(double lat, double lon) {
        Map<String, routing.routingEngineModels.Stop.Stop> allStops = StopsCache.getAllStops();
        String nearestStopId = null;
        double minDistance = Double.MAX_VALUE;
        for (routing.routingEngineModels.Stop.Stop stop : allStops.values()) {
            Coordinates stopCoords = stop.getCoordinates();
            double distance = calculateDistance(lat, lon,
                                                stopCoords.getLatitude(),
                                                stopCoords.getLongitude());
            if (distance < minDistance) {
                minDistance = distance;
                nearestStopId = stop.getStopID();
            }
        }
        return nearestStopId;
    }

    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
}