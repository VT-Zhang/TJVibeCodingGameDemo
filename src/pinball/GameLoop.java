package pinball;

import javax.swing.Timer;

/** Advances fixed 240 Hz physics on the EDT and requests frames independently. */
final class GameLoop {
    private static final double STEP_SECONDS = 1.0 / 240;
    private final GameModel model;
    private final Runnable repaint;
    private final Timer timer;
    private long lastTick;
    private double accumulator;

    GameLoop(GameModel model, Runnable repaint) {
        this.model = model;
        this.repaint = repaint;
        timer = new Timer(8, event -> tick());
    }

    void start() {
        lastTick = System.nanoTime();
        accumulator = 0;
        timer.start();
    }

    void stop() {
        timer.stop();
    }

    private void tick() {
        long now = System.nanoTime();
        accumulator += Math.min(.05, (now - lastTick) / 1e9);
        lastTick = now;
        while (accumulator >= STEP_SECONDS) {
            model.step(STEP_SECONDS);
            accumulator -= STEP_SECONDS;
        }
        repaint.run();
    }
}
