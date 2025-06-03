// package gui;

// import java.awt.Dimension;
// import java.awt.FlowLayout;
// import java.awt.event.ActionListener;

// import javax.swing.JButton;
// import javax.swing.JPanel;

// public class PanelButton implements ControlPanelItem {
//     private int width;
//     private int height;
//     private String text;
//     private JPanel panel;
//     private JButton button;

//     /**
//      * Constructor for ZoomButton Objects.
//      *
//      * @param width  the desired width of the button
//      * @param height the desired height of the button
//      * @param text   the desired text that the button will show
//      */
//     public PanelButton(int width, int height, String text) {
//         this.width = width;
//         this.height = height;
//         this.text = text;

//         // Create the button with the specified text
//         this.button = new JButton(this.text);
//         this.button.setPreferredSize(new Dimension(this.width, this.height));

//         // Create the panel and add the button
//         this.panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//         this.panel.add(this.button);
//         this.panel.setSize(width + 10, height + 10); // Give a bit of extra space
//         this.panel.setOpaque(false); // Make it transparent for overlay
//     }

//     /**
//      * Add an action listener to the button
//      *
//      * @param listener The action listener to add
//      */
//     public void addActionListener(ActionListener listener) {
//         this.button.addActionListener(listener);
//     }

//     /**
//      * Returns the panel containing the button
//      *
//      * @return The button panel
//      */
//     @Override
//     public JPanel getPanel() {
//         return this.panel;
//     }

// }