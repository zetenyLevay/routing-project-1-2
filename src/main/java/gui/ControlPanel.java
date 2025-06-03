// package gui;

// import javax.swing.*;
// import java.awt.*;

// import org.jxmapviewer.JXMapViewer;
// import eventHandlers.ZoomHandler;

// public class ControlPanel extends JPanel {
//     private static JPanel contentPanel;

//     /**
//      * Constructor for the ControlPanel
//      */
//     public ControlPanel() {
//         this.setLayout(new BorderLayout());
//         this.contentPanel = new JPanel(null);
//         this.contentPanel.setOpaque(false);
//         this.add(contentPanel, BorderLayout.CENTER);
//         this.setOpaque(false);
//     }

//     /**
//      * Adds a control panel item to the control panel.
//      * For now, we only have the SearchBar.
//      *
//      * @param item The control panel item to add.
//      * @param x    the x co-ordinate where you want to add the item
//      * @param y    the y co-ordinate where you want to add the item
//      */
//     public void addItem(ControlPanelItem item, int x, int y) {
//         JPanel itemPanel = (JPanel) item.getPanel();
//         itemPanel.setLocation(x, y);
//         contentPanel.add(itemPanel);
//     }

//     /**
//      * Returns the main panel containing all control elements
//      *
//      * @return The control panel
//      */
//     public JPanel getPanel() {
//         return this;
//     }

//     /**
//      * Factory method to create a control panel instance with elements
//      *
//      * @return The configured ControlPanel instance
//      */
//     public static ControlPanel create(JXMapViewer mapViewer) {
//         ControlPanel cp = new ControlPanel();
//         SearchBar sb = new SearchBar(300, 40);
//         cp.addItem(sb, 20, 20);

//         if (mapViewer != null) {
//             ZoomHandler zoomHandler = new ZoomHandler(mapViewer);

//             //zoom in button
//             PanelButton zoomInBtn = new PanelButton(40, 40, "+");
//             zoomInBtn.addActionListener(e -> zoomHandler.zoomIn());
//             cp.addItem(zoomInBtn, 700, 10);

//             //zoom out button
//             PanelButton zoomOutBtn = new PanelButton(40, 40, "-");
//             zoomOutBtn.addActionListener(e -> zoomHandler.zoomOut());
//             cp.addItem(zoomOutBtn, 700, 50);
//         }

//         return cp;
//     }
// }