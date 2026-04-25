import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.prefs.Preferences;

/**
 * StageManager — Drives the 5-stage progression system for Newton's Glitch.
 *
 * Stages:
 *   1  MEMORY_LEAK        (0–14 pipes)   — Pipes only, tutorial pacing
 *   2  FIREWALL_BREACH    (15–29 pipes)  — Lasers engage (all difficulties)
 *   3  KERNEL_PANIC       (30–49 pipes)  — Crushers + weather escalation
 *   4  STACK_OVERFLOW     (50–74 pipes)  — All obstacles, top speed
 *   5  REALITY_RESTORE    (75–99 pipes)  — Maximum chaos
 *   VICTORY               (100+ pipes)   — Boss gauntlet; reality restored
 */
public class StageManager {

    private static final Preferences PREFS = Preferences.userNodeForPackage(StageManager.class);

    // =========================================================
    // STAGE DEFINITIONS
    // =========================================================
    public enum Stage {
        MEMORY_LEAK     (1,  0,  "MEMORY_LEAK",      "SECTOR 1: MEMORY LEAK DETECTED",
                         "THE GLITCH BEGINS..."),
        FIREWALL_BREACH (2, 15,  "FIREWALL_BREACH",  "SECTOR 2: FIREWALL ENGAGED",
                         "SECURITY PROTOCOLS ACTIVE — LASERS ONLINE"),
        KERNEL_PANIC    (3, 30,  "KERNEL_PANIC",     "SECTOR 3: KERNEL PANIC",
                         "ALL SYSTEMS FAILING — CRUSHERS DEPLOYED"),
        STACK_OVERFLOW  (4, 50,  "STACK_OVERFLOW",   "SECTOR 4: STACK OVERFLOW",
                         "RECURSION DEPTH CRITICAL — MAXIMUM SPEED"),
        REALITY_RESTORE (5, 75,  "REALITY_RESTORE",  "SECTOR 5: REALITY RESTORE",
                         "FINAL PUSH — DEBUG EVERYTHING");

        public final int number;
        public final int pipeThreshold;   // baseScore to enter this stage
        public final String codeName;
        public final String banner;
        public final String subtitle;

        Stage(int number, int pipeThreshold, String codeName, String banner, String subtitle) {
            this.number = number;
            this.pipeThreshold = pipeThreshold;
            this.codeName = codeName;
            this.banner = banner;
            this.subtitle = subtitle;
        }
    }

    private static final Stage[] STAGES = Stage.values();
    private static final int VICTORY_THRESHOLD = 100;

    // =========================================================
    // STATE
    // =========================================================
    private Stage currentStage = Stage.MEMORY_LEAK;
    private boolean victoryReached = false;

    // Cinematic transition overlay
    private boolean showTransition = false;
    private double transitionTimer = 0;
    private static final double TRANSITION_DURATION = 2.5; // seconds
    private String transitionBanner  = "";
    private String transitionSubtitle = "";
    private int    transitionStageNum = 1;

    // Victory overlay
    private boolean showVictory = false;
    private double victoryTimer = 0;
    private static final double VICTORY_DURATION = 5.0;

    // Speed bonuses per stage (multiplied into the game's speedMultiplier)
    private static final double[] STAGE_SPEED_BONUS = { 1.0, 1.08, 1.18, 1.30, 1.42 };

    // Rocket unlock tracking
    private boolean rocketUnlocked;
    private static final String PREF_ROCKET = "rocketUnlocked";

    // Stage-change listener callback (set by GameEngine)
    private Runnable onRocketUnlockCallback;

    public StageManager() {
        rocketUnlocked = PREFS.getBoolean(PREF_ROCKET, false);
    }

    // =========================================================
    // UPDATE — call every frame with current baseScore
    // Returns true if a stage transition just happened
    // =========================================================
    public boolean update(int baseScore, double dt) {
        // Countdown transition overlay
        if (showTransition) {
            transitionTimer -= dt;
            if (transitionTimer <= 0) showTransition = false;
        }

        // Victory overlay
        if (showVictory) {
            victoryTimer -= dt;
            if (victoryTimer <= 0) showVictory = false;
            return false;
        }

        // Check for victory
        if (!victoryReached && baseScore >= VICTORY_THRESHOLD) {
            victoryReached = true;
            showVictory = true;
            victoryTimer = VICTORY_DURATION;
            return false;
        }

        // Check for stage transition (walk forward through stages)
        for (int i = STAGES.length - 1; i >= 0; i--) {
            Stage s = STAGES[i];
            if (baseScore >= s.pipeThreshold && s != currentStage && currentStage.number < s.number) {
                currentStage = s;
                triggerTransition(s);
                checkRocketUnlock();
                return true;
            }
        }
        return false;
    }

    private void triggerTransition(Stage s) {
        transitionBanner   = s.banner;
        transitionSubtitle = s.subtitle;
        transitionStageNum = s.number;
        transitionTimer    = TRANSITION_DURATION;
        showTransition     = true;
    }

    private void checkRocketUnlock() {
        if (!rocketUnlocked && currentStage.number >= 3) {
            rocketUnlocked = true;
            PREFS.putBoolean(PREF_ROCKET, true);
            if (onRocketUnlockCallback != null) onRocketUnlockCallback.run();
        }
    }

    // =========================================================
    // RENDER — stage transition cinematic
    // =========================================================
    public void renderTransition(GraphicsContext gc, double w, double h) {
        if (!showTransition) return;

        double alpha = Math.min(1.0, transitionTimer / 0.4);
        if (transitionTimer < 0.5) alpha = transitionTimer / 0.5;

        // Dark overlay
        gc.setFill(Color.rgb(0, 0, 0, 0.82 * alpha));
        gc.fillRect(0, 0, w, h);

        // Coloured accent strip
        Color accent = stageAccentColor(transitionStageNum);
        gc.setFill(Color.rgb(
                (int)(accent.getRed()   * 255),
                (int)(accent.getGreen() * 255),
                (int)(accent.getBlue()  * 255),
                0.9 * alpha));
        gc.fillRect(0, h / 2 - 55, w, 4);
        gc.fillRect(0, h / 2 + 55, w, 4);

        // Stage number label
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 13));
        gc.setFill(Color.rgb(
                (int)(accent.getRed()   * 255),
                (int)(accent.getGreen() * 255),
                (int)(accent.getBlue()  * 255),
                alpha));
        gc.fillText("— STAGE " + transitionStageNum + " —", w / 2, h / 2 - 32);

        // Main banner
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 26));
        gc.setFill(Color.rgb(255, 255, 255, alpha));
        gc.fillText(transitionBanner, w / 2, h / 2 + 8);

        // Subtitle
        gc.setFont(Font.font("Courier New", FontWeight.NORMAL, 14));
        gc.setFill(Color.rgb(200, 200, 200, alpha * 0.85));
        gc.fillText(transitionSubtitle, w / 2, h / 2 + 38);
    }

    // =========================================================
    // RENDER — victory overlay
    // =========================================================
    public void renderVictory(GraphicsContext gc, double w, double h) {
        if (!showVictory) return;

        double alpha = Math.min(1.0, victoryTimer / 0.5);

        gc.setFill(Color.rgb(0, 0, 0, 0.88 * alpha));
        gc.fillRect(0, 0, w, h);

        gc.setTextAlign(TextAlignment.CENTER);

        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
        gc.setFill(Color.rgb(0, 229, 255, alpha));
        gc.fillText("— SYSTEM RESTORED —", w / 2, h / 2 - 55);

        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 38));
        gc.setFill(Color.WHITE);
        gc.fillText("REALITY RESTORED", w / 2, h / 2);

        gc.setFont(Font.font("Courier New", FontWeight.NORMAL, 15));
        gc.setFill(Color.rgb(0, 255, 150, alpha * 0.9));
        gc.fillText("Dr. Newton has debugged the simulation.", w / 2, h / 2 + 40);

        gc.setFont(Font.font("Courier New", FontWeight.NORMAL, 12));
        gc.setFill(Color.rgb(180, 180, 180, alpha * 0.7));
        gc.fillText("Press SPACE to continue", w / 2, h / 2 + 80);
    }

    // =========================================================
    // RENDER — stage HUD label (call from game HUD)
    // =========================================================
    public void renderStageHUD(GraphicsContext gc, double x, double y) {
        Color accent = stageAccentColor(currentStage.number);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 11));
        gc.setFill(accent);
        gc.fillText("STAGE " + currentStage.number + " — " + currentStage.codeName, x, y);
    }

    // =========================================================
    // QUERIES — used by GameEngine for spawn gates
    // =========================================================
    public boolean canSpawnLasers()   { return currentStage.number >= 2; }
    public boolean canSpawnCrushers() { return currentStage.number >= 3; }

    /** Speed bonus multiplier for the current stage */
    public double getStageSpeedBonus() {
        return STAGE_SPEED_BONUS[currentStage.number - 1];
    }

    public Stage getCurrentStage()    { return currentStage; }
    public int   getCurrentStageNum() { return currentStage.number; }
    public boolean isVictoryReached() { return victoryReached; }
    public boolean isShowingTransition() { return showTransition; }
    public boolean isShowingVictory()    { return showVictory; }

    public boolean isRocketUnlocked() { return rocketUnlocked; }

    /** Called by GameEngine to trigger UI notification on rocket unlock */
    public void setOnRocketUnlock(Runnable callback) {
        this.onRocketUnlockCallback = callback;
    }

    /** Reset stage state at the start of a new game */
    public void reset() {
        currentStage   = Stage.MEMORY_LEAK;
        victoryReached = false;
        showTransition = false;
        showVictory    = false;
    }

    // =========================================================
    // HELPERS
    // =========================================================
    private Color stageAccentColor(int stageNum) {
        return switch (stageNum) {
            case 1 -> Color.rgb(0, 229, 255);    // Cyan
            case 2 -> Color.rgb(255, 80, 80);    // Red (laser)
            case 3 -> Color.rgb(255, 140, 0);    // Orange (crusher)
            case 4 -> Color.rgb(220, 0, 220);    // Purple (overflow)
            case 5 -> Color.rgb(0, 255, 150);    // Green (restore)
            default -> Color.WHITE;
        };
    }
}
