package pinball;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.nio.file.Path;

/** Application entry point; gameplay lives in the model and view classes. */
public final class Main {
    private Main() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameModel model = new GameModel(new ScoreStore(Path.of(".neon-reactor-best")));
            PinballPanel panel = new PinballPanel(model);
            JFrame frame = new JFrame("TJHSST Colonials • Pinball");
            frame.setContentPane(panel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setMinimumSize(new Dimension(900, 740));
            frame.setVisible(true);
            panel.requestFocusInWindow();
        });
    }
}
