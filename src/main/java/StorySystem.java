import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.prefs.Preferences;
import java.util.Random;

/**
 * StorySystem — Narrative layer for Newton's Glitch.
 *
 * Features:
 *  - First-launch typewriter intro animation ("YEAR 2087. GRAVITY IS A GLITCH...")
 *  - Obstacle first-encounter lore notifications (pipes, lasers, crushers)
 *  - 5 randomised lore death messages
 *  - Score milestone announcements every 10 pipes
 *  - Day/night lore labels
 */
public class StorySystem {
    private static final Preferences PREFS = Preferences.userNodeForPackage(StorySystem.class);

    // =========================================================
    // INTRO ANIMATION
    // =========================================================
    private static final String INTRO_TEXT =
            "YEAR 2087.\nGRAVITY IS A GLITCH.\nDEBUG REALITY.";
    private static final long CHAR_DELAY_NS = 80_000_000L; // 80ms per character

    private boolean showIntro;
    private int bootPhase = 0; // 0: Terminal, 1: Title Slam, 2: Final Prompt
    private int bootLineIndex = 0;
    private double bootTimer = 0;
    private double bootCharCursor = 0;
    private long lastBootCharTime = 0;
    private double titleSlamScale = 4.0;
    private double titleAlpha = 0.0;
    private double controlsAlpha = 0.0;
    
    // Glitch background state
    private double glitchShift = 0;
    private double glitchShiftTimer = 0;

    // Stage Transition State
    private boolean transitionActive = false;
    private String transitionMsg = "";
    private double transitionTimer = 0;
    private double transitionCharCursor = 0;
    private static final double TRANSITION_DUR = 1.5;

    // Rocket Unlock Overlay
    private boolean showRocketUnlock = false;
    private double rocketUnlockTimer = 0;
    private double rocketCharCursor = 0;
    private static final double ROCKET_UNLOCK_DUR = 3.0;

    // Floating Texts
    private static class FloatingText {
        String text;
        double x, y, life, maxLife;
    }
    private final java.util.List<FloatingText> floatingTexts = new java.util.ArrayList<>();

    // =========================================================
    // LORE NOTIFICATION BANNER
    // =========================================================
    private boolean showLoreBanner;
    private String loreBannerText;
    private double loreBannerTimer;   // seconds remaining
    private static final double BANNER_DURATION = 3.0;
    private static final double BANNER_FADE_TIME = 0.5;

    // First-encounter flags (saved to Preferences)
    private boolean seenPipeLore;
    private boolean seenLaserLore;
    private boolean seenCrusherLore;

    // =========================================================
    // DEATH MESSAGES
    // =========================================================
    private static final String[] DEATH_MESSAGES = {
        "SEGMENTATION FAULT: REALITY.EXE STOPPED",
        "ERROR 404: GRAVITY NOT FOUND",
        "STACK OVERFLOW: TOO MANY FLIPS",
        "NULL POINTER: DR. NEWTON DEREFERENCED",
        "CRITICAL EXCEPTION: SIMULATION REJECTED TRAJECTORY",
        "STAGE CLEAR FAILED — REGRESSION DETECTED",
        "KERNEL PANIC: PLAYER_PROCESS TERMINATED"
    };
    private final Random rng = new Random();
    private String currentDeathMessage = "";

    // =========================================================
    // SCORE MILESTONE LABELS
    // =========================================================
    private static final String[] MILESTONES = {
        "DEBUGGED 10 SECTORS",
        "20% REALITY RESTORED",
        "30 SECTORS CLEARED — KERNEL PANIC AHEAD",
        "40% REALITY RESTORED",
        "HALFWAY THROUGH THE BREACH",
        "60% REALITY RESTORED",
        "70 SECTORS BREACHED — STACK OVERFLOW ZONE",
        "80% REALITY RESTORED",
        "ALMOST FREE — REALITY RESTORE STAGE AHEAD",
        "FULL REALITY RESTORATION ACHIEVED"
    };

    public StorySystem() {
        showIntro = !PREFS.getBoolean("seenIntro", false);

        seenPipeLore    = PREFS.getBoolean("seenPipe", false);
        seenLaserLore   = PREFS.getBoolean("seenLaser", false);
        seenCrusherLore = PREFS.getBoolean("seenCrusher", false);
    }

    // =========================================================
    // UPDATE — called each game loop frame
    // =========================================================

    public boolean update(double dt, long nowNano) {
        if (showIntro) {
            updateBootSequence(dt, nowNano);
        }
        if (transitionActive) {
            updateTransition(dt);
        }
        if (showRocketUnlock) {
            rocketUnlockTimer -= dt;
            rocketCharCursor += dt * 30;
            if (rocketUnlockTimer <= 0) showRocketUnlock = false;
        }
        if (showLoreBanner) {
            loreBannerTimer -= dt;
            if (loreBannerTimer <= 0) showLoreBanner = false;
        }
        
        // Update Floating Texts
        for (int i = floatingTexts.size() - 1; i >= 0; i--) {
            FloatingText ft = floatingTexts.get(i);
            ft.life -= dt;
            ft.y -= dt * 25; // upward drift
            if (ft.life <= 0) floatingTexts.remove(i);
        }
        
        return (showIntro && bootPhase == 0) || showRocketUnlock;
    }

    private void updateBootSequence(double dt, long nowNano) {
        if (bootPhase == 0) { // Terminal Typing
            String currentLine = NarrativeStrings.BOOT_SEQUENCE[bootLineIndex];
            if (nowNano - lastBootCharTime > 40_000_000L) { // 40ms
                bootCharCursor++;
                lastBootCharTime = nowNano;
                if (bootCharCursor > currentLine.length() + 10) { // Add generic delay after line
                    bootCharCursor = 0;
                    bootLineIndex++;
                    if (bootLineIndex >= NarrativeStrings.BOOT_SEQUENCE.length) {
                        bootPhase = 1; // Transition to Title Card
                    }
                }
            }
        } else if (bootPhase >= 1) { // Title Phase
            titleAlpha = Math.min(1.0, titleAlpha + dt * 2);
            titleSlamScale = Math.max(1.0, titleSlamScale - dt * 10);
            if (titleSlamScale <= 1.0 && bootPhase == 1) {
                bootPhase = 2;
            }
            if (bootPhase == 2) {
                controlsAlpha = Math.min(1.0, controlsAlpha + dt * 1.5);
            }
        }

        // Glitch bg effect
        glitchShiftTimer -= dt;
        if (glitchShiftTimer <= 0) {
            if (glitchShift == 0) {
                glitchShift = Math.random() > 0.5 ? 8 : -8;
                glitchShiftTimer = 0.08;
            } else {
                glitchShift = 0;
                glitchShiftTimer = 2.0 + Math.random() * 4.0;
            }
        }
    }

    private void updateTransition(double dt) {
        transitionTimer -= dt;
        transitionCharCursor += dt * 40; // Speed of typing
        if (transitionTimer <= 0) {
            transitionActive = false;
        }
    }

    public void skipIntro() {
        if (showIntro && bootPhase == 0) {
            bootPhase = 1;
            PREFS.putBoolean("seenIntro", true);
        }
    }

    public boolean isIntroShowing() { return showIntro; }
    public boolean isBootPhaseActive() { return showIntro && bootPhase == 0; }
    public void setShowIntro(boolean state) { this.showIntro = state; if (state) { bootPhase = 1; titleSlamScale = 1.0; titleAlpha = 1.0; controlsAlpha = 1.0; } }
    public double getControlsAlpha() { return controlsAlpha; }

    // =========================================================
    // RENDER — intro overlay
    // =========================================================

    public void renderIntro(GraphicsContext gc, double w, double h) {
        if (!showIntro) return;

        // Black background + Glitch Shift
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, w, h);
        
        if (glitchShift != 0) {
            gc.save();
            gc.translate(glitchShift, 0);
        }

        if (bootPhase == 0) { // Terminal Sequence
            gc.setTextAlign(TextAlignment.LEFT);
            gc.setFont(Font.font("Courier New", FontWeight.BOLD, 18));
            double tx = 50, ty = 100;
            for (int i = 0; i <= bootLineIndex; i++) {
                if (i >= NarrativeStrings.BOOT_SEQUENCE.length) break;
                String line = NarrativeStrings.BOOT_SEQUENCE[i];
                if (i == bootLineIndex) {
                    line = line.substring(0, Math.min(line.length(), (int)bootCharCursor));
                }
                
                // Last message flickers critical red
                if (i == NarrativeStrings.BOOT_SEQUENCE.length - 1) {
                    gc.setFill((System.currentTimeMillis() % 400 < 200) ? NarrativeStrings.COLOR_CRITICAL : Color.TRANSPARENT);
                } else {
                    gc.setFill(NarrativeStrings.COLOR_SYSTEM);
                }
                
                gc.fillText("> " + line, tx, ty + i * 30);
            }
        } else { // Title Phase
            gc.setTextAlign(TextAlignment.CENTER);
            
            // CRT Lines (Subtle)
            gc.setStroke(Color.rgb(255, 255, 255, 0.05));
            for(int i=0; i<h; i+=3) gc.strokeLine(0, i, w, i);

            // Title Slam
            gc.save();
            gc.translate(w / 2, h / 2 - 40);
            gc.scale(titleSlamScale, titleSlamScale);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 64));
            gc.setFill(Color.GOLD);
            gc.fillText("NEWTON'S GLITCH", 0, 0);
            gc.restore();

            // Subtitle
                gc.setFont(Font.font(NarrativeStrings.FONT_NARRATIVE, FontWeight.NORMAL, 20));
                gc.setFill(NarrativeStrings.COLOR_GRAY);
                gc.setGlobalAlpha(titleAlpha);
                gc.fillText("escape the broken simulation", w / 2, h / 2 + 20);

                // Pulsing Prompt
                double pulse = 0.5 + 0.5 * Math.sin(System.currentTimeMillis() / 200.0);
                gc.setFont(Font.font(NarrativeStrings.FONT_NARRATIVE, FontWeight.BOLD, 18));
                gc.setFill(Color.rgb(255, 255, 255, pulse));
                gc.fillText("[ PRESS SPACE TO INITIALIZE ]", w / 2, h / 2 + 80);
                gc.setGlobalAlpha(1.0);

                if (controlsAlpha > 0) {
                    renderControlsCard(gc, w, h);
                }
        }

        if (glitchShift != 0) gc.restore();
    }

    public void renderFloatingTexts(GraphicsContext gc) {
        for (FloatingText ft : floatingTexts) {
            double alpha = Math.min(1.0, ft.life / 0.5); // fade out over last 500ms
            gc.setFont(Font.font(NarrativeStrings.FONT_NARRATIVE, FontWeight.BOLD, 14));
            gc.setFill(Color.rgb((int)(NarrativeStrings.COLOR_CYAN.getRed()*255), 
                                 (int)(NarrativeStrings.COLOR_CYAN.getGreen()*255), 
                                 (int)(NarrativeStrings.COLOR_CYAN.getBlue()*255), alpha));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(ft.text, ft.x, ft.y);
        }
    }

    public void renderTransition(GraphicsContext gc, double w, double h) {
        if (!transitionActive) return;

        // Static Overlay (reuse class-level rng to avoid GC pressure)
        for (int i = 0; i < 1000; i++) {
            gc.setFill(Color.gray(rng.nextDouble(), 0.1));
            gc.fillRect(rng.nextDouble() * w, rng.nextDouble() * h, 2, 2);
        }

        // Terminal Message
        gc.setFill(Color.BLACK);
        gc.fillRect(w / 2 - 250, h / 2 - 30, 500, 60);
        gc.setStroke(NarrativeStrings.COLOR_SYSTEM);
        gc.strokeRect(w / 2 - 250, h / 2 - 30, 500, 60);

        gc.setFill(NarrativeStrings.COLOR_SYSTEM);
        gc.setFont(Font.font(NarrativeStrings.FONT_NARRATIVE, FontWeight.BOLD, 16));
        gc.setTextAlign(TextAlignment.CENTER);
        
        int show = Math.min(transitionMsg.length(), (int)transitionCharCursor);
        gc.fillText(transitionMsg.substring(0, show), w / 2, h / 2 + 6);
    }

    // =========================================================
    // RENDER — lore notification banner
    // =========================================================

    public void renderLoreBanner(GraphicsContext gc, double w) {
        if (!showLoreBanner) return;

        double alpha = Math.min(1.0, loreBannerTimer / BANNER_FADE_TIME);
        double bW = 480, bH = 36;
        double bX = w / 2 - bW / 2;
        double bY = 110;

        gc.setFill(Color.rgb(0, 0, 0, 0.7 * alpha));
        gc.fillRoundRect(bX, bY, bW, bH, 6, 6);
        gc.setStroke(Color.rgb(0, 229, 255, alpha));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(bX, bY, bW, bH, 6, 6);

        gc.setFill(Color.rgb(0, 229, 255, alpha));
        gc.setFont(Font.font(NarrativeStrings.FONT_NARRATIVE, FontWeight.BOLD, 14));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("[ " + loreBannerText + " ]", w / 2, bY + 23);
    }

    public void addFloatingText(String text, double x, double y) {
        FloatingText ft = new FloatingText();
        ft.text = text;
        ft.x = x;
        ft.y = y;
        ft.life = 1.5;
        ft.maxLife = 1.5;
        floatingTexts.add(ft);
    }

    public void triggerRocketUnlock() {
        showRocketUnlock = true;
        rocketUnlockTimer = ROCKET_UNLOCK_DUR;
        rocketCharCursor = 0;
    }

    public void renderRocketUnlock(GraphicsContext gc, double w, double h) {
        if (!showRocketUnlock) return;

        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, w, h);

        gc.setStroke(NarrativeStrings.COLOR_WARNING);
        gc.setLineWidth(2);
        gc.strokeRect(w / 2 - 250, h / 2 - 40, 500, 80);

        gc.setFill(NarrativeStrings.COLOR_WARNING);
        gc.setFont(Font.font(NarrativeStrings.FONT_NARRATIVE, FontWeight.BOLD, 14));
        gc.setTextAlign(TextAlignment.CENTER);

        String msg1 = NarrativeStrings.ROCKET_UNLOCK_MSG1;
        String msg2 = NarrativeStrings.ROCKET_UNLOCK_MSG2;
        
        int show1 = Math.min(msg1.length(), (int)rocketCharCursor);
        gc.fillText(msg1.substring(0, show1), w / 2, h / 2 - 10);
        
        if (rocketCharCursor > msg1.length() + 5) {
            int show2 = Math.min(msg2.length(), (int)(rocketCharCursor - (msg1.length() + 5)));
            gc.setFont(Font.font(NarrativeStrings.FONT_NARRATIVE, FontWeight.NORMAL, 12));
            gc.setFill(NarrativeStrings.COLOR_GRAY);
            gc.fillText(msg2.substring(0, show2), w / 2, h / 2 + 15);
        }
    }

    private void renderControlsCard(GraphicsContext gc, double w, double h) {
        double cardW = 340, cardH = 90;
        double cardX = w / 2 - cardW / 2;
        double cardY = h - 130;

        gc.setGlobalAlpha(controlsAlpha);
        gc.setFill(Color.rgb(20, 24, 30, 0.8));
        gc.fillRoundRect(cardX, cardY, cardW, cardH, 12, 12);
        gc.setStroke(Color.rgb(255, 255, 255, 0.15));
        gc.strokeRoundRect(cardX, cardY, cardW, cardH, 12, 12);

        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font(NarrativeStrings.FONT_UI, FontWeight.NORMAL, 12));
        double tx1 = cardX + 20;
        double ty = cardY + 25;
        double lineH = 22;

        gc.fillText("SPACE / Click  \u2192  Flap", tx1, ty);
        gc.fillText("SHIFT / R-Click \u2192  Flip Gravity", tx1, ty + lineH);
        gc.fillText("P \u2192 Pause          S \u2192 Settings", tx1, ty + lineH * 2);
        gc.fillText("A \u2192 Avatar         R \u2192 Replay", tx1 + 170, ty + lineH * 2);
        gc.setGlobalAlpha(1.0);
    }

    // =========================================================
    // TRIGGER — obstacle lore notifications
    // =========================================================

    public void onFirstPipe() {
        if (!seenPipeLore) {
            seenPipeLore = true;
            PREFS.putBoolean("seenPipe", true);
            showBanner("CORRUPTED_CODE_COLUMNS DETECTED");
        }
    }

    public void onFirstLaser() {
        if (!seenLaserLore) {
            seenLaserLore = true;
            PREFS.putBoolean("seenLaser", true);
            showBanner("FIREWALL_GATES ACTIVATED — PROCEED WITH CAUTION");
        }
    }

    public void onFirstCrusher() {
        if (!seenCrusherLore) {
            seenCrusherLore = true;
            PREFS.putBoolean("seenCrusher", true);
            showBanner("MEMORY_COLLAPSE INITIATED — BRACE FOR IMPACT");
        }
    }

    /** Called by GameEngine when StageManager fires a stage transition */
    public void onStageChange(int stageNumber, String stageCodeName) {
        transitionActive = true;
        transitionTimer = TRANSITION_DUR;
        transitionCharCursor = 0;
        transitionMsg = switch (stageNumber) {
            case 2 -> NarrativeStrings.TRANSITION_1_2;
            case 3 -> NarrativeStrings.TRANSITION_2_3;
            case 4 -> NarrativeStrings.TRANSITION_3_4;
            case 5 -> NarrativeStrings.TRANSITION_4_5;
            default -> "SECTOR BREACH DETECTED.";
        };
    }

    private void showBanner(String text) {
        loreBannerText = text;
        loreBannerTimer = BANNER_DURATION;
        showLoreBanner = true;
    }

    // =========================================================
    // DEATH MESSAGE
    // =========================================================

    /** Called when the player dies. Selects a random lore message. */
    public void onDeath() {
        currentDeathMessage = DEATH_MESSAGES[rng.nextInt(DEATH_MESSAGES.length)];
    }

    /** Render the lore death message on the game over screen. */
    public void renderDeathMessage(GraphicsContext gc, double cx, double y) {
        if (currentDeathMessage.isEmpty()) return;
        gc.setFont(Font.font(NarrativeStrings.FONT_NARRATIVE, FontWeight.NORMAL, 13));
        gc.setFill(Color.rgb(255, 45, 107, 0.85));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(currentDeathMessage, cx, y);
    }

    // =========================================================
    // SCORE MILESTONES
    // =========================================================

    /**
     * Returns a milestone announcement string if score hits a 10-pipe multiple.
     * Returns null otherwise.
     */
    public String getMilestoneLabel(int score) {
        if (score > 0 && score % 10 == 0) {
            int idx = (score / 10 - 1) % MILESTONES.length;
            return MILESTONES[idx];
        }
        return null;
    }

    // =========================================================
    // DAY/NIGHT LORE LABELS
    // =========================================================

    /** Returns the narrative label for the current time-of-day theme. */
    public String getDayLabel(String timeOfDay) {
        return switch (timeOfDay.toLowerCase()) {
            case "day"     -> "RUNTIME_ACTIVE";
            case "sunset"  -> "CORRUPTION_SPREADING";
            case "night"   -> "DEEP_SCAN_MODE";
            case "dawn"    -> "SYSTEM_RECOVERY";
            default        -> "SIMULATION_RUNNING";
        };
    }
}
