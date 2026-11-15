package pinball;

import java.awt.*;
import java.awt.geom.*;

/** Shared school palette and Java2D drawing primitives. */
final class ArcadeGraphics {
    static final Color WHITE = new Color(231, 238, 249);
    static final Color RED = new Color(224, 56, 76);
    static final Color SILVER = new Color(182, 199, 222);
    static void text(Graphics2D g, String s, int x, int y, int size, Color c) {
        g.setColor(c);
        g.setFont(new Font("SansSerif", Font.BOLD, size));
        g.drawString(s, x, y);
    }

    static void line(Graphics2D g, double ax, double ay, double bx, double by, Color c, float width) {
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 25));
        g.setStroke(new BasicStroke(width+12, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(ax, ay, bx, by));
        g.setColor(new Color(0, 0, 0, 130));
        g.setStroke(new BasicStroke(width+4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(ax+3, ay+8, bx+3, by+8));
        g.setColor(c.darker().darker());
        g.setStroke(new BasicStroke(width+2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(ax, ay+4, bx, by+4));
        g.setColor(c);
        g.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(ax, ay, bx, by));
    }

    static void raisedOrb(Graphics2D g, double a, double b, double r, Color c) {
        g.setColor(new Color(0, 0, 0, 120));
        g.fill(new Ellipse2D.Double(a-r+6, b-r+15, r*2+8, r*2));
        g.setPaint(new GradientPaint((float)(a-r), 0, c.darker(), (float)(a+r), 0, new Color(16, 23, 38)));
        g.fill(new RoundRectangle2D.Double(a-r, b-r-12, r*2, r*2+12, r*2, r*2));
        b -= 12;
        orb(g, a, b, r, c);
    }

    static void orb(Graphics2D g, double a, double b, double r, Color c) {
        g.setPaint(new RadialGradientPaint(new Point2D.Double(a, b), (float)(r*2), new float[] {0, .5f, 1}, new Color[] {new Color(c.getRed(), c.getGreen(), c.getBlue(), 110), new Color(c.getRed(), c.getGreen(), c.getBlue(), 35), new Color(0, 0, 0, 0)}));
        g.fill(new Ellipse2D.Double(a-r*2, b-r*2, r*4, r*4));
        g.setColor(new Color(16, 24, 42));
        g.fill(new Ellipse2D.Double(a-r, b-r, r*2, r*2));
        g.setColor(c);
        g.setStroke(new BasicStroke(3));
        g.draw(new Ellipse2D.Double(a-r, b-r, r*2, r*2));
        g.draw(new Ellipse2D.Double(a-r+7, b-r+7, r*2-14, r*2-14));
    }
}
