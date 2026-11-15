package pinball;

/** Collision response in table coordinates, independent of Swing and perspective. */
final class PhysicsEngine {
    private static final double BALL_RADIUS = 9;
    private static final double FLIPPER_LENGTH = 88;
    private static final double GRAVITY = 620;
    private static final double DRAG = .07;
    private static final double MAX_SPEED = 1450;
    private final GameState state;
    PhysicsEngine(GameState state) {
        this.state = state;
    }

    void integrate(double dt) {
        double drag = Math.exp(-DRAG * dt);
        state.velocityX *= drag;
        state.velocityY = state.velocityY * drag + GRAVITY * dt;
        state.ballX += state.velocityX * dt;
        state.ballY += state.velocityY * dt;
    }

    void collideWithRails() {
        wall(35, 170, 35, 650);
        wall(35, 650, 135, 795);
        wall(585, 170, 585, 790);
        wall(540, 190, 540, 750);
        wall(540, 660, 465, 795);
        double previousX = 35, previousY = 170;
        for (int i = 1; i <= 24; i++) {
            double angle = Math.PI + i * Math.PI / 24;
            double x = 310 + 275 * Math.cos(angle);
            double y = 170 + 140 * Math.sin(angle);
            wall(previousX, previousY, x, y);
            previousX = x;
            previousY = y;
        }
        wall(95, 575, 110, 650);
        wall(110, 650, 190, 710);
        wall(505, 575, 490, 650);
        wall(490, 650, 410, 710);
    }

    void limitSpeed() {
        double speed = Math.hypot(state.velocityX, state.velocityY);
        if (speed > MAX_SPEED) {
            state.velocityX *= MAX_SPEED / speed;
            state.velocityY *= MAX_SPEED / speed;
        }
    }

    boolean hit(double cx, double cy, double r, double boost) {
        return contact(cx, cy, r, boost, boost>0 ? .66 : .55, 0, 0);
    }
    /** Resolve only inward relative velocity, leaving tangential motion intact. */
    private boolean contact(double cx, double cy, double radius, double boost, double restitution, double surfaceVx, double surfaceVy) {
        double dx = state.ballX-cx, dy = state.ballY-cy, d = Math.hypot(dx, dy);
        if (d >= radius + BALL_RADIUS)return false;
        double nx = d>1e-8 ? dx/d : 0, ny = d>1e-8 ? dy/d : -1;
        double penetration = radius + BALL_RADIUS-d;
        state.ballX += nx*(penetration+.01);
        state.ballY += ny*(penetration+.01);
        double incoming = (state.velocityX-surfaceVx)*nx+(state.velocityY-surfaceVy)*ny;
        if (incoming >= 0)return false;
        // Suppress tiny rebounds so resting contacts do not chatter.
        double bounce = incoming < -45 ? restitution : 0;
        double impulse = -(1+bounce)*incoming+boost;
        state.velocityX += impulse*nx;
        state.velocityY += impulse*ny;
        return true;
    }

    void wall(double ax, double ay, double bx, double by) {
        double dx = bx-ax, dy = by-ay;
        double t = Math.max(0, Math.min(1, ((state.ballX-ax)*dx+(state.ballY-ay)*dy)/(dx*dx+dy*dy)));
        hit(ax+t*dx, ay+t*dy, 3, 0);
    }

    void flipper(double ax, double ay, double angle, double omega) {
        double dx = FLIPPER_LENGTH * Math.cos(angle), dy = FLIPPER_LENGTH * Math.sin(angle);
        double t = Math.max(0, Math.min(1, ((state.ballX-ax)*dx+(state.ballY-ay)*dy)/(FLIPPER_LENGTH * FLIPPER_LENGTH)));
        // Transfer paddle motion through the collision normal rather than an extra kick.
        contact(ax+t*dx, ay+t*dy, 11, 0, .45, -t*dy*omega, t*dx*omega);
    }
}
