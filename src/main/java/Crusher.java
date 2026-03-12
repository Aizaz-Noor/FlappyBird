import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Crusher - Two blocks that close and open, creating a timing challenge
 */
public class Crusher {
    private double x;
    private double topY; // Top block Y position (moves down)
    private double bottomY; // Bottom block Y position (moves up)
    private double speed;
    private double blockHeight = 100;
    private double maxGap = 300;
    private double minGap = 160; // FIXED: Was 120px (too tight!), now 160px for bird to pass
    private double currentGap;
    private boolean closing; // true = closing, false = opening

    private static final double WIDTH = 120;
    private static final double CLOSE_SPEED = 90.0; // Pixels per second

    public Crusher(double x, double centerY, double speed) {
        this.x = x;
        this.speed = speed;
        this.currentGap = maxGap;
        this.topY = centerY - maxGap / 2 - blockHeight;
        this.bottomY = centerY + maxGap / 2;
        this.closing = true;
    }

    public void update(double dt) {
        x -= speed * dt;

        // Close/Open animation
        if (closing) {
            currentGap -= CLOSE_SPEED * dt;
            if (currentGap <= minGap) {
                closing = false;
            }
        } else {
            currentGap += CLOSE_SPEED * dt;
            if (currentGap >= maxGap) {
                closing = true;
            }
        }

        // Update block positions
        double centerY = (topY + blockHeight + bottomY) / 2;
        topY = centerY - currentGap / 2 - blockHeight;
        bottomY = centerY + currentGap / 2;
    }

    public void render(GraphicsContext gc, double groundY) {
        // Danger indicator (red tint when closing)
        // Show danger when approaching minimum gap (e.g. within 60px of closing)
        if (closing && currentGap < minGap + 60) {
            gc.setFill(Color.rgb(255, 0, 0, 0.1));
            gc.fillRect(x, 0, WIDTH, groundY);
        }

        // Top block
        gc.setFill(Color.rgb(60, 60, 60));
        gc.fillRect(x, topY, WIDTH, blockHeight);

        // Gradient for 3D effect
        gc.setFill(Color.rgb(40, 40, 40));
        gc.fillRect(x, topY, 10, blockHeight);
        gc.setFill(Color.rgb(80, 80, 80));
        gc.fillRect(x + WIDTH - 10, topY, 10, blockHeight);

        // Bottom block
        gc.setFill(Color.rgb(60, 60, 60));
        gc.fillRect(x, bottomY, WIDTH, blockHeight);

        // Gradient
        gc.setFill(Color.rgb(40, 40, 40));
        gc.fillRect(x, bottomY, 10, blockHeight);
        gc.setFill(Color.rgb(80, 80, 80));
        gc.fillRect(x + WIDTH - 10, bottomY, 10, blockHeight);

        // Warning stripes
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(3);
        for (int i = 0; i < WIDTH; i += 20) {
            gc.strokeLine(x + i, topY + blockHeight - 5, x + i + 10, topY + blockHeight - 5);
            gc.strokeLine(x + i, bottomY + 5, x + i + 10, bottomY + 5);
        }
    }

    public boolean collidesWith(double birdX, double birdY, double birdRadius) {
        // Check if bird is horizontally within crusher
        if (birdX + birdRadius > x && birdX - birdRadius < x + WIDTH) {
            // Check if bird hits top or bottom block
            if (birdY - birdRadius < topY + blockHeight ||
                    birdY + birdRadius > bottomY) {
                return true;
            }
        }
        return false;
    }

    public boolean isOffScreen() {
        return x + WIDTH < 0;
    }

    public double getX() {
        return x;
    }

    public double getCurrentGap() {
        return currentGap;
    }

    public double getMinGap() {
        return minGap;
    }

    public boolean isClosing() {
        return closing;
    }
}
