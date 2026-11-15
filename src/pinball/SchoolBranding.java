package pinball;

import java.awt.*;
import java.awt.geom.*;

/** Original Colonial mascot artwork, drawn as vectors rather than an official school logo. */
final class SchoolBranding {
    private SchoolBranding() {}

    static void mascot(Graphics2D graphics, double x, double y, double radius) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.translate(x, y);
            g.scale(radius / 70, radius / 70);
            g.setColor(new Color(16, 34, 65));
            g.fillOval(-70, -70, 140, 140);
            g.setStroke(new BasicStroke(3));
            g.setColor(ArcadeGraphics.RED);
            g.drawOval(-67, -67, 134, 134);
            // Red coat, white lapels, and cravat.
            g.setColor(ArcadeGraphics.RED);
            g.fillRoundRect(-43, 25, 86, 34, 25, 25);
            g.setColor(ArcadeGraphics.WHITE);
            g.fillPolygon(new int[] {-24, 0, -8}, new int[] {24, 30, 58}, 3);
            g.fillPolygon(new int[] {24, 0, 8}, new int[] {24, 30, 58}, 3);
            g.setColor(new Color(229, 190, 150));
            g.fillRoundRect(-12, 13, 24, 22, 8, 8);
            // Curled hair frames a friendly face.
            g.setColor(new Color(215, 222, 233));
            for (int side : new int[] {-1, 1}) {
                for (int i = 0; i < 3; i++) g.fillOval(side * 24 - 10, -17 + i * 13, 20, 21);
            }
            g.setColor(new Color(244, 210, 172));
            g.fillOval(-25, -29, 50, 59);
            g.setColor(new Color(24, 39, 65));
            g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(-16, -7, -8, -9);
            g.drawLine(8, -9, 16, -7);
            g.fillOval(-13, -4, 4, 5);
            g.fillOval(9, -4, 4, 5);
            g.drawArc(-10, 6, 20, 13, 195, 150);
            // Broad navy tricorn with a white brim and red cockade.
            Path2D hat = new Path2D.Double();
            hat.moveTo(-53, -28);
            hat.lineTo(-27, -53);
            hat.lineTo(-12, -41);
            hat.quadTo(0, -72, 14, -42);
            hat.lineTo(31, -53);
            hat.lineTo(53, -28);
            hat.quadTo(0, -13, -53, -28);
            hat.closePath();
            g.setColor(new Color(22, 44, 80));
            g.fill(hat);
            g.setColor(ArcadeGraphics.WHITE);
            g.draw(hat);
            g.setColor(ArcadeGraphics.RED);
            g.fillOval(23, -40, 15, 15);
            g.setColor(ArcadeGraphics.WHITE);
            g.fillOval(28, -35, 5, 5);
        } finally {
            g.dispose();
        }
    }
}
