package pinball;

import java.awt.*;
import static pinball.ArcadeGraphics.*;

/** Screen-space score, instructions, status, and pause/game-over overlays. */
final class HudRenderer {
    void drawBranding(Graphics2D g, GameState state) {
        text(g, "TJHSST  /  PINBALL", 32, 38, 13, WHITE);
        text(g, "COLONIALS     •     SCIENCE & TECHNOLOGY", 780, 38, 11, Color.GRAY);
        text(g, "TJHSST", 32, 150, 43, Color.WHITE);
        text(g, "COLONIALS", 32, 194, 26, RED);
        text(g, "THOMAS JEFFERSON", 33, 235, 12, WHITE);
        text(g, "HIGH SCHOOL", 33, 255, 12, WHITE);
        text(g, "SCIENCE & TECHNOLOGY", 33, 276, 11, WHITE);
        SchoolBranding.mascot(g, 105, 505, 67);
        text(g, "THE STEM CHALLENGE", 33, 360, 12, SILVER);
        text(g, "Light three labs.", 33, 390, 13, Color.LIGHT_GRAY);
        text(g, "Hit the TJ bumper.", 33, 412, 13, Color.LIGHT_GRAY);
        text(g, "Start Colonial Spirit.", 33, 434, 13, Color.LIGHT_GRAY);
        text(g, "PERSONAL BEST", 33, 620, 11, Color.GRAY);
        text(g, String.format("%08d", Math.max(state.best, state.score)), 33, 653, 25, SILVER);
        text(g, "GO COLONIALS!", 33, 815, 12, RED);
    }

    void drawStatus(Graphics2D g, GameState state) {
        text(g, "PLAYER 01", 900, 110, 12, WHITE);
        text(g, String.format("%08d", state.score), 890, 159, 30, SILVER);
        text(g, "BALL  0"+state.ballNumber+" / 03", 900, 221, 18, Color.WHITE);
        text(g, "MATH / SCIENCE / TECH", 900, 300, 12, Color.GRAY);
        for (int i = 0; i<3; i++) {
            orb(g, 920+i*60, 345, 16, state.litTargets[i] ? SILVER : new Color(55, 78, 96));
            text(g, new String[] {"M", "S", "T"}[i], 914+i*60, 350, 12, Color.WHITE);
        }
        text(g, state.overload>0 ? String.format("3X  /  %.1fs", state.overload) : "1X  /  STANDBY", 900, 405, 18, state.overload>0 ? SILVER : WHITE);
        text(g, "TAKE CONTROL", 900, 510, 12, Color.GRAY);
        String[] help = {
            "← → / A D   Flippers", "SPACE   Hold / launch", "Z   Nudge (watch tilt)", "P   Pause", "R   New game"
        };
        for (int i = 0; i<help.length; i++)text(g, help[i], 900, 545+i*29, 11, Color.LIGHT_GRAY);
        g.setColor(new Color(20, 41, 54));
        g.fillRoundRect(895, 735, 195, 65, 12, 12);
        text(g, state.ready ? "HOLD SPACE" : state.over ? "SPACE TO RETRY" : "GO COLONIALS!", 911, 763, 15, WHITE);
        g.setColor(SILVER);
        g.fillRoundRect(910, 780, (int)(165*state.charge), 4, 4, 4);
        text(g, state.message, 260, 890, 12, SILVER);
        if (state.paused || state.over) {
            g.setColor(new Color(3, 6, 15, 215));
            g.fillRoundRect(330, 360, 450, 165, 22, 22);
            text(g, state.paused ? "GAME PAUSED" : "GAME COMPLETE", 360, 416, 29, Color.WHITE);
            text(g, state.paused ? "Press P to return to the table" : "SCORE  "+state.score+"    /    SPACE TO RETRY", 360, 466, 16, WHITE);
        }
    }
}
