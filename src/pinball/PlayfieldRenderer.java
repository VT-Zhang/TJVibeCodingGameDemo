package pinball;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import static pinball.ArcadeGraphics.*;

/** Draws the flat playfield into a reusable texture before perspective projection. */
final class PlayfieldRenderer {
    private final BufferedImage surface = new BufferedImage(650, 850, BufferedImage.TYPE_INT_ARGB);

    BufferedImage render(GameState state) {
        Graphics2D g = surface.createGraphics();
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, 650, 850);
        g.setComposite(AlphaComposite.SrcOver);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.translate(20, 20);
        g.setPaint(new GradientPaint(0, 0, new Color(43, 52, 69), 620, 800, new Color(13, 18, 30)));
        g.fillRoundRect(-12, -12, 624, 824, 45, 45);
        g.setColor(new Color(10, 25, 54));
        g.fillRoundRect(0, 0, 600, 800, 110, 110);
        Shape clip = g.getClip();
        g.clip(new RoundRectangle2D.Double(0, 0, 600, 800, 110, 110));
        g.setColor(new Color(25, 45, 76));
        for (int i = 0; i < 800; i += 30) g.drawLine(0, i, 600, i);
        for (int i = 0; i < 600; i += 30) g.drawLine(i, 0, i, 800);
        g.setClip(clip);
        line(g, 35, 170, 35, 650, WHITE, 4);
        line(g, 35, 650, 135, 795, WHITE, 4);
        line(g, 585, 170, 585, 790, RED, 4);
        line(g, 540, 190, 540, 770, RED, 4);
        line(g, 540, 660, 465, 795, RED, 4);
        double px = 35, py = 170;
        for (int i = 1; i <= 24; i++) {
            double a = Math.PI + i * Math.PI / 24,
                    nx = 310 + 275 * Math.cos(a),
                    ny = 170 + 140 * Math.sin(a);
            line(g, px, py, nx, ny, WHITE, 4);
            px = nx;
            py = ny;
        }
        for (int i = 0; i < 3; i++) {
            double[] b = TableLayout.BUMPERS[i];
            raisedOrb(
                    g,
                    b[0],
                    b[1],
                    b[2] + Math.sin(state.time * 4 + i) * 1.4,
                    i == 2 ? SILVER : RED);
            text(g, i == 2 ? "TJ" : "+100", (int) b[0] - 16, (int) b[1] + 4, 10, Color.WHITE);
            double[] t = TableLayout.TARGETS[i];
            raisedOrb(g, t[0], t[1], 13, state.litTargets[i] ? SILVER : WHITE);
            text(
                    g,
                    new String[] {"M", "S", "T"}[i],
                    (int) t[0] - 4,
                    (int) t[1] + 4,
                    11,
                    Color.WHITE);
        }
        SchoolBranding.mascot(g, 300, 395, 47);
        text(g, "TJHSST", 244, 465, 23, WHITE);
        text(g, "COLONIALS", 195, 502, 32, Color.WHITE);
        text(
                g,
                state.overload > 0 ? "COLONIAL SPIRIT" : "SCIENCE • TECHNOLOGY • SPIRIT",
                212,
                530,
                11,
                state.overload > 0 ? SILVER : RED);
        line(g, 95, 575, 110, 650, SILVER, 5);
        line(g, 110, 650, 190, 710, SILVER, 5);
        line(g, 505, 575, 490, 650, RED, 5);
        line(g, 490, 650, 410, 710, RED, 5);
        line(
                g,
                190,
                715,
                190 + 88 * Math.cos(state.leftFlipperAngle),
                715 + 88 * Math.sin(state.leftFlipperAngle),
                WHITE,
                21);
        line(
                g,
                410,
                715,
                410 + 88 * Math.cos(state.rightFlipperAngle),
                715 + 88 * Math.sin(state.rightFlipperAngle),
                RED,
                21);
        for (int i = 0; i < 8; i++)
            line(
                    g,
                    550,
                    735 + i * 6 + state.charge * 12,
                    575,
                    738 + i * 6 + state.charge * 12,
                    SILVER,
                    2);
        for (Particle p : state.particles) {
            g.setColor(new Color(255, 210, 100, (int) (Math.min(1, p.remainingLife / .6) * 255)));
            g.fill(new Ellipse2D.Double(p.x, p.y, 3, 3));
        }
        g.setPaint(
                new RadialGradientPaint(
                        new Point2D.Double(state.ballX - 3, state.ballY - 4),
                        14,
                        new float[] {0, .4f, 1},
                        new Color[] {
                            Color.WHITE, new Color(180, 220, 230), new Color(30, 48, 70)
                        }));
        g.fill(new Ellipse2D.Double(state.ballX - 9, state.ballY - 9, 18, 18));
        g.dispose();
        return surface;
    }
}
