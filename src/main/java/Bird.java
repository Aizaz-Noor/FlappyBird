import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

/**
 * Bird - The player controlled character
 * Uses simple physics: gravity pulls down, jump pushes up
 * Now with avatar support!
 */
public class Bird {
    private double x, y; // Position
    private double velocity; // Speed (positive=down, negative=up)
    private double rotation; // Visual tilt angle
    private AvatarManager avatarManager; // For custom faces

    // Physics constants (Pixels per second)
    private static final double GRAVITY = 1600;
    private static final double JUMP_STRENGTH = -500;
    private static final double MAX_VELOCITY = 800; // Terminal velocity
    private static final double SIZE = 30;

    private boolean gravityFlipped = false;
    private boolean ghostMode = false;
    private boolean shrunk = false;

    public Bird(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.velocity = 0;
        this.rotation = 0;
    }

    /**
     * Set the avatar manager for custom bird faces
     */
    public void setAvatarManager(AvatarManager avatarManager) {
        this.avatarManager = avatarManager;
    }

    // update() Apply gravity and move bird with Delta Time
    public void update(double dt) {
        if (gravityFlipped) {
            velocity -= GRAVITY * dt;
            if (velocity < -MAX_VELOCITY)
                velocity = -MAX_VELOCITY;
        } else {
            velocity += GRAVITY * dt;
            if (velocity > MAX_VELOCITY)
                velocity = MAX_VELOCITY;
        }

        y += velocity * dt;

        // Calculate rotation (Visual only)
        // Scaled down to match previous visual feel
        double rot = (velocity / MAX_VELOCITY) * 30; // Max tilt 30 degrees

        // Tilt up faster when jumping
        if ((!gravityFlipped && velocity < 0) || (gravityFlipped && velocity > 0)) {
            rot = (!gravityFlipped ? -25 : 25);
        }

        rotation = Math.min(Math.max(rot, -30), 90);
    }

    /**
     * Apply wind force to bird (affects horizontal position)
     */
    public void applyWind(double windForce) {
        x += windForce;
        // Clamp bird position to keep it on screen
        final double MIN_X = SIZE;
        final double MAX_X = 1000 - SIZE; // Screen width (1000) - SIZE
        x = Math.max(MIN_X, Math.min(x, MAX_X)); // Keep bird visible within bounds
    }

    // jump() Make bird fly up
    public void jump() {
        if (gravityFlipped) {
            velocity = -JUMP_STRENGTH; // Jump DOWN (positive Y)
        } else {
            velocity = JUMP_STRENGTH; // Jump UP (negative Y)
        }
    }

    public void flipGravity() {
        gravityFlipped = !gravityFlipped;
    }

    public boolean isGravityFlipped() {
        return gravityFlipped;
    }

    // render() Draw the bird with avatar

    public void render(GraphicsContext gc) {
        gc.save();
        gc.translate(x, y);

        if (gravityFlipped) {
            gc.rotate(180);
        }

        gc.rotate(rotation);

        // Power-up visual effects
        if (shrunk) {
            gc.scale(0.5, 0.5);
        }

        // NOTE: Ghost mode transparency is handled in GameEngine.render()
        // to avoid double-applying (50% × 50% = 25% opacity)

        // Draw bird body circle with avatar-specific color
        Color[] colors = getAvatarBodyColors();
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, colors[0]), new Stop(1, colors[1]));
        gc.setFill(gradient);
        gc.fillOval(-SIZE / 2, -SIZE / 2, SIZE, SIZE);

        // Draw avatar face if available (pass 0,0 since we're already translated)
        if (avatarManager != null && avatarManager.hasAvatars()) {
            // Render avatar in circular format - coordinates are 0,0 since we're already
            // translated
            avatarManager.renderAvatar(gc, 0, 0, rotation, SIZE);
        } else {
            // Fallback: Draw simple bird face
            // Eye
            gc.setFill(Color.WHITE);
            gc.fillOval(5, -10, 8, 8);
            gc.setFill(Color.BLACK);
            gc.fillOval(7, -8, 4, 4);

            // Beak
            gc.setFill(Color.ORANGERED);
            gc.fillPolygon(new double[] { SIZE / 2, SIZE / 2 + 10, SIZE / 2 }, new double[] { -3, 0, 3 }, 3);
        }

        // Add border/outline for definition
        gc.setStroke(Color.rgb(200, 160, 0));
        gc.setLineWidth(2);
        gc.strokeOval(-SIZE / 2, -SIZE / 2, SIZE, SIZE);

        gc.restore();
    }

    // reset() Reset bird to starting position

    public void reset(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.velocity = 0;
        this.rotation = 0;
        this.gravityFlipped = false;
        this.ghostMode = false;
        this.shrunk = false;
    }

    // Getters for collision detection
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRadius() {
        return (shrunk ? SIZE / 2 : SIZE) / 2;
    }

    public void setGhostMode(boolean active) {
        this.ghostMode = active;
    }

    public boolean isGhostMode() {
        return ghostMode;
    }

    public void setShrunk(boolean active) {
        this.shrunk = active;
    }

    /**
     * Get body colors based on current avatar
     */
    private Color[] getAvatarBodyColors() {
        if (avatarManager != null && avatarManager.hasAvatars()) {
            return avatarManager.getBodyColors(avatarManager.getCurrentAvatarIndex());
        }
        // Default yellow bird
        return new Color[] { Color.YELLOW, Color.GOLD };
    }

    public double getRotation() {
        return rotation;
    }
}
