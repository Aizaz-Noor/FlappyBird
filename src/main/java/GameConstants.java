import javafx.scene.paint.Color;

/**
 * GameConstants — Single source of truth for all display labels and layout values.
 *
 * Rule: If a string or number appears in more than one place in the UI,
 * it belongs here. Never type a difficulty label or layout gap as a raw literal twice.
 */
public final class GameConstants {

    private GameConstants() {} // Utility class — no instances

    // ── Difficulty Display Labels ────────────────────────────────────────────────
    // These are the player-facing names. The underlying enum stays EASY/MEDIUM/HARD.
    public static final String DIFF_LABEL_EASY    = "EASY";
    public static final String DIFF_LABEL_NORMAL  = "NORMAL";
    public static final String DIFF_LABEL_EXTREME = "EXTREME";

    // ── Difficulty Badge Colors ──────────────────────────────────────────────────
    public static final Color DIFF_COLOR_EASY    = Color.rgb(46,  204, 113); // Green
    public static final Color DIFF_COLOR_NORMAL  = Color.rgb(241, 196,  15); // Amber
    public static final Color DIFF_COLOR_EXTREME = Color.rgb(231,  76,  60); // Red

    // ── Canvas Layout — Vertical Stack Spacing ───────────────────────────────────
    // Equivalent of VBox.setSpacing() for Canvas-drawn menus.
    // All vertical gaps between UI blocks use one of these constants.
    public static final double SPACING_SM  = 14;  // Tight: label → value
    public static final double SPACING_MD  = 22;  // Standard: between rows
    public static final double SPACING_LG  = 30;  // Required: between sections
    public static final double SPACING_XL  = 50;  // Hero gaps: header → body

    // ── Difficulty Select Screen Layout ──────────────────────────────────────────
    public static final double DIFF_BTN_W  = 240;
    public static final double DIFF_BTN_H  = 90;
    public static final double DIFF_BTN_SP = 60;  // Gap between the 3 buttons

    // ── Font sizes ───────────────────────────────────────────────────────────────
    public static final double FONT_HERO   = 52;  // Screen titles
    public static final double FONT_BODY   = 20;  // Subtitles / descriptors
    public static final double FONT_LABEL  = 16;  // Small hints / footnotes
    public static final double FONT_BTN    = 32;  // Button primary text
    public static final double FONT_BTN_SM = 13;  // Button secondary (key hint)

    // ── Helpers ──────────────────────────────────────────────────────────────────

    /**
     * Returns the player-facing display label for a given difficulty enum value.
     * Use this everywhere instead of Difficulty.name() or raw string literals.
     */
    public static String diffLabel(GameEngine.Difficulty d) {
        return switch (d) {
            case EASY   -> DIFF_LABEL_EASY;
            case MEDIUM -> DIFF_LABEL_NORMAL;
            case HARD   -> DIFF_LABEL_EXTREME;
        };
    }

    /**
     * Returns the canonical HUD color for a difficulty level.
     */
    public static Color diffColor(GameEngine.Difficulty d) {
        return switch (d) {
            case EASY   -> DIFF_COLOR_EASY;
            case MEDIUM -> DIFF_COLOR_NORMAL;
            case HARD   -> DIFF_COLOR_EXTREME;
        };
    }
}
