package pinball;

import java.util.ArrayList;

/** Mutable state of one run, shared by the simulation and read by renderers. */
final class GameState {
    final boolean[] heldKeys = new boolean[512];
    final boolean[] litTargets = new boolean[3];
    final ArrayList<Particle> particles = new ArrayList<>();
    double ballX = 560, ballY = 690, velocityX, velocityY;
    double charge, time, overload, save, tilt;
    double leftFlipperAngle = .32, rightFlipperAngle = Math.PI - .32;
    long score, best;
    int ballNumber = 1;
    boolean ready = true, paused, over, muted, saved;
    String message = "HOLD SPACE. LAUNCH FOR TJ!";
}
