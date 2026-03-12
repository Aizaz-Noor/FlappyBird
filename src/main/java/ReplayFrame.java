import java.util.ArrayList;
import java.util.List;

/**
 * ReplayFrame - Stores game state for a single frame
 * Used to record and replay gameplay
 */
public class ReplayFrame {
    public final double birdY;
    public final double birdRotation;
    public final boolean gravityFlipped;
    public final List<PipeData> pipes;
    public final List<LaserData> lasers;
    public final List<CrusherData> crushers;
    public final int score;

    public ReplayFrame(double birdY, double birdRotation, boolean gravityFlipped, int score) {
        this.birdY = birdY;
        this.birdRotation = birdRotation;
        this.gravityFlipped = gravityFlipped;
        this.score = score;
        this.pipes = new ArrayList<>();
        this.lasers = new ArrayList<>();
        this.crushers = new ArrayList<>();
    }

    public void addPipe(double x, double gapY, double gapSize, boolean inverted) {
        pipes.add(new PipeData(x, gapY, gapSize, inverted));
    }

    public void addLaser(double x, double y, double height, boolean active) {
        lasers.add(new LaserData(x, y, height, active));
    }

    public void addCrusher(double x, double currentGap, double minGap, boolean closing) {
        crushers.add(new CrusherData(x, currentGap, minGap, closing));
    }

    public static class PipeData {
        public final double x, gapY, gapSize;
        public final boolean inverted;

        public PipeData(double x, double gapY, double gapSize, boolean inverted) {
            this.x = x;
            this.gapY = gapY;
            this.gapSize = gapSize;
            this.inverted = inverted;
        }
    }

    public static class LaserData {
        public final double x, y, height;
        public final boolean active;

        public LaserData(double x, double y, double height, boolean active) {
            this.x = x;
            this.y = y;
            this.height = height;
            this.active = active;
        }
    }

    public static class CrusherData {
        public final double x, currentGap, minGap;
        public final boolean closing;

        public CrusherData(double x, double currentGap, double minGap, boolean closing) {
            this.x = x;
            this.currentGap = currentGap;
            this.minGap = minGap;
            this.closing = closing;
        }
    }
}
