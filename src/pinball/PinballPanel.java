package pinball;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Composes the table and HUD; owns the Swing lifecycle, not game rules. */
final class PinballPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final int DESIGN_WIDTH = 1120;
    private static final int DESIGN_HEIGHT = 900;

    private final transient GameModel model;
    private final transient PlayfieldRenderer playfield = new PlayfieldRenderer();
    private final transient HudRenderer hud = new HudRenderer();
    private final transient TableView table = new TableView();
    private final transient GameLoop loop;

    PinballPanel(GameModel model) {
        this.model = model;
        setPreferredSize(new Dimension(DESIGN_WIDTH, DESIGN_HEIGHT));
        setBackground(new Color(6, 9, 20));
        setFocusable(true);
        new GameControls(model).install(this);
        loop = new GameLoop(model, this::repaint);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        loop.start();
    }

    @Override
    public void removeNotify() {
        loop.stop();
        super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            double scale = Math.min(getWidth() / (double) DESIGN_WIDTH,
                getHeight() / (double) DESIGN_HEIGHT);
            g.translate((getWidth() - DESIGN_WIDTH * scale) / 2,
                (getHeight() - DESIGN_HEIGHT * scale) / 2);
            g.scale(scale, scale);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            hud.drawBranding(g, model.state);
            table.draw(g, playfield.render(model.state));
            hud.drawStatus(g, model.state);
        } finally {
            g.dispose();
        }
    }
}
