package gui.interaction;

import javax.swing.*;

import gui.components.MapDisplay;
import gui.data.LocationPoint;
import gui.transform.MapViewTransform;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;


public class MapInteractionHandler extends MouseAdapter implements MouseWheelListener {
    private final MapViewTransform viewTransform;
    private final CoordinateSelectionManager selectionManager;
    private final JComponent component;
    private Point lastMousePosition;
    private boolean isDraggingMap = false;

    public MapInteractionHandler(MapViewTransform viewTransform,
                                 CoordinateSelectionManager selectionManager,
                                 JComponent component) {
        this.viewTransform = viewTransform;
        this.selectionManager = selectionManager;
        this.component = component;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent wheelEvent) {
        if (wheelEvent.getWheelRotation() < 0) {
            viewTransform.zoomAtPoint(1.1, wheelEvent.getX(), wheelEvent.getY());
        } else {
            viewTransform.zoomAtPoint(0.9, wheelEvent.getX(), wheelEvent.getY());
        }
        component.revalidate();
        component.repaint();
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        lastMousePosition = mouseEvent.getPoint();
        isDraggingMap = false;
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        Point currentMousePosition = mouseEvent.getPoint();
        double deltaX = currentMousePosition.x - lastMousePosition.x;
        double deltaY = currentMousePosition.y - lastMousePosition.y;
        viewTransform.pan(deltaX, deltaY);
        lastMousePosition = currentMousePosition;
        isDraggingMap = true;
        component.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        if (!isDraggingMap) {
            handleMapClick(mouseEvent);
        }
    }

    private void handleMapClick(MouseEvent mouseEvent) {
        MapDisplay mapDisplay = (MapDisplay) component;
        Point click = mouseEvent.getPoint();
        for (LocationPoint lp : mapDisplay.getBusStopPoints()) {
            Point screen = viewTransform.geoToScreen(lp.getLatitude(), lp.getLongitude());
            if (screen != null) {
                int dx = screen.x - click.x;
                int dy = screen.y - click.y;
                int radius = 8;
                if (dx * dx + dy * dy <= radius * radius) {
                    String name = lp.getStopName() != null ? lp.getStopName() : "Unnamed Stop";
                    String id = lp.getStopId();
                    double lat = lp.getLatitude();
                    double lon = lp.getLongitude();
                    JOptionPane.showMessageDialog(component,
                        String.format("Stop: %s%nID: %s%nLat: %.6f%nLon: %.6f", name, id, lat, lon));
                    selectionManager.selectCoordinate(lp);
                    return;
                }
            }
        }
        LocationPoint clickedLocation = viewTransform.convertPixelToLocation(click);
        if (clickedLocation != null) {
            selectionManager.selectCoordinate(clickedLocation);
        }
    }
}
