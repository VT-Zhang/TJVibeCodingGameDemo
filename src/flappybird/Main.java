package flappybird;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** Standalone Flappy Bird entry point. */
public final class Main {
    private Main() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlappyPanel panel = new FlappyPanel();
            JFrame frame = new JFrame("Flappy Bird • Java Arcade");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(panel);
            frame.setMinimumSize(new Dimension(360, 560));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.addWindowFocusListener(
                    new WindowAdapter() {
                        @Override
                        public void windowLostFocus(WindowEvent event) {
                            panel.pauseForFocusLoss();
                        }
                    });
            frame.setVisible(true);
        });
    }
}
