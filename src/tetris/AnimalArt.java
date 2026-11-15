package tetris;

import java.awt.*;

/** Tiny vector animal portraits stay crisp when the window is resized. */
final class AnimalArt {
    private AnimalArt() {}

    static void block(Graphics2D graphics, Tetromino type, int x, int y, int size, boolean ghost) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.translate(x, y);
            g.scale(size / 36.0, size / 36.0);
            if (ghost) {
                g.setColor(
                        new Color(
                                type.color.getRed(),
                                type.color.getGreen(),
                                type.color.getBlue(),
                                50));
                g.fillRoundRect(1, 1, 34, 34, 8, 8);
                g.setColor(type.color);
                g.setStroke(new BasicStroke(1.2f));
                g.drawRoundRect(3, 3, 30, 30, 7, 7);
                return;
            }
            g.setColor(type.color.darker().darker());
            g.fillRoundRect(1, 2, 34, 34, 8, 8);
            g.setColor(type.color);
            g.fillRoundRect(1, 1, 34, 32, 8, 8);
            g.setColor(new Color(255, 255, 255, 70));
            g.drawLine(7, 4, 28, 4);
            Color ink = new Color(23, 36, 59), cream = new Color(255, 240, 213);
            if (type == Tetromino.I) {
                g.setColor(ink);
                for (int side : new int[] {-1, 1})
                    for (int i = 0; i < 3; i++) {
                        int xx = side < 0 ? 9 : 27;
                        g.drawLine(xx, 13 + i * 4, xx + side * 5, 10 + i * 6);
                    }
                g.setColor(new Color(235, 240, 249));
                g.fillOval(8, 10, 20, 20);
            } else if (type == Tetromino.S) {
                g.setColor(cream);
                g.fillOval(5, 7, 12, 13);
                g.fillOval(19, 7, 12, 13);
            } else if (type == Tetromino.O) {
                g.setColor(cream);
                g.fillOval(5, 8, 14, 19);
                g.fillOval(17, 8, 14, 19);
                g.setColor(ink);
                g.fillPolygon(new int[] {6, 7, 14}, new int[] {13, 5, 9}, 3);
                g.fillPolygon(new int[] {22, 29, 30}, new int[] {9, 5, 13}, 3);
            } else if (type == Tetromino.J) {
                g.setColor(ink);
                g.fillOval(6, 6, 24, 26);
                g.setColor(cream);
                g.fillOval(9, 11, 18, 20);
            } else {
                g.setColor(ink);
                g.fillPolygon(new int[] {6, 8, 16}, new int[] {16, 5, 13}, 3);
                g.fillPolygon(new int[] {20, 28, 30}, new int[] {13, 5, 16}, 3);
                g.setColor(cream);
                g.fillOval(7, 13, 22, 17);
                if (type == Tetromino.L) {
                    g.setColor(ink);
                    g.drawLine(14, 8, 16, 12);
                    g.drawLine(22, 8, 20, 12);
                    g.drawLine(4, 20, 9, 22);
                    g.drawLine(32, 20, 27, 22);
                }
            }
            g.setColor(ink);
            g.fillOval(11, 16, 3, 4);
            g.fillOval(22, 16, 3, 4);
            if (type == Tetromino.O || type == Tetromino.J) {
                g.setColor(new Color(224, 114, 57));
                g.fillPolygon(new int[] {15, 21, 18}, new int[] {22, 22, 27}, 3);
            } else {
                g.setColor(ink);
                g.drawArc(14, 21, 8, 6, 190, 160);
                if (type != Tetromino.I && type != Tetromino.S) g.fillOval(16, 21, 4, 3);
                if (type == Tetromino.T) {
                    g.drawLine(5, 23, 11, 24);
                    g.drawLine(25, 24, 31, 23);
                }
            }
        } finally {
            g.dispose();
        }
    }
}
