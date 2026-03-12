import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

/**
 * Pipe - Obstacle with top and bottom parts and a gap
 * Bird must fly through the gap to score points
 */
public class Pipe {
    private double x; // X position (moves left)
    private double gapY; // Center of gap
    private double gapSize; // Size of gap
    private double speed; // Movement speed
    private boolean scored; // Already scored?
    private double vy; // Vertical speed
    private boolean inverted; // For gravity flip mode

    private static final double WIDTH = 80;
    private static final double CAP_HEIGHT = 25;

    // Constructor with custom speed and gap size (for difficulty levels)
    public Pipe(double x, double gapY, double speed, double gapSize, boolean inverted) {
        this.x = x;
        this.gapY = gapY;
        this.gapSize = gapSize;
        this.speed = speed;
        this.scored = false;
        this.vy = (Math.random() < 0.5 ? 1 : -1) * (Math.random() * 1.5 + 0.5);
        this.inverted = inverted;
    }

    // pdate() Move pipe left

    // update() Move pipe left with Delta Time
    public void update(double dt) {
        x -= speed * dt;
        // Vertical movement
        gapY += vy * (dt * 60); // Scale vy to match previous frame-based speed

        // Bounce logic - keep gap within playable area
        // MIN: 150px from top, MAX: screen height - ground - 150px buffer
        double minGapY = 150;
        double maxGapY = 700 - 50 - 150; // HEIGHT - GROUND - buffer = 500
        if (gapY < minGapY || gapY > maxGapY) {
            vy = -vy;
            gapY = Math.max(minGapY, Math.min(gapY, maxGapY)); // Clamp to bounds
        }
    }

    // render() Draw both pipes with 3D effect

    public void render(GraphicsContext gc, double groundY) {
        if (inverted) {
            renderInverted(gc, groundY);
        } else {
            renderNormal(gc, groundY);
        }
    }

    private void renderNormal(GraphicsContext gc, double groundY) {
        double topHeight = gapY - gapSize / 2;
        double bottomY = gapY + gapSize / 2;

        // 3D gradient (light left, dark right)
        LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(100, 220, 100)),
                new Stop(0.3, Color.rgb(50, 180, 50)),
                new Stop(0.7, Color.rgb(30, 140, 30)),
                new Stop(1, Color.rgb(20, 100, 20)));

        gc.setFill(gradient);

        // TOP PIPE
        gc.fillRect(x, 0, WIDTH, topHeight);
        gc.fillRect(x - 6, topHeight - CAP_HEIGHT, WIDTH + 12, CAP_HEIGHT);

        // Highlight (left edge shine)
        gc.setFill(Color.rgb(150, 255, 150, 0.4));
        gc.fillRect(x + 3, 0, 6, topHeight - CAP_HEIGHT);

        // Border
        gc.setStroke(Color.rgb(10, 50, 10));
        gc.setLineWidth(2);
        gc.strokeRect(x, 0, WIDTH, topHeight);
        gc.strokeRect(x - 6, topHeight - CAP_HEIGHT, WIDTH + 12, CAP_HEIGHT);

        // BOTTOM PIPE
        gc.setFill(gradient);
        gc.fillRect(x, bottomY + CAP_HEIGHT, WIDTH, groundY - bottomY - CAP_HEIGHT);
        gc.fillRect(x - 6, bottomY, WIDTH + 12, CAP_HEIGHT);

        // Highlight
        gc.setFill(Color.rgb(150, 255, 150, 0.4));
        gc.fillRect(x + 3, bottomY + CAP_HEIGHT, 6, groundY - bottomY - CAP_HEIGHT);

        // Border
        gc.setStroke(Color.rgb(10, 50, 10));
        gc.strokeRect(x, bottomY + CAP_HEIGHT, WIDTH, groundY - bottomY - CAP_HEIGHT);
        gc.strokeRect(x - 6, bottomY, WIDTH + 12, CAP_HEIGHT);
    }

    // collidesWith() Check collision with bird

    public boolean collidesWith(double birdX, double birdY, double birdRadius) {
        // Check horizontal overlap
        if (birdX + birdRadius > x && birdX - birdRadius < x + WIDTH) {
            double topBottom = gapY - gapSize / 2;
            double bottomTop = gapY + gapSize / 2;
            // Check if outside safe zone
            if (birdY - birdRadius < topBottom || birdY + birdRadius > bottomTop) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if bird is close to pipe (for danger sound warning)
     */
    public boolean isCloseBy(double birdX, double birdY, double dangerRadius) {
        // Check if bird is horizontally close to pipe
        if (birdX + dangerRadius > x && birdX - dangerRadius < x + WIDTH) {
            double topBottom = gapY - gapSize / 2;
            double bottomTop = gapY + gapSize / 2;
            // Check if outside safe zone or close to edges
            if (birdY - dangerRadius < topBottom || birdY + dangerRadius > bottomTop) {
                return true;
            }
        }
        return false;
    }

    // isPassed() Check if bird passed this pipe

    public boolean isPassed(double birdX) {
        return !scored && birdX > x + WIDTH;
    }

    public void setScored() {
        scored = true;
    }

    public boolean isOffScreen() {
        return x + WIDTH < 0;
    }

    // Getters for replay system
    public double getX() {
        return x;
    }

    public double getGapY() {
        return gapY;
    }

    public double getGapSize() {
        return gapSize;
    }

    public boolean isInverted() {
        return inverted;
    }

    /**
     * Render pipes in inverted orientation (for gravity flip)
     */
    private void renderInverted(GraphicsContext gc, double groundY) {
        // In inverted mode, the gap is near the ground, pipes extend from gap upward
        double bottomGapEdge = gapY + gapSize / 2;
        double topGapEdge = gapY - gapSize / 2;

        // 3D gradient (light left, dark right)
        LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(100, 220, 100)),
                new Stop(0.3, Color.rgb(50, 180, 50)),
                new Stop(0.7, Color.rgb(30, 140, 30)),
                new Stop(1, Color.rgb(20, 100, 20)));

        gc.setFill(gradient);

        // TOP PIPE (extends upward from top gap edge)
        gc.fillRect(x, 0, WIDTH, topGapEdge);
        gc.fillRect(x - 6, topGapEdge - CAP_HEIGHT, WIDTH + 12, CAP_HEIGHT);

        // Highlight
        gc.setFill(Color.rgb(150, 255, 150, 0.4));
        gc.fillRect(x + 3, 0, 6, topGapEdge);

        // Border
        gc.setStroke(Color.rgb(10, 50, 10));
        gc.setLineWidth(2);
        gc.strokeRect(x, 0, WIDTH, topGapEdge);
        gc.strokeRect(x - 6, topGapEdge - CAP_HEIGHT, WIDTH + 12, CAP_HEIGHT);

        // BOTTOM PIPE (extends downward from bottom gap edge)
        gc.setFill(gradient);
        gc.fillRect(x, bottomGapEdge + CAP_HEIGHT, WIDTH, groundY - bottomGapEdge - CAP_HEIGHT);
        gc.fillRect(x - 6, bottomGapEdge, WIDTH + 12, CAP_HEIGHT);

        // Highlight
        gc.setFill(Color.rgb(150, 255, 150, 0.4));
        gc.fillRect(x + 3, bottomGapEdge + CAP_HEIGHT, 6, groundY - bottomGapEdge - CAP_HEIGHT);

        // Border
        gc.setStroke(Color.rgb(10, 50, 10));
        gc.strokeRect(x, bottomGapEdge + CAP_HEIGHT, WIDTH, groundY - bottomGapEdge - CAP_HEIGHT);
        gc.strokeRect(x - 6, bottomGapEdge, WIDTH + 12, CAP_HEIGHT);
    }

    // Static renderer for replay system (No object creation)
    public static void renderStatic(GraphicsContext gc, double x, double gapY, double gapSize, boolean inverted,
            double groundY) {
        if (inverted) {
            renderInvertedStatic(gc, x, gapY, gapSize, groundY);
        } else {
            renderNormalStatic(gc, x, gapY, gapSize, groundY);
        }
    }

    private static void renderNormalStatic(GraphicsContext gc, double x, double gapY, double gapSize, double groundY) {
        double topHeight = gapY - gapSize / 2;
        double bottomY = gapY + gapSize / 2;

        LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(100, 220, 100)),
                new Stop(0.3, Color.rgb(50, 180, 50)),
                new Stop(0.7, Color.rgb(30, 140, 30)),
                new Stop(1, Color.rgb(20, 100, 20)));

        gc.setFill(gradient);
        // TOP PIPE
        gc.fillRect(x, 0, WIDTH, topHeight);
        gc.fillRect(x - 6, topHeight - CAP_HEIGHT, WIDTH + 12, CAP_HEIGHT);
        // Highlight
        gc.setFill(Color.rgb(150, 255, 150, 0.4));
        gc.fillRect(x + 3, 0, 6, topHeight - CAP_HEIGHT);
        // Border
        gc.setStroke(Color.rgb(10, 50, 10));
        gc.setLineWidth(2);
        gc.strokeRect(x, 0, WIDTH, topHeight);
        gc.strokeRect(x - 6, topHeight - CAP_HEIGHT, WIDTH + 12, CAP_HEIGHT);

        // BOTTOM PIPE
        gc.setFill(gradient);
        gc.fillRect(x, bottomY + CAP_HEIGHT, WIDTH, groundY - bottomY - CAP_HEIGHT);
        gc.fillRect(x - 6, bottomY, WIDTH + 12, CAP_HEIGHT);
        // Highlight
        gc.setFill(Color.rgb(150, 255, 150, 0.4));
        gc.fillRect(x + 3, bottomY + CAP_HEIGHT, 6, groundY - bottomY - CAP_HEIGHT);
        // Border
        gc.setStroke(Color.rgb(10, 50, 10));
        gc.strokeRect(x, bottomY + CAP_HEIGHT, WIDTH, groundY - bottomY - CAP_HEIGHT);
        gc.strokeRect(x - 6, bottomY, WIDTH + 12, CAP_HEIGHT);
    }

    private static void renderInvertedStatic(GraphicsContext gc, double x, double gapY, double gapSize,
            double groundY) {
        double bottomGapEdge = gapY + gapSize / 2;
        double topGapEdge = gapY - gapSize / 2;

        LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(100, 220, 100)),
                new Stop(0.3, Color.rgb(50, 180, 50)),
                new Stop(0.7, Color.rgb(30, 140, 30)),
                new Stop(1, Color.rgb(20, 100, 20)));

        gc.setFill(gradient);
        // TOP PIPE
        gc.fillRect(x, 0, WIDTH, topGapEdge);
        gc.fillRect(x - 6, topGapEdge - CAP_HEIGHT, WIDTH + 12, CAP_HEIGHT);
        // Highlight
        gc.setFill(Color.rgb(150, 255, 150, 0.4));
        gc.fillRect(x + 3, 0, 6, topGapEdge);
        // Border
        gc.setStroke(Color.rgb(10, 50, 10));
        gc.setLineWidth(2);
        gc.strokeRect(x, 0, WIDTH, topGapEdge);
        gc.strokeRect(x - 6, topGapEdge - CAP_HEIGHT, WIDTH + 12, CAP_HEIGHT);

        // BOTTOM PIPE
        gc.setFill(gradient);
        gc.fillRect(x, bottomGapEdge + CAP_HEIGHT, WIDTH, groundY - bottomGapEdge - CAP_HEIGHT);
        gc.fillRect(x - 6, bottomGapEdge, WIDTH + 12, CAP_HEIGHT);
        // Highlight
        gc.setFill(Color.rgb(150, 255, 150, 0.4));
        gc.fillRect(x + 3, bottomGapEdge + CAP_HEIGHT, 6, groundY - bottomGapEdge - CAP_HEIGHT);
        // Border
        gc.setStroke(Color.rgb(10, 50, 10));
        gc.strokeRect(x, bottomGapEdge + CAP_HEIGHT, WIDTH, groundY - bottomGapEdge - CAP_HEIGHT);
        gc.strokeRect(x - 6, bottomGapEdge, WIDTH + 12, CAP_HEIGHT);
    }
}
