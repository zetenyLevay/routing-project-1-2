package gui.components;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JTextField;

public class UserInterfaceBuilder {    //Builds control panel with button and text fields. Sets the layput
    public static JPanel createControlPanel(JTextField startField, JTextField endField, 
                                          MapDisplay mapDisplay) {
        JToggleButton heatmapToggle = new JToggleButton("Show Heatmap", true);
        heatmapToggle.addActionListener(e -> 
            mapDisplay.toggleHeatmapVisibility(heatmapToggle.isSelected()));

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
        controlPanel.add(heatmapToggle);

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
}
