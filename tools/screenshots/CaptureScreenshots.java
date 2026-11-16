import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** Captures the actual Swing panels, without changing production code or painting extra artwork. */
public final class CaptureScreenshots {
    private static Path destination;

    private CaptureScreenshots() {}

    public static void main(String[] args) throws Exception {
        destination = Path.of(args.length == 0 ? "docs/covers" : args[0]);
        Files.createDirectories(destination);
        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    pinball();
                    flappyBird();
                    tetris();
                    alchemy();
                    capitalDash();
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            });
        } catch (Exception failure) {
            failure.printStackTrace();
            System.exit(1);
        }
        // FlappyPanel starts a Swing timer in its constructor, even without a window.
        System.exit(0);
    }

    private static void pinball() throws Exception {
        Object model = create("pinball.GameModel", create("pinball.ScoreStore", (Object) null));
        Object state = get(model, "state");
        set(state, "muted", true);
        set(state, "charge", .65);
        call(model, "launch");
        boolean[] keys = (boolean[]) get(state, "heldKeys");
        keys[java.awt.event.KeyEvent.VK_LEFT] = true;
        for (int frame = 0; frame < 340; frame++) {
            call(model, "step", 1.0 / 240);
        }
        capture("pinball", (JPanel) create("pinball.PinballPanel", model));
    }

    private static void flappyBird() throws Exception {
        Object model =
                create(
                        "flappybird.FlappyModel",
                        new Random(19),
                        create("flappybird.ScoreStore", (Object) null));
        JPanel panel = (JPanel) create("flappybird.FlappyPanel", model);
        call(model, "flap");
        for (int frame = 0; frame < 620; frame++) {
            List<?> pipes = (List<?>) get(model, "pipes");
            double target = 300;
            for (Object pipe : pipes) {
                if ((double) get(pipe, "x") + 76 >= 115) {
                    target = (double) get(pipe, "gapY");
                    break;
                }
            }
            if ((double) get(model, "birdY") > target + 8 && (double) get(model, "velocity") > 0) {
                call(model, "flap");
            }
            call(model, "update", 1.0 / 120);
            require(get(model, "state").toString().equals("PLAYING"), "Flappy Bird run ended");
        }
        set(panel, "animation", 620.0 / 120);
        capture("flappybird", panel);
    }

    private static void tetris() throws Exception {
        Object model = create("tetris.TetrisModel", new Random(7));
        call(model, "start");
        int[] positions = {0, 6, 2, 7, 0, 5, 1, 6, 0, 7, 2, 5};
        for (int piece = 0; piece < positions.length; piece++) {
            for (int rotation = 0; rotation < piece % 3; rotation++) {
                call(model, "rotate", 1);
            }
            for (int move = 0; move < 12; move++) {
                call(model, "move", -1);
            }
            for (int move = 0; move < positions[piece]; move++) {
                call(model, "move", 1);
            }
            call(model, "hardDrop");
            require(get(model, "state").toString().equals("PLAYING"), "Tetris stack topped out");
        }
        call(model, "hold");
        // Fast-forward the two NPC countdowns to capture their real animations together.
        set(model, "throwCountdown", .01);
        set(model, "vacuumCountdown", .01);
        call(model, "update", .02);
        call(model, "update", .25);
        capture("tetris", (JPanel) create("tetris.TetrisPanel", model, null));
    }

    private static void alchemy() throws Exception {
        Object model = create("alchemy.AlchemyModel", (Object) null);
        String[][] recipes = {
            {"City", "City"}, {"City", "Metropolis"}, {"Land", "Land"},
            {"City", "Ball"}, {"Ball", "Land"}, {"Ball", "Sun"},
            {"City", "Snow"}, {"Snow", "Land"}, {"Mountain", "Snow"}
        };
        for (String[] recipe : recipes) {
            require(call(model, "combine", recipe[0], recipe[1]) != null, "Invalid alchemy recipe");
        }
        JPanel panel = (JPanel) create("alchemy.AlchemyPanel", model);
        panel.setSize(panel.getPreferredSize());
        layout(panel);
        Object board = get(panel, "board");
        ((List<?>) get(board, "tokens")).clear();
        call(board, "addToken", "Tower", 290, 110);
        call(board, "dropLibrary", "City", 290, 110);
        call(board, "addToken", "City", 30, 110);
        call(board, "addToken", "Basketball", 550, 110);
        call(board, "addToken", "Soccer", 30, 260);
        call(board, "addToken", "Tennis", 290, 260);
        call(board, "addToken", "Oslo", 550, 260);
        call(board, "addToken", "Mountain", 150, 410);
        call(board, "addToken", "Skiing", 410, 410);
        capture("alchemy", panel);
    }

    private static void capitalDash() throws Exception {
        Object model = create("capitaldash.DashModel");
        call(model, "start");
        set(model, "x", 250.0);
        call(model, "update", 1.0 / 120, 0, false);
        require((boolean) get(model, "grown"), "Burger did not enlarge the player");
        // Stage a boss-arena moment; movement, projectiles and art use the actual game code.
        set(model, "x", 3360.0);
        set(model, "bossCooldown", .01);
        call(model, "update", 1.0 / 120, 0, false);
        call(model, "jump");
        for (int frame = 0; frame < 12; frame++) {
            call(model, "update", 1.0 / 120, 0, false);
        }
        capture("capitaldash", (JPanel) create("capitaldash.DashPanel", model));
    }

    private static void capture(String name, JPanel panel) throws Exception {
        panel.setSize(panel.getPreferredSize());
        layout(panel);
        BufferedImage image =
                new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            panel.paint(graphics);
        } finally {
            graphics.dispose();
        }
        Path path = destination.resolve(name + ".png");
        require(ImageIO.write(image, "png", path.toFile()), "PNG encoder unavailable");
        System.out.println(path + " / " + image.getWidth() + " × " + image.getHeight());
    }

    private static void layout(Container container) {
        container.doLayout();
        for (java.awt.Component child : container.getComponents()) {
            if (child instanceof Container nested) {
                layout(nested);
            }
        }
    }

    // Package-private models stay encapsulated; reflection is limited to this documentation tool.
    private static Object create(String name, Object... arguments) throws Exception {
        for (Constructor<?> constructor : Class.forName(name).getDeclaredConstructors()) {
            if (matches(constructor.getParameterTypes(), arguments)) {
                constructor.setAccessible(true);
                return constructor.newInstance(arguments);
            }
        }
        throw new NoSuchMethodException(name);
    }

    private static Object call(Object object, String name, Object... arguments) throws Exception {
        for (Method method : object.getClass().getDeclaredMethods()) {
            if (method.getName().equals(name) && matches(method.getParameterTypes(), arguments)) {
                method.setAccessible(true);
                return method.invoke(object, arguments);
            }
        }
        throw new NoSuchMethodException(object.getClass().getName() + "." + name);
    }

    private static boolean matches(Class<?>[] types, Object[] arguments) {
        if (types.length != arguments.length) {
            return false;
        }
        for (int index = 0; index < types.length; index++) {
            Class<?> type = types[index];
            if (type == int.class) type = Integer.class;
            if (type == double.class) type = Double.class;
            if (type == boolean.class) type = Boolean.class;
            if (arguments[index] != null && !type.isInstance(arguments[index])) {
                return false;
            }
        }
        return true;
    }

    private static Object get(Object object, String name) throws Exception {
        Field field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(object);
    }

    private static void set(Object object, String name, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(object, value);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
