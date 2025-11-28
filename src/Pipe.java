import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

/**
 * Represents a pair of pipes (top and bottom) that the bird must navigate
 * through
 */
public class Pipe {
    private double x;
    private double gapY;
    private boolean scored;

    private static final double PIPE_WIDTH = 80;
    private static final double GAP_SIZE = 180;
    private static final double SPEED = 3;
    private static final double PIPE_CAP_HEIGHT = 30;

    private int index;

    public Pipe(double x, double gapY, int index) {
        this.x = x;
        this.gapY = gapY;
        this.index = index;
        this.scored = false;
    }

    public int getIndex() {
        return index;
    }

    /**
     * Update pipe position (move left)
     */
    public void update() {
        x -= SPEED;
    }

    /**
     * Render the pipe pair with attractive styling
     */
    public void render(GraphicsContext gc, double canvasHeight) {
        // Create gradient for pipes
        LinearGradient pipeGradient = new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(50, 205, 50)), // Lime green
                new Stop(0.5, Color.rgb(34, 139, 34)), // Forest green
                new Stop(1, Color.rgb(0, 100, 0)) // Dark green
        );

        gc.setFill(pipeGradient);
        gc.setStroke(Color.rgb(0, 80, 0));
        gc.setLineWidth(3);

        // Top pipe body
        double topPipeHeight = gapY - GAP_SIZE / 2;
        gc.fillRect(x, 0, PIPE_WIDTH, topPipeHeight);
        gc.strokeRect(x, 0, PIPE_WIDTH, topPipeHeight);

        // Top pipe cap
        gc.fillRect(x - 5, topPipeHeight - PIPE_CAP_HEIGHT, PIPE_WIDTH + 10, PIPE_CAP_HEIGHT);
        gc.strokeRect(x - 5, topPipeHeight - PIPE_CAP_HEIGHT, PIPE_WIDTH + 10, PIPE_CAP_HEIGHT);

        // Bottom pipe body
        double bottomPipeY = gapY + GAP_SIZE / 2;
        double bottomPipeHeight = canvasHeight - bottomPipeY;
        gc.fillRect(x, bottomPipeY + PIPE_CAP_HEIGHT, PIPE_WIDTH, bottomPipeHeight);
        gc.strokeRect(x, bottomPipeY + PIPE_CAP_HEIGHT, PIPE_WIDTH, bottomPipeHeight);

        // Bottom pipe cap
        gc.fillRect(x - 5, bottomPipeY, PIPE_WIDTH + 10, PIPE_CAP_HEIGHT);
        gc.strokeRect(x - 5, bottomPipeY, PIPE_WIDTH + 10, PIPE_CAP_HEIGHT);

        // Add highlights for depth
        gc.setFill(Color.rgb(100, 255, 100, 0.3));
        gc.fillRect(x + 5, 0, 10, topPipeHeight);
        gc.fillRect(x + 5, bottomPipeY + PIPE_CAP_HEIGHT, 10, bottomPipeHeight);
    }

    /**
     * Check if bird collides with this pipe
     */
    public boolean collidesWith(double birdX, double birdY, double birdRadius) {
        // Check if bird is horizontally aligned with pipe
        if (birdX + birdRadius > x && birdX - birdRadius < x + PIPE_WIDTH) {
            // Check if bird hits top or bottom pipe
            double topPipeBottom = gapY - GAP_SIZE / 2;
            double bottomPipeTop = gapY + GAP_SIZE / 2;

            if (birdY - birdRadius < topPipeBottom || birdY + birdRadius > bottomPipeTop) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if bird has passed this pipe (for scoring)
     */
    public boolean isPassed(double birdX) {
        return !scored && birdX > x + PIPE_WIDTH;
    }

    /**
     * Mark this pipe as scored
     */
    public void setScored() {
        this.scored = true;
    }

    // Getters
    public double getX() {
        return x;
    }

    public boolean isOffScreen() {
        return x + PIPE_WIDTH < 0;
    }

    public static double getWidth() {
        return PIPE_WIDTH;
    }

    /**
     * Get the distance from the bird to this pipe (horizontal distance)
     */
    public double getDistanceToBird(double birdX) {
        // Distance to the center of the pipe
        double pipeCenterX = x + PIPE_WIDTH / 2;
        return Math.abs(birdX - pipeCenterX);
    }

    /**
     * Check if bird is near this pipe (for proximity sound trigger)
     */
    public boolean isNearBird(double birdX, double threshold) {
        // Check if bird is approaching the pipe from the left
        double distanceToLeft = x - birdX;
        return distanceToLeft > 0 && distanceToLeft < threshold;
    }
}
