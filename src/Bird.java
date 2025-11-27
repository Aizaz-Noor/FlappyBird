import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

/**
 * Represents the player-controlled bird with physics simulation
 */
public class Bird {
    private double x, y;
    private double velocity;
    private double rotation;

    private static final double GRAVITY = 0.5;
    private static final double JUMP_STRENGTH = -10;
    private static final double MAX_VELOCITY = 10;
    private static final double BIRD_SIZE = 30;

    public Bird(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.velocity = 0;
        this.rotation = 0;
    }

    /**
     * Apply physics updates to the bird
     */
    public void update() {
        velocity += GRAVITY;

        // Limit maximum fall speed
        if (velocity > MAX_VELOCITY) {
            velocity = MAX_VELOCITY;
        }

        y += velocity;

        // Update rotation based on velocity for smooth animation
        rotation = Math.min(Math.max(velocity * 3, -30), 90);
    }

    /**
     * Make the bird jump
     */
    public void jump() {
        velocity = JUMP_STRENGTH;
    }

    /**
     * Render the bird with avatar or attractive gradient and rotation
     */
    public void render(GraphicsContext gc, AvatarManager avatarManager) {
        gc.save();

        // Translate to bird center for rotation
        gc.translate(x, y);
        gc.rotate(rotation);

        // Render avatar face if available
        if (avatarManager != null && avatarManager.hasAvatars()) {
            // Render the avatar face
            avatarManager.renderAvatar(gc, x, y, rotation, BIRD_SIZE);
        } else {
            // Fallback to default bird rendering
            renderDefaultBird(gc);
        }

        // Always render beak (on top of avatar for realistic effect)
        gc.setFill(Color.rgb(255, 100, 0));
        double[] beakX = { BIRD_SIZE / 2, BIRD_SIZE / 2 + 10, BIRD_SIZE / 2 };
        double[] beakY = { -3, 0, 3 };
        gc.fillPolygon(beakX, beakY, 3);

        gc.restore();
    }

    /**
     * Render default bird appearance (when no avatars are loaded)
     */
    private void renderDefaultBird(GraphicsContext gc) {
        // Create attractive gradient for bird body
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(255, 215, 0)), // Gold
                new Stop(0.5, Color.rgb(255, 165, 0)), // Orange
                new Stop(1, Color.rgb(255, 140, 0)) // Dark Orange
        );

        gc.setFill(gradient);
        gc.fillOval(-BIRD_SIZE / 2, -BIRD_SIZE / 2, BIRD_SIZE, BIRD_SIZE);

        // Add bird eye
        gc.setFill(Color.WHITE);
        gc.fillOval(BIRD_SIZE / 4 - 8, -BIRD_SIZE / 4 - 4, 8, 8);
        gc.setFill(Color.BLACK);
        gc.fillOval(BIRD_SIZE / 4 - 6, -BIRD_SIZE / 4 - 2, 4, 4);
    }

    /**
     * Reset bird to initial position
     */
    public void reset(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.velocity = 0;
        this.rotation = 0;
    }

    // Getters for collision detection
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getSize() {
        return BIRD_SIZE;
    }

    public double getRadius() {
        return BIRD_SIZE / 2;
    }
}
