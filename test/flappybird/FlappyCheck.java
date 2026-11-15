package flappybird;

import java.util.Random;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.SwingUtilities;

/** Headless rules and rendering checks; no external test framework required. */
public final class FlappyCheck {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        FlappyModel game = new FlappyModel(new Random(1), new ScoreStore(null));
        check(game.state == FlappyModel.State.READY, "starts ready");
        game.flap();
        double before = game.birdY;
        game.update(1.0 / 120);
        check(game.birdY < before, "flap lifts bird");
        game.pause();
        before = game.birdY;
        game.update(.1);
        check(game.birdY == before, "pause freezes physics");
        game.pause();
        game.pipes.clear();
        game.birdY = 300;
        game.pipes.add(new FlappyModel.Pipe(20, 300));
        game.update(1.0 / 120);
        check(game.score == 1, "passed pipe scores");
        game.update(1.0 / 120);
        check(game.score == 1, "pipe only scores once");
        game.pipes.clear();
        game.pipes.add(new FlappyModel.Pipe(120, 500));
        game.update(1.0 / 120);
        check(game.state == FlappyModel.State.OVER, "pipe collision ends run");
        check(game.best == 1, "best records score");
        game.flap();
        check(game.score == 0 && game.state == FlappyModel.State.PLAYING, "retry resets run");
        game.birdY = 620;
        game.update(1.0 / 120);
        check(game.state == FlappyModel.State.OVER, "ground collision");
        game.reset();
        game.flap();
        game.birdY = 10;
        game.update(1.0 / 120);
        check(game.state == FlappyModel.State.OVER, "ceiling collision");
        SwingUtilities.invokeAndWait(() -> {
            FlappyPanel panel = new FlappyPanel();
            panel.setSize(480, 720);
            BufferedImage image = new BufferedImage(480, 720, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            panel.paint(graphics);
            graphics.dispose();
            check(image.getRGB(0, 0) != image.getRGB(0, 680), "scene renders");
        });
        System.out.println(
                "PASS: flight, pause, scoring, collisions, restart, best score, rendering");
        System.exit(0);
    }
}
