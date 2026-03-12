import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Modern Settings Menu - Clean, aligned, Theme-Aware UI
 */
public class SettingsMenu {
    private boolean isOpen;
    private double canvasWidth, canvasHeight;
    private SoundManager soundManager;
    private AvatarManager avatarManager;
    private GameEngine gameEngine;

    // Menu dimensions
    private static final double MENU_WIDTH = 700;
    private static final double MENU_HEIGHT = 600;
    private static final double AVATAR_SIZE = 70;

    // Palette (Theme Aware)
    private static class Palette {
        Color background, overlay, card, textPrimary, textSecondary, accent, border;

        Palette(boolean isDark) {
            if (isDark) {
                overlay = Color.rgb(0, 0, 0, 0.6);
                background = Color.rgb(30, 30, 35, 0.95);
                card = Color.rgb(45, 45, 50);
                textPrimary = Color.WHITE;
                textSecondary = Color.rgb(200, 200, 200);
                accent = Color.GOLD;
                border = Color.rgb(255, 215, 0, 0.5);
            } else {
                overlay = Color.rgb(255, 255, 255, 0.4);
                background = Color.rgb(240, 245, 250, 0.95);
                card = Color.WHITE;
                textPrimary = Color.rgb(40, 40, 50);
                textSecondary = Color.rgb(100, 100, 120);
                accent = Color.rgb(255, 140, 0); // Orange-Gold
                border = Color.rgb(200, 200, 220);
            }
        }
    }

    public SettingsMenu(double canvasWidth, double canvasHeight, SoundManager soundManager,
            AvatarManager avatarManager, GameEngine gameEngine) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.soundManager = soundManager;
        this.avatarManager = avatarManager;
        this.gameEngine = gameEngine;
        this.isOpen = false;
    }

    public void toggle() {
        isOpen = !isOpen;
    }

    public void close() {
        isOpen = false;
    }

    public boolean isOpen() {
        return isOpen;
    }

    /**
     * Render the modern settings menu
     */
    public void render(GraphicsContext gc) {
        if (!isOpen)
            return;

        boolean isDark = gameEngine.getCurrentTheme() == GameEngine.Theme.DARK;
        Palette palette = new Palette(isDark);

        double menuX = (canvasWidth - MENU_WIDTH) / 2;
        double menuY = (canvasHeight - MENU_HEIGHT) / 2;

        // Dark/Light Overlay
        gc.setFill(palette.overlay);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        // Menu Background (Glassmorphism base)
        gc.setFill(palette.background);
        gc.fillRoundRect(menuX, menuY, MENU_WIDTH, MENU_HEIGHT, 25, 25);

        // Border
        gc.setStroke(palette.border);
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(menuX, menuY, MENU_WIDTH, MENU_HEIGHT, 25, 25);

        double centerX = canvasWidth / 2;
        double currentY = menuY + 40;

        // === TITLE ===
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        gc.setFill(palette.accent);
        gc.fillText("⚙ SETTINGS", centerX, currentY);
        currentY += 50;

        // === AVATAR SECTION ===
        renderAvatarSection(gc, centerX, currentY, palette);
        currentY += 110;

        // === TOGGLES ROW ===
        renderToggles(gc, centerX, currentY, palette);
        currentY += 70;

        // === DIFFICULTY ===
        renderDifficulty(gc, centerX, currentY, palette);
        currentY += 90;

        // === SOUND VARIANTS (Aligned Logic) ===
        renderSoundVariants(gc, centerX, currentY, palette);
        currentY += 130;

        // === DATA CONTROL ===
        renderDataControl(gc, centerX, currentY, palette);

        // === FOOTER ===
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        gc.setFill(palette.textSecondary);
        gc.fillText("Press S or ESC to close", centerX, menuY + MENU_HEIGHT - 20);
    }

    private void renderAvatarSection(GraphicsContext gc, double centerX, double y, Palette palette) {
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gc.setFill(palette.textPrimary);
        gc.fillText("Avatar Selection", centerX, y);

        double totalWidth = (AVATAR_SIZE + 15) * avatarManager.getAvatarCount() - 15;
        double startX = centerX - totalWidth / 2;
        double avatarY = y + 20;

        for (int i = 0; i < avatarManager.getAvatarCount(); i++) {
            double ax = startX + i * (AVATAR_SIZE + 15);
            boolean selected = (i == avatarManager.getCurrentAvatarIndex());

            if (selected) {
                gc.setStroke(palette.accent);
                gc.setLineWidth(4);
                gc.strokeRoundRect(ax - 4, avatarY - 4, AVATAR_SIZE + 8, AVATAR_SIZE + 8, 15, 15);
            }
            // Draw Avatar
            avatarManager.renderAvatarPreview(gc, ax, avatarY, AVATAR_SIZE, i);
        }

        // Avatar Name
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        gc.setFill(palette.textSecondary);
        gc.fillText(avatarManager.getCurrentAvatarName(), centerX, avatarY + AVATAR_SIZE + 20);
    }

    private void renderToggles(GraphicsContext gc, double centerX, double y, Palette palette) {
        double btnWidth = 220;
        double btnHeight = 45;
        double gap = 20;
        double startX = centerX - btnWidth - gap / 2;

        // Music Toggle
        boolean musicOn = soundManager.isSoundEnabled();
        Color musicColor = musicOn ? Color.rgb(46, 204, 113) : Color.rgb(231, 76, 60);
        renderModernButton(gc, startX, y, btnWidth, btnHeight, "🎵 Music: " + (musicOn ? "ON" : "OFF"), musicColor,
                true);

        // Theme Toggle
        double themeX = centerX + gap / 2;
        boolean isDark = gameEngine.getCurrentTheme() == GameEngine.Theme.DARK;
        Color themeColor = isDark ? Color.rgb(60, 60, 70) : Color.rgb(200, 220, 240);
        Color textColor = isDark ? Color.WHITE : Color.rgb(40, 40, 50);
        renderModernButton(gc, themeX, y, btnWidth, btnHeight, (isDark ? "🌙 DARK" : "☀ LIGHT"), themeColor, textColor);
    }

    private void renderDifficulty(GraphicsContext gc, double centerX, double y, Palette palette) {
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gc.setFill(palette.textPrimary);
        gc.fillText("Difficulty", centerX, y);

        double pillW = 120;
        double pillH = 40;
        double gap = 15;
        double totalW = pillW * 3 + gap * 2;
        double startX = centerX - totalW / 2;
        double pillY = y + 20;

        GameEngine.Difficulty diff = gameEngine.getCurrentDifficulty();

        renderPill(gc, startX, pillY, pillW, pillH, "EASY", diff == GameEngine.Difficulty.EASY, Color.rgb(46, 204, 113),
                palette);
        renderPill(gc, startX + pillW + gap, pillY, pillW, pillH, "MEDIUM", diff == GameEngine.Difficulty.MEDIUM,
                Color.rgb(241, 196, 15), palette);
        renderPill(gc, startX + (pillW + gap) * 2, pillY, pillW, pillH, "HARD", diff == GameEngine.Difficulty.HARD,
                Color.rgb(231, 76, 60), palette);
    }

    private void renderPill(GraphicsContext gc, double x, double y, double w, double h, String text, boolean active,
            Color color, Palette p) {
        gc.setFill(active ? color : p.card);
        gc.fillRoundRect(x, y, w, h, 20, 20);

        if (active) {
            gc.setStroke(p.accent);
            gc.setLineWidth(2);
            gc.strokeRoundRect(x, y, w, h, 20, 20);
        } else {
            gc.setStroke(p.textSecondary);
            gc.setLineWidth(1);
            gc.strokeRoundRect(x, y, w, h, 20, 20);
        }

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.setFill(active ? Color.WHITE : p.textSecondary);
        gc.fillText(text, x + w / 2, y + h / 2 + 5);
    }

    private void renderSoundVariants(GraphicsContext gc, double centerX, double y, Palette p) {
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gc.setFill(p.textPrimary);
        gc.fillText("Sound Variants", centerX, y);

        // Layout: Label (Right Align) | | Controls (Left Align)
        double centerGap = 20;
        double startY = y + 30;

        renderSoundRow(gc, centerX, startY, "Game Over", soundManager.getCurrentGameOverIndex(),
                soundManager.getGameOverSoundCount(), p);
        renderSoundRow(gc, centerX, startY + 35, "Danger", soundManager.getCurrentDangerIndex(),
                soundManager.getDangerSoundCount(), p);
        renderSoundRow(gc, centerX, startY + 70, "Safe", soundManager.getCurrentSafeIndex(),
                soundManager.getSafeSoundCount(), p);
    }

    private void renderSoundRow(GraphicsContext gc, double centerX, double y, String label, int idx, int total,
            Palette p) {
        // Label
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        gc.setFill(p.textSecondary);
        gc.fillText(label, centerX - 20, y + 18);

        // Controls
        double x = centerX + 20;
        double size = 26;

        // Left Btn
        renderArrowBtn(gc, x, y, size, false, p);

        // Text
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.setFill(p.accent);
        gc.fillText((idx + 1) + "/" + total, x + size + 25, y + 19);

        // Right Btn
        renderArrowBtn(gc, x + size + 50, y, size, true, p);
    }

    private void renderArrowBtn(GraphicsContext gc, double x, double y, double s, boolean right, Palette p) {
        gc.setFill(p.card);
        gc.fillRoundRect(x, y, s, s, 5, 5);
        gc.setStroke(p.textSecondary);
        gc.setLineWidth(1);
        gc.strokeRoundRect(x, y, s, s, 5, 5);

        gc.setFill(p.textPrimary);
        double[] xP, yP;
        if (right) {
            xP = new double[] { x + 8, x + s - 8, x + 8 };
        } else {
            xP = new double[] { x + s - 8, x + 8, x + s - 8 };
        }
        yP = new double[] { y + 6, y + s / 2, y + s - 6 };
        gc.fillPolygon(xP, yP, 3);
    }

    private void renderDataControl(GraphicsContext gc, double centerX, double y, Palette p) {
        double w = 240;
        double h = 40;
        // Subtle red styling
        renderModernButton(gc, centerX - w / 2, y, w, h, "🗑 Clear Heatmap Data", Color.rgb(220, 50, 50), true);
    }

    private void renderModernButton(GraphicsContext gc, double x, double y, double w, double h, String text, Color bg,
            boolean textWhite) {
        renderModernButton(gc, x, y, w, h, text, bg, textWhite ? Color.WHITE : Color.BLACK);
    }

    private void renderModernButton(GraphicsContext gc, double x, double y, double w, double h, String text, Color bg,
            Color textC) {
        gc.setFill(bg);
        gc.fillRoundRect(x, y, w, h, 12, 12);

        // Highlight logic could go here

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.setFill(textC);
        gc.fillText(text, x + w / 2, y + h / 2 + 6);
    }

    // --- INPUT HANDLING ---

    public boolean handleClick(double mx, double my) {
        if (!isOpen)
            return false;

        double menuX = (canvasWidth - MENU_WIDTH) / 2;
        double menuY = (canvasHeight - MENU_HEIGHT) / 2;
        double centerX = canvasWidth / 2;

        // Match strictly with render() flow
        double currentY = menuY + 40;
        currentY += 50; // Title

        // 1. Avatars (Drawn at y + 20)
        double avatarDrawY = currentY + 20;
        double totalW = (AVATAR_SIZE + 15) * avatarManager.getAvatarCount() - 15;
        double startX = centerX - totalW / 2;
        for (int i = 0; i < avatarManager.getAvatarCount(); i++) {
            double ax = startX + i * (AVATAR_SIZE + 15);
            if (mx >= ax && mx <= ax + AVATAR_SIZE && my >= avatarDrawY && my <= avatarDrawY + AVATAR_SIZE) {
                avatarManager.switchToAvatar(i);
                return true;
            }
        }
        currentY += 110;

        // 2. Toggles (Drawn at y)
        double toggleDrawY = currentY;
        double btnW = 220, btnH = 45, gap = 20;
        double tX = centerX - btnW - gap / 2; // Music

        if (mx >= tX && mx <= tX + btnW && my >= toggleDrawY && my <= toggleDrawY + btnH) {
            soundManager.toggleSound();
            return true;
        }
        tX = centerX + gap / 2; // Theme
        if (mx >= tX && mx <= tX + btnW && my >= toggleDrawY && my <= toggleDrawY + btnH) {
            GameEngine.Theme newTheme = gameEngine.getCurrentTheme() == GameEngine.Theme.DARK ? GameEngine.Theme.LIGHT
                    : GameEngine.Theme.DARK;
            gameEngine.setTheme(newTheme);
            return true;
        }
        currentY += 70;

        // 3. Difficulty (Drawn at y + 20)
        double diffDrawY = currentY + 20;
        double pW = 120, pH = 40, pGap = 15;
        double pTotalW = pW * 3 + pGap * 2;
        double pX = centerX - pTotalW / 2;

        if (checkClick(mx, my, pX, diffDrawY, pW, pH)) {
            gameEngine.setDifficulty(GameEngine.Difficulty.EASY);
            return true;
        }
        if (checkClick(mx, my, pX + pW + pGap, diffDrawY, pW, pH)) {
            gameEngine.setDifficulty(GameEngine.Difficulty.MEDIUM);
            return true;
        }
        if (checkClick(mx, my, pX + (pW + pGap) * 2, diffDrawY, pW, pH)) {
            gameEngine.setDifficulty(GameEngine.Difficulty.HARD);
            return true;
        }
        currentY += 90;

        // 4. Sound Variants (Starts at y + 30)
        double soundDrawY = currentY + 30;
        double sX = centerX + 20;
        double sSize = 26;
        if (checkSoundClick(mx, my, sX, soundDrawY, sSize, 0))
            return true;
        if (checkSoundClick(mx, my, sX, soundDrawY + 35, sSize, 1))
            return true;
        if (checkSoundClick(mx, my, sX, soundDrawY + 70, sSize, 2))
            return true;

        currentY += 130;

        // 5. Data Control (Drawn at y)
        double dataDrawY = currentY;
        double dW = 240, dH = 40;
        if (mx >= centerX - dW / 2 && mx <= centerX + dW / 2 && my >= dataDrawY && my <= dataDrawY + dH) {
            gameEngine.clearHeatmapData();
            soundManager.playMilestoneSound();
            return true;
        }

        return false;
    }

    private boolean checkClick(double mx, double my, double x, double y, double w, double h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    /**
     * Helper method to switch sound variant (reduces code duplication)
     */
    private void switchSound(int type, boolean forward) {
        if (type == 0)
            soundManager.switchGameOverSound(forward);
        else if (type == 1)
            soundManager.switchDangerSound(forward);
        else
            soundManager.switchSafeSound(forward);
    }

    // soundType: 0=GameOver, 1=Danger, 2=Safe
    private boolean checkSoundClick(double mx, double my, double x, double y, double s, int type) {
        // Left button
        if (checkClick(mx, my, x, y, s, s)) {
            switchSound(type, false); // FALSE for backward
            return true;
        }
        // Right button
        if (checkClick(mx, my, x + s + 50, y, s, s)) {
            switchSound(type, true); // TRUE for forward
            return true;
        }
        return false;
    }
}
