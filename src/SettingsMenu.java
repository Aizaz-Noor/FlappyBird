import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.List;

/**
 * Settings menu for avatar and sound info
 */
public class SettingsMenu {
    private boolean isOpen;
    private double canvasWidth;
    private double canvasHeight;

    private static final double MENU_WIDTH = 600;
    private static final double MENU_HEIGHT = 450;
    private static final double BUTTON_SIZE = 80;
    private static final double BUTTON_SPACING = 20;

    private AvatarManager avatarManager;
    private SoundManager soundManager;

    public SettingsMenu(double canvasWidth, double canvasHeight,
            AvatarManager avatarManager, SoundManager soundManager) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.avatarManager = avatarManager;
        this.soundManager = soundManager;
        this.isOpen = false;
    }

    public void toggle() {
        isOpen = !isOpen;
    }

    public void open() {
        isOpen = true;
    }

    public void close() {
        isOpen = false;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void render(GraphicsContext gc) {
        if (!isOpen) {
            return;
        }

        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        double menuX = (canvasWidth - MENU_WIDTH) / 2;
        double menuY = (canvasHeight - MENU_HEIGHT) / 2;

        gc.setFill(Color.rgb(40, 40, 40, 0.95));
        gc.fillRoundRect(menuX, menuY, MENU_WIDTH, MENU_HEIGHT, 20, 20);

        gc.setStroke(Color.rgb(255, 215, 0));
        gc.setLineWidth(3);
        gc.strokeRoundRect(menuX, menuY, MENU_WIDTH, MENU_HEIGHT, 20, 20);

        gc.setFill(Color.rgb(255, 215, 0));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("⚙ SETTINGS", canvasWidth / 2, menuY + 50);

        renderAvatarSection(gc, menuX, menuY + 90);
        renderSoundInfo(gc, menuX, menuY + 260);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        gc.fillText("Press S-Close • 1-Danger • 2-Safe • 3-GameOver • A-Avatar",
                canvasWidth / 2, menuY + MENU_HEIGHT - 20);
    }

    private void renderAvatarSection(GraphicsContext gc, double menuX, double startY) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("👤 Select Avatar:", menuX + 30, startY);

        List<Image> avatars = avatarManager.getAllAvatars();
        List<String> names = avatarManager.getAllAvatarNames();

        if (avatars.isEmpty()) {
            gc.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
            gc.setFill(Color.rgb(255, 100, 100));
            gc.fillText("No avatars loaded", menuX + 30, startY + 40);
            return;
        }

        double startX = menuX + 30;
        double currentX = startX;
        double currentY = startY + 35;

        for (int i = 0; i < avatars.size(); i++) {
            if (i > 0 && i % 5 == 0) {
                currentX = startX;
                currentY += BUTTON_SIZE + BUTTON_SPACING + 20;
            }

            if (i == avatarManager.getCurrentAvatarIndex()) {
                gc.setFill(Color.rgb(255, 215, 0));
                gc.fillRoundRect(currentX - 5, currentY - 5,
                        BUTTON_SIZE + 10, BUTTON_SIZE + 10, 10, 10);
            }

            gc.setFill(Color.rgb(60, 60, 60));
            gc.fillRoundRect(currentX, currentY, BUTTON_SIZE, BUTTON_SIZE, 8, 8);

            gc.save();
            gc.beginPath();
            gc.arc(currentX + BUTTON_SIZE / 2, currentY + BUTTON_SIZE / 2,
                    BUTTON_SIZE / 2 - 5, BUTTON_SIZE / 2 - 5, 0, 360);
            gc.closePath();
            gc.clip();
            gc.drawImage(avatars.get(i), currentX + 5, currentY + 5,
                    BUTTON_SIZE - 10, BUTTON_SIZE - 10);
            gc.restore();

            gc.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
            gc.setFill(Color.WHITE);
            gc.setTextAlign(TextAlignment.CENTER);
            String displayName = names.get(i);
            if (displayName.length() > 10) {
                displayName = displayName.substring(0, 10) + "..";
            }
            gc.fillText(displayName, currentX + BUTTON_SIZE / 2, currentY + BUTTON_SIZE + 15);

            currentX += BUTTON_SIZE + BUTTON_SPACING;
        }
    }

    private void renderSoundInfo(GraphicsContext gc, double menuX, double startY) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("🔊 Sound Categories:", menuX + 30, startY);

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        gc.setFill(Color.rgb(220, 220, 220));
        double textY = startY + 40;
        gc.fillText("Press 1: Danger Sound (near pipes)", menuX + 50, textY);
        gc.fillText("Press 2: Safe Sound (passed safely)", menuX + 50, textY + 30);
        gc.fillText("Press 3: GameOver Sound", menuX + 50, textY + 60);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.setFill(Color.GOLD);
        gc.fillText("Auto: High Altitude • Every 5 Pillars", menuX + 50, textY + 100);
    }

    public boolean handleClick(double mouseX, double mouseY) {
        if (!isOpen) {
            return false;
        }

        double menuX = (canvasWidth - MENU_WIDTH) / 2;
        double menuY = (canvasHeight - MENU_HEIGHT) / 2;

        return handleAvatarClick(mouseX, mouseY, menuX, menuY + 125);
    }

    private boolean handleAvatarClick(double mouseX, double mouseY, double menuX, double startY) {
        List<Image> avatars = avatarManager.getAllAvatars();
        if (avatars.isEmpty()) {
            return false;
        }

        double startX = menuX + 30;
        double currentX = startX;
        double currentY = startY;

        for (int i = 0; i < avatars.size(); i++) {
            if (i > 0 && i % 5 == 0) {
                currentX = startX;
                currentY += BUTTON_SIZE + BUTTON_SPACING + 20;
            }

            if (mouseX >= currentX && mouseX <= currentX + BUTTON_SIZE &&
                    mouseY >= currentY && mouseY <= currentY + BUTTON_SIZE) {
                avatarManager.switchToAvatar(i);
                return true;
            }

            currentX += BUTTON_SIZE + BUTTON_SPACING;
        }

        return false;
    }
}
