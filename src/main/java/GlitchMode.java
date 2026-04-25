import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import java.util.Random;

/**
 * GlitchMode - Every 20 pipes, reality breaks for 5 seconds.
 * Visual: CRT scanlines, screen noise, RGB aberration indicator.
 * Gameplay: one random mutation per glitch event (double gravity, invisible pipes, etc.)
 */
public class GlitchMode {
    private boolean active = false;
    private double timer = 0;            // Time elapsed in current glitch
    private static final double DURATION = 5.0; // seconds
    private static final int TRIGGER_INTERVAL = 20; // every N pipes
    private int lastTriggerScore = 0;
    private final Random random = new Random();

    // Warning state
    public boolean isWarning(int currentScore) {
        return !active && !isExiting && currentScore > 0 && (currentScore % TRIGGER_INTERVAL >= TRIGGER_INTERVAL - 3) && (currentScore % TRIGGER_INTERVAL != 0);
    }
    
    // Exit state
    private boolean isExiting = false;
    private double exitTimer = 0;

    // Current gameplay effect
    public enum GlitchEffect {
        DOUBLE_GRAVITY,      // Gravity 2x stronger
        INVISIBLE_PIPES,     // Pipes at 20% opacity for 3s
        REVERSE_GRAVITY,     // Auto-flip gravity for 2s
        SPEED_BURST          // 50% speed increase for 4s
    }
    private GlitchEffect currentEffect = null;

    // Screen noise data (reused per frame)
    private double[][] noisePositions;
    private static final int NOISE_COUNT = 150;

    // Warning text flicker
    private double flickerAlpha = 1.0;

    public GlitchMode() {
        noisePositions = new double[NOISE_COUNT][2];
    }

    /**
     * Check if glitch should trigger based on score.
     * Call this when score increases.
     */
    public void checkTrigger(int baseScore) {
        if (!active && baseScore > 0 && baseScore % TRIGGER_INTERVAL == 0 
                && baseScore != lastTriggerScore) {
            activate();
            lastTriggerScore = baseScore;
        }
    }

    private void activate() {
        active = true;
        timer = 0;
        // Pick random gameplay effect
        GlitchEffect[] effects = GlitchEffect.values();
        currentEffect = effects[random.nextInt(effects.length)];
    }

    public void update(double dt) {
        if (isExiting) {
            exitTimer += dt;
            if (exitTimer >= 0.4) isExiting = false;
        }

        if (!active) return;
        timer += dt;
        flickerAlpha = 0.6 + 0.4 * Math.sin(timer * 12); // Fast flicker

        if (timer >= DURATION) {
            active = false;
            currentEffect = null;
            isExiting = true;
            exitTimer = 0;
        }
    }

    /**
     * Render ALL visual glitch effects on top of the game canvas.
     */
    public void render(GraphicsContext gc, double width, double height) {
        if (!active && !isExiting) return;

        double overallAlpha = isExiting ? Math.max(0, 1.0 - (exitTimer / 0.4)) : 1.0;
        gc.setGlobalAlpha(overallAlpha);

        // 1. CRT Scanlines
        gc.setStroke(Color.rgb(0, 0, 0, 0.15));
        gc.setLineWidth(1);
        for (double y = 0; y < height; y += 3) {
            gc.strokeLine(0, y, width, y);
        }

        // 2. Screen noise
        for (int i = 0; i < NOISE_COUNT; i++) {
            double nx = random.nextDouble() * width;
            double ny = random.nextDouble() * height;
            int r = random.nextInt(256);
            int g = random.nextInt(256);
            int b = random.nextInt(256);
            gc.setFill(Color.rgb(r, g, b, 0.06));
            gc.fillRect(nx, ny, 2, 2);
        }

        // 3. Subtle red overlay
        gc.setFill(Color.rgb(255, 0, 0, 0.03));
        gc.fillRect(0, 0, width, height);

        if (isExiting) {
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFont(Font.font(NarrativeStrings.FONT_NARRATIVE, FontWeight.BOLD, 22));
            gc.setFill(NarrativeStrings.COLOR_CYAN);
            gc.fillText(NarrativeStrings.GLITCH_STABILIZING, width / 2, height / 2);
            gc.setGlobalAlpha(1.0);
            return;
        }

        // --- GLITCH ENTRY (first 600ms) ---
        if (timer < 0.6) {
            if (timer < 0.1) {
                gc.setFill(Color.WHITE);
                gc.fillRect(0, 0, width, height);
            }
            
            // Screen tears
            if (timer > 0.1 && timer < 0.3) {
                for (int i=0; i<3; i++) {
                    gc.setFill(Color.rgb(255, 255, 255, 0.8));
                    gc.fillRect(random.nextDouble() * 50 - 25, random.nextDouble() * height, width, random.nextDouble() * 10 + 2);
                }
            }
            
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFont(Font.font(NarrativeStrings.FONT_NARRATIVE, FontWeight.BOLD, 28));
            gc.setFill(NarrativeStrings.COLOR_CRITICAL);
            
            String entryText = switch (currentEffect) {
                case DOUBLE_GRAVITY -> NarrativeStrings.GLITCH_DOUBLE_GRAVITY;
                case INVISIBLE_PIPES -> NarrativeStrings.GLITCH_INVISIBLE_PIPES;
                case REVERSE_GRAVITY -> NarrativeStrings.GLITCH_REVERSE_GRAVITY;
                case SPEED_BURST -> NarrativeStrings.GLITCH_SPEED_BURST;
            };
            gc.fillText(entryText, width / 2 + (random.nextDouble() * 4 - 2), height / 2);
        } else {
            // --- GLITCH NORMAL (after 600ms) ---
            gc.setTextAlign(TextAlignment.RIGHT);
            gc.setFont(Font.font(NarrativeStrings.FONT_NARRATIVE, FontWeight.BOLD, 18));
            gc.setFill(Color.rgb(255, 60, 60, flickerAlpha));
            gc.fillText(String.format("GLITCH MODE // %.1fs REMAINING", DURATION - timer), width - 20, 80);
        }

        gc.setGlobalAlpha(1.0);
    }

    // --- Getters for GameEngine to apply gameplay effects ---

    public boolean isActive() { return active; }
    public GlitchEffect getCurrentEffect() { return currentEffect; }
    public double getTimer() { return timer; }

    /**
     * Get gravity multiplier (2.0 during DOUBLE_GRAVITY, 1.0 otherwise).
     */
    public double getGravityMultiplier() {
        if (active && currentEffect == GlitchEffect.DOUBLE_GRAVITY) return 2.0;
        return 1.0;
    }

    /**
     * Get pipe opacity (0.2 during INVISIBLE_PIPES for first 3s, 1.0 otherwise).
     */
    public double getPipeOpacity() {
        if (active && currentEffect == GlitchEffect.INVISIBLE_PIPES && timer < 3.0) return 0.2;
        return 1.0;
    }

    /**
     * Should gravity auto-reverse? (Only during REVERSE_GRAVITY, first 2s).
     */
    public boolean shouldAutoReverseGravity() {
        return active && currentEffect == GlitchEffect.REVERSE_GRAVITY && timer < 2.0;
    }

    /**
     * Get speed multiplier (1.5 during SPEED_BURST for first 4s, 1.0 otherwise).
     */
    public double getSpeedMultiplier() {
        if (active && currentEffect == GlitchEffect.SPEED_BURST && timer < 4.0) return 1.5;
        return 1.0;
    }

    /**
     * Reset state for new game.
     */
    public void reset() {
        active = false;
        isExiting = false;
        timer = 0;
        exitTimer = 0;
        currentEffect = null;
        lastTriggerScore = 0;
    }
}
