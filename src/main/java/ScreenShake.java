import java.util.Random;

/**
 * ScreenShake - Camera shake effect for dramatic death impacts.
 * Apply the offset to your Canvas translation each frame during rendering.
 */
public class ScreenShake {
    private boolean shaking = false;
    private double timer = 0;
    private double duration = 0;
    private double initialIntensity = 0;
    private double offsetX = 0;
    private double offsetY = 0;
    private final Random random = new Random();

    /**
     * Start a decaying screen shake.
     * @param intensity maximum pixel displacement (e.g., 12)
     * @param durationSeconds total shake duration in seconds (e.g., 0.3)
     */
    public void start(double intensity, double durationSeconds) {
        this.shaking = true;
        this.timer = 0;
        this.duration = durationSeconds;
        this.initialIntensity = intensity;
    }

    /**
     * Update the shake each frame. Call this in your game loop.
     * @param dt delta time in seconds
     */
    public void update(double dt) {
        if (!shaking) return;

        timer += dt;
        if (timer >= duration) {
            stop();
            return;
        }

        // Decay intensity: starts at full, ends at ~33%
        double progress = timer / duration;
        double currentIntensity = initialIntensity * (1.0 - progress * 0.67);

        // Random offset within current intensity
        offsetX = (random.nextDouble() * 2 - 1) * currentIntensity;
        offsetY = (random.nextDouble() * 2 - 1) * currentIntensity;
    }

    /**
     * Stop the shake and reset offsets.
     */
    public void stop() {
        shaking = false;
        offsetX = 0;
        offsetY = 0;
        timer = 0;
    }

    public boolean isShaking() { return shaking; }
    public double getOffsetX() { return offsetX; }
    public double getOffsetY() { return offsetY; }
}
