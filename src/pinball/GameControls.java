package pinball;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;

/** Maps keyboard and focus events to game actions; ignores key auto-repeat. */
final class GameControls {
    private final GameModel model;
    private final GameState state;

    GameControls(GameModel model) {
        this.model = model;
        this.state = model.state;
    }

    void install(JComponent component) {
        int[] codes = {
            KeyEvent.VK_LEFT,
            KeyEvent.VK_RIGHT,
            KeyEvent.VK_A,
            KeyEvent.VK_D,
            KeyEvent.VK_SPACE,
            KeyEvent.VK_P,
            KeyEvent.VK_R,
            KeyEvent.VK_M,
            KeyEvent.VK_Z
        };
        for (int code : codes) {
            bind(component, code, false);
            bind(component, code, true);
        }
        component.addFocusListener(
                new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent event) {
                        releaseAllAndPause();
                    }
                });
    }

    private void bind(JComponent component, int code, boolean released) {
        String action = (released ? "release-" : "press-") + code;
        component
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(code, 0, released), action);
        component
                .getActionMap()
                .put(
                        action,
                        new AbstractAction() {
                            @Override
                            public void actionPerformed(ActionEvent event) {
                                if (released) release(code);
                                else press(code);
                            }
                        });
    }

    void press(int code) {
        if (state.heldKeys[code]) return;
        state.heldKeys[code] = true;
        switch (code) {
            case KeyEvent.VK_P -> state.paused = !state.paused;
            case KeyEvent.VK_M -> state.muted = !state.muted;
            case KeyEvent.VK_R -> model.reset();
            case KeyEvent.VK_Z -> model.nudge();
            case KeyEvent.VK_SPACE -> {
                if (state.over) model.reset();
            }
            default -> {
                /* Flipper and launcher holds are sampled by the simulation. */
            }
        }
    }

    void release(int code) {
        state.heldKeys[code] = false;
        if (code == KeyEvent.VK_SPACE) model.launch();
    }

    void releaseAllAndPause() {
        Arrays.fill(state.heldKeys, false);
        state.paused = true;
    }
}
