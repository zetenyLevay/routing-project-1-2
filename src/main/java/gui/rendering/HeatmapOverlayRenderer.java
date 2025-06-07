package gui.rendering;

import java.awt.*;
import java.util.List;
import java.util.Map;

import gui.data.LocationPoint;
import gui.transform.MapViewTransform;

public class HeatmapOverlayRenderer {
    private final MapViewTransform viewTransform;
    private final List<LocationPoint> busStopPoints;
    private final Map<String, Color> stopColors;

    public HeatmapOverlayRenderer(MapViewTransform viewTransform,
                                  List<LocationPoint> allStops,
                                  Map<String, Color> stopColors) {
        this.viewTransform = viewTransform;
        this.busStopPoints = allStops;
        this.stopColors = stopColors;
    }

    public void paint(Graphics2D g2d, int panelWidth, int panelHeight) {
        enableAntialiasing(g2d);
        drawAllStops(g2d, panelWidth, panelHeight);
        drawLegend(g2d, panelWidth, panelHeight);
    }

    private void enableAntialiasing(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    private void drawAllStops(Graphics2D g2d, int panelWidth, int panelHeight) {
        for (LocationPoint busStop : busStopPoints) {
            String stopId = busStop.getStopId();
            Color color = stopColors.get(stopId);
            if (color != null) {
                Point screenPos = viewTransform.geoToScreen(busStop.getLatitude(), busStop.getLongitude());
                if (screenPos != null && isPointOnScreen(screenPos, panelWidth, panelHeight)) {
                    drawColoredStop(g2d, screenPos, color);
                }
            }
        }
    }

    private boolean isPointOnScreen(Point point, int width, int height) {
        return point.x >= 0 && point.x < width && point.y >= 0 && point.y < height;
    }

    private void drawColoredStop(Graphics2D g2d, Point screenPos, Color color) {
        int radius = 8;
        int diameter = radius * 2;
        g2d.setColor(color);
        g2d.fillOval(screenPos.x - radius, screenPos.y - radius, diameter, diameter);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawOval(screenPos.x - radius, screenPos.y - radius, diameter, diameter);
    }

    private void drawLegend(Graphics2D g2d, int panelWidth, int panelHeight) {
        int width = 140;
        int height = 80;
        int x = panelWidth - 150;
        int y = 20;
        drawLegendBackground(g2d, x, y, width, height);
        drawLegendTitle(g2d, x, y);
        drawLegendItems(g2d, x, y);
    }

    private void drawLegendBackground(Graphics2D g2d, int x, int y, int width, int height) {
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fillRect(x - 5, y - 5, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x - 5, y - 5, width, height);
    }

    private void drawLegendTitle(Graphics2D g2d, int x, int y) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Travel Time", x, y + 15);
    }

    private void drawLegendItems(Graphics2D g2d, int x, int y) {
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        drawLegendItem(g2d, x, y + 25, Color.GREEN, "Short");
        drawLegendItem(g2d, x, y + 45, Color.YELLOW, "Medium");
        drawLegendItem(g2d, x, y + 65, Color.RED, "Long");
    }

    private void drawLegendItem(Graphics2D g2d, int x, int y, Color color, String label) {
        g2d.setColor(color);
        g2d.fillOval(x, y, 12, 12);
        g2d.setColor(Color.BLACK);
        g2d.drawString(label, x + 20, y + 10);
    }
}
