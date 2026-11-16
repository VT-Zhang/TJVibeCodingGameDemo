package flappybird;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.nio.file.Path;
import java.util.Random;

/** A small, self-contained arcade scene painted with Java2D. */
final class FlappyPanel extends JPanel {
    private final FlappyModel game;
    private long previous = System.nanoTime();
    private double accumulator, animation;
    private boolean held;

    FlappyPanel() {
        this(new FlappyModel(new Random(), new ScoreStore(Path.of(".flappy-best"))));
    }

    FlappyPanel(FlappyModel game) {
        this.game = game;
        setPreferredSize(new Dimension(480, 720));
        setBackground(new Color(24, 48, 65));
        bind(
                "pressed SPACE",
                "flap",
                () -> {
                    if (!held) game.flap();
                    held = true;
                });
        bind("released SPACE", "release", () -> held = false);
        bind("pressed UP", "up", game::flap);
        bind("pressed P", "pause", game::pause);
        bind("pressed R", "reset", game::reset);
        addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        game.flap();
                    }
                });
        new Timer(1000 / 60, e -> tick()).start();
    }

    void pauseForFocusLoss() {
        held = false;
        if (game.state == FlappyModel.State.PLAYING) game.pause();
    }

    private void bind(String key, String name, Runnable action) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), name);
        getActionMap()
                .put(
                        name,
                        new AbstractAction() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                action.run();
                            }
                        });
    }

    private void tick() {
        long now = System.nanoTime();
        double elapsed = Math.min((now - previous) / 1e9, .1);
        previous = now;
        animation += elapsed;
        accumulator += elapsed;
        while (accumulator >= 1.0 / 120) {
            game.update(1.0 / 120);
            accumulator -= 1.0 / 120;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        double scale = Math.min(getWidth() / 480.0, getHeight() / 720.0);
        g.translate((getWidth() - 480 * scale) / 2, (getHeight() - 720 * scale) / 2);
        g.scale(scale, scale);
        g.setClip(0, 0, 480, 720);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(
                new GradientPaint(0, 0, new Color(99, 195, 218), 0, 630, new Color(218, 243, 218)));
        g.fillRect(0, 0, 480, 720);
        g.setColor(new Color(255, 243, 186));
        g.fillOval(350, 65, 66, 66);
        for (int i = 0; i < 5; i++) {
            int x = (int) ((i * 151 - game.distance * .17) % 760);
            if (x < -130) x += 760;
            cloud(g, x, 100 + i % 3 * 65);
        }
        g.setColor(new Color(143, 205, 174));
        for (int i = -1; i < 6; i++) g.fillOval(i * 120, 500 + (i & 1) * 25, 180, 190);
        for (FlappyModel.Pipe p : game.pipes) {
            pipe(g, (int) p.x, 0, (int) (p.gapY - FlappyModel.GAP / 2), true);
            int y = (int) (p.gapY + FlappyModel.GAP / 2);
            pipe(g, (int) p.x, y, 630 - y, false);
        }
        g.setColor(new Color(90, 159, 89));
        g.fillRect(0, 630, 480, 12);
        g.setColor(new Color(213, 186, 133));
        g.fillRect(0, 642, 480, 78);
        g.setColor(new Color(238, 216, 165));
        g.fillRect(0, 646, 480, 7);
        g.setColor(new Color(187, 160, 111));
        for (int x = -40; x < 520; x += 30) g.fillRect(x - (int) (game.distance % 30), 663, 15, 4);
        bird(g);
        text(g, "FLAPPY / JAVA", 22, 31, 13, new Color(35, 88, 98));
        text(g, "BEST " + game.best, 365, 31, 13, new Color(35, 88, 98));
        if (game.state == FlappyModel.State.PLAYING)
            centered(g, Integer.toString(game.score), 108, 58, Color.WHITE);
        if (game.state == FlappyModel.State.READY)
            card(g, "Flappy Bird", "A little bird. A big adventure.", "SPACE / ↑ / CLICK TO FLY");
        if (game.state == FlappyModel.State.PAUSED)
            card(g, "Take a breather", "Your flight is paused", "PRESS P TO RESUME");
        if (game.state == FlappyModel.State.OVER)
            card(
                    g,
                    "One more flight?",
                    "SCORE  " + game.score + "     •     BEST  " + game.best,
                    "SPACE / CLICK TO RETRY");
        centered(g, "SPACE / ↑  fly     P  pause     R  restart", 700, 12, new Color(89, 81, 62));
        g.dispose();
    }

    private void bird(Graphics2D g) {
        AffineTransform old = g.getTransform();
        double y =
                game.birdY
                        + (game.state == FlappyModel.State.READY ? Math.sin(animation * 4) * 7 : 0);
        g.translate(FlappyModel.BIRD_X, y);
        g.rotate(
                game.state == FlappyModel.State.READY
                        ? -.1
                        : Math.max(-.45, Math.min(1.2, game.velocity / 650)));
        g.setColor(new Color(122, 82, 33));
        g.fillOval(-21, -18, 42, 36);
        g.setColor(new Color(255, 206, 63));
        g.fillOval(-19, -16, 38, 32);
        g.setColor(new Color(255, 235, 133));
        g.fillOval(-16, -3, 21, 16);
        g.setColor(new Color(232, 160, 39));
        int wing = (int) (Math.sin(animation * 22) * 4);
        g.fillOval(-21, -3 + wing, 22, 12);
        g.setColor(Color.WHITE);
        g.fillOval(3, -13, 17, 19);
        g.setColor(new Color(34, 54, 62));
        g.fillOval(12, -8, 6, 9);
        g.setColor(new Color(239, 110, 55));
        g.fillRoundRect(12, 3, 20, 9, 5, 5);
        g.setTransform(old);
    }

    private void pipe(Graphics2D g, int x, int y, int h, boolean top) {
        g.setColor(new Color(48, 112, 76));
        g.fillRoundRect(x - 2, y, 80, h, 5, 5);
        g.setPaint(
                new GradientPaint(
                        x, 0, new Color(133, 206, 95), x + 76, 0, new Color(57, 152, 84)));
        g.fillRect(x + 2, y, 72, h);
        int lipY = top ? y + h - 25 : y;
        g.setColor(new Color(48, 112, 76));
        g.fillRoundRect(x - 5, lipY, 86, 25, 5, 5);
        g.setColor(new Color(132, 201, 93));
        g.fillRect(x - 1, lipY + 4, 78, 16);
    }

    private void cloud(Graphics2D g, int x, int y) {
        g.setColor(new Color(255, 255, 255, 175));
        g.fillRoundRect(x, y + 20, 112, 26, 26, 26);
        g.fillOval(x + 17, y + 4, 45, 42);
        g.fillOval(x + 48, y, 48, 46);
    }

    private void card(Graphics2D g, String title, String subtitle, String prompt) {
        g.setColor(new Color(25, 65, 72, 35));
        g.fillRoundRect(40, 352, 400, 180, 28, 28);
        g.setColor(new Color(255, 252, 237, 245));
        g.fillRoundRect(36, 346, 408, 180, 28, 28);
        centered(g, title, 395, 32, new Color(39, 85, 87));
        centered(g, subtitle, 431, 16, new Color(96, 119, 112));
        centered(g, prompt, 484, 16, new Color(48, 130, 99));
    }

    private void centered(Graphics2D g, String s, int y, int size, Color c) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, size));
        text(g, s, (480 - g.getFontMetrics().stringWidth(s)) / 2, y, size, c);
    }

    private void text(Graphics2D g, String s, int x, int y, int size, Color c) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, size));
        g.setColor(c);
        g.drawString(s, x, y);
    }
}
