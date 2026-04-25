import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * RocketMode — Unlockable gameplay mode for Newton's Glitch.
 *
 * When active:
 *  - SPACE triggers a powerful rocket BOOST (1.5× jump strength)
 *  - A FUEL BAR depletes while boosting and recharges when idle
 *  - flame trail particles are emitted every frame behind the bird
 *  - game speed gains +15% while mode is enabled
 *
 * Unlocked when the player first reaches Stage 3 (30 pipes).
 * Enabled/disabled via the main menu [R] toggle.
 */
public class RocketMode {

    // Boost physics
    public static final double BOOST_STRENGTH   = 750.0;  // px/s (vs normal 500)
    public static final double SPEED_BONUS       = 1.15;  // 15% faster

    // Fuel system
    private static final double MAX_FUEL         = 5.0;   // seconds of full boost
    private static final double FUEL_DRAIN_RATE  = 1.0;   // units/sec while boosting
    private static final double FUEL_REGEN_RATE  = 0.5;   // units/sec when idle

    private double fuel = MAX_FUEL;
    private boolean isBoosting = false;
    private boolean enabled    = false;    // toggled from menu
    private boolean available  = false;    // true once unlocked

    // Flame particle state
    private static final int FLAME_COUNT = 12;
    private double[] flameX    = new double[FLAME_COUNT];
    private double[] flameY    = new double[FLAME_COUNT];
    private double[] flameLife = new double[FLAME_COUNT]; // 0..1
    private double[] flameSize = new double[FLAME_COUNT];

    private final java.util.Random rng = new java.util.Random();

    // Unlock notification banner
    private boolean showUnlockBanner = false;
    private double  unlockBannerTimer = 0;
    private static final double UNLOCK_BANNER_DURATION = 4.0;

    public RocketMode() {}

    // =========================================================
    // UPDATE — call each frame during PLAYING state
    // =========================================================
    public void update(double dt, double birdX, double birdY, boolean gravityFlipped) {
        if (!enabled) { isBoosting = false; return; }

        // Fuel regen when not boosting
        if (!isBoosting) {
            fuel = Math.min(MAX_FUEL, fuel + FUEL_REGEN_RATE * dt);
        } else {
            fuel = Math.max(0, fuel - FUEL_DRAIN_RATE * dt);
            if (fuel <= 0) isBoosting = false; // ran out
        }

        // Emit flame particles behind the bird
        updateFlames(dt, birdX, birdY, gravityFlipped);

        // Unlock banner countdown
        if (showUnlockBanner) {
            unlockBannerTimer -= dt;
            if (unlockBannerTimer <= 0) showUnlockBanner = false;
        }
    }

    private void updateFlames(double dt, double birdX, double birdY, boolean gravityFlipped) {
        // Move existing particles
        for (int i = 0; i < FLAME_COUNT; i++) {
            if (flameLife[i] > 0) {
                flameLife[i] -= dt * 2.5;
                flameX[i] -= dt * 80;  // drift left
                flameY[i] += (gravityFlipped ? -1 : 1) * dt * (rng.nextDouble() * 40 - 20);
                if (flameLife[i] < 0) flameLife[i] = 0;
            }
        }

        // Spawn a new particle if boosting or just enabled
        if (enabled) {
            for (int i = 0; i < FLAME_COUNT; i++) {
                if (flameLife[i] <= 0) {
                    flameX[i]    = birdX - 10 + rng.nextDouble() * 6;
                    flameY[i]    = birdY + (rng.nextDouble() * 12 - 6);
                    flameLife[i] = 0.5 + rng.nextDouble() * 0.5;
                    flameSize[i] = 6 + rng.nextDouble() * 8;
                    break;  // one new particle per frame
                }
            }
        }
    }

    // =========================================================
    // BOOST — call from GameEngine when player presses SPACE
    //         Returns true if boost was applied (fuel available)
    // =========================================================
    public boolean boost() {
        if (!enabled || fuel <= 0.1) return false;
        isBoosting = true;
        return true;
    }

    /** Stop boosting (bird landed, key released, or external stop) */
    public void stopBoost() { isBoosting = false; }

    // =========================================================
    // RENDER — flame trail
    // =========================================================
    public void renderFlames(GraphicsContext gc) {
        if (!enabled) return;
        gc.save();
        for (int i = 0; i < FLAME_COUNT; i++) {
            if (flameLife[i] <= 0) continue;
            double a = flameLife[i];
            double sz = flameSize[i] * a;

            // Outer orange glow
            gc.setFill(Color.rgb(255, 100, 0, a * 0.5));
            gc.fillOval(flameX[i] - sz, flameY[i] - sz / 2, sz * 2, sz);

            // Inner yellow core
            gc.setFill(Color.rgb(255, 220, 30, a * 0.85));
            gc.fillOval(flameX[i] - sz * 0.5, flameY[i] - sz * 0.25, sz, sz * 0.5);
        }
        gc.restore();
    }

    // =========================================================
    // RENDER — fuel bar HUD (bottom-left area)
    // =========================================================
    public void renderFuelBar(GraphicsContext gc, double x, double y) {
        if (!enabled) return;

        double barW = 110;
        double barH = 10;

        // Background
        gc.setFill(Color.rgb(20, 20, 30, 0.8));
        gc.fillRoundRect(x, y, barW, barH, 5, 5);

        // Fill (green → orange → red based on fuel level)
        double ratio = fuel / MAX_FUEL;
        Color fill;
        if (ratio > 0.5)      fill = Color.rgb(0,  220, 80);
        else if (ratio > 0.2) fill = Color.rgb(255, 160, 0);
        else                   fill = Color.rgb(255,  40, 40);

        gc.setFill(fill);
        gc.fillRoundRect(x, y, barW * ratio, barH, 5, 5);

        // Border
        gc.setStroke(Color.rgb(200, 200, 200, 0.5));
        gc.setLineWidth(1);
        gc.strokeRoundRect(x, y, barW, barH, 5, 5);

        // Label
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 9));
        gc.setFill(Color.rgb(220, 220, 220, 0.9));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("🚀 FUEL", x, y - 3);
    }

    // =========================================================
    // RENDER — unlock notification banner
    // =========================================================
    public void renderUnlockBanner(GraphicsContext gc, double w) {
        if (!showUnlockBanner) return;

        double alpha = Math.min(1.0, unlockBannerTimer / 0.4);

        double bW = 520; double bH = 44;
        double bX = w / 2 - bW / 2;
        double bY = 55;

        gc.setFill(Color.rgb(0, 0, 0, 0.75 * alpha));
        gc.fillRoundRect(bX, bY, bW, bH, 8, 8);
        gc.setStroke(Color.rgb(255, 140, 0, alpha));
        gc.setLineWidth(2);
        gc.strokeRoundRect(bX, bY, bW, bH, 8, 8);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 15));
        gc.setFill(Color.rgb(255, 180, 0, alpha));
        gc.fillText("🚀  ROCKET MODE UNLOCKED!  Press [R] in menu to enable", w / 2, bY + 28);
    }

    // =========================================================
    // UNLOCK NOTIFICATION — triggered by StageManager callback
    // =========================================================
    public void triggerUnlockNotification() {
        showUnlockBanner = true;
        unlockBannerTimer = UNLOCK_BANNER_DURATION;
    }

    // =========================================================
    // MENU TOGGLE — call from GameEngine when player presses R on menu
    // =========================================================
    public void toggle() {
        if (available) {
            enabled = !enabled;
            fuel = MAX_FUEL; // Reset fuel on toggle
        }
    }

    public void setAvailable(boolean v) { this.available = v; }
    public boolean isAvailable()  { return available; }
    public boolean isEnabled()    { return enabled; }
    public boolean isBoosting()   { return isBoosting; }
    public double  getFuelRatio() { return fuel / MAX_FUEL; }

    /** Reset per-game state (called on startGame) */
    public void reset() {
        fuel      = MAX_FUEL;
        isBoosting = false;
        java.util.Arrays.fill(flameLife, 0);
    }
}
