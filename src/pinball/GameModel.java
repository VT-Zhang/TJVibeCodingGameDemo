package pinball;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Random;

/** Run lifecycle, scoring, objectives, and fixed-step simulation. */
final class GameModel {
    final GameState state = new GameState();
    final PhysicsEngine physics = new PhysicsEngine(state);
    private final Random random = new Random();
    private final ScoreStore scores;

    GameModel(ScoreStore scores) {
        this.scores = scores;
        state.best = scores.load();
    }

    void reset() {
        state.score = 0;
        state.ballNumber = 1;
        state.over = false;
        state.paused = false;
        state.overload = 0;
        Arrays.fill(state.litTargets, false);
        serve();
    }

    private void serve() {
        state.saved = false;
        state.ballX = 560;
        state.ballY = 690;
        state.velocityX = state.velocityY = 0;
        state.ready = true;
        state.charge = 0;
        state.tilt = 0;
        state.message = "HOLD SPACE TO LAUNCH";
    }

    void step(double dt) {
        if (state.paused || state.over) return;
        state.time += dt;
        state.overload = Math.max(0, state.overload - dt);
        state.save = Math.max(0, state.save - dt);
        state.tilt = Math.max(0, state.tilt - dt * .15);

        double oldLeft = state.leftFlipperAngle;
        double oldRight = state.rightFlipperAngle;
        updateFlippers(dt);
        state.particles.removeIf(particle -> !particle.update(dt));

        if (state.ready) {
            if (state.heldKeys[KeyEvent.VK_SPACE]) {
                state.charge = Math.min(1, state.charge + dt);
            }
            return;
        }
        physics.integrate(dt);
        physics.collideWithRails();
        physics.flipper(190, 715, state.leftFlipperAngle, (state.leftFlipperAngle - oldLeft) / dt);
        physics.flipper(
                410, 715, state.rightFlipperAngle, (state.rightFlipperAngle - oldRight) / dt);
        updateObjectives();
        physics.limitSpeed();
        if (state.ballY > 825) drain();
    }

    private void updateFlippers(double dt) {
        boolean leftHeld = state.heldKeys[KeyEvent.VK_LEFT] || state.heldKeys[KeyEvent.VK_A];
        boolean rightHeld = state.heldKeys[KeyEvent.VK_RIGHT] || state.heldKeys[KeyEvent.VK_D];
        state.leftFlipperAngle =
                approach(state.leftFlipperAngle, leftHeld && state.tilt < 4 ? -.5 : .32, dt * 16);
        state.rightFlipperAngle =
                approach(
                        state.rightFlipperAngle,
                        rightHeld && state.tilt < 4 ? Math.PI + .5 : Math.PI - .32,
                        dt * 16);
    }

    private void updateObjectives() {
        for (int i = 0; i < TableLayout.BUMPERS.length; i++) {
            double[] bumper = TableLayout.BUMPERS[i];
            if (physics.hit(bumper[0], bumper[1], bumper[2], 135)) {
                state.score += state.overload > 0 ? 300 : 100;
                burst(bumper[0], bumper[1]);
                if (i == 2 && allTargetsLit()) {
                    state.overload = 20;
                    state.score += 2500;
                    Arrays.fill(state.litTargets, false);
                    state.message = "COLONIAL SPIRIT / 3X SCORING";
                }
            }
            double[] target = TableLayout.TARGETS[i];
            if (physics.hit(target[0], target[1], 13, 65)) {
                state.litTargets[i] = true;
                state.score += state.overload > 0 ? 750 : 250;
                burst(target[0], target[1]);
                if (allTargetsLit()) state.message = "LABS READY — HIT THE TJ BUMPER";
            }
        }
    }

    private boolean allTargetsLit() {
        return state.litTargets[0] && state.litTargets[1] && state.litTargets[2];
    }

    private void drain() {
        if (state.save > 0) {
            serve();
            state.saved = true;
            state.message = "BALL SAVED";
            state.save = 0;
        } else if (++state.ballNumber > 3) {
            state.ballNumber = 3;
            state.over = true;
            state.message = "GAME COMPLETE";
            state.best = Math.max(state.best, state.score);
            scores.save(state.best);
        } else {
            serve();
        }
    }

    private void tone(int frequency) {
        if (!state.muted) Sound.play(frequency);
    }

    private void burst(double x, double y) {
        tone(600 + (int) (x % 300));
        for (int i = 0; i < 18; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 50 + random.nextDouble() * 180;
            state.particles.add(
                    new Particle(x, y, Math.cos(angle) * speed, Math.sin(angle) * speed));
        }
    }

    private static double approach(double value, double target, double amount) {
        return value + Math.max(-amount, Math.min(amount, target - value));
    }

    void launch() {
        if (!state.ready || state.paused || state.over) return;
        state.ready = false;
        state.velocityY = -1000 - state.charge * 360;
        state.velocityX = -25;
        state.save = state.saved ? 0 : 8;
        state.charge = 0;
        tone(330);
        state.message = "LIGHT THE MATH, SCIENCE & TECH LABS";
    }

    void nudge() {
        if (state.ready || state.paused || state.over) return;
        state.velocityX += (random.nextBoolean() ? 1 : -1) * 160;
        state.velocityY -= 160;
        state.tilt += 1;
        if (state.tilt > 3) {
            state.message = "TILT — FLIPPERS OFFLINE";
            state.tilt = 6;
        }
    }
}
