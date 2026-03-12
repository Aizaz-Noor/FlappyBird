import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * LaserGate - A timed obstacle that turns on and off
 */
public class LaserGate {
    private double x;
    private double y; // Center Y position
    private double width = 3; // Thin laser
    private double height; // From top to bottom or partial
    private double speed;
    private boolean active;
    private long lastToggleTime;
    private static final long TOGGLE_INTERVAL = 2_000_000_000L; // 2 seconds

    public LaserGate(double x, double y, double height, double speed) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.speed = speed;
        this.active = true;
        this.lastToggleTime = System.nanoTime();
    }

    public void update(long now, double dt) {
        x -= speed * dt;

        // Toggle laser on/off every 2 seconds
        if (now - lastToggleTime >= TOGGLE_INTERVAL) {
            active = !active;
            lastToggleTime = now;
        }
    }

    public void render(GraphicsContext gc) {
        if (active) {
            // Draw active red laser
            gc.setFill(Color.rgb(255, 0, 0, 0.8));
            gc.fillRect(x, y - height / 2, width, height);

            // Glow effect
            gc.setFill(Color.rgb(255, 100, 100, 0.3));
            gc.fillRect(x - 3, y - height / 2, width + 6, height);
        } else {
            // Draw inactive laser (dim)
            gc.setFill(Color.rgb(100, 0, 0, 0.3));
            gc.fillRect(x, y - height / 2, width, height);
        }
    }

    public boolean collidesWith(double birdX, double birdY, double birdRadius) {
        if (!active)
            return false;

        // Check if bird overlaps with laser
        if (birdX + birdRadius > x && birdX - birdRadius < x + width) {
            if (birdY - birdRadius < y + height / 2 && birdY + birdRadius > y - height / 2) {
                return true;
            }
        }
        return false;
    }

    public boolean isOffScreen() {
        return x + width < 0;
    }

    public boolean isActive() {
        return active;
    }

    public double getHeight() {
        return height;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
