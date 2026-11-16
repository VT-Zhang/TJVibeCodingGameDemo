# Actual game screenshots

These PNGs come directly from the existing Swing panels through
`JPanel.paint(Graphics2D)` at each game's native preferred window size. They
replace the earlier AI-generated promotional covers. No generated artwork,
retouching, compositing, or extra labels are added to the screenshots.

The capture tool uses reproducible, scripted gameplay states. It launches a
pinball ball, flies through Flappy Bird with a seeded controller, drops Tetris
pieces and starts both NPC animations, combines real alchemy recipes, and stages
a Capital Dash boss-arena moment after collecting a burger. Some positions and
NPC countdowns are set explicitly to capture representative scenes. These are
offscreen captures of the actual game UI, not recordings of a desktop session.

Personal saves and audio devices are not used. Flappy Bird's panel accepts an
in-memory model for capture; normal launch behavior is unchanged.

| Game | File | Native resolution |
| --- | --- | --- |
| TJHSST Colonials Pinball | [pinball.png](pinball.png) | 1120 × 900 |
| Flappy Bird | [flappybird.png](flappybird.png) | 480 × 720 |
| TJHSST Animal Tetris | [tetris.png](tetris.png) | 1280 × 860 |
| City × Sport Alchemy | [alchemy.png](alchemy.png) | 1160 × 780 |
| Capital Dash | [capitaldash.png](capitaldash.png) | 1100 × 760 |

## Refresh screenshots

From the project root, using JDK 17 or newer:

```sh
javac --release 17 -d out src/*/*.java tools/screenshots/CaptureScreenshots.java
java -Djava.awt.headless=true -cp out CaptureScreenshots
```

The command replaces the five PNGs in this folder. Pass another directory to
preview new captures without replacing them:

```sh
java -Djava.awt.headless=true -cp out CaptureScreenshots /tmp/java-arcade-screenshots
```

The helper uses reflection to access package-private models only from the
capture tool; it never replaces renderers or mutates final fields. Screenshot
pixel appearance may vary slightly with the JDK, installed fonts, and Swing
look and feel.
