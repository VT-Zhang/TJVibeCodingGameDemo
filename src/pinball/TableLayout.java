package pinball;

/** Playfield geometry in unprojected simulation coordinates. */
final class TableLayout {
    static final double[][] BUMPERS = {{210, 230, 29}, {390, 230, 29}, {300, 330, 38}};
    static final double[][] TARGETS = {{115, 360}, {485, 360}, {300, 115}};

    private TableLayout() {}
}
