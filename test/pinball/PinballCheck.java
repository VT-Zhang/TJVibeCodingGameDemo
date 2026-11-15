package pinball;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

/** Dependency-free regression checks. Run with -Djava.awt.headless=true. */
public final class PinballCheck {
    private static final double STEP = 1.0 / 240;

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static GameModel newGame() {
        GameModel model = new GameModel(new ScoreStore(null));
        model.state.muted = true;
        return model;
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                checkLaunchAndDrain();
                checkObjectives();
                checkCollisions();
                checkControls();
                checkPersistence();
                checkRendering();
                System.out.println(
                        "PASS: launcher, drain, ball save, overload, softer rails, "
                                + "resting contacts, flipper transfer, controls, persistence, rendering");
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    private static void checkLaunchAndDrain() {
        GameModel model = newGame();
        GameState state = model.state;
        model.launch();
        boolean entered = false;
        for (int i = 0; i < 2400; i++) {
            model.step(STEP);
            if (state.ballX < 530 && state.ballY < 500) entered = true;
            check(
                    Double.isFinite(state.ballX) && Double.isFinite(state.velocityY),
                    "finite simulation");
        }
        check(entered, "minimum launch reaches main playfield");
        model.reset();
        state.ready = false;
        state.save = 0;
        state.ballY = 830;
        model.step(STEP);
        check(state.ballNumber == 2 && state.ready, "drain advances ball");
        state.ready = false;
        state.save = 5;
        state.ballY = 830;
        model.step(STEP);
        check(state.ballNumber == 2 && state.saved, "early save preserves ball");
        model.launch();
        check(state.save == 0, "saved ball cannot renew its save window");
        state.ballNumber = 3;
        state.ballY = 830;
        model.step(STEP);
        check(state.over && state.ballNumber == 3, "third drain ends the run");
    }

    private static void checkObjectives() {
        GameModel model = newGame();
        GameState state = model.state;
        state.ready = false;
        java.util.Arrays.fill(state.litTargets, true);
        state.ballX = 300;
        state.ballY = 282;
        state.velocityY = 200;
        model.step(STEP);
        model.step(STEP);
        check(state.overload > 0 && state.score >= 2500, "armed core activates overload");
    }

    private static void checkCollisions() {
        GameModel model = newGame();
        GameState state = model.state;
        state.ballX = 45;
        state.ballY = 400;
        state.velocityX = -400;
        state.velocityY = 120;
        model.physics.wall(35, 170, 35, 650);
        check(
                Math.abs(state.velocityX - 220) < .001 && state.velocityY == 120,
                "rail softens normal rebound without altering tangent");
        state.ballX = 45;
        state.ballY = 400;
        state.velocityX = -10;
        state.velocityY = 0;
        model.physics.wall(35, 170, 35, 650);
        check(Math.abs(state.velocityX) < .001, "gentle contacts do not chatter");
        state.ballX = 230;
        state.ballY = 696;
        state.velocityX = 0;
        state.velocityY = 100;
        model.physics.flipper(190, 715, 0, -8);
        check(
                state.velocityY < 0 && state.velocityY > -600,
                "moving flipper transfers controlled upward motion");
    }

    private static void checkControls() {
        GameModel model = newGame();
        GameState state = model.state;
        PinballPanel panel = new PinballPanel(model);
        panel.getActionMap().get("press-32").actionPerformed(null);
        model.step(.5);
        check(state.charge == .5, "holding Space charges launcher through Swing binding");
        panel.getActionMap().get("release-32").actionPerformed(null);
        check(!state.ready && state.velocityY == -1180, "releasing Space launches charged ball");
        GameControls controls = new GameControls(model);
        controls.press(KeyEvent.VK_P);
        controls.press(KeyEvent.VK_P);
        check(state.paused, "auto-repeat does not toggle pause twice");
        double previousY = state.ballY;
        model.step(STEP);
        check(state.ballY == previousY, "paused simulation does not advance");
        controls.release(KeyEvent.VK_P);
        controls.press(KeyEvent.VK_P);
        controls.release(KeyEvent.VK_P);
        controls.press(KeyEvent.VK_LEFT);
        model.step(STEP);
        check(state.leftFlipperAngle < .32, "left input moves flipper");
        controls.releaseAllAndPause();
        check(
                state.paused && !state.heldKeys[KeyEvent.VK_LEFT],
                "focus loss clears held keys and pauses");
        controls.press(KeyEvent.VK_R);
        check(state.ready && !state.paused && state.score == 0, "restart resets run");
    }

    private static void checkPersistence() throws Exception {
        Path path = Files.createTempFile("neon-reactor-test-", ".score");
        try {
            ScoreStore store = new ScoreStore(path);
            check(store.load() == 0, "empty score file is safe");
            GameModel model = new GameModel(store);
            model.state.muted = true;
            model.state.score = 12345;
            model.state.ready = false;
            model.state.ballNumber = 3;
            model.state.ballY = 830;
            model.step(STEP);
            check(new GameModel(store).state.best == 12345, "completed run persists best score");
            Files.writeString(path, "invalid");
            check(store.load() == 0, "malformed score file is safe");
        } finally {
            Files.deleteIfExists(path);
        }
    }

    private static void checkRendering() throws Exception {
        GameModel model = newGame();
        PinballPanel panel = new PinballPanel(model);
        BufferedImage image = new BufferedImage(1120, 900, BufferedImage.TYPE_INT_RGB);
        panel.setSize(image.getWidth(), image.getHeight());
        Graphics2D graphics = image.createGraphics();
        try {
            panel.paint(graphics);
        } finally {
            graphics.dispose();
        }
        check(image.getRGB(500, 400) != image.getRGB(0, 0), "table is painted over background");
        Path preview = Path.of(System.getProperty("java.io.tmpdir"), "neon-reactor-preview.png");
        ImageIO.write(image, "png", preview.toFile());
    }
}
