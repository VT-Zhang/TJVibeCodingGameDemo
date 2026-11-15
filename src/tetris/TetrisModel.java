package tetris;

import java.util.*;

/** Game rules and power-ups; has no Swing dependency. */
final class TetrisModel {
    static final int COLS = 10, ROWS = 20;

    enum State {
        READY,
        PLAYING,
        PAUSED,
        OVER
    }

    final Tetromino[][] board = new Tetromino[ROWS][COLS];
    final Deque<Tetromino> queue = new ArrayDeque<>();
    final int[] powers = new int[3];
    private final Random random;
    Tetromino active, held;
    State state;
    int x, y, rotation, score, lines, pieces;
    boolean holdUsed;
    double fallTimer, lockTimer, slowRemaining;
    private int lockResets;
    String message;
    static final double THROW_INTERVAL = 14, THROW_FLIGHT = .8;
    double throwCountdown, throwFlight;
    int throwColumn, throwRow, throwsLanded;
    Tetromino thrownAnimal;
    static final double VACUUM_INTERVAL = 18, VACUUM_FLIGHT = 1.1;

    record VacuumBlock(int column, int row, Tetromino animal) {}

    final List<VacuumBlock> vacuumBlocks = new ArrayList<>();
    double vacuumCountdown, vacuumFlight;
    int blocksVacuumed;

    TetrisModel(Random random) {
        this.random = random;
        reset();
    }

    void reset() {
        for (Tetromino[] row : board) Arrays.fill(row, null);
        queue.clear();
        refill();
        held = null;
        holdUsed = false;
        score = lines = pieces = 0;
        slowRemaining = 0;
        throwCountdown = THROW_INTERVAL;
        throwFlight = 0;
        throwColumn = -1;
        throwRow = 19;
        throwsLanded = 0;
        thrownAnimal = Tetromino.Z;
        vacuumCountdown = VACUUM_INTERVAL;
        vacuumFlight = 0;
        vacuumBlocks.clear();
        blocksVacuumed = 0;
        Arrays.fill(powers, 1);
        spawn();
        state = State.READY;
        message = "Go Colonials! Build a better habitat.";
    }

    private void refill() {
        while (queue.size() < 8) {
            List<Tetromino> bag = new ArrayList<>(Arrays.asList(Tetromino.values()));
            Collections.shuffle(bag, random);
            queue.addAll(bag);
        }
    }

    void start() {
        if (state == State.READY) state = State.PLAYING;
    }

    void togglePause() {
        if (state == State.PLAYING) state = State.PAUSED;
        else if (state == State.PAUSED) state = State.PLAYING;
    }

    int level() {
        return 1 + lines / 10;
    }

    private void spawn() {
        refill();
        active = queue.removeFirst();
        refill();
        x = 3;
        y = 0;
        rotation = 0;
        fallTimer = lockTimer = 0;
        lockResets = 0;
        if (!fits(x, y, rotation)) state = State.OVER;
    }

    boolean fits(int px, int py, int r) {
        for (int[] cell : active.cells(r)) {
            int cx = px + cell[0], cy = py + cell[1];
            if (cx < 0 || cx >= COLS || cy >= ROWS || cy < 0 || board[cy][cx] != null) return false;
        }
        return true;
    }

    boolean move(int dx) {
        if (state != State.PLAYING || !fits(x + dx, y, rotation)) return false;
        boolean grounded = !fits(x, y + 1, rotation);
        x += dx;
        resetLock(grounded);
        return true;
    }

    void rotate(int direction) {
        if (state != State.PLAYING) return;
        int next = Math.floorMod(rotation + direction, 4);
        boolean grounded = !fits(x, y + 1, rotation);
        // Small wall/floor kicks make turns near edges forgiving.
        int[][] kicks = {{0, 0}, {-1, 0}, {1, 0}, {-2, 0}, {2, 0}, {0, -1}, {0, -2}};
        for (int[] kick : kicks)
            if (fits(x + kick[0], y + kick[1], next)) {
                x += kick[0];
                y += kick[1];
                rotation = next;
                resetLock(grounded);
                return;
            }
    }

    private void resetLock(boolean grounded) {
        if (grounded && lockResets < 15) {
            lockTimer = 0;
            lockResets++;
        }
    }

    void softDrop() {
        if (state == State.PLAYING && fits(x, y + 1, rotation)) {
            y++;
            score++;
            fallTimer = 0;
        }
    }

    int ghostY() {
        int gy = y;
        while (fits(x, gy + 1, rotation)) gy++;
        return gy;
    }

    void hardDrop() {
        if (state != State.PLAYING) return;
        int landing = ghostY();
        score += (landing - y) * 2;
        y = landing;
        lock();
    }

    void hold() {
        if (state != State.PLAYING || holdUsed) return;
        Tetromino previous = held;
        held = active;
        if (previous == null) spawn();
        else {
            active = previous;
            x = 3;
            y = rotation = 0;
            fallTimer = lockTimer = 0;
            lockResets = 0;
            if (!fits(x, y, rotation)) state = State.OVER;
        }
        holdUsed = true;
    }

    void update(double dt) {
        if (state != State.PLAYING) return;
        updateTroublemaker(dt);
        updateTimothy(dt);
        slowRemaining = Math.max(0, slowRemaining - dt);
        double interval = Math.max(.075, .72 * Math.pow(.82, level() - 1));
        if (slowRemaining > 0) interval *= 2.5;
        fallTimer += dt;
        if (fallTimer >= interval) {
            fallTimer %= interval;
            if (fits(x, y + 1, rotation)) y++;
        }
        if (!fits(x, y + 1, rotation)) {
            lockTimer += dt;
            if (lockTimer >= .45) lock();
        } else lockTimer = 0;
    }

    private void updateTroublemaker(double dt) {
        if (throwFlight > 0) {
            throwFlight = Math.max(0, throwFlight - dt);
            if (throwFlight == 0) {
                boolean blocked = throwRow < 0 || board[throwRow][throwColumn] != null;
                for (int[] cell : active.cells(rotation))
                    if (x + cell[0] == throwColumn && y + cell[1] == throwRow) blocked = true;
                // Never complete a row outside the normal lock/score cycle.
                boolean lastGap = throwRow >= 0;
                if (throwRow >= 0)
                    for (int c = 0; c < COLS; c++)
                        if (c != throwColumn && board[throwRow][c] == null) lastGap = false;
                if (!blocked && !lastGap) {
                    board[throwRow][throwColumn] = thrownAnimal;
                    throwsLanded++;
                    message = "THE SUIT: One more deliverable! Use a power-up to clean up.";
                } else message = "THE SUIT missed! Back to business.";
                throwCountdown = THROW_INTERVAL;
                throwColumn = -1;
            }
            return;
        }
        throwCountdown = Math.max(0, throwCountdown - dt);
        if (throwCountdown <= 2 && throwColumn < 0) {
            throwColumn = random.nextInt(COLS);
            thrownAnimal = Tetromino.values()[random.nextInt(Tetromino.values().length)];
            message = "INCOMING! The Suit is targeting column " + (throwColumn + 1) + ".";
        }
        if (throwCountdown == 0) {
            throwRow = ROWS - 1;
            for (int r = 0; r < ROWS; r++)
                if (board[r][throwColumn] != null) {
                    throwRow = r - 1;
                    break;
                }
            throwFlight = THROW_FLIGHT;
        }
    }

    private void updateTimothy(double dt) {
        if (vacuumFlight > 0) {
            vacuumFlight = Math.max(0, vacuumFlight - dt);
            if (vacuumFlight == 0) {
                vacuumBlocks.clear();
                vacuumCountdown = VACUUM_INTERVAL;
            }
            return;
        }
        vacuumCountdown = Math.max(0, vacuumCountdown - dt);
        if (vacuumCountdown > 0) return;
        // Peel up to three squares off the rightmost occupied columns.
        // Only settled cells are touched; the falling animal stays under player control.
        for (int c = COLS - 1; c >= 0 && vacuumBlocks.size() < 3; c--) {
            for (int r = 0; r < ROWS && vacuumBlocks.size() < 3; r++) {
                if (board[r][c] == null) continue;
                vacuumBlocks.add(new VacuumBlock(c, r, board[r][c]));
                board[r][c] = null;
                blocksVacuumed++;
            }
        }
        vacuumFlight = VACUUM_FLIGHT;
        message =
                vacuumBlocks.isEmpty()
                        ? "TIMOTHY: Spotless! I'll be back."
                        : "TIMOTHY: Whoosh! Vacuumed "
                                + vacuumBlocks.size()
                                + " blocks. You're welcome!";
    }

    private void lock() {
        for (int[] cell : active.cells(rotation)) board[y + cell[1]][x + cell[0]] = active;
        int cleared = clearLines();
        if (cleared > 0) {
            score += new int[] {0, 100, 300, 500, 800}[cleared] * level();
            lines += cleared;
            int award = (lines - cleared) / 3;
            while (award < lines / 3) {
                int power = award % 3;
                powers[power] = Math.min(3, powers[power] + 1);
                award++;
            }
            message =
                    cleared == 4
                            ? "COLONIAL SWEEP! Four habitats restored."
                            : "+" + cleared + " habitat rows • Go TJ!";
        }
        pieces++;
        holdUsed = false;
        spawn();
    }

    private int clearLines() {
        int write = ROWS - 1, cleared = 0;
        for (int read = ROWS - 1; read >= 0; read--) {
            boolean full = true;
            for (Tetromino cell : board[read])
                if (cell == null) {
                    full = false;
                    break;
                }
            if (full) cleared++;
            else {
                System.arraycopy(board[read], 0, board[write], 0, COLS);
                write--;
            }
        }
        while (write >= 0) Arrays.fill(board[write--], null);
        return cleared;
    }

    void power(int index) {
        if (state != State.PLAYING || index < 0 || index > 2 || powers[index] == 0) return;
        powers[index]--;
        if (index == 0) {
            // Remove the lowest occupied row, then let the rows above it settle.
            int row = ROWS - 1;
            while (row >= 0 && Arrays.stream(board[row]).allMatch(Objects::isNull)) row--;
            if (row < 0) {
                powers[index]++;
                message = "No habitat needs rescuing yet.";
                return;
            }
            for (int r = row; r > 0; r--) System.arraycopy(board[r - 1], 0, board[r], 0, COLS);
            Arrays.fill(board[0], null);
            message = "OWL RESCUE • Lowest occupied row removed!";
        } else if (index == 1) {
            slowRemaining = 12;
            message = "AXOLOTL CHILL • Slower gravity for 12 seconds!";
        } else {
            int centerX = Math.max(1, Math.min(COLS - 2, x + 1));
            int centerY = Math.max(1, Math.min(ROWS - 2, ghostY() + 1));
            for (int r = centerY - 1; r <= centerY + 1; r++)
                for (int c = centerX - 1; c <= centerX + 1; c++) board[r][c] = null;
            message = "FOX BURST • 3×3 space cleared at the landing zone!";
        }
        // Row shifting may bring a settled block into the active piece's space.
        while (!fits(x, y, rotation) && y > 0) y--;
        if (!fits(x, y, rotation)) state = State.OVER;
        lockTimer = 0;
    }
}
