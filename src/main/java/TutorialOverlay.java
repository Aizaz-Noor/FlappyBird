import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import java.util.prefs.Preferences;

/**
 * TutorialOverlay - First-run tutorial explaining the gravity flip mechanic.
 * Shows only on the player's very first game, then never again.
 * Auto-dismisses after 3 seconds, or on SHIFT/Right-Click/"Got it!" button.
 */
public class TutorialOverlay {
    private boolean active = false;
    private boolean firstRunCompleted;
    private double timer = 0; // Seconds elapsed since overlay appeared
    private static final double AUTO_DISMISS_TIME = 5.0; // seconds (generous for reading)
    private static final Preferences prefs = Preferences.userRoot().node("newtons_glitch");

    // Button dimensions
    private static final double BTN_W = 160;
    private static final double BTN_H = 45;
    private double btnX, btnY;

    // Canvas dimensions
    private double width, height;

    public TutorialOverlay(double canvasWidth, double canvasHeight) {
        this.width = canvasWidth;
        this.height = canvasHeight;
        this.firstRunCompleted = prefs.getBoolean("tutorialCompleted", false);

        // Center the button
        this.btnX = width / 2 - BTN_W / 2;
        this.btnY = height / 2 + 140;
    }

    /**
     * Try to show the tutorial. Only activates if this is the first run ever.
     * Call this when transitioning to PLAYING state for the first time.
     */
    public void tryShow() {
        if (!firstRunCompleted) {
            active = true;
            timer = 0;
        }
    }

    /**
     * Check if the tutorial overlay is currently displayed.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Dismiss the tutorial and mark it as completed permanently.
     */
    public void dismiss() {
        if (!active) return;
        active = false;
        firstRunCompleted = true;
        prefs.putBoolean("tutorialCompleted", true);
    }

    /**
     * Check if a mouse click hit the "Got it!" button.
     * Returns true if click was consumed (button was hit).
     */
    public boolean handleClick(double mx, double my) {
        if (!active) return false;
        if (mx >= btnX && mx <= btnX + BTN_W && my >= btnY && my <= btnY + BTN_H) {
            dismiss();
            return true;
        }
        return false;
    }

    /**
     * Update the tutorial timer. Call every frame with deltaTime in seconds.
     */
    public void update(double dt) {
        if (!active) return;
        timer += dt;
        if (timer >= AUTO_DISMISS_TIME) {
            dismiss();
        }
    }

    /**
     * Render the tutorial overlay on top of the game.
     */
    public void render(GraphicsContext gc) {
        if (!active) return;

        // --- Dark overlay ---
        gc.setFill(Color.rgb(0, 0, 0, 0.75));
        gc.fillRect(0, 0, width, height);

        double centerX = width / 2;
        double centerY = height / 2;
        gc.setTextAlign(TextAlignment.CENTER);

        // --- Animated arrow (bouncing up/down) ---
        double arrowBounce = Math.sin(timer * 4.0) * 25; // Oscillate ±25px
        double arrowX = centerX;
        double arrowBaseY = centerY - 80;

        // Draw upward arrow
        gc.setStroke(Color.rgb(0, 229, 255, 0.9)); // Cyan
        gc.setLineWidth(4);
        double upY = arrowBaseY + arrowBounce - 25;
        gc.strokeLine(arrowX, upY + 30, arrowX, upY);
        gc.strokeLine(arrowX - 10, upY + 10, arrowX, upY);
        gc.strokeLine(arrowX + 10, upY + 10, arrowX, upY);

        // Draw downward arrow
        gc.setStroke(Color.rgb(255, 45, 107, 0.9)); // Pink
        double downY = arrowBaseY - arrowBounce + 25;
        gc.strokeLine(arrowX, downY - 30, arrowX, downY);
        gc.strokeLine(arrowX - 10, downY - 10, arrowX, downY);
        gc.strokeLine(arrowX + 10, downY - 10, arrowX, downY);

        // Double-headed arrow symbol between
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        gc.setFill(Color.WHITE);
        gc.fillText("↕", arrowX, arrowBaseY + 10);

        // --- Small bird representation (circle that flips) ---
        double birdY = arrowBaseY + arrowBounce;
        gc.setFill(Color.YELLOW);
        gc.fillOval(arrowX - 50 - 12, birdY - 12, 24, 24);
        gc.setStroke(Color.rgb(200, 160, 0));
        gc.setLineWidth(2);
        gc.strokeOval(arrowX - 50 - 12, birdY - 12, 24, 24);

        // --- Main instruction text (pulsing alpha) ---
        double textAlpha = 0.7 + 0.3 * Math.sin(timer * 3.0);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        gc.setFill(Color.rgb(255, 255, 255, textAlpha));
        gc.fillText("PRESS  SHIFT  or  RIGHT-CLICK", centerX, centerY + 20);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 34));
        gc.setFill(Color.rgb(0, 229, 255, textAlpha)); // Cyan
        gc.fillText("to FLIP GRAVITY", centerX, centerY + 60);

        // --- Sub-instruction ---
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        gc.setFill(Color.rgb(200, 200, 200, 0.8));
        gc.fillText("Master this mechanic — it's what makes you different!", centerX, centerY + 100);
        gc.fillText("(Tip: Customize your Sound Effects in the Settings menu!)", centerX, centerY + 120);

        // --- "Got it!" button ---
        // Hover-like glow effect based on timer
        double btnGlow = 0.6 + 0.2 * Math.sin(timer * 2.5);
        gc.setFill(Color.rgb(0, 229, 255, btnGlow));
        gc.fillRoundRect(btnX, btnY, BTN_W, BTN_H, 12, 12);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRoundRect(btnX, btnY, BTN_W, BTN_H, 12, 12);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.setFill(Color.WHITE);
        gc.fillText("Got it!", centerX, btnY + BTN_H / 2 + 7);

        // --- Auto-dismiss countdown ---
        double remaining = AUTO_DISMISS_TIME - timer;
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        gc.setFill(Color.rgb(150, 150, 150));
        gc.fillText("Auto-continuing in " + (int) Math.ceil(remaining) + "s...", centerX, btnY + BTN_H + 30);
    }
}
