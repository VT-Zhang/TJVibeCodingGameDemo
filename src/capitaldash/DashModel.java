package capitaldash;

import java.util.ArrayList;
import java.util.List;

/** Fixed-step platforming, pickups, checkpoints, and the first-stage boss. */
final class DashModel {
    static final double WORLD = 4200, FLOOR = 620, PW = 30, PH = 48;

    enum State {
        READY,
        PLAYING,
        PAUSED,
        WON,
        OVER
    }

    static final class Platform {
        final double x, y, w, h;
        final boolean box;
        boolean used;

        Platform(double x, double y, double w, double h, boolean box) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.box = box;
        }
    }

    static final class Enemy {
        double x, y;
        final double min, max;
        double direction = 1;
        boolean alive = true;

        Enemy(double x, double min, double max) {
            this.x = x;
            this.y = FLOOR - 30;
            this.min = min;
            this.max = max;
        }
    }

    static final class Pickup {
        final double x, y;
        final boolean burger;
        boolean collected;

        Pickup(double x, double y, boolean burger) {
            this.x = x;
            this.y = y;
            this.burger = burger;
        }
    }

    static final class Shot {
        double x, y, vx, vy;

        Shot(double x, double y, double vx, double vy) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
        }
    }

    final List<Platform> platforms = new ArrayList<>();
    final List<Enemy> enemies = new ArrayList<>();
    final List<Pickup> pickups = new ArrayList<>();
    final List<Shot> shots = new ArrayList<>();
    State state;
    double x, y, vx, vy, time, invulnerable, coyote, jumpBuffer, checkpoint;
    double bossX, bossY, bossVY, bossDirection, bossCooldown, bossJump, bossInvulnerable;
    int health, maxHealth, lives, coins, bossHP;
    boolean grounded, powered, grown, bossActive, facingRight = true;

    enum Cue {
        JUMP,
        STAR,
        BURGER,
        BOX,
        STOMP,
        HURT,
        LIFE_LOST,
        CHECKPOINT,
        BOSS_SHOT,
        BOSS_HIT,
        BOSS_DEFEAT,
        WIN
    }

    private final List<Cue> sounds = new ArrayList<>();

    List<Cue> drainSounds() {
        List<Cue> result = List.copyOf(sounds);
        sounds.clear();
        return result;
    }

    private void sound(Cue cue) {
        if (sounds.size() < 48) sounds.add(cue);
    }

    double playerWidth() {
        return grown ? PW * 1.5 : PW;
    }

    double playerHeight() {
        return grown ? PH * 1.5 : PH;
    }

    private void tryGrow() {
        if (!powered || grown) return;
        double nx = Math.max(0, Math.min(WORLD - PW * 1.5, x - PW * .25)), ny = y - PH * .5;
        for (Platform p : platforms)
            if (overlap(nx, ny, PW * 1.5, PH * 1.5, p.x, p.y, p.w, p.h)) return;
        x = nx;
        y = ny;
        grown = true;
    }

    String message;

    DashModel() {
        reset();
    }

    void reset() {
        platforms.clear();
        enemies.clear();
        pickups.clear();
        shots.clear();
        sounds.clear();
        ground(0, 850);
        ground(960, 640);
        ground(1710, 720);
        ground(2540, 1660);
        ledge(330, 490, 100);
        ledge(570, 420, 140);
        ledge(1100, 500, 150);
        ledge(1350, 420, 130);
        ledge(1860, 495, 150);
        ledge(2170, 450, 140);
        ledge(2700, 500, 140);
        ledge(2920, 430, 140);
        box(375, 390);
        box(1195, 390);
        box(1970, 385);
        box(2770, 390);
        enemies.add(new Enemy(590, 480, 790));
        enemies.add(new Enemy(1320, 1030, 1510));
        enemies.add(new Enemy(2050, 1810, 2300));
        enemies.add(new Enemy(2820, 2630, 3020));
        for (int i = 0; i < 32; i++) pickups.add(new Pickup(160 + i * 87, FLOOR - 80, false));
        for (Platform p : platforms)
            if (!p.box && p.y < FLOOR)
                for (int i = 0; i < 3; i++)
                    pickups.add(new Pickup(p.x + 20 + i * 30, p.y - 42, false));
        pickups.add(new Pickup(250, FLOOR - 32, true));
        x = 90;
        y = FLOOR - PH;
        vx = vy = time = invulnerable = coyote = jumpBuffer = 0;
        checkpoint = 90;
        health = maxHealth = lives = 3;
        coins = 0;
        grounded = true;
        powered = false;
        grown = false;
        bossActive = false;
        resetBoss();
        state = State.READY;
        message = "Reach the White House. Watch the gaps!";
    }

    private void ground(double x, double width) {
        platforms.add(new Platform(x, FLOOR, width, 160, false));
    }

    private void ledge(double x, double y, double width) {
        platforms.add(new Platform(x, y, width, 22, false));
    }

    private void box(double x, double y) {
        platforms.add(new Platform(x, y, 40, 40, true));
    }

    private void resetBoss() {
        bossX = 3510;
        bossY = FLOOR - 100;
        bossVY = 0;
        bossDirection = -1;
        bossHP = 5;
        bossCooldown = 1.8;
        bossJump = 2.5;
        bossInvulnerable = 0;
        shots.clear();
    }

    void start() {
        if (state == State.READY) state = State.PLAYING;
    }

    void pause() {
        if (state == State.PLAYING) state = State.PAUSED;
        else if (state == State.PAUSED) state = State.PLAYING;
    }

    void jump() {
        if (state == State.PLAYING) jumpBuffer = .13;
    }

    void update(double dt, int direction, boolean sprint) {
        if (state != State.PLAYING) return;
        tryGrow();
        time += dt;
        invulnerable = Math.max(0, invulnerable - dt);
        bossInvulnerable = Math.max(0, bossInvulnerable - dt);
        jumpBuffer = Math.max(0, jumpBuffer - dt);
        coyote = grounded ? .1 : Math.max(0, coyote - dt);
        if (jumpBuffer > 0 && coyote > 0) {
            vy = powered ? -800 : -620;
            grounded = false;
            coyote = jumpBuffer = 0;
            sound(Cue.JUMP);
        }
        vx = direction * (sprint ? 325 : 240);
        if (direction != 0) facingRight = direction > 0;
        x += vx * dt;
        for (Platform p : platforms)
            if (overlap(x, y, playerWidth(), playerHeight(), p.x, p.y, p.w, p.h))
                x = vx > 0 ? p.x - playerWidth() : p.x + p.w;
        x = Math.max(bossActive && bossHP > 0 ? 3110 : 0, Math.min(WORLD - playerWidth(), x));
        double oldBottom = y + playerHeight();
        vy = Math.min(900, vy + 1650 * dt);
        y += vy * dt;
        grounded = false;
        Platform hitBox = null;
        for (Platform p : platforms)
            if (overlap(x, y, playerWidth(), playerHeight(), p.x, p.y, p.w, p.h)) {
                if (vy >= 0) {
                    y = p.y - playerHeight();
                    vy = 0;
                    grounded = true;
                } else {
                    y = p.y + p.h;
                    vy = 0;
                    if (p.box && !p.used) hitBox = p;
                }
            }
        if (hitBox != null) {
            sound(Cue.BOX);
            hitBox.used = true;
            pickups.add(new Pickup(hitBox.x + 5, hitBox.y - 34, true));
            message = "A Big Mac! Grab it for extra hearts and a higher jump.";
        }
        if (y > 850) {
            loseLife();
            return;
        }
        if (x > 1850 && checkpoint < 1850) {
            checkpoint = 1810;
            sound(Cue.CHECKPOINT);
            message = "Checkpoint reached: the National Mall.";
        }
        if (x > 3140 && !bossActive) {
            bossActive = true;
            checkpoint = 3140;
            message = "STAGE 1 BOSS: Kamala! Jump onto her shell five times.";
        }
        for (Pickup p : pickups)
            if (!p.collected
                    && overlap(
                            x,
                            y,
                            playerWidth(),
                            playerHeight(),
                            p.x,
                            p.y,
                            p.burger ? 30 : 18,
                            p.burger ? 28 : 22)) {
                p.collected = true;
                if (p.burger) {
                    powered = true;
                    maxHealth = 5;
                    health = 5;
                    tryGrow();
                    sound(Cue.BURGER);
                    message = "BIG MAC POWER! Grow bigger, jump higher, and gain five hearts!";
                } else {
                    coins++;
                    sound(Cue.STAR);
                }
            }
        for (Enemy e : enemies)
            if (e.alive) {
                e.x += e.direction * 75 * dt;
                if (e.x < e.min) {
                    e.x = e.min;
                    e.direction = 1;
                }
                if (e.x > e.max) {
                    e.x = e.max;
                    e.direction = -1;
                }
                if (overlap(x, y, playerWidth(), playerHeight(), e.x, e.y, 34, 30)) {
                    if (vy > 0 && oldBottom <= e.y + 14) {
                        e.alive = false;
                        sound(Cue.STOMP);
                        vy = -390;
                        coins += 3;
                        message = "Stomp! +3 stars.";
                    } else if (hurt()) return;
                }
            }
        if (bossActive && bossHP > 0) updateBoss(dt, oldBottom);
        if (state != State.PLAYING) return;
        if (bossActive && bossHP == 0 && x > 3910) {
            state = State.WON;
            sound(Cue.WIN);
            message = "Stage 1 clear! The White House awaits.";
        }
    }

    private void updateBoss(double dt, double oldBottom) {
        bossX += bossDirection * 100 * dt;
        if (bossX < 3300) {
            bossX = 3300;
            bossDirection = 1;
        }
        if (bossX > 3650) {
            bossX = 3650;
            bossDirection = -1;
        }
        bossJump -= dt;
        if (bossJump <= 0 && bossY >= FLOOR - 100) {
            bossVY = -470;
            bossJump = 2.8;
        }
        bossVY += 1350 * dt;
        bossY += bossVY * dt;
        if (bossY > FLOOR - 100) {
            bossY = FLOOR - 100;
            bossVY = 0;
        }
        bossCooldown -= dt;
        if (bossCooldown <= 0) {
            double direction = x < bossX ? -1 : 1;
            sound(Cue.BOSS_SHOT);
            shots.add(new Shot(bossX + 44, bossY + 43, direction * 230, -130));
            bossCooldown = 1.65;
        }
        if (overlap(x, y, playerWidth(), playerHeight(), bossX, bossY, 88, 100)) {
            if (vy > 0 && oldBottom <= bossY + 23) {
                vy = -570;
                y = bossY - playerHeight();
                if (bossInvulnerable == 0) {
                    bossHP--;
                    sound(Cue.BOSS_HIT);
                    bossInvulnerable = .8;
                    message = "Boss hit! " + bossHP + " / 5 hearts left.";
                }
                if (bossHP == 0) {
                    sound(Cue.BOSS_DEFEAT);
                    shots.clear();
                    message = "Kamala defeated! Head right to the White House entrance.";
                    coins += 25;
                    return;
                }
            } else if (hurt()) return;
        }
        for (Shot shot : shots) {
            shot.x += shot.vx * dt;
            shot.vy += 260 * dt;
            shot.y += shot.vy * dt;
            if (overlap(x, y, playerWidth(), playerHeight(), shot.x - 12, shot.y - 12, 24, 24)
                    && hurt()) return;
        }
        shots.removeIf(s -> s.y > FLOOR + 30 || s.x < 3070 || s.x > 4000);
    }

    private boolean hurt() {
        if (invulnerable > 0) return false;
        health--;
        sound(Cue.HURT);
        invulnerable = 1.5;
        vy = -330;
        if (health <= 0) {
            loseLife();
            return true;
        }
        message = "Ouch! " + health + " hearts left. Jump on enemies from above.";
        return false;
    }

    private void loseLife() {
        lives--;
        sound(Cue.LIFE_LOST);
        if (lives <= 0) {
            state = State.OVER;
            message = "Out of lives. Press Enter to try again.";
            return;
        }
        x = checkpoint;
        y = FLOOR - PH;
        vx = vy = 0;
        grounded = true;
        powered = grown = false;
        maxHealth = health = 3;
        invulnerable = 2;
        jumpBuffer = coyote = 0;
        if (bossActive && bossHP > 0) resetBoss();
        message = "Back at the checkpoint. " + lives + " lives left.";
    }

    static boolean overlap(
            double x, double y, double w, double h, double a, double b, double c, double d) {
        return x < a + c && x + w > a && y < b + d && y + h > b;
    }
}
