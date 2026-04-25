import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import java.util.List;
import java.util.Random;

public class UIManager {
    private final IGameState gameState;
    private final GraphicsContext gc;
    private final javafx.scene.canvas.Canvas canvas;
    private final double WIDTH = GameEngine.WIDTH;
    private final double HEIGHT = GameEngine.HEIGHT;
    private final Random rng = new Random();

    // Game-over animation state
    private long gameOverOpenTime = 0;
    private double scanLineY = 0;
    private boolean scanComplete = false;

    // Cinematic death effects
    private static final ColorAdjust DEATH_DESATURATE = new ColorAdjust(0, -0.85, -0.15, 0.05);
    private static final BoxBlur   DEATH_BLUR        = new BoxBlur(6, 6, 2);

    public UIManager(IGameState gameState, GraphicsContext gc) {
        this.gameState = gameState;
        this.gc = gc;
        this.canvas = gc.getCanvas();
    }

    public void renderNameInput() {
        gc.setFill(Color.rgb(0, 0, 0, 0.2));
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 42));
        gc.setFill(Color.GOLD);
        gc.fillText("Welcome!", WIDTH / 2, HEIGHT / 2 - 100);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 24));
        gc.setFill(Color.WHITE);
        gc.fillText("Enter your name:", WIDTH / 2, HEIGHT / 2 - 40);
        gc.setFill(Color.WHITE);
        gc.fillRoundRect(WIDTH / 2 - 120, HEIGHT / 2 - 20, 240, 45, 10, 10);
        gc.setStroke(Color.GOLD);
        gc.setLineWidth(2);
        gc.strokeRoundRect(WIDTH / 2 - 120, HEIGHT / 2 - 20, 240, 45, 10, 10);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        String display = gameState.getNameInput().length() > 0 ? gameState.getNameInput().toString() : "Flappy";
        gc.fillText(display + "_", WIDTH / 2, HEIGHT / 2 + 12);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        gc.setFill(Color.LIGHTGRAY);
        gc.fillText("Press ENTER to continue", WIDTH / 2, HEIGHT / 2 + 70);
    }

    public void renderDifficultySelect() {
        // ── Full dark overlay ────────────────────────────────────────────────────
        gc.setFill(Color.rgb(0, 0, 0, 0.2));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // ── Center-Anchored Vertical Stack ───────────────────────────────────────
        // All elements are pinned to a single anchor (stackTop) with SPACING_* gaps.
        // Changing any one element's size never disturbs the others.
        double stackTop = HEIGHT / 2 - 230;

        // Row 0: Header title
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, (int) GameConstants.FONT_HERO));
        gc.setFill(Color.GOLD);
        gc.fillText("SELECT DIFFICULTY", WIDTH / 2, stackTop);

        // Row 1: Subtitle  (+SPACING_LG from header baseline)
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, (int) GameConstants.FONT_BODY));
        gc.setFill(Color.rgb(200, 200, 200));
        gc.fillText("Choose your challenge level", WIDTH / 2, stackTop + GameConstants.SPACING_LG + 20);

        // Row 2: Three difficulty buttons  (+SPACING_XL from subtitle)
        double bW = GameConstants.DIFF_BTN_W;
        double bH = GameConstants.DIFF_BTN_H;
        double sp = GameConstants.DIFF_BTN_SP;
        double bY = stackTop + GameConstants.SPACING_XL + 60;
        double total = (bW * 3) + (sp * 2);
        double sX = (WIDTH - total) / 2;

        renderDifficultyButton(sX,            bY, bW, bH,
            GameConstants.DIFF_LABEL_EASY,    "1", GameEngine.Difficulty.EASY,
            GameConstants.DIFF_COLOR_EASY,    Color.WHITE);
        renderDifficultyButton(sX + bW + sp,  bY, bW, bH,
            GameConstants.DIFF_LABEL_NORMAL,  "2", GameEngine.Difficulty.MEDIUM,
            GameConstants.DIFF_COLOR_NORMAL,  Color.rgb(40, 40, 40));
        renderDifficultyButton(sX + (bW + sp) * 2, bY, bW, bH,
            GameConstants.DIFF_LABEL_EXTREME, "3", GameEngine.Difficulty.HARD,
            GameConstants.DIFF_COLOR_EXTREME, Color.WHITE);

        // Row 3: Footer hint  (+SPACING_LG from buttons bottom)
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, (int) GameConstants.FONT_LABEL));
        gc.setFill(Color.GOLD);
        gc.fillText("Press ESC to skip  (uses " + GameConstants.DIFF_LABEL_NORMAL + " difficulty)",
            WIDTH / 2, bY + bH + GameConstants.SPACING_LG + 10);
    }

    private void renderDifficultyButton(double x, double y, double w, double h, String text, String key, GameEngine.Difficulty diff, Color bgColor, Color textColor) {
        boolean sel = gameState.getCurrentDifficulty() == diff;
        if (sel) { gc.setFill(Color.rgb(255, 215, 0, 0.15)); gc.fillRoundRect(x - 10, y - 10, w + 20, h + 20, 18, 18); }
        gc.setFill(bgColor); gc.fillRoundRect(x, y, w, h, 12, 12);
        gc.setStroke(sel ? Color.GOLD : Color.WHITE); gc.setLineWidth(sel ? 4 : 2); gc.strokeRoundRect(x, y, w, h, 12, 12);
        gc.setFill(textColor); gc.setFont(Font.font("Arial", FontWeight.BOLD, 32)); gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(text, x + w / 2, y + h / 2 + 5);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 13)); gc.fillText("Press " + key, x + w / 2, y + h - 18);
    }

    public void renderMenu() {
        gc.setFill(Color.rgb(0, 0, 0, 0.8));
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        double alpha = gameState.getStorySystem().getControlsAlpha();
        if (alpha > 0) {
            gc.setGlobalAlpha(alpha);
            if (gameState.getHighScore() > 0) {
                gc.setFont(Font.font(NarrativeStrings.FONT_NARRATIVE, FontWeight.BOLD, 18));
                gc.setFill(NarrativeStrings.COLOR_CYAN);
                gc.fillText("HIGH SCORE: " + String.format("%03d", gameState.getHighScore()), WIDTH / 2, 40);
            }
            if (gameState.getRocketMode().isAvailable()) {
                boolean rOn = gameState.getRocketMode().isEnabled();
                gc.setFont(Font.font(NarrativeStrings.FONT_NARRATIVE, FontWeight.BOLD, 14));
                gc.setFill(rOn ? NarrativeStrings.COLOR_WARNING : NarrativeStrings.COLOR_GRAY);
                gc.fillText("> ROCKET_PROTOCOL: [" + (rOn ? "ACTIVE" : "OFFLINE") + "]  (R to toggle)", WIDTH / 2, 70);
            }
            gc.setGlobalAlpha(1.0);
        }
        renderControlsCard();
    }

    private void renderControlsCard() {
        double alpha = gameState.getStorySystem().getControlsAlpha();
        if (alpha <= 0) return;
        double cW = 340, cH = 90, cX = WIDTH / 2 - cW / 2, cY = HEIGHT - 130;
        gc.setFill(Color.rgb(20, 24, 30, 0.8 * alpha)); gc.fillRoundRect(cX, cY, cW, cH, 12, 12);
        gc.setStroke(Color.rgb(255, 255, 255, 0.15 * alpha)); gc.strokeRoundRect(cX, cY, cW, cH, 12, 12);
        gc.setFill(Color.rgb(255, 255, 255, alpha)); gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        double tx1 = cX + 20, ty = cY + 25, lH = 22;
        gc.fillText("SPACE / Click  \u2192  Flap", tx1, ty);
        gc.fillText("SHIFT / R-Click \u2192  Flip Gravity", tx1, ty + lH);
        gc.fillText("P \u2192 Pause          S \u2192 Settings", tx1, ty + lH * 2);
        gc.fillText("A \u2192 Avatar         R \u2192 Replay", tx1 + 170, ty + lH * 2);
    }

    public void renderHUD() {
        if (gameState.getGameState() != GameEngine.GameState.PLAYING && gameState.getGameState() != GameEngine.GameState.PAUSED) return;
        double integrity = gameState.getStability();
        double barW = 140, barH = 12, barX = 20, barY = 35;
        gc.setFill(NarrativeStrings.COLOR_GRAY); gc.setFont(Font.font(NarrativeStrings.FONT_NARRATIVE, FontWeight.BOLD, 10));
        gc.setTextAlign(TextAlignment.LEFT); gc.fillText(NarrativeStrings.HUD_INTEGRITY, barX, barY - 8);
        gc.setFill(Color.rgb(40, 45, 50, 0.7)); gc.fillRoundRect(barX, barY, barW, barH, 4, 4);
        gc.setFill(integrity > 0.4 ? NarrativeStrings.COLOR_CYAN : NarrativeStrings.COLOR_CRITICAL);
        gc.fillRoundRect(barX, barY, barW * integrity, barH, 4, 4);
        gc.setStroke(Color.rgb(255, 255, 255, 0.15)); gc.strokeRoundRect(barX, barY, barW, barH, 4, 4);
        gc.setTextAlign(TextAlignment.CENTER); gc.setFont(Font.font(NarrativeStrings.FONT_NARRATIVE, FontWeight.BOLD, 22));
        gc.setFill(NarrativeStrings.COLOR_LORE);
        int ds = gameState.getScore(); if (System.currentTimeMillis() % 8000 < 150) ds += (int)(Math.random() * 10 - 5);
        gc.fillText(NarrativeStrings.HUD_NODES + String.format("%03d", ds), WIDTH / 2, 45);
        gc.setTextAlign(TextAlignment.RIGHT); gc.setFont(Font.font(NarrativeStrings.FONT_NARRATIVE, FontWeight.BOLD, 14));
        String sec = NarrativeStrings.HUD_SECTOR + String.format("%02d", gameState.getStageManager().getCurrentStageNum());
        String st = NarrativeStrings.HUD_CORRUPTED;
        if (gameState.isGlitchWarningActive()) { st = NarrativeStrings.HUD_INSTABILITY; gc.setFill((System.currentTimeMillis() % 400 < 200) ? NarrativeStrings.COLOR_CRITICAL : Color.TRANSPARENT); }
        else gc.setFill(NarrativeStrings.COLOR_WARNING);
        gc.fillText(sec + st, WIDTH - 20, 45);
        double avX = 20, avY = HEIGHT - 50; AvatarManager am = gameState.getAvatarManager();
        am.renderAvatar(gc, avX + 25, avY + 15, 0, 30);
        gc.setTextAlign(TextAlignment.LEFT); gc.setFont(Font.font(NarrativeStrings.FONT_NARRATIVE, FontWeight.NORMAL, 12));
        gc.setFill(NarrativeStrings.COLOR_GRAY); gc.fillText(am.getCurrentAvatarName() + " // UNIT #00" + (am.getCurrentAvatarIndex() + 1), avX, avY + 42);
        double px = GameEngine.PAUSE_X, py = GameEngine.PAUSE_Y;
        gc.setFill(Color.rgb(255, 255, 255, 0.2)); gc.fillOval(px, py, GameEngine.PAUSE_SIZE, GameEngine.PAUSE_SIZE);
        gc.setFill(Color.WHITE);
        if (gameState.getGameState() == GameEngine.GameState.PAUSED) gc.fillPolygon(new double[]{px+10, px+10, px+25}, new double[]{py+8, py+22, py+15}, 3);
        else { gc.fillRect(px+10, py+8, 4, 14); gc.fillRect(px+18, py+8, 4, 14); }
    }

    public void renderPauseOverlay() {
        gc.setFill(Color.rgb(0, 0, 0, 0.6)); gc.fillRect(0, 0, WIDTH, HEIGHT);
        gc.setTextAlign(TextAlignment.CENTER); gc.setFont(Font.font("Arial", FontWeight.BOLD, 64));
        gc.setFill(Color.WHITE); gc.fillText("PAUSED", WIDTH / 2, HEIGHT / 2);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 20)); gc.setFill(Color.LIGHTGRAY);
        gc.fillText("Press P to resume", WIDTH / 2, HEIGHT / 2 + 50);
    }

    // Death headline words mapped to cause keywords
    private static final String[] DEATH_WORDS = { "VOIDED", "CRASHED", "FAILED", "NULL" };

    private String getDeathWord() {
        String cause = gameState.getDeathCause();
        if (cause == null) return "VOIDED";
        if (cause.contains("Laser"))   return "CRASHED";
        if (cause.contains("Crusher")) return "FAILED";
        if (cause.contains("Ceiling") || cause.contains("Floor")) return "NULL";
        return "VOIDED"; // pipe
    }

    public void notifyGameOver() {
        gameOverOpenTime = System.currentTimeMillis();
        scanLineY = 0;
        scanComplete = false;
    }

    public void applyGameOverEffectTo(GraphicsContext g) {
        DEATH_BLUR.setInput(DEATH_DESATURATE);
        g.setEffect(DEATH_BLUR);
    }

    public void clearGameOverEffectFrom(GraphicsContext g) {
        g.setEffect(null);
    }

    /** Call when leaving the game-over screen to restore canvas */
    public void clearGameOverEffect() {
        canvas.setEffect(null); // Keep as safety catch
    }

    public void renderGameOver() {
        long elapsed = System.currentTimeMillis() - gameOverOpenTime;
        if (gameOverOpenTime == 0) { gameOverOpenTime = System.currentTimeMillis(); elapsed = 0; }

        long now = System.currentTimeMillis();
        double cy = HEIGHT / 2.0;

        // ── PHASE 0 (0–500ms): Cinematic red-dark wash + simulated Gaussian blur ──────
        double washAlpha = Math.min(1.0, elapsed / 500.0);

        // Bokeh blur simulation: 4 passes of semi-transparent dark rects with slight offsets
        // This mimics the look of a Gaussian blur cheaply at 60FPS
        double[] blurOffsets = {0, 2, -2, 1, -1};
        for (double off : blurOffsets) {
            gc.setFill(Color.rgb(8, 2, 4, 0.16 * washAlpha));
            gc.fillRect(off, off, WIDTH, HEIGHT);
        }
        // Main overlay — deep crimson-black
        gc.setFill(Color.rgb(12, 3, 6, 0.65 * washAlpha));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // Radial red vignette from edges
        gc.setFill(Color.rgb(180, 0, 15, 0.30 * washAlpha));
        gc.fillRect(0, 0, WIDTH, 6);
        gc.fillRect(0, HEIGHT - 6, WIDTH, 6);
        gc.fillRect(0, 0, 6, HEIGHT);
        gc.fillRect(WIDTH - 6, 0, 6, HEIGHT);
        for (int i = 1; i <= 4; i++) {
            gc.setFill(Color.rgb(160, 0, 12, 0.05 * ((5 - i) / 5.0) * washAlpha));
            gc.fillRect(i * 8, i * 8, WIDTH - i * 16, HEIGHT - i * 16);
        }

        // CRT scanline texture
        gc.setStroke(Color.rgb(0, 0, 0, 0.10 * washAlpha));
        gc.setLineWidth(1);
        for (double y = 0; y < HEIGHT; y += 3) gc.strokeLine(0, y, WIDTH, y);

        // ── PHASE 1 (50–700ms): VOIDED — breathing + neon-blue glow ─────────────────
        if (elapsed > 50) {
            double wordAlpha = Math.min(1.0, (elapsed - 50) / 650.0);
            String word = getDeathWord();

            // Breathing (slow-zoom) animation: gentle oscillation ±3% scale
            double breathPhase = (now / 1800.0) * Math.PI * 2.0;
            double breathScale = 1.0 + 0.03 * Math.sin(breathPhase);

            // Glitch jitter (fires ~12% of frames)
            double jitter = (now % 100 < 12) ? rng.nextDouble() * 5 - 2.5 : 0;

            double wordY   = cy - 90;
            double baseSize = 100;
            double scaledSize = baseSize * breathScale;

            // Glitchy red/cyan split (RGB channel shift effect)
            double channelJitter = (now % 100 < 50) ? 2.5 * wordAlpha : -2.5 * wordAlpha;
            gc.setTextAlign(TextAlignment.CENTER);

            // Red channel shift
            gc.setFill(Color.rgb(255, 0, 80, wordAlpha * 0.7));
            gc.fillText(word, WIDTH / 2 + channelJitter, wordY + 2);
            // Cyan channel shift
            gc.setFill(Color.rgb(0, 255, 255, wordAlpha * 0.7));
            gc.fillText(word, WIDTH / 2 - channelJitter, wordY - 2);

            // Core white text
            gc.setFill(Color.rgb(255, 255, 255, wordAlpha));
            gc.fillText(word, WIDTH / 2, wordY);
            
            // Professional UI Tip: Stroke to cut through glitch blur
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(0.5);
            gc.strokeText(word, WIDTH / 2, wordY);

            // Death cause — readable narrative text, fades in at 500ms with white drop-shadow
            if (elapsed > 500) {
                double causeAlpha = Math.min(0.85, (elapsed - 500) / 600.0 * 0.85);
                gc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
                // White drop-shadow (offset 1px down-right) so text pops against dark pipes
                gc.setFill(Color.rgb(255, 255, 255, causeAlpha * 0.35));
                gc.fillText(gameState.getDeathCause(), WIDTH / 2 + 1, wordY + 29);
                // Main text — warm rose, clearly visible
                gc.setFill(Color.rgb(210, 140, 150, causeAlpha));
                gc.fillText(gameState.getDeathCause(), WIDTH / 2, wordY + 28);
            }
        }

        // ── PHASE 2 (900ms): SCORE — hero element ───────────────────────────────────
        if (elapsed > 900) {
            double scoreAlpha = Math.min(1.0, (elapsed - 900) / 550.0);
            int score = gameState.getScore();
            // Moved down slightly to provide padding between the death message and score
            double scoreY = cy + 40; 

            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 90));
            gc.setFill(Color.rgb(255, 255, 255, scoreAlpha));
            gc.fillText(String.valueOf(score), WIDTH / 2, scoreY);
        }

        // ── PHASE 3 (1400ms): GRADE | BEST Stats Bar (The Box Method) ──────────────
        if (elapsed > 1400) {
            double rowAlpha = Math.min(1.0, (elapsed - 1400) / 450.0);
            double rowY = cy + 130;
            int sc = gameState.getScore();

            String grade; Color gColor;
            if      (sc >= 50) { grade = "S"; gColor = Color.rgb(255, 215, 0); }
            else if (sc >= 30) { grade = "A"; gColor = Color.rgb(0, 229, 255); }
            else if (sc >= 15) { grade = "B"; gColor = Color.rgb(46, 213, 115); }
            else if (sc >=  5) { grade = "C"; gColor = Color.rgb(241, 196, 15); }
            else               { grade = "D"; gColor = Color.rgb(150, 90, 90); }

            int gr = (int)(gColor.getRed()*255), gg = (int)(gColor.getGreen()*255), gb = (int)(gColor.getBlue()*255);

            // The Box Method: Dark, semi-transparent 'Stats Bar' spanning 40% of width
            double barWidth = WIDTH * 0.45;
            double barHeight = 85;
            double barX = (WIDTH - barWidth) / 2;
            double barY = rowY - 45;
            
            gc.setFill(Color.rgb(10, 12, 18, 0.75 * rowAlpha));
            gc.fillRoundRect(barX, barY, barWidth, barHeight, 12, 12);
            gc.setStroke(Color.rgb(255, 255, 255, 0.1 * rowAlpha));
            gc.setLineWidth(1);
            gc.strokeRoundRect(barX, barY, barWidth, barHeight, 12, 12);

            // Terminal Look & Scanner Effect
            gc.setTextAlign(TextAlignment.LEFT);
            gc.setFont(Font.font("Courier New", FontWeight.NORMAL, 10));
            gc.setFill(Color.rgb(0, 229, 255, rowAlpha * 0.7)); // Cyan, 70% opacity
            int displayNodes = Math.min(sc, (int)(sc * Math.max(0, (elapsed - 1400) / 800.0)));
            gc.fillText(String.format("> DATANODES_ESCAPED: %03d", displayNodes), barX + 12, barY + barHeight - 10);

            // Reset alignment for the rest of the columns

            // VBox alignment emulation via horizontal centering offsets
            double colOffset = 50;

            // GRADE Column (Left)
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFont(Font.font("Arial", FontWeight.NORMAL, 20)); // Scaled 2x
            gc.setFill(Color.rgb(120, 130, 145, rowAlpha * 0.7));
            gc.fillText("GRADE", WIDTH / 2 - colOffset, rowY - 20);
            
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 44)); // Scaled 2x
            gc.setFill(Color.rgb(gr, gg, gb, rowAlpha));
            gc.fillText(grade, WIDTH / 2 - colOffset, rowY + 20);

            // Vertical separator
            gc.setFill(Color.rgb(60, 65, 75, rowAlpha));
            gc.setFont(Font.font("Arial", FontWeight.NORMAL, 36));
            gc.fillText("|", WIDTH / 2, rowY + 10);

            // BEST Column (Right)
            gc.setFont(Font.font("Arial", FontWeight.NORMAL, 20)); // Scaled 2x
            gc.setFill(Color.rgb(120, 130, 145, rowAlpha * 0.7));
            gc.fillText("BEST", WIDTH / 2 + colOffset, rowY - 20);
            
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 44)); // Scaled 2x
            gc.setFill(Color.rgb(210, 220, 235, rowAlpha));
            gc.fillText(String.valueOf(gameState.getHighScore()), WIDTH / 2 + colOffset, rowY + 20);

            if (gameState.isNewPersonalBest()) {
                double pbFlash = (now % 650 < 325) ? rowAlpha : rowAlpha * 0.4;
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                gc.setFill(Color.rgb(255, 215, 0, pbFlash));
                gc.fillText("\u2605 NEW BEST", WIDTH / 2 + colOffset + 40, rowY - 20);
            }
        }

        // ── PHASE 4 (2500ms): REINITIALIZE / ABANDON buttons ────────────────────────
        if (elapsed > 2500) {
            double btnAlpha = Math.min(1.0, (elapsed - 2500) / 600.0);
            double pulse    = 0.5 + 0.5 * Math.sin(now / 260.0);

            // Section separator line
            gc.setStroke(Color.rgb(255, 255, 255, 0.12 * btnAlpha));
            gc.setLineWidth(1);
            gc.strokeLine(WIDTH / 2 - 160, HEIGHT - 95, WIDTH / 2 + 160, HEIGHT - 95);

            // ── REINITIALIZE button ──────────────────────────────────────────────────
            double rX = WIDTH / 2 - 165, rY = HEIGHT - 82, rW = 145, rH = 34;

            // Outer glow ring (pulsing) — drawn first, widest
            gc.setStroke(Color.rgb(0, 200, 255, pulse * 0.25 * btnAlpha));
            gc.setLineWidth(4);
            gc.strokeRoundRect(rX - 3, rY - 3, rW + 6, rH + 6, 10, 10);

            // High-contrast white border — always visible even on low-brightness screens
            gc.setStroke(Color.rgb(255, 255, 255, 0.85 * btnAlpha));
            gc.setLineWidth(1.5);
            gc.strokeRoundRect(rX, rY, rW, rH, 8, 8);

            // Subtle interior fill — dark navy
            gc.setFill(Color.rgb(0, 20, 40, 0.70 * btnAlpha));
            gc.fillRoundRect(rX + 1, rY + 1, rW - 2, rH - 2, 7, 7);

            // Glowing underline along bottom edge of button
            double underlineAlpha = (0.4 + 0.6 * pulse) * btnAlpha;
            gc.setStroke(Color.rgb(0, 229, 255, underlineAlpha));
            gc.setLineWidth(2);
            gc.strokeLine(rX + 10, rY + rH, rX + rW - 10, rY + rH);

            // Pulsing dot indicator left of text
            double dotX = rX + 14, dotY = rY + rH / 2 - 4;
            gc.setFill(Color.rgb(0, 229, 255, pulse * 0.20 * btnAlpha));
            gc.fillOval(dotX - 5, dotY - 5, 18, 18);
            gc.setFill(Color.rgb(0, 229, 255, pulse * 0.90 * btnAlpha));
            gc.fillOval(dotX, dotY, 8, 8);

            // REINITIALIZE label — neon cyan
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            double textBright = 180 + (int)(75 * pulse);
            gc.setFill(Color.rgb(0, (int) textBright, 255, btnAlpha));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("REINITIALIZE", rX + rW / 2 + 6, rY + rH / 2 + 5);

            // ── ABANDON button ───────────────────────────────────────────────────────
            double aX = WIDTH / 2 + 20, aY = HEIGHT - 82, aW = 120, aH = 34;

            // High-contrast muted-red border — clearly visible on dark screens
            gc.setStroke(Color.rgb(200, 80, 80, 0.80 * btnAlpha));
            gc.setLineWidth(1.5);
            gc.strokeRoundRect(aX, aY, aW, aH, 8, 8);

            // Subtle interior fill — dark burgundy
            gc.setFill(Color.rgb(30, 8, 8, 0.60 * btnAlpha));
            gc.fillRoundRect(aX + 1, aY + 1, aW - 2, aH - 2, 7, 7);

            // ABANDON label — muted rose
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gc.setFill(Color.rgb(220, 110, 110, btnAlpha));
            gc.fillText("ABANDON", aX + aW / 2, aY + aH / 2 + 5);

            // ── Keyboard hint ────────────────────────────────────────────────────────
            // Fix 5: Interaction Hint Legibility - Glow effect or Keyboard Key graphic
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            gc.setFill(Color.rgb(255, 255, 255, btnAlpha));
            
            // Draw small boxes around keys
            gc.setStroke(Color.rgb(255, 255, 255, btnAlpha * 0.8));
            gc.setLineWidth(1);
            gc.strokeRoundRect(WIDTH / 2 - 130, HEIGHT - 45, 50, 18, 4, 4); // SPACE
            gc.strokeRoundRect(WIDTH / 2 + 55, HEIGHT - 45, 36, 18, 4, 4);  // ESC

            // Text
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("SPACE", WIDTH / 2 - 105, HEIGHT - 32);
            gc.fillText("ESC", WIDTH / 2 + 73, HEIGHT - 32);

            gc.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
            gc.setFill(Color.rgb(150, 160, 175, btnAlpha * 0.90));
            gc.setTextAlign(TextAlignment.LEFT);
            gc.fillText("reinitialize", WIDTH / 2 - 75, HEIGHT - 32);
            gc.fillText("abandon", WIDTH / 2 + 95, HEIGHT - 32);

        }

        // ── PHASE 5 (50ms+): The "Nodes Escaped" Terminal Footer ────────────────
        if (elapsed > 50) {
            double footAlpha = Math.min(1.0, (elapsed - 50) / 500.0);
            
            // Thin semi-transparent black bar
            gc.setFill(Color.rgb(0, 0, 0, 0.85 * footAlpha));
            gc.fillRect(0, HEIGHT - 20, WIDTH, 20);
            
            // Typewriter effect
            String fullText = String.format("> DATANODES_ESCAPED: %03d | SYSTEM_STATUS: CRITICAL | LOG_DUMP: ENABLED", gameState.getScore());
            int charsToShow = (int)((elapsed - 50) / 30.0); // 1 char every 30ms
            if (charsToShow > 0) {
                charsToShow = Math.min(charsToShow, fullText.length());
                gc.setTextAlign(TextAlignment.LEFT);
                gc.setFont(Font.font("Courier New", FontWeight.BOLD, 11));
                gc.setFill(Color.rgb(0, 229, 255, footAlpha * 0.9)); // Bright cyan
                gc.fillText(fullText.substring(0, charsToShow), 10, HEIGHT - 6);
            }
        }
    }

    /** Heatmap as a transparent world overlay — no border box */
    private void renderDeathHeatmapOverlay(double x, double y, double w, double h, double baseAlpha, long elapsed) {
        if (gameState.getDeathTracker().getDeathCount() == 0) return;

        // Tiny label — almost invisible
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Courier New", FontWeight.NORMAL, 9));
        gc.setFill(Color.rgb(0, 229, 255, baseAlpha * 0.5));
        gc.fillText("DEATH MAP", x + 2, y + 10);

        // Grid — very faint
        gc.setStroke(Color.rgb(0, 229, 255, 0.03));
        gc.setLineWidth(0.5);
        for (double gx = x; gx < x + w; gx += 25) gc.strokeLine(gx, y + 12, gx, y + h);
        for (double gy = y + 14; gy < y + h; gy += 18) gc.strokeLine(x, gy, x + w, gy);

        // Death dots
        List<double[]> deaths = gameState.getDeathTracker().getRawDeathPositions();
        double scanDuration = 1200.0;
        double scanProgress = Math.min(1.0, (elapsed - 200) / scanDuration);

        // Scan line
        if (scanProgress < 1.0) {
            double sl = y + 12 + scanProgress * (h - 12);
            gc.setStroke(Color.rgb(0, 229, 255, baseAlpha * 0.8));
            gc.setLineWidth(1);
            gc.strokeLine(x, sl, x + w, sl);
        }

        for (double[] pos : deaths) {
            double hx = x + (pos[0] / WIDTH) * w;
            double hy = y + 12 + (pos[1] / (HEIGHT - GameEngine.GROUND)) * (h - 12);
            double dotProgress = (hy - y - 12) / (h - 12);
            if (dotProgress > scanProgress) continue;

            // Glow
            gc.setFill(Color.rgb(255, 40, 40, baseAlpha * 0.12));
            gc.fillOval(hx - 5, hy - 5, 10, 10);
            gc.setFill(Color.rgb(255, 30, 30, baseAlpha * 0.6));
            gc.fillOval(hx - 2, hy - 2, 4, 4);
        }
    }

    public void renderCredits() {
        gc.setFill(Color.rgb(0, 0, 0, 0.82)); gc.fillRect(0, 0, WIDTH, HEIGHT);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 48)); gc.setFill(Color.rgb(0, 229, 255));
        gc.fillText("NEWTON'S GLITCH", WIDTH / 2, HEIGHT / 2 - 190);
        String[][] credits = {
            {"Game Design & Programming", gameState.getPlayerName()},
            {"Engine", "JavaFX 17 + Java 17"}, {"Rendering", "Canvas API (Zero assets)"},
            {"Narrative Design", "Newton's Glitch Story System"}, {"Special Thanks", "All Playtesters!"}
        };
        double sY = HEIGHT / 2 - 110;
        for (int i = 0; i < credits.length; i++) {
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14)); gc.setFill(Color.rgb(120, 200, 255));
            gc.fillText(credits[i][0], WIDTH / 2 - 10, sY + i * 32);
            gc.setFont(Font.font("Arial", FontWeight.NORMAL, 14)); gc.setFill(Color.WHITE);
            gc.fillText(credits[i][1], WIDTH / 2 + 200, sY + i * 32);
        }
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 16)); gc.setFill(Color.GOLD);
        gc.fillText("Press ESC or C to return", WIDTH / 2, HEIGHT - 35);
    }
}
