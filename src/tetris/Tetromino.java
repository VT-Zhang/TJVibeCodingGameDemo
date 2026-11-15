package tetris;

import java.awt.Color;

/** Each species keeps a distinct color, silhouette, and animal face. */
enum Tetromino {
    I("Axolotl", new Color(92, 200, 219), new int[][] {{0,1},{1,1},{2,1},{3,1}}),
    O("Owl", new Color(243, 197, 92), new int[][] {{1,0},{2,0},{1,1},{2,1}}),
    T("Cat", new Color(184, 139, 227), new int[][] {{1,0},{0,1},{1,1},{2,1}}),
    S("Frog", new Color(117, 195, 131), new int[][] {{1,0},{2,0},{0,1},{1,1}}),
    Z("Fox", new Color(230, 91, 109), new int[][] {{0,0},{1,0},{1,1},{2,1}}),
    J("Penguin", new Color(119, 158, 220), new int[][] {{0,0},{0,1},{1,1},{2,1}}),
    L("Tiger", new Color(235, 161, 85), new int[][] {{2,0},{0,1},{1,1},{2,1}});

    final String animal;
    final Color color;
    private final int[][] base;
    Tetromino(String animal, Color color, int[][] base) {
        this.animal = animal; this.color = color; this.base = base;
    }
    int[][] cells(int rotation) {
        int[][] result = new int[4][2];
        for (int i = 0; i < 4; i++) {
            int x = base[i][0], y = base[i][1];
            for (int r = 0; r < Math.floorMod(rotation, 4) && this != O; r++) {
                int oldX = x;
                x = (this == I ? 3 : 2) - y;
                y = oldX;
            }
            result[i][0] = x; result[i][1] = y;
        }
        return result;
    }
}
