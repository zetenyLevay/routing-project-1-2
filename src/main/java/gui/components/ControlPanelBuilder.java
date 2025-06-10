package gui.components;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import gui.MapLine;
import heatmap.TravelTimeHeatmapAPI;
import routing.db.DBConnectionManager;
import routing.routingEngineAstar.RoutingEngineAstar;
import routing.routingEngineModels.RouteStep;

/**
 * ControlPanelBuilder.java
 *
 * This class builds the control panel for the GUI, including buttons for
 * generating heatmaps, evaluating stops, zooming in and out, and showing
 * routes.
 */
public class ControlPanelBuilder {

    static UserInterfaceBuilder userInterfaceBuilder;
    private static final String DB_URL = "jdbc:sqlite:budapest_gtfs.db";
    private final static DBConnectionManager dbManager = new DBConnectionManager(DB_URL);
    private static final RoutingEngineAstar routingEngine = new RoutingEngineAstar(dbManager);

    /**
     * Initializes the UserInterfaceBuilder instance.
     */
    static JButton createHeatmapButton(
            JTextField startField,
            MapDisplay mapDisplay,
            TravelTimeHeatmapAPI heatmapAPI
    ) {
        JButton button = new JButton("Generate Heatmap");
        button.addActionListener(e -> userInterfaceBuilder.generateHeatmap(startField, mapDisplay, heatmapAPI, button));
        return button;
    }

    /**
     * Creates a button that generates a heatmap with lazy initialization. This
     * means the heatmap is generated only when the button is clicked.
     *
     * @param startField The text field containing the start coordinates.
     * @param mapDisplay The map display where the heatmap will be shown.
     * @return A JButton configured to generate a heatmap on click.
     */
    static JButton createLazyHeatmapButton(
            JTextField startField,
            MapDisplay mapDisplay
    ) {
        JButton button = new JButton("Generate Heatmap");
        button.addActionListener(e -> userInterfaceBuilder.generateHeatmapWithInitialization(startField, mapDisplay, button));
        return button;
    }

    /**
     * Creates a button that evaluates stops in the map display.
     *
     * @param mapDisplay The map display where the stops will be evaluated.
     * @return A JButton configured to evaluate stops on click.
     */
    static JButton createStopEvaluatorButton(MapDisplay mapDisplay) {
        JButton button = new JButton("Evaluate Stops");
        button.addActionListener(e -> userInterfaceBuilder.runStopEvaluator(mapDisplay, button));
        return button;
    }

    /**
     * Creates a button that evaluates stops in the map display with lazy initialization.
     *
     * @param mapDisplay The map display where the stops will be evaluated.
     * @return A JButton configured to evaluate stops on click.
     */
    static JButton createZoomButton(
            String text,
            double factor,
            MapDisplay mapDisplay
    ) {
        JButton button = new JButton(text);
        button.addActionListener(e -> mapDisplay.adjustZoom(factor));
        return button;
    }

    /**
     * Creates a button that shows the route between two coordinates.
     *
     * @param startField The text field containing the start coordinates.
     * @param endField The text field containing the end coordinates.
     * @param timeField The text field containing the time for the route.
     * @param mapDisplay The map display where the route will be shown.
     * @return A JButton configured to show the route on click.
     */
    static JButton createShowRouteButton(JTextField startField, JTextField endField, JTextField timeField, MapDisplay mapDisplay) {
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

                // Get the time from the time field and convert to hh:mm:ss format
                String timeInput = timeField.getText().trim();
                String startTime;
                if (timeInput.isEmpty()) {
                    String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    startTime = currentTime;
                } else {
                    // Convert hh:mm to hh:mm:ss
                    if (timeInput.matches("\\d{2}:\\d{2}")) {
                        startTime = timeInput + ":00";
                    } else {
                        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                        startTime = currentTime;
                    }
                }

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

    /**
     * Builds the control panel with input fields, buttons, and map controls.
     *
     * @param startField The text field for the start coordinates.
     * @param endField The text field for the end coordinates.
     * @param timeField The text field for the time.
     * @param zoomIn The button to zoom in on the map.
     * @param zoomOut The button to zoom out on the map.
     * @param actionButton The button for additional actions (e.g., generating heatmap).
     * @param stopIdField The text field for stop ID (optional).
     * @param nlcButton The button to analyze stop impact (optional).
     * @param clearButton The button to clear the NLC analysis (optional).
     * @param evaluateStops The button to evaluate stops.
     * @param showRouteButton The button to show the route.
     * @param mapDisplay The map display component.
     * @return A JPanel containing the control panel components.
     */
    static JPanel buildControlPanel(JTextField startField, JTextField endField, JTextField timeField,
            JButton zoomIn, JButton zoomOut, JButton actionButton,
            JTextField stopIdField, JButton nlcButton, JButton clearButton,
            JButton evaluateStops, JButton showRouteButton, MapDisplay mapDisplay) {

        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Top panel for input fields, route planning, and map controls
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Start:"));
        topPanel.add(startField);
        topPanel.add(new JLabel("End:"));
        topPanel.add(endField);
        topPanel.add(new JLabel("Time:"));
        topPanel.add(timeField);
        topPanel.add(showRouteButton);
        topPanel.add(Box.createHorizontalStrut(20)); // Add some spacing
        topPanel.add(zoomIn);
        topPanel.add(zoomOut);

        // Bottom panel for analysis tools
        JPanel analysisPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        analysisPanel.add(actionButton);
        analysisPanel.add(evaluateStops);

        // Add NLC controls if they exist
        if (stopIdField != null) {
            analysisPanel.add(Box.createHorizontalStrut(20));
            analysisPanel.add(new JLabel("Out of Service:"));
            analysisPanel.add(stopIdField);
            analysisPanel.add(nlcButton);
            analysisPanel.add(clearButton);
        }

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(analysisPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

}
