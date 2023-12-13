/* (C)2023 */
package org.transitclock.gui;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * @author Brendan Egan
 */
public class ExceptionPanel {
    String message = null;
    Exception ex = null;
    private JFrame frmTransitimequickstart;

    /** Launch the application. */
    public void ExceptionPanelstart() {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ExceptionPanel window = new ExceptionPanel(message, ex);
                    window.frmTransitimequickstart.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** Create the application. */
    public ExceptionPanel(String message, Exception ex) {

        this.message = message;
        this.ex = ex;
        initialize();
    }

    /** Initialize the contents of the frame. */
    private void initialize() {
        JPanel middlePanel = new JPanel();
        middlePanel.setBorder(new TitledBorder(new EtchedBorder(), "Error Starting TransitimeQuickStart"));

        // create the middle panel components

        JTextArea display = new JTextArea(35, 90);
        display.setEditable(false); // set textArea non-editable
        JScrollPane scroll = new JScrollPane(display);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        // Add Textarea in to middle panel
        middlePanel.add(scroll);

        JFrame frame = new JFrame();
        frame.add(middlePanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        String stackTrace = ExceptionUtils.getStackTrace(ex);
        display.setText(message + "\n" + ex.toString() + "\n" + stackTrace);
    }
}
