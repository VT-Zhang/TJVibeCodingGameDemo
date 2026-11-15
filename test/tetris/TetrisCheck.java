package tetris;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;

/** Dependency-free checks for gameplay, input, and offscreen rendering. */
public final class TetrisCheck {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static TetrisModel game() {
        TetrisModel game = new TetrisModel(new Random(7));
        game.start();
        return game;
    }

    private static void empty(TetrisModel game) {
        for (Tetromino[] row : game.board) Arrays.fill(row, null);
    }

    public static void main(String[] args) throws Exception {
        TetrisModel g = game();
        Set<Tetromino> bag = EnumSet.of(g.active);
        Iterator<Tetromino> it = g.queue.iterator();
        for (int i = 0; i < 6; i++) bag.add(it.next());
        check(bag.size() == 7, "seven-bag supplies every animal");
        for (Tetromino type : Tetromino.values()) {
            check(Arrays.deepEquals(type.cells(0), type.cells(4)), "rotation wraps");
            for (int r = 0; r < 4; r++) {
                Set<String> positions = new HashSet<>();
                for (int[] c : type.cells(r)) positions.add(Arrays.toString(c));
                check(positions.size() == 4, "four unique cells");
            }
        }
        g.active = Tetromino.T;
        g.x = 0;
        g.y = 0;
        g.rotation = 0;
        check(!g.move(-1), "left wall collision");
        g.active = Tetromino.I;
        g.rotation = 1;
        g.x = -2;
        check(g.fits(g.x, g.y, g.rotation), "vertical I fits at wall");
        g.rotate(1);
        check(g.rotation == 2 && g.fits(g.x, g.y, g.rotation), "rotation kicks away from wall");
        g = game();
        Tetromino first = g.active;
        g.hold();
        check(g.held == first, "hold stores species");
        Tetromino next = g.active;
        g.hold();
        check(g.active == next, "hold limited to once per piece");
        g.hardDrop();
        g.hold();
        check(g.active == first, "hold available after locking");
        g = game();
        g.active = Tetromino.I;
        g.rotation = 1;
        g.x = 2;
        g.y = 0;
        for (int r = 16; r < 20; r++) {
            Arrays.fill(g.board[r], Tetromino.O);
            g.board[r][4] = null;
        }
        check(g.ghostY() == 16, "ghost finds four-line well");
        g.hardDrop();
        check(g.lines == 4 && g.score >= 800, "four-row clear scores");
        check(g.powers[0] == 2, "three lines award rescue charge");
        for (Tetromino[] row : g.board)
            for (Tetromino cell : row) check(cell == null, "cleared rows compact");
        g = game();
        g.active = Tetromino.O;
        g.x = 3;
        g.y = 0;
        g.board[19][0] = Tetromino.Z;
        g.board[18][1] = Tetromino.T;
        g.power(0);
        check(
                g.board[19][1] == Tetromino.T && g.board[19][0] == null,
                "rescue removes row and settles above");
        check(
                g.powers[0] == 0 && g.lines == 0,
                "power consumes charge without awarding normal lines");
        g.power(0);
        check(g.board[19][1] == Tetromino.T, "empty charge cannot activate");
        g.power(1);
        check(g.slowRemaining == 12, "chill starts timer");
        g.togglePause();
        double timer = g.slowRemaining;
        int y = g.y;
        g.update(5);
        g.softDrop();
        g.hardDrop();
        g.power(2);
        check(
                g.slowRemaining == timer && g.y == y && g.powers[2] == 1,
                "pause freezes simulation and actions");
        g.togglePause();
        g.update(.5);
        check(g.slowRemaining < timer && g.y == y, "chill slows falling");
        g = game();
        g.active = Tetromino.O;
        g.x = 3;
        g.y = 0;
        for (int c = 3; c <= 5; c++) g.board[19][c] = Tetromino.Z;
        g.power(2);
        check(g.board[19][4] == null && g.powers[2] == 0, "burst clears landing zone");
        g = game();
        g.active = Tetromino.O;
        g.x = 3;
        g.y = 18;
        g.update(.2);
        check(g.pieces == 0, "lock delay allows adjustment");
        g.update(.26);
        check(g.pieces == 1, "grounded piece locks");
        g = game();
        g.queue.clear();
        g.queue.add(Tetromino.O);
        g.board[0][4] = Tetromino.Z;
        g.hardDrop();
        check(g.state == TetrisModel.State.OVER, "blocked spawn ends game");
        g.reset();
        check(
                g.state == TetrisModel.State.READY && g.lines == 0 && g.powers[0] == 1,
                "restart resets run");
        g = game();
        g.power(0);
        check(g.powers[0] == 1, "empty-board rescue refunds charge");
        g = game();
        g.active = Tetromino.O;
        g.x = 3;
        g.y = 0;
        g.throwCountdown = 2.01;
        g.update(.02);
        check(g.throwColumn >= 0 && g.throwFlight == 0, "two-second warning chooses a target");
        g.throwColumn = 0;
        g.throwCountdown = .01;
        g.update(.02);
        check(g.throwFlight > 0 && g.board[19][0] == null, "throw flies before landing");
        g.togglePause();
        double flight = g.throwFlight;
        g.update(1);
        check(g.throwFlight == flight, "pause freezes thrown blocks");
        g.togglePause();
        g.update(.81);
        check(
                g.board[19][0] == g.thrownAnimal && g.throwsLanded == 1,
                "thrown animal lands on board");
        check(g.fits(g.x, g.y, g.rotation), "throw preserves active piece");
        g.power(0);
        check(g.board[19][0] == null, "rescue cleans up thrown blocks");
        g = game();
        g.active = Tetromino.O;
        g.x = 3;
        g.y = 18;
        g.throwColumn = 4;
        g.throwCountdown = .01;
        g.update(.02);
        g.update(.81);
        check(g.throwsLanded == 0, "throw misses rather than overwriting active piece");
        g = game();
        g.throwColumn = 0;
        g.throwCountdown = .01;
        g.board[19][0] = Tetromino.T;
        g.update(.02);
        check(g.throwRow == 18, "throw targets top of stack");
        g.board[18][0] = Tetromino.I;
        g.update(.81);
        check(g.board[18][0] == Tetromino.I, "throw cannot overwrite settled blocks");
        g.reset();
        check(
                g.throwColumn == -1 && g.throwFlight == 0 && g.throwCountdown == 14,
                "restart clears nuisance timers");
        g = game();
        g.board[17][9] = Tetromino.Z;
        g.board[18][9] = Tetromino.O;
        g.board[19][9] = Tetromino.T;
        g.board[19][8] = Tetromino.L;
        g.vacuumCountdown = .01;
        g.update(.02);
        check(
                g.vacuumBlocks.size() == 3 && g.blocksVacuumed == 3,
                "Timothy collects up to three blocks");
        check(
                g.board[17][9] == null && g.board[18][9] == null && g.board[19][9] == null,
                "vacuum removes rightmost settled squares");
        check(
                g.board[19][8] == Tetromino.L && g.fits(g.x, g.y, g.rotation),
                "vacuum leaves remaining stack and active animal intact");
        check(g.score == 0 && g.lines == 0, "vacuum does not award line-clear points");
        g.togglePause();
        double suction = g.vacuumFlight;
        g.update(2);
        check(
                g.vacuumFlight == suction && g.vacuumBlocks.size() == 3,
                "pause freezes suction animation");
        g.togglePause();
        g.update(1.2);
        check(
                g.vacuumBlocks.isEmpty() && g.vacuumCountdown == 18,
                "vacuum flight finishes and schedules next cleanup");
        g.vacuumCountdown = .01;
        g.update(.02);
        check(
                g.vacuumBlocks.size() == 1 && g.blocksVacuumed == 4,
                "vacuum handles fewer than three blocks");
        g.reset();
        g.start();
        g.vacuumCountdown = .01;
        g.update(.02);
        check(g.vacuumBlocks.isEmpty() && g.blocksVacuumed == 0, "empty-board cleanup is safe");
        g.reset();
        check(
                g.vacuumFlight == 0 && g.vacuumCountdown == 18 && g.vacuumBlocks.isEmpty(),
                "reset clears Timothy's state");
        g.update(5);
        check(g.vacuumCountdown == 18, "Timothy waits for game start");
        // Longer deterministic run exercises compaction and state transitions.
        Random random = new Random(41);
        for (int turn = 0; turn < 500; turn++) {
            if (g.state == TetrisModel.State.OVER) {
                g.reset();
                g.start();
            }
            for (int j = 0; j < random.nextInt(4); j++) g.rotate(1);
            for (int j = 0; j < random.nextInt(7); j++) g.move(random.nextBoolean() ? 1 : -1);
            if (turn % 5 == 0) g.power(turn % 3);
            check(g.fits(g.x, g.y, g.rotation), "active remains valid");
            g.hardDrop();
        }
        SwingUtilities.invokeAndWait(() -> {
            try {
                TetrisModel model = game();
                TetrisPanel panel = new TetrisPanel(model, null);
                panel.getActionMap()
                        .get(KeyEvent.VK_P + ":false")
                        .actionPerformed(new ActionEvent(panel, 0, ""));
                check(model.state == TetrisModel.State.PAUSED, "pause key binding");
                panel.getActionMap()
                        .get(KeyEvent.VK_P + ":false")
                        .actionPerformed(new ActionEvent(panel, 0, ""));
                panel.pauseForFocusLoss();
                check(model.state == TetrisModel.State.PAUSED, "focus loss pauses");
                panel.setSize(TetrisPanel.WIDTH, TetrisPanel.HEIGHT);
                BufferedImage image =
                        new BufferedImage(
                                TetrisPanel.WIDTH, TetrisPanel.HEIGHT, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = image.createGraphics();
                panel.paint(graphics);
                graphics.dispose();
                check(image.getRGB(330, 110) != image.getRGB(0, 0), "board renders");
                model.togglePause();
                model.hardDrop();
                model.board[17][9] = Tetromino.S;
                model.board[18][9] = Tetromino.J;
                model.board[19][9] = Tetromino.O;
                model.vacuumCountdown = .01;
                model.throwCountdown = .01;
                model.throwColumn = 0;
                model.update(.02);
                model.update(.25);
                graphics = image.createGraphics();
                panel.paint(graphics);
                graphics.dispose();
                ImageIO.write(
                        image,
                        "png",
                        Path.of(System.getProperty("java.io.tmpdir"), "tj-tetris-preview.png")
                                .toFile());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println(
                "PASS: bag, rotations, walls, hold, drops, scoring, power-ups, pause, lock delay, game over, restart, input, rendering, 500-piece simulation, nuisance warnings/throws/pause/collisions/reset, Timothy suction/pause/reset/empty board");
    }
}
