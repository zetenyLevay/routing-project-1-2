// package gui;

// import java.awt.Dimension;
// import java.awt.FlowLayout;

// import javax.swing.JPanel;
// import javax.swing.JTextField;

// public class SearchBar implements ControlPanelItem {
//     private int width;
//     private int height;
//     private JTextField textField;
//     private JPanel panel;

//     /**
//      * Constructor to create a searchbar object
//      *
//      * @param width  the desired width of the searchbar
//      * @param height the desired height of the searchbar
//      */
//     public SearchBar(int width, int height) {
//         textField = new JTextField(20);
//         textField.setPreferredSize(new Dimension(width - 10, height - 10));
//         panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//         panel.setSize(width, height);
//         panel.add(textField);
//         panel.setOpaque(false);
//     }


//     /**
//      * Gets the panel
//      *
//      * @return the current panel the object is attached to
//      */
//     @Override
//     public JPanel getPanel() {
//         return panel;
//     }


// }

