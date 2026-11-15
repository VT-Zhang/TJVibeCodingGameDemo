package flappybird;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Fixed-step game rules, independent of Swing so they can be checked headlessly. */
final class FlappyModel {
    static final int WIDTH = 480, HEIGHT = 720, GROUND = 630;
    static final double BIRD_X = 132, RADIUS = 17, PIPE_WIDTH = 76, GAP = 176;
    enum State { READY, PLAYING, PAUSED, OVER }
    static final class Pipe {
        double x;
        final double gapY;
        boolean scored;
        Pipe(double x, double gapY) { this.x = x; this.gapY = gapY; }
    }
    final List<Pipe> pipes = new ArrayList<>();
    private final Random random;
    private final ScoreStore store;
    State state;
    double birdY, velocity, distance;
    int score;
    long best;

    FlappyModel(Random random, ScoreStore store) {
        this.random = random;
        this.store = store;
        best = Math.max(0, store.load());
        reset();
    }
    void reset() {
        state = State.READY;
        birdY = 290;
        velocity = 0;
        score = 0;
        distance = 0;
        pipes.clear();
        pipes.add(new Pipe(540, nextGap()));
    }
    private double nextGap() { return 205 + random.nextDouble() * 225; }
    void flap() {
        if (state == State.OVER) reset();
        if (state == State.PAUSED) return;
        state = State.PLAYING;
        velocity = -355;
    }
    void pause() {
        if (state == State.PLAYING) state = State.PAUSED;
        else if (state == State.PAUSED) state = State.PLAYING;
    }
    void update(double dt) {
        if (state != State.PLAYING) return;
        velocity += 1060 * dt;
        birdY += velocity * dt;
        double travel = (176 + Math.min(score * 2, 44)) * dt;
        distance += travel;
        for (Pipe pipe : pipes) pipe.x -= travel;
        Pipe last = pipes.get(pipes.size() - 1);
        if (last.x < WIDTH - 165) pipes.add(new Pipe(last.x + 250, nextGap()));
        pipes.removeIf(pipe -> pipe.x + PIPE_WIDTH < -10);
        if (birdY - RADIUS <= 0 || birdY + RADIUS >= GROUND) { die(); return; }
        for (Pipe pipe : pipes) {
            if (hits(pipe)) { die(); return; }
            if (!pipe.scored && pipe.x + PIPE_WIDTH < BIRD_X - RADIUS) {
                pipe.scored = true;
                score++;
            }
        }
    }
    private boolean hits(Pipe pipe) {
        return circleRect(pipe.x - 5, 0, PIPE_WIDTH + 10, pipe.gapY - GAP / 2)
                || circleRect(pipe.x - 5, pipe.gapY + GAP / 2, PIPE_WIDTH + 10,
                              GROUND - pipe.gapY - GAP / 2);
    }
    private boolean circleRect(double x, double y, double w, double h) {
        double dx = BIRD_X - Math.max(x, Math.min(BIRD_X, x + w));
        double dy = birdY - Math.max(y, Math.min(birdY, y + h));
        return dx * dx + dy * dy <= RADIUS * RADIUS;
    }
    private void die() {
        state = State.OVER;
        if (score > best) { best = score; store.save(best); }
    }
}
