package gui.interaction;

import javax.swing.*;

import gui.data.LocationPoint;

import java.awt.*;

public class CoordinateSelectionManager {    //picks start and end point when map is clicked
    private final JTextField startCoordinateField;
    private final JTextField endCoordinateField;
    private boolean isSelectingStartPoint = true;

    public CoordinateSelectionManager(JTextField startField, JTextField endField) {
        this.startCoordinateField = startField;
        this.endCoordinateField = endField;
    }

    public void selectCoordinate(LocationPoint location) {
        String coordinateString = String.format("%.6f,%.6f", 
            location.getLatitude(), location.getLongitude());

        if (isSelectingStartPoint) {
            startCoordinateField.setText(coordinateString);
            highlightField(startCoordinateField, endCoordinateField);
        } else {
            endCoordinateField.setText(coordinateString);
            highlightField(endCoordinateField, startCoordinateField);
        }

        isSelectingStartPoint = !isSelectingStartPoint;
    }

    private void highlightField(JTextField activeField, JTextField inactiveField) {
        activeField.setBackground(Color.LIGHT_GRAY);
        inactiveField.setBackground(Color.WHITE);

        Timer highlightTimer = new Timer(300, e -> {
            activeField.setBackground(Color.WHITE);
            inactiveField.setBackground(Color.WHITE);
        });
        highlightTimer.setRepeats(false);
        highlightTimer.start();
    }

    public boolean isSelectingStartPoint() {
        return isSelectingStartPoint;
    }
}
