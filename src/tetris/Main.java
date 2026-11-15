package tetris;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.event.*;
import java.nio.file.Path;
import java.util.Random;

/** Standalone launcher; the other games remain in their own packages. */
public final class Main {
    private Main() {}
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TetrisPanel panel = new TetrisPanel(new TetrisModel(new Random()), Path.of(".tj-tetris-best"));
            JFrame frame = new JFrame("TJHSST Animal Tetris • Go Colonials!");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(panel);
            frame.pack(); frame.setMinimumSize(new Dimension(760,680)); frame.setLocationRelativeTo(null);
            frame.addWindowFocusListener(new WindowAdapter() {
                @Override public void windowLostFocus(WindowEvent e) { panel.pauseForFocusLoss(); }
            });
            frame.setVisible(true);
        });
    }
}
