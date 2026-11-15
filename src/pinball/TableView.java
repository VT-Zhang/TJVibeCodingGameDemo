package pinball;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

/** Perspective projection of the playfield; collision coordinates remain unchanged. */
final class TableView {
    private final BufferedImage projected =
            new BufferedImage(640, 790, BufferedImage.TYPE_INT_ARGB);

    void draw(Graphics2D g, BufferedImage surface) {
        g.setPaint(
                new RadialGradientPaint(
                        new Point2D.Double(550, 765),
                        360,
                        new float[] {0, 1},
                        new Color[] {new Color(25, 64, 83, 100), new Color(0, 0, 0, 0)}));
        g.fillOval(210, 570, 700, 310);
        Path2D body = new Path2D.Double();
        body.moveTo(324, 103);
        body.lineTo(777, 103);
        body.lineTo(861, 799);
        body.lineTo(861, 849);
        body.lineTo(235, 849);
        body.lineTo(235, 799);
        body.closePath();
        g.setPaint(new GradientPaint(230, 0, new Color(48, 56, 77), 860, 0, new Color(16, 22, 38)));
        g.fill(body);
        g.setColor(new Color(2, 5, 13, 150));
        g.fillRoundRect(254, 847, 587, 18, 12, 12);
        // Inverse perspective mapping, with bilinear sampling for smooth moving objects.
        int[] source =
                ((java.awt.image.DataBufferInt) surface.getRaster().getDataBuffer()).getData();
        int[] pixels =
                ((java.awt.image.DataBufferInt) projected.getRaster().getDataBuffer()).getData();
        for (int py = 0; py < 790; py++) {
            double v = py / 789.;
            double depth = .72 + .28 * v;
            double sy = 849 * v / (.72 + .28 * v);
            // nonlinear depth along the inclined plane
            for (int px = 0; px < 640; px++) {
                double sx = 325 + (px - 320) / depth;
                int color = 0;
                if (sx >= 0 && sx < 649 && sy >= 0 && sy < 849) {
                    int ix = (int) sx, iy = (int) sy;
                    double fx = sx - ix, fy = sy - iy;
                    int offset = iy * 650 + ix;
                    int a = source[offset],
                            b = source[offset + 1],
                            c = source[offset + 650],
                            d = source[offset + 651];
                    for (int shift = 0; shift <= 24; shift += 8) {
                        int value =
                                (int)
                                        (((a >>> shift & 255) * (1 - fx) + (b >>> shift & 255) * fx)
                                                        * (1 - fy)
                                                + ((c >>> shift & 255) * (1 - fx)
                                                                + (d >>> shift & 255) * fx)
                                                        * fy);
                        color |= value << shift;
                    }
                }
                pixels[py * 640 + px] = color;
            }
        }
        g.drawImage(projected, 228, 70, null);
        g.setPaint(new GradientPaint(0, 807, new Color(62, 72, 94), 0, 849, new Color(14, 20, 34)));
        g.fill(new Rectangle2D.Double(235, 807, 626, 42));
        g.setColor(ArcadeGraphics.RED);
        g.setStroke(new BasicStroke(2));
        g.drawLine(239, 809, 857, 809);
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        g.setColor(new Color(175, 191, 209));
        g.drawString("T J H S S T   C O L O N I A L S   /   GO TJ!", 365, 834);
        for (int x : new int[] {252, 844}) {
            g.setColor(new Color(100, 113, 135));
            g.fillOval(x, 822, 8, 8);
            g.setColor(new Color(15, 22, 35));
            g.drawLine(x + 2, 826, x + 6, 826);
        }
        // Subtle glass reflection, restricted to a narrow diagonal strip.
        Path2D shine = new Path2D.Double();
        shine.moveTo(370, 108);
        shine.lineTo(399, 108);
        shine.lineTo(727, 799);
        shine.lineTo(665, 799);
        shine.closePath();
        g.setPaint(
                new GradientPaint(
                        370,
                        108,
                        new Color(220, 245, 255, 18),
                        700,
                        799,
                        new Color(220, 245, 255, 0)));
        g.fill(shine);
    }
}
