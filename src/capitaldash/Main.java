package capitaldash;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.event.*;

/** Standalone launcher for the DC cartoon platformer. */
public final class Main {
    private Main() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Capital Dash • Washington, DC");
            DashPanel panel = new DashPanel(new DashModel());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(panel);
            frame.pack();
            frame.setMinimumSize(new Dimension(880, 640));
            frame.setLocationRelativeTo(null);
            frame.addWindowFocusListener(
                    new WindowAdapter() {
                        @Override
                        public void windowLostFocus(WindowEvent e) {
                            panel.pauseForFocusLoss();
                        }
                    });
            frame.setVisible(true);
        });
    }
}
