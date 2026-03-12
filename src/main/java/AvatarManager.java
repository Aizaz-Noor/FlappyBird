import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages custom avatar faces - all drawn with code for consistent art style
 * No images needed - everything rendered with shapes!
 */
public class AvatarManager {
    private List<String> avatarNames;
    private int currentAvatarIndex;

    public enum AvatarType {
        BIRD, CAT, DOG, PANDA, ROBOT, ALIEN
    }

    public AvatarManager() {
        this.avatarNames = new ArrayList<>();
        this.currentAvatarIndex = 0;

        // All avatars are code-drawn, no images!
        avatarNames.add("Yellow Bird");
        avatarNames.add("Orange Cat");
        avatarNames.add("Brown Dog");
        avatarNames.add("Panda Bear");
        avatarNames.add("Blue Robot");
        avatarNames.add("Green Alien");

        System.out.println("✓ Loaded " + avatarNames.size() + " code-drawn avatars");
    }

    /**
     * Render the current avatar face using pure code/shapes
     */
    public void renderAvatar(GraphicsContext gc, double x, double y, double rotation, double size) {
        // All drawing happens at 0,0 since we're already in transformed context
        switch (currentAvatarIndex) {
            case 0:
                renderBirdFace(gc, size);
                break;
            case 1:
                renderCatFace(gc, size);
                break;
            case 2:
                renderDogFace(gc, size);
                break;
            case 3:
                renderPandaFace(gc, size);
                break;
            case 4:
                renderRobotFace(gc, size);
                break;
            case 5:
                renderAlienFace(gc, size);
                break;
        }
    }

    /**
     * BIRD FACE - Simple bird with eye and beak
     */
    private void renderBirdFace(GraphicsContext gc, double size) {
        // Eye
        gc.setFill(Color.WHITE);
        gc.fillOval(5, -10, 8, 8);
        gc.setFill(Color.BLACK);
        gc.fillOval(7, -8, 4, 4);

        // Beak
        gc.setFill(Color.ORANGERED);
        gc.fillPolygon(new double[] { size / 2, size / 2 + 10, size / 2 },
                new double[] { -3, 0, 3 }, 3);
    }

    /**
     * CAT FACE - Orange tabby with green eyes, whiskers, pink nose
     */
    private void renderCatFace(GraphicsContext gc, double size) {
        double s = size / 30.0; // Scale factor

        // Cat ears (triangles)
        gc.setFill(Color.rgb(255, 140, 60));
        gc.fillPolygon(new double[] { -10 * s, -6 * s, -8 * s },
                new double[] { -15 * s, -15 * s, -10 * s }, 3);
        gc.fillPolygon(new double[] { 6 * s, 10 * s, 8 * s },
                new double[] { -15 * s, -15 * s, -10 * s }, 3);

        // Inner ear (pink)
        gc.setFill(Color.rgb(255, 182, 193));
        gc.fillPolygon(new double[] { -9 * s, -7 * s, -8 * s },
                new double[] { -14 * s, -14 * s, -11 * s }, 3);
        gc.fillPolygon(new double[] { 7 * s, 9 * s, 8 * s },
                new double[] { -14 * s, -14 * s, -11 * s }, 3);

        // Eyes (green cat eyes with slits)
        gc.setFill(Color.rgb(100, 200, 100));
        gc.fillOval(-8 * s, -6 * s, 6 * s, 6 * s);
        gc.fillOval(2 * s, -6 * s, 6 * s, 6 * s);

        // Pupils (vertical slits)
        gc.setFill(Color.BLACK);
        gc.fillRect(-6 * s, -5 * s, 2 * s, 4 * s);
        gc.fillRect(4 * s, -5 * s, 2 * s, 4 * s);

        // Nose (pink triangle)
        gc.setFill(Color.rgb(255, 182, 193));
        gc.fillPolygon(new double[] { -2 * s, 2 * s, 0 },
                new double[] { 0, 0, 2 * s }, 3);

        // Whiskers (black lines)
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeLine(-12 * s, -2 * s, -6 * s, -1 * s); // Left whiskers
        gc.strokeLine(-12 * s, 0, -6 * s, 1 * s);
        gc.strokeLine(6 * s, -1 * s, 12 * s, -2 * s); // Right whiskers
        gc.strokeLine(6 * s, 1 * s, 12 * s, 0);

        // Mouth (W shape)
        gc.strokeLine(-2 * s, 2 * s, 0, 4 * s);
        gc.strokeLine(0, 4 * s, 2 * s, 2 * s);
    }

    /**
     * DOG FACE - Friendly brown dog with floppy ears
     */
    private void renderDogFace(GraphicsContext gc, double size) {
        double s = size / 30.0;

        // Floppy ears (brown ovals)
        gc.setFill(Color.rgb(139, 90, 43));
        gc.fillOval(-14 * s, -8 * s, 8 * s, 12 * s); // Left ear
        gc.fillOval(6 * s, -8 * s, 8 * s, 12 * s); // Right ear

        // Eyes (big friendly eyes)
        gc.setFill(Color.WHITE);
        gc.fillOval(-8 * s, -6 * s, 7 * s, 7 * s);
        gc.fillOval(2 * s, -6 * s, 7 * s, 7 * s);

        // Pupils (brown)
        gc.setFill(Color.rgb(101, 67, 33));
        gc.fillOval(-6 * s, -4 * s, 4 * s, 4 * s);
        gc.fillOval(4 * s, -4 * s, 4 * s, 4 * s);

        // Light reflection
        gc.setFill(Color.WHITE);
        gc.fillOval(-5 * s, -5 * s, 2 * s, 2 * s);
        gc.fillOval(5 * s, -5 * s, 2 * s, 2 * s);

        // Nose (big black nose)
        gc.setFill(Color.rgb(50, 30, 20));
        gc.fillOval(-3 * s, 0, 6 * s, 5 * s);

        // Nostrils
        gc.setFill(Color.BLACK);
        gc.fillOval(-2 * s, 1 * s, 1.5 * s, 2 * s);
        gc.fillOval(0.5 * s, 1 * s, 1.5 * s, 2 * s);

        // Mouth (happy smile)
        gc.setStroke(Color.rgb(50, 30, 20));
        gc.setLineWidth(1.5);
        gc.strokeArc(-6 * s, 2 * s, 12 * s, 8 * s, 200, 140, ArcType.OPEN);

        // Tongue (pink)
        gc.setFill(Color.rgb(255, 182, 193));
        gc.fillOval(-2 * s, 6 * s, 4 * s, 3 * s);
    }

    /**
     * PANDA FACE - Black and white panda with bamboo
     */
    private void renderPandaFace(GraphicsContext gc, double size) {
        double s = size / 30.0;

        // Ears (black circles on top)
        gc.setFill(Color.BLACK);
        gc.fillOval(-12 * s, -14 * s, 6 * s, 6 * s);
        gc.fillOval(6 * s, -14 * s, 6 * s, 6 * s);

        // Eye patches (black ovals)
        gc.fillOval(-10 * s, -6 * s, 8 * s, 9 * s);
        gc.fillOval(2 * s, -6 * s, 8 * s, 9 * s);

        // White eyes
        gc.setFill(Color.WHITE);
        gc.fillOval(-8 * s, -4 * s, 5 * s, 5 * s);
        gc.fillOval(3 * s, -4 * s, 5 * s, 5 * s);

        // Pupils (black dots)
        gc.setFill(Color.BLACK);
        gc.fillOval(-6 * s, -2 * s, 3 * s, 3 * s);
        gc.fillOval(5 * s, -2 * s, 3 * s, 3 * s);

        // Nose (black triangle)
        gc.fillPolygon(new double[] { -2 * s, 2 * s, 0 },
                new double[] { 1 * s, 1 * s, 3 * s }, 3);

        // Mouth (simple line)
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.5);
        gc.strokeLine(0, 3 * s, 0, 5 * s);
        gc.strokeLine(-3 * s, 5 * s, 0, 5 * s);
        gc.strokeLine(0, 5 * s, 3 * s, 5 * s);
    }

    /**
     * ROBOT FACE - Futuristic robot with antenna
     */
    private void renderRobotFace(GraphicsContext gc, double size) {
        double s = size / 30.0;

        // Antenna
        gc.setStroke(Color.rgb(100, 150, 200));
        gc.setLineWidth(2);
        gc.strokeLine(0, -14 * s, 0, -10 * s);
        gc.setFill(Color.rgb(255, 100, 100));
        gc.fillOval(-2 * s, -16 * s, 4 * s, 4 * s);

        // Eyes (LED style)
        gc.setFill(Color.rgb(0, 255, 255));
        gc.fillRect(-8 * s, -6 * s, 5 * s, 5 * s);
        gc.fillRect(3 * s, -6 * s, 5 * s, 5 * s);

        // Pupils (bright blue)
        gc.setFill(Color.rgb(0, 100, 255));
        gc.fillRect(-6 * s, -4 * s, 2 * s, 2 * s);
        gc.fillRect(5 * s, -4 * s, 2 * s, 2 * s);

        // Mouth (digital grid)
        gc.setStroke(Color.rgb(100, 150, 200));
        gc.setLineWidth(1);
        for (int i = -5; i <= 5; i++) {
            gc.strokeLine(i * s, 2 * s, i * s, 5 * s);
        }
        gc.strokeRect(-6 * s, 2 * s, 12 * s, 3 * s);
    }

    /**
     * ALIEN FACE - Cute green alien with big eyes
     */
    private void renderAlienFace(GraphicsContext gc, double size) {
        double s = size / 30.0;

        // Eyes (HUGE alien eyes)
        gc.setFill(Color.BLACK);
        gc.fillOval(-10 * s, -8 * s, 9 * s, 12 * s);
        gc.fillOval(1 * s, -8 * s, 9 * s, 12 * s);

        // Eye whites
        gc.setFill(Color.WHITE);
        gc.fillOval(-9 * s, -7 * s, 7 * s, 10 * s);
        gc.fillOval(2 * s, -7 * s, 7 * s, 10 * s);

        // Pupils (green glow)
        RadialGradient gradient = new RadialGradient(0, 0, 0.5, 0.5, 0.5, true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(100, 255, 100)),
                new Stop(1, Color.rgb(0, 150, 0)));
        gc.setFill(gradient);
        gc.fillOval(-7 * s, -4 * s, 5 * s, 6 * s);
        gc.fillOval(4 * s, -4 * s, 5 * s, 6 * s);

        // Reflection
        gc.setFill(Color.WHITE);
        gc.fillOval(-6 * s, -5 * s, 2 * s, 2 * s);
        gc.fillOval(5 * s, -5 * s, 2 * s, 2 * s);

        // Small mouth (line)
        gc.setStroke(Color.rgb(50, 150, 50));
        gc.setLineWidth(1);
        gc.strokeLine(-3 * s, 4 * s, 3 * s, 4 * s);

        // Antennae
        gc.setStroke(Color.rgb(100, 200, 100));
        gc.setLineWidth(1.5);
        gc.strokeLine(-6 * s, -12 * s, -8 * s, -16 * s);
        gc.strokeLine(6 * s, -12 * s, 8 * s, -16 * s);
        gc.setFill(Color.rgb(150, 255, 150));
        gc.fillOval(-10 * s, -18 * s, 4 * s, 4 * s);
        gc.fillOval(6 * s, -18 * s, 4 * s, 4 * s);
    }

    // ===== GETTERS AND MANAGEMENT =====

    public void switchToNextAvatar() {
        currentAvatarIndex = (currentAvatarIndex + 1) % avatarNames.size();
        System.out.println("Switched to avatar: " + getCurrentAvatarName());
    }

    public void switchToAvatar(int index) {
        if (index >= 0 && index < avatarNames.size()) {
            currentAvatarIndex = index;
            System.out.println("Switched to avatar: " + getCurrentAvatarName());
        }
    }

    public String getCurrentAvatarName() {
        return avatarNames.get(currentAvatarIndex);
    }

    public int getCurrentAvatarIndex() {
        return currentAvatarIndex;
    }

    public int getAvatarCount() {
        return avatarNames.size();
    }

    public boolean hasAvatars() {
        return true; // Always have avatars since they're code-drawn
    }

    public List<String> getAllAvatarNames() {
        return avatarNames;
    }

    // For settings menu preview
    public void renderAvatarPreview(GraphicsContext gc, double x, double y, double size, int avatarIndex) {
        gc.save();
        gc.translate(x + size / 2, y + size / 2);

        // Draw background circle
        LinearGradient bg = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, getAvatarColor(avatarIndex, true)),
                new Stop(1, getAvatarColor(avatarIndex, false)));
        gc.setFill(bg);
        gc.fillOval(-size / 2, -size / 2, size, size);

        // Draw the face
        int oldIndex = currentAvatarIndex;
        currentAvatarIndex = avatarIndex;
        renderAvatar(gc, 0, 0, 0, size);
        currentAvatarIndex = oldIndex;

        gc.restore();
    }

    private Color getAvatarColor(int index, boolean top) {
        switch (index) {
            case 0:
                return top ? Color.YELLOW : Color.GOLD;
            case 1:
                return top ? Color.rgb(255, 140, 60) : Color.rgb(220, 100, 40);
            case 2:
                return top ? Color.rgb(160, 110, 70) : Color.rgb(120, 80, 50);
            case 3:
                return top ? Color.WHITE : Color.rgb(220, 220, 220);
            case 4:
                return top ? Color.rgb(150, 180, 220) : Color.rgb(100, 130, 180);
            case 5:
                return top ? Color.rgb(150, 255, 150) : Color.rgb(100, 200, 100);
            default:
                return Color.GRAY;
        }
    }

    /**
     * Get body color gradient for the specified avatar
     * Returns array of [topColor, bottomColor]
     */
    public Color[] getBodyColors(int avatarIndex) {
        switch (avatarIndex) {
            case 0:
                return new Color[] { Color.YELLOW, Color.GOLD }; // Bird
            case 1:
                return new Color[] { Color.rgb(255, 140, 60), Color.rgb(220, 100, 40) }; // Cat - Orange
            case 2:
                return new Color[] { Color.rgb(160, 110, 70), Color.rgb(120, 80, 50) }; // Dog - Brown
            case 3:
                return new Color[] { Color.WHITE, Color.rgb(220, 220, 220) }; // Panda - White
            case 4:
                return new Color[] { Color.rgb(150, 180, 220), Color.rgb(100, 130, 180) }; // Robot - Blue
            case 5:
                return new Color[] { Color.rgb(150, 255, 150), Color.rgb(100, 200, 100) }; // Alien - Green
            default:
                return new Color[] { Color.YELLOW, Color.GOLD };
        }
    }
}
