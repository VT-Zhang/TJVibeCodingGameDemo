package pinball;

/** One short-lived spark in table coordinates. */
final class Particle {
    double x, y;
    private final double velocityX, velocityY;
    double remainingLife = .6;

    Particle(double x, double y, double velocityX, double velocityY) {
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    boolean update(double dt) {
        x += velocityX * dt;
        y += velocityY * dt;
        remainingLife -= dt;
        return remainingLife >= 0;
    }
}
