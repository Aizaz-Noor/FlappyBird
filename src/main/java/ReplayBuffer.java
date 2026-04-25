

/**
 * ReplayBuffer - A zero-allocation circular buffer storing primitive data for replays.
 */
public class ReplayBuffer {
    private static final int MAX_FRAMES = 7200; // 2 minutes at 60fps

    // Bird state
    public double[] birdY = new double[MAX_FRAMES];
    public double[] birdRotation = new double[MAX_FRAMES];
    public boolean[] gravityFlipped = new boolean[MAX_FRAMES];
    public int[] score = new int[MAX_FRAMES];

    // Pipes (max 5 per frame)
    public int[] pipeCount = new int[MAX_FRAMES];
    public double[] pipeX = new double[MAX_FRAMES * 5];
    public double[] pipeGapY = new double[MAX_FRAMES * 5];
    public double[] pipeGapSize = new double[MAX_FRAMES * 5];
    public boolean[] pipeInverted = new boolean[MAX_FRAMES * 5];

    // Lasers (max 3 per frame)
    public int[] laserCount = new int[MAX_FRAMES];
    public double[] laserX = new double[MAX_FRAMES * 3];
    public double[] laserY = new double[MAX_FRAMES * 3];
    public double[] laserHeight = new double[MAX_FRAMES * 3];
    public boolean[] laserActive = new boolean[MAX_FRAMES * 3];

    // Crushers (max 3 per frame)
    public int[] crusherCount = new int[MAX_FRAMES];
    public double[] crusherX = new double[MAX_FRAMES * 3];
    public double[] crusherCurrentGap = new double[MAX_FRAMES * 3];
    public double[] crusherMinGap = new double[MAX_FRAMES * 3];
    public boolean[] crusherClosing = new boolean[MAX_FRAMES * 3];

    private int head = 0;
    private int size = 0;
    
    public void recordFrame(double bY, double bRot, boolean bFlipped, int bScore, 
                            java.util.List<Pipe> pipes, java.util.List<LaserGate> lasers, java.util.List<Crusher> crushers) {
        int i = head;
        birdY[i] = bY;
        birdRotation[i] = bRot;
        gravityFlipped[i] = bFlipped;
        score[i] = bScore;
        
        int pCount = 0;
        int framePipeOffset = i * 5;
        for(int p = 0; p < pipes.size() && pCount < 5; p++) {
            Pipe pipe = pipes.get(p);
            if (pipe.isPoolActive()) {
                pipeX[framePipeOffset + pCount] = pipe.getX();
                pipeGapY[framePipeOffset + pCount] = pipe.getGapY();
                pipeGapSize[framePipeOffset + pCount] = pipe.getGapSize();
                pipeInverted[framePipeOffset + pCount] = pipe.isInverted();
                pCount++;
            }
        }
        pipeCount[i] = pCount;
        
        int lCount = 0;
        int frameLaserOffset = i * 3;
        for(int l = 0; l < lasers.size() && lCount < 3; l++) {
            LaserGate laser = lasers.get(l);
            if (laser.isPoolActive()) {
                laserX[frameLaserOffset + lCount] = laser.getX();
                laserY[frameLaserOffset + lCount] = laser.getY();
                laserHeight[frameLaserOffset + lCount] = laser.getHeight();
                laserActive[frameLaserOffset + lCount] = laser.isActive();
                lCount++;
            }
        }
        laserCount[i] = lCount;
        
        int cCount = 0;
        int frameCrusherOffset = i * 3;
        for(int c = 0; c < crushers.size() && cCount < 3; c++) {
            Crusher crusher = crushers.get(c);
            if (crusher.isPoolActive()) {
                crusherX[frameCrusherOffset + cCount] = crusher.getX();
                crusherCurrentGap[frameCrusherOffset + cCount] = crusher.getCurrentGap();
                crusherMinGap[frameCrusherOffset + cCount] = crusher.getMinGap();
                crusherClosing[frameCrusherOffset + cCount] = crusher.isClosing();
                cCount++;
            }
        }
        crusherCount[i] = cCount;
        
        head = (head + 1) % MAX_FRAMES;
        if (size < MAX_FRAMES) size++;
    }

    public void clear() {
        head = 0;
        size = 0;
    }

    public int getSize() {
        return size;
    }

    public int getOrderedIndex(int playhead) {
        if (size < MAX_FRAMES) return playhead;
        return (head + playhead) % MAX_FRAMES;
    }
}
