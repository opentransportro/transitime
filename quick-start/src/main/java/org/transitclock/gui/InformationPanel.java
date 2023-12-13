/* (C)2023 */
package org.transitclock.gui;

import java.awt.EventQueue;
import java.awt.Font;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JTextPane;

/**
 * @author Brendan Egan
 */
public class InformationPanel {

    private JFrame frame;

    /** Launch the application. */
    public void InformationPanelstart() {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    InformationPanel window = new InformationPanel();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** Create the application. */
    public InformationPanel() {
        initialize();
    }

    /** Initialize the contents of the frame. */
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 522, 442);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTextPane txtpnInfopanel = new JTextPane();
        txtpnInfopanel.setFont(new Font("Tahoma", Font.PLAIN, 16));
        txtpnInfopanel.setText("GTFS File location: This is the location of the GTFS file you want transitime to"
                + " use eg:\r\n"
                + "C:\\Users\\..\\transitimeQuickStart\\src\\main\\r"
                + "esources\\Intercity.zip \r\n\r\n"
                + "GTFS Realtime feed location: Generally a URL, more information can found"
                + " at:https://developers.google.com/transit/gtfs-realtime/\r\n\r\n"
                + " Log location: where you want the outputted log files to go\r\n\r\n"
                + "(transitimeQuickStart will use defaults if nothing entered)");
        GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
        groupLayout.setHorizontalGroup(groupLayout
                .createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addComponent(txtpnInfopanel, GroupLayout.PREFERRED_SIZE, 471, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(21, Short.MAX_VALUE)));
        groupLayout.setVerticalGroup(groupLayout
                .createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addComponent(txtpnInfopanel, GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE)
                        .addContainerGap()));
        frame.getContentPane().setLayout(groupLayout);
    }
}
