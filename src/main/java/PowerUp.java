import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class PowerUp {
    public enum Type {
        TIME_DILATION, // Blue - Slow motion
        GHOST, // White - Invincible
        SHRINK // Purple - Tiny bird
    }

    private double x, y;
    private double size = 30;
    private Type type;
    private boolean collected = false;

    public PowerUp(double x, double y, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public void update(double speed) {
        x -= speed;
    }

    public void render(GraphicsContext gc) {
        if (collected)
            return;

        Color centerColor, outerColor;
        switch (type) {
            case TIME_DILATION:
                centerColor = Color.CYAN;
                outerColor = Color.BLUE;
                break;
            case GHOST:
                centerColor = Color.WHITE;
                outerColor = Color.LIGHTGRAY;
                break;
            case SHRINK:
                centerColor = Color.MAGENTA;
                outerColor = Color.PURPLE;
                break;
            default:
                centerColor = Color.WHITE;
                outerColor = Color.BLACK;
        }

        // Draw Glow
        gc.setGlobalAlpha(0.6);
        gc.setFill(outerColor);
        gc.fillOval(x - 5, y - 5, size + 10, size + 10);
        gc.setGlobalAlpha(1.0);

        // Core
        gc.setFill(centerColor);
        gc.fillOval(x, y, size, size);

        // Border
        gc.setStroke(outerColor);
        gc.setLineWidth(2);
        gc.strokeOval(x, y, size, size);
    }

    public boolean collidesWith(double bx, double by, double br) {
        if (collected)
            return false;
        // Simple circle collision
        double centerX = x + size / 2;
        double centerY = y + size / 2;
        double dx = centerX - bx;
        double dy = centerY - by;
        double dist = Math.sqrt(dx * dx + dy * dy);
        return dist < (size / 2 + br);
    }

    public Type getType() {
        return type;
    }

    public void collect() {
        collected = true;
    }

    public boolean isCollected() {
        return collected;
    }

    public boolean isOffScreen() {
        return x + size < 0;
    }
}
