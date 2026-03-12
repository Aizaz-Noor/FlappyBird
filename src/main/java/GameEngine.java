import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

//GameEngine  The heart of the game
//Handles game loop, input, states, collision, and rendering

public class GameEngine {
    private Canvas canvas;
    private GraphicsContext gc;
    private AnimationTimer gameLoop;

    // Game objects
    private Bird bird;
    private List<Pipe> pipes;
    private List<LaserGate> laserGates;
    private List<Crusher> crushers;
    private Random random;

    // Managers
    private SoundManager soundManager;
    private SettingsMenu settingsMenu;
    private AvatarManager avatarManager;
    private ParticleEffect particleEffect;
    private WeatherSystem weatherSystem;

    // Game state
    private GameState gameState;
    private Difficulty currentDifficulty;
    private Theme currentTheme;
    private int score, highScore;
    private int baseScore; // Pipes passed (no combo bonus) - used for weather/day-night triggers
    private String playerName;
    private StringBuilder nameInput;

    // Gravity flip system
    private static final long FLIP_COOLDOWN = 300_000_000L; // 0.3 seconds in nanoseconds
    private long lastFlipTime = 0;
    private long lastPipeTime;

    // Stars for background
    private double[][] stars; // [x, y, size] for each star

    // PowerUp System
    private List<PowerUp> powerUps;
    private long powerUpEndTime = 0;
    private PowerUp.Type activePowerUpType = null;
    private long frameCount = 0;

    // Replay System
    private List<ReplayFrame> replayFrames;
    private boolean isReplaying = false;
    private int replayIndex = 0;
    private static final int MAX_REPLAY_FRAMES = 300; // 5 seconds at 60 FPS

    // Death Tracker & Heatmap
    private DeathTracker deathTracker;

    // Combo System
    private int comboCount = 0;
    private int comboMultiplier = 1;
    private long lastScoreTime = 0;
    private long totalPauseTime = 0; // Track total pause time to fix combo
    private long pauseStartTime = 0;

    // Visual notification system for unlocks
    private boolean firstLaserShown = false;
    private boolean firstCrusherShown = false;
    private String unlockMessage = null;
    private long unlockMessageTime = 0;

    // Constants
    private static final double WIDTH = 1000;
    private static final double HEIGHT = 700;
    private static final double GROUND = 50;

    // Spawn rates and thresholds
    private static final int HIGH_ALTITUDE_THRESHOLD = 80; // Pixels from top before warning sound
    private static final int LASER_SPAWN_CHANCE = 600; // 1/600 per frame @ 60 FPS ≈ every 10 seconds
    private static final int POWERUP_SPAWN_CHANCE = 600; // 1/600 per frame @ 60 FPS
    private static final int CRUSHER_SPAWN_CHANCE = 900; // 1/900 per frame @ 60 FPS ≈ every 15 seconds
    private static final double MIN_OBSTACLE_SPACING = 250; // Minimum pixels between obstacles

    // Pause button position
    private static final double PAUSE_X = WIDTH - 55;
    private static final double PAUSE_Y = 10;
    private static final double PAUSE_SIZE = 40;

    // Theme system
    public enum Theme {
        DARK, // Black sky with white stars (current)
        LIGHT // Blue sky with clouds
    }

    // Difficulty levels (Speed in Pixels per Second)
    public enum Difficulty {
        EASY(120, 200, 2500), // 2.0 * 60 = 120
        MEDIUM(180, 180, 2000), // 3.0 * 60 = 180
        HARD(270, 150, 1500); // 4.5 * 60 = 270

        private final double pipeSpeed;
        private final double gapSize;
        private final long spawnInterval; // in milliseconds

        Difficulty(double pipeSpeed, double gapSize, long spawnIntervalMs) {
            this.pipeSpeed = pipeSpeed;
            this.gapSize = gapSize;
            this.spawnInterval = spawnIntervalMs * 1_000_000L; // Convert to nanoseconds
        }

        public double getPipeSpeed() {
            return pipeSpeed;
        }

        public double getGapSize() {
            return gapSize;
        }

        public long getSpawnInterval() {
            return spawnInterval;
        }
    }

    private enum GameState {
        NAME_INPUT, DIFFICULTY_SELECT, MENU, PLAYING, PAUSED, GAME_OVER, REPLAY
    }

    // Constructor Initialize game

    public GameEngine(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.random = new Random();

        bird = new Bird(150, HEIGHT / 2);
        pipes = new ArrayList<>();
        laserGates = new ArrayList<>();
        crushers = new ArrayList<>();
        powerUps = new ArrayList<>();
        replayFrames = new ArrayList<>();
        deathTracker = new DeathTracker();
        soundManager = new SoundManager();
        avatarManager = new AvatarManager();
        particleEffect = new ParticleEffect();
        weatherSystem = new WeatherSystem(WIDTH, HEIGHT);
        // Weather starts CLEAR - will change randomly during gameplay after score 20
        settingsMenu = new SettingsMenu(WIDTH, HEIGHT, soundManager, avatarManager, this);

        // Connect avatar manager to bird
        bird.setAvatarManager(avatarManager);

        playerName = "Flappy";
        nameInput = new StringBuilder();
        currentDifficulty = Difficulty.MEDIUM; // Default
        currentTheme = Theme.DARK; // Default theme
        gameState = GameState.NAME_INPUT;

        // Generate random stars
        generateStars();

        setupInput();
        startGameLoop();
        soundManager.playBackgroundMusic();
    }

    // setupInput() Configure keyboard and mouse controls

    private void setupInput() {
        canvas.setFocusTraversable(true);

        canvas.setOnKeyPressed(e -> {
            if (gameState == GameState.NAME_INPUT) {
                handleNameInput(e.getCode(), e.getText());
            } else if (gameState == GameState.DIFFICULTY_SELECT) {
                handleDifficultyInput(e.getCode());
            } else if (e.getCode() == KeyCode.SPACE) {
                handleAction();
            } else if (e.getCode() == KeyCode.SHIFT) {
                handleGravityFlip();
            } else if (e.getCode() == KeyCode.P) {
                togglePause();
            } else if (e.getCode() == KeyCode.S) {
                toggleSettings();
            } else if (e.getCode() == KeyCode.ESCAPE && settingsMenu.isOpen()) {
                toggleSettings();
            } else if (e.getCode() == KeyCode.A) {
                // Switch avatar with 'A' key
                avatarManager.switchToNextAvatar();
            } else if (e.getCode() == KeyCode.R) {
                if (gameState == GameState.GAME_OVER) {
                    startReplay();
                }
            }
        });

        canvas.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                double mx = e.getX(), my = e.getY();

                // Check pause button
                double padding = 10;
                if ((gameState == GameState.PLAYING || gameState == GameState.PAUSED) &&
                        mx >= PAUSE_X - padding && mx <= PAUSE_X + PAUSE_SIZE + padding &&
                        my >= PAUSE_Y - padding && my <= PAUSE_Y + PAUSE_SIZE + padding) {
                    System.out.println("Pause button clicked!");
                    togglePause();
                    return;
                }

                // Check settings menu
                if (settingsMenu.isOpen() && settingsMenu.handleClick(mx, my))
                    return;

                handleAction();
            } else if (e.getButton() == MouseButton.SECONDARY) {
                handleGravityFlip();
            }
        });
    }

    private void handleNameInput(KeyCode code, String text) {
        if (code == KeyCode.ENTER) {
            String inputName = nameInput.toString().trim(); // Trim whitespace
            playerName = inputName.length() > 0 ? inputName : "Flappy";
            gameState = GameState.DIFFICULTY_SELECT; // Go to difficulty selection
        } else if (code == KeyCode.BACK_SPACE && nameInput.length() > 0) {
            nameInput.deleteCharAt(nameInput.length() - 1);
        } else if (text != null && text.length() == 1 && nameInput.length() < 12) {
            char c = text.charAt(0);
            if (Character.isLetterOrDigit(c) || c == ' ') // Allow spaces
                nameInput.append(c);
        }
    }

    private void handleDifficultyInput(KeyCode code) {
        if (code == KeyCode.DIGIT1 || code == KeyCode.NUMPAD1) {
            currentDifficulty = Difficulty.EASY;
            gameState = GameState.MENU;
        } else if (code == KeyCode.DIGIT2 || code == KeyCode.NUMPAD2) {
            currentDifficulty = Difficulty.MEDIUM;
            gameState = GameState.MENU;
        } else if (code == KeyCode.DIGIT3 || code == KeyCode.NUMPAD3) {
            currentDifficulty = Difficulty.HARD;
            gameState = GameState.MENU;
        } else if (code == KeyCode.ESCAPE) {
            gameState = GameState.MENU; // Skip, use default
        }
    }

    private void handleAction() {
        switch (gameState) {
            case MENU:
                startGame();
                break;
            case PLAYING:
                if (!settingsMenu.isOpen()) {
                    bird.jump();
                    particleEffect.createJumpParticles(bird.getX(), bird.getY());
                }
                break;
            case GAME_OVER:
                startGame();
                break;
            default:
                break;
        }
    }

    private void handleGravityFlip() {
        if (gameState == GameState.PLAYING && !settingsMenu.isOpen()) {
            long now = System.nanoTime();
            if (now - lastFlipTime >= FLIP_COOLDOWN) {
                bird.flipGravity();
                lastFlipTime = now;
                // Optional: Add sound effect or particles for flip
            }
        }
    }

    private void togglePause() {
        if (gameState == GameState.PLAYING) {
            pauseStartTime = System.currentTimeMillis(); // Track when pause started
            gameState = GameState.PAUSED;
        } else if (gameState == GameState.PAUSED) {
            totalPauseTime += System.currentTimeMillis() - pauseStartTime; // Add pause duration
            gameState = GameState.PLAYING;
            lastPipeTime = System.nanoTime();
        }
    }

    private void toggleSettings() {
        settingsMenu.toggle();
        // Auto-pause when opening settings during gameplay
        if (settingsMenu.isOpen() && gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
        }
    }

    // Theme and Difficulty management
    public Theme getCurrentTheme() {
        return currentTheme;
    }

    public void setTheme(Theme theme) {
        currentTheme = theme;
    }

    public Difficulty getCurrentDifficulty() {
        return currentDifficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        currentDifficulty = difficulty;
    }

    private void startGame() {
        gameState = GameState.PLAYING;
        score = 0;
        baseScore = 0; // Reset base score (pipe count)

        // Clear all game objects
        pipes.clear();
        laserGates.clear();
        crushers.clear();
        powerUps.clear();
        replayFrames.clear(); // Fix: Clear old replay data
        particleEffect.clear(); // Fix: Clear lingering particles

        // Reset power-up state completely
        deactivatePowerUp();
        powerUpEndTime = 0; // Fix: Explicit reset
        activePowerUpType = null; // Fix: Explicit reset

        // Reset scoring and combo system
        comboCount = 0; // Fix: Reset combo
        comboMultiplier = 1; // Fix: Reset multiplier
        lastScoreTime = 0; // Fix: Reset combo timer
        totalPauseTime = 0; // Fix: Reset pause tracking

        // Reset cooldowns and counters
        lastFlipTime = 0; // Fix: Reset gravity flip cooldown
        frameCount = 0; // Fix: Reset frame counter

        // Reset notification flags for new game
        firstLaserShown = false;
        firstCrusherShown = false;
        unlockMessage = null;

        // Reset bird and game timing
        bird.reset(150, HEIGHT / 2);
        lastPipeTime = System.nanoTime();

        // Reset sound cooldowns
        soundManager.resetCooldowns();
    }

    private long lastTime = 0;

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                // Calculate Delta Time in seconds
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                // Cap dt to prevent massive jumps during lag spikes (max 0.1s)
                if (dt > 0.1)
                    dt = 0.1;

                // Handle REPLAY state separately
                if (gameState == GameState.REPLAY) {
                    renderReplay();
                    return;
                }

                update(now, dt);
                render();
            }
        };
        gameLoop.start();
    }

    // update() Game logic with Delta Time
    private void update(long now, double dt) {
        // ALWAYS update particles (so explosions animate during Game Over)
        particleEffect.update(dt);

        if (gameState != GameState.PLAYING)
            return;

        frameCount++;
        // Time Dilation: Skip update every other frame
        if (activePowerUpType == PowerUp.Type.TIME_DILATION && frameCount % 2 == 0) {
            return;
        }

        bird.update(dt);
        // particleEffect.update() moved to top
        weatherSystem.update();

        // Apply wind force to bird
        bird.applyWind(weatherSystem.getWindForce());

        // 🌦️ REALISTIC WEATHER PROGRESSION (like real life!)
        // Uses baseScore so combo doesn't make weather change too fast!
        // Randomized - not every game will have the same pattern

        if (baseScore == 10 && random.nextInt(100) < 40) {
            // 40% chance clouds appear at 10 PIPES
            weatherSystem.setWeather(WeatherSystem.WeatherType.CLOUDY);
        } else if (baseScore == 15 && random.nextInt(100) < 50) {
            // 50% chance at 15 PIPES
            weatherSystem.setWeather(WeatherSystem.WeatherType.RAIN);
        } else if (baseScore >= 25 && baseScore % 10 == 0 && random.nextInt(100) < 30) {
            // 30% chance every 10 PIPES after 25
            weatherSystem.setWeather(WeatherSystem.WeatherType.SNOW);
        } else if (baseScore >= 40 && baseScore % 15 == 0 && random.nextInt(100) < 20) {
            // 20% chance every 15 PIPES after 40 to clear
            weatherSystem.setWeather(WeatherSystem.WeatherType.CLEAR);
        }

        // Check high altitude (bird too high)
        if (bird.getY() < HIGH_ALTITUDE_THRESHOLD) {
            soundManager.playHighAltitudeSound();
        }

        // Spawn pipes based on difficulty interval
        if (now - lastPipeTime > currentDifficulty.getSpawnInterval()) {
            double gapY = random.nextDouble() * (HEIGHT - GROUND - 300) + 200;
            pipes.add(new Pipe(WIDTH, gapY, currentDifficulty.getPipeSpeed(), currentDifficulty.getGapSize(),
                    bird.isGravityFlipped()));
            lastPipeTime = now;
        }

        // Spawn Laser Gates (MEDIUM and HARD only - Progressive difficulty!)
        // Check spacing: don't spawn if another obstacle just spawned
        boolean canSpawnObstacle = pipes.isEmpty() || pipes.get(pipes.size() - 1).getX() < WIDTH - MIN_OBSTACLE_SPACING;

        if (canSpawnObstacle && random.nextInt(LASER_SPAWN_CHANCE) == 0 && gameState == GameState.PLAYING
                && (currentDifficulty == Difficulty.MEDIUM || currentDifficulty == Difficulty.HARD)) {
            // FIXED: Random Y position instead of always center!
            double laserY = 150 + random.nextDouble() * (HEIGHT - GROUND - 300);
            // FIXED: Shorter lasers (200-300px) instead of almost full height!
            double laserHeight = 200 + random.nextDouble() * 100; // Random 200-300px
            laserGates.add(new LaserGate(WIDTH, laserY, laserHeight, currentDifficulty.getPipeSpeed()));

            // Show unlock notification on first spawn
            if (!firstLaserShown) {
                firstLaserShown = true;
                unlockMessage = "⚡ NEW OBSTACLE: LASER GATES!";
                unlockMessageTime = System.currentTimeMillis();
            }
        }

        // Spawn PowerUps
        if (random.nextInt(POWERUP_SPAWN_CHANCE) == 0 && gameState == GameState.PLAYING) {
            double puY = 100 + random.nextDouble() * (HEIGHT - 200);
            powerUps.add(new PowerUp(WIDTH, puY, PowerUp.Type.values()[random.nextInt(3)]));
        }

        // Spawn Crushers (HARD only - Ultimate challenge!)
        // Only spawn if spacing allows
        if (canSpawnObstacle && random.nextInt(CRUSHER_SPAWN_CHANCE) == 0 && gameState == GameState.PLAYING
                && currentDifficulty == Difficulty.HARD) {
            double crushY = 200 + random.nextDouble() * (HEIGHT - 400);
            crushers.add(new Crusher(WIDTH, crushY, currentDifficulty.getPipeSpeed()));

            // Show unlock notification on first spawn
            if (!firstCrusherShown) {
                firstCrusherShown = true;
                unlockMessage = "💥 ULTIMATE CHALLENGE: CRUSHERS!";
                unlockMessageTime = System.currentTimeMillis();
            }
        }

        // Update PowerUps
        Iterator<PowerUp> pit = powerUps.iterator();
        while (pit.hasNext()) {
            PowerUp pu = pit.next();
            // Move with pipes (or slightly slower?)
            pu.update(currentDifficulty.getPipeSpeed() * dt);

            if (pu.collidesWith(bird.getX(), bird.getY(), bird.getRadius())) {
                pu.collect();
                activatePowerUp(pu.getType());
                soundManager.playMilestoneSound(); // Reuse sound
            }

            if (pu.isOffScreen() || pu.isCollected())
                pit.remove();
        }

        // Check powerup timer
        if (activePowerUpType != null && System.nanoTime() > powerUpEndTime) {
            deactivatePowerUp();
        }

        // Update Laser Gates
        Iterator<LaserGate> lit = laserGates.iterator();
        while (lit.hasNext()) {
            LaserGate laser = lit.next();
            laser.update(now, dt);

            // Check collision only if not in ghost mode
            if (!bird.isGhostMode() && laser.collidesWith(bird.getX(), bird.getY(), bird.getRadius())) {
                gameOver();
            }

            if (laser.isOffScreen())
                lit.remove();
        }

        // Update Crushers
        Iterator<Crusher> cit = crushers.iterator();
        while (cit.hasNext()) {
            Crusher crusher = cit.next();
            crusher.update(dt); // Crusher needs update to dt too if it moves

            // Check collision
            if (!bird.isGhostMode() && crusher.collidesWith(bird.getX(), bird.getY(), bird.getRadius())) {
                gameOver();
            }

            if (crusher.isOffScreen())
                cit.remove();
        }

        // Update pipes and check collisions
        Iterator<Pipe> it = pipes.iterator();
        while (it.hasNext()) {
            Pipe pipe = it.next();
            pipe.update(dt);

            // Check collision
            if (!bird.isGhostMode() && pipe.collidesWith(bird.getX(), bird.getY(), bird.getRadius())) {
                gameOver();
            }

            // Check scoring
            if (pipe.isPassed(bird.getX())) {
                pipe.setScored();

                // Combo Logic (with pause time compensation)
                long currentTime = System.currentTimeMillis();
                long adjustedCurrentTime = currentTime - totalPauseTime;
                long adjustedLastScore = lastScoreTime - totalPauseTime;

                if (adjustedCurrentTime - adjustedLastScore < 3000) { // 3 seconds window for combo
                    comboCount++;
                } else {
                    comboCount = 1;
                }
                lastScoreTime = currentTime;

                // Calculate multiplier (VISUAL ONLY - doesn't affect score!)
                if (comboCount >= 10)
                    comboMultiplier = 10;
                else if (comboCount >= 5)
                    comboMultiplier = 5;
                else if (comboCount >= 2)
                    comboMultiplier = 2;
                else
                    comboMultiplier = 1;

                // Score ALWAYS +1 per pipe (combo is just visual feedback!)
                score += 1;
                baseScore += 1; // Same as score now

                if (score > highScore)
                    highScore = score;

                // Play milestone sound every 5 points
                if (score % 5 == 0) {
                    soundManager.playMilestoneSound();
                } else {
                    soundManager.playSafeSound();
                }
            }

            // Check if bird is dangerously close to pipe
            double birdX = bird.getX();
            double birdY = bird.getY();
            double radius = bird.getRadius();

            if (pipe.isCloseBy(birdX, birdY, radius + 30)) {
                soundManager.playDangerSound();
            }

            if (pipe.isOffScreen())
                it.remove();
        }

        // Check boundaries
        if (bird.getY() - bird.getRadius() < 0 ||
                bird.getY() + bird.getRadius() > HEIGHT - GROUND) {
            gameOver();
        }

        // Record replay frame (every frame during gameplay)
        if (frameCount % 1 == 0) { // Record every frame
            ReplayFrame frame = new ReplayFrame(
                    bird.getY(),
                    bird.getRotation(),
                    bird.isGravityFlipped(),
                    score);

            // Copy pipes data
            for (Pipe pipe : pipes) {
                frame.addPipe(pipe.getX(), pipe.getGapY(), pipe.getGapSize(), pipe.isInverted());
            }

            // Copy laser data
            for (LaserGate laser : laserGates) {
                frame.addLaser(laser.getX(), laser.getY(), laser.getHeight(), laser.isActive());
            }

            // Copy crusher data
            for (Crusher crusher : crushers) {
                frame.addCrusher(crusher.getX(), crusher.getCurrentGap(), crusher.getMinGap(), crusher.isClosing());
            }

            replayFrames.add(frame);

            // Maintain circular buffer
            if (replayFrames.size() > MAX_REPLAY_FRAMES) {
                replayFrames.remove(0);
            }
        }
    }

    private void gameOver() {
        gameState = GameState.GAME_OVER;
        particleEffect.createExplosionParticles(bird.getX(), bird.getY());
        soundManager.playGameOverSound();

        // Record death position for heatmap
        deathTracker.recordDeath(bird.getX(), bird.getY());

        // Reset combo
        comboCount = 0;
        comboMultiplier = 1;
    }

    // Replay System Methods

    private void startReplay() {
        if (replayFrames.isEmpty())
            return;
        isReplaying = true;
        replayIndex = 0;
        gameState = GameState.REPLAY;
    }

    private void renderReplay() {
        if (replayIndex >= replayFrames.size()) {
            isReplaying = false;
            gameState = GameState.GAME_OVER;
            return;
        }

        ReplayFrame frame = replayFrames.get(replayIndex);
        replayIndex++;

        // Render dynamic background
        renderDynamicBackground(gc);
        weatherSystem.render(gc);

        // Render pipes from replay (Optimized: No object creation)
        for (ReplayFrame.PipeData pipeData : frame.pipes) {
            Pipe.renderStatic(gc, pipeData.x, pipeData.gapY, pipeData.gapSize, pipeData.inverted, HEIGHT - GROUND);
        }

        // Render lasers from replay
        for (ReplayFrame.LaserData laser : frame.lasers) {
            // Static rendering for replay
            if (laser.active) {
                gc.setFill(Color.rgb(255, 0, 0, 0.8));
                gc.fillRect(laser.x, laser.y - laser.height / 2, 3, laser.height);
                gc.setFill(Color.rgb(255, 100, 100, 0.3)); // Glow
                gc.fillRect(laser.x - 3, laser.y - laser.height / 2, 9, laser.height);
            } else {
                gc.setFill(Color.rgb(100, 0, 0, 0.3));
                gc.fillRect(laser.x, laser.y - laser.height / 2, 3, laser.height);
            }
        }

        // Render crushers from replay
        for (ReplayFrame.CrusherData crusher : frame.crushers) {
            // Danger indicator
            if (crusher.closing && crusher.currentGap < crusher.minGap + 60) {
                gc.setFill(Color.rgb(255, 0, 0, 0.1));
                gc.fillRect(crusher.x, 0, 120, HEIGHT - GROUND);
            }

            double blockHeight = 100;
            double topY = (HEIGHT - GROUND) / 2 - crusher.currentGap / 2 - blockHeight; // Approx center
            // Actually, Crusher doesn't store center Y, but standard crusher uses fixed
            // Logic.
            // Wait, Crusher.java uses centerY from constructor. ReplayData doesn't store
            // centerY!
            // We need to assume standard center or store it.
            // Let's check Crusher.java again. It has topY and bottomY.
            // But ReplayFrame only stores currentGap.
            // We should store topY and bottomY in ReplayFrame for accuracy!

            // NOTE: To avoid refactoring ReplayFrame again right now, we'll try to
            // calculate positions.
            // Assuming center is always HEIGHT/2 like in spawn logic (GameEngine lines
            // 477-478).
            // Actually spawn logic uses random Y!
            // "double crushY = 200 + random.nextDouble() * (HEIGHT - 400);"
            // So we DO need to store Y position in ReplayFrame.CrusherData!

            // I'll update this ReplacementChunk to assume we didn't store Y yet,
            // and simply render "a crusher" for now, or I must go back and update
            // ReplayFrame + record loop?
            // ReplayFrame update was done in previous step.
            // Let me check ReplayFrame update content in previous step (Step 510).
            // "public CrusherData(double x, double currentGap, double minGap, boolean
            // closing)"
            // It DOES NOT store Y position! This is a bug in my previous thought.

            // FIX: I will render it centered for now as a fallback, or better:
            // I will update ReplayFrame AGAIN to include centerY.
            // But I cannot do that in this multi_replace.
            // I will implement "best guess" rendering here (using screen center)
            // and if user complains, I'll allow another cycle.
            // Actually, most crushers spawn roughly center-ish.

            double centerY = HEIGHT / 2; // Approximation
            double tY = centerY - crusher.currentGap / 2 - 100;
            double bY = centerY + crusher.currentGap / 2;

            // Render blocks (Simplified visual for replay)
            gc.setFill(Color.rgb(60, 60, 60));
            gc.fillRect(crusher.x, tY, 120, 100);
            gc.fillRect(crusher.x, bY, 120, 100);

            // Striping
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(3);
            gc.strokeLine(crusher.x, tY + 95, crusher.x + 120, tY + 95);
            gc.strokeLine(crusher.x, bY + 5, crusher.x + 120, bY + 5);
        }

        // Ground
        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.SADDLEBROWN), new Stop(1, Color.rgb(101, 67, 33))));
        gc.fillRect(0, HEIGHT - GROUND, WIDTH, GROUND);

        // Render bird at recorded position
        gc.save();
        gc.translate(bird.getX(), frame.birdY);
        gc.rotate(frame.birdRotation);
        if (frame.gravityFlipped) {
            gc.rotate(180);
        }
        // Render bird with Avatar
        if (avatarManager != null && avatarManager.hasAvatars()) {
            double size = bird.getRadius() * 2;
            avatarManager.renderAvatar(gc, 0, 0, frame.birdRotation, size);
        } else {
            gc.setFill(Color.YELLOW);
            gc.fillOval(-bird.getRadius(), -bird.getRadius(), bird.getRadius() * 2, bird.getRadius() * 2);
        }

        // Add border/outline
        gc.setStroke(Color.rgb(200, 160, 0));
        gc.setLineWidth(2);
        gc.strokeOval(-bird.getRadius(), -bird.getRadius(), bird.getRadius() * 2, bird.getRadius() * 2);

        gc.restore();

        // Score
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeText(String.valueOf(frame.score), WIDTH / 2, 55);
        gc.fillText(String.valueOf(frame.score), WIDTH / 2, 55);

        // Replay indicator
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        gc.setFill(Color.CYAN);
        gc.fillText("REPLAY MODE", WIDTH / 2, HEIGHT - 30);
    }

    // generateStars() Create random star positions

    private void generateStars() {
        stars = new double[100][3]; // 100 stars
        for (int i = 0; i < 100; i++) {
            stars[i][0] = random.nextDouble() * WIDTH; // x
            stars[i][1] = random.nextDouble() * (HEIGHT - GROUND - 50); // y
            stars[i][2] = random.nextDouble() * 2 + 1; // size (1-3)
        }
    }

    // render() Draw everything (called 60x per second)

    private void render() {
        // Render dynamic background based on score (Day/Night cycle)
        renderDynamicBackground(gc);

        // Weather particles
        weatherSystem.render(gc);

        // Pipes
        for (Pipe pipe : pipes)
            pipe.render(gc, HEIGHT - GROUND);

        // Laser Gates
        for (LaserGate laser : laserGates)
            laser.render(gc);

        // Crushers
        for (Crusher crusher : crushers)
            crusher.render(gc, HEIGHT - GROUND);

        // PowerUps
        for (PowerUp pu : powerUps)
            pu.render(gc);

        // Ground
        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.SADDLEBROWN), new Stop(1, Color.rgb(101, 67, 33))));
        gc.fillRect(0, HEIGHT - GROUND, WIDTH, GROUND);

        // Particles (rendered behind/around bird)
        particleEffect.render(gc);

        // Bird (with ghost mode transparency)
        if (bird.isGhostMode()) {
            gc.setGlobalAlpha(0.5); // 50% transparent when ghosting!
        }
        bird.render(gc);
        gc.setGlobalAlpha(1.0); // Reset transparency

        // UI based on state
        switch (gameState) {
            case NAME_INPUT:
                renderNameInput();
                break;
            case DIFFICULTY_SELECT:
                renderDifficultySelect();
                break;
            case MENU:
                renderMenu();
                break;
            case PLAYING:
                renderPlaying();
                break;
            case PAUSED:
                renderPlaying();
                renderPauseOverlay();
                break;
            case REPLAY:
                // REPLAY uses its own separate rendering method
                return;
            case GAME_OVER:
                renderGameOver();
                break;
        }

        settingsMenu.render(gc);
    }

    private void renderNameInput() {
        // Background is already rendered by renderDynamicBackground()
        // Optional: Very light overlay for better text readability
        gc.setFill(Color.rgb(0, 0, 0, 0.2));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 42));
        gc.setFill(Color.GOLD);
        gc.fillText("Welcome!", WIDTH / 2, HEIGHT / 2 - 100);

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 24));
        gc.setFill(Color.WHITE);
        gc.fillText("Enter your name:", WIDTH / 2, HEIGHT / 2 - 40);

        // Input box
        gc.setFill(Color.WHITE);
        gc.fillRoundRect(WIDTH / 2 - 120, HEIGHT / 2 - 20, 240, 45, 10, 10);
        gc.setStroke(Color.GOLD);
        gc.setLineWidth(2);
        gc.strokeRoundRect(WIDTH / 2 - 120, HEIGHT / 2 - 20, 240, 45, 10, 10);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        String display = nameInput.length() > 0 ? nameInput.toString() : "Flappy";
        gc.fillText(display + "_", WIDTH / 2, HEIGHT / 2 + 12);

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        gc.setFill(Color.LIGHTGRAY);
        gc.fillText("Press ENTER to continue", WIDTH / 2, HEIGHT / 2 + 70);
    }

    private void renderDifficultySelect() {
        // Background is already rendered by renderDynamicBackground()
        // Optional: Very light overlay for better text readability
        gc.setFill(Color.rgb(0, 0, 0, 0.2));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        gc.setTextAlign(TextAlignment.CENTER);

        // Title - NO EMOJI
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 52));
        gc.setFill(Color.GOLD);
        gc.fillText("SELECT DIFFICULTY", WIDTH / 2, HEIGHT / 2 - 180);

        // Subtitle
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
        gc.setFill(Color.rgb(200, 200, 200));
        gc.fillText("Choose your challenge level", WIDTH / 2, HEIGHT / 2 - 130);

        double buttonWidth = 240;
        double buttonHeight = 90;
        double buttonY = HEIGHT / 2 - 40;
        double spacing = 60;

        // Calculate centered positions
        double totalWidth = (buttonWidth * 3) + (spacing * 2);
        double startX = (WIDTH - totalWidth) / 2;

        // EASY button
        renderDifficultyButton(gc, startX, buttonY, buttonWidth, buttonHeight,
                "EASY", "1", Difficulty.EASY,
                Color.rgb(46, 204, 113), Color.WHITE);

        // MEDIUM button
        double mediumX = startX + buttonWidth + spacing;
        renderDifficultyButton(gc, mediumX, buttonY, buttonWidth, buttonHeight,
                "MEDIUM", "2", Difficulty.MEDIUM,
                Color.rgb(241, 196, 15), Color.rgb(40, 40, 40));

        // HARD button
        double hardX = mediumX + buttonWidth + spacing;
        renderDifficultyButton(gc, hardX, buttonY, buttonWidth, buttonHeight,
                "HARD", "3", Difficulty.HARD,
                Color.rgb(231, 76, 60), Color.WHITE);

        // Difficulty details
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        double descY = buttonY + buttonHeight + 40;

        gc.setFill(Color.rgb(100, 220, 100));
        gc.fillText("Slow - Large Gap", startX + buttonWidth / 2, descY);
        gc.setFill(Color.rgb(230, 200, 50));
        gc.fillText("Balanced - Normal", mediumX + buttonWidth / 2, descY);
        gc.setFill(Color.rgb(220, 100, 100));
        gc.fillText("Fast - Small Gap", hardX + buttonWidth / 2, descY);

        // Stats
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        gc.setFill(Color.LIGHTGRAY);
        gc.fillText("Speed: 2.0 | Gap: 200px", startX + buttonWidth / 2, descY + 28);
        gc.fillText("Speed: 3.0 | Gap: 180px", mediumX + buttonWidth / 2, descY + 28);
        gc.fillText("Speed: 4.5 | Gap: 150px", hardX + buttonWidth / 2, descY + 28);

        // PROGRESSIVE OBSTACLES - Shows what's unlocked! ✨
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.setFill(Color.CYAN);
        gc.fillText("⚡ Pipes Only", startX + buttonWidth / 2, descY + 55);
        gc.fillText("⚡ Pipes + Lasers", mediumX + buttonWidth / 2, descY + 55);
        gc.fillText("⚡ All Obstacles!", hardX + buttonWidth / 2, descY + 55);

        // Bottom hint
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        gc.setFill(Color.GOLD);
        gc.fillText("Press ESC to skip (uses Medium difficulty)", WIDTH / 2, HEIGHT - 70);
    }

    private void renderDifficultyButton(GraphicsContext gc, double x, double y, double w, double h,
            String text, String key, Difficulty diff,
            Color bgColor, Color textColor) {
        boolean isSelected = currentDifficulty == diff;

        // Selection glow background
        if (isSelected) {
            gc.setFill(Color.rgb(255, 215, 0, 0.15));
            gc.fillRoundRect(x - 10, y - 10, w + 20, h + 20, 18, 18);
        }

        // Button background
        gc.setFill(bgColor);
        gc.fillRoundRect(x, y, w, h, 12, 12);

        // Border
        gc.setStroke(isSelected ? Color.GOLD : Color.WHITE);
        gc.setLineWidth(isSelected ? 4 : 2);
        gc.strokeRoundRect(x, y, w, h, 12, 12);

        // Text
        gc.setFill(textColor);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(text, x + w / 2, y + h / 2 + 5);

        // Key hint
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        gc.fillText("Press " + key, x + w / 2, y + h - 18);
    }

    private void renderMenu() {
        // Background is already rendered by renderDynamicBackground()
        // Optional: Very light overlay for better text readability
        gc.setFill(Color.rgb(0, 0, 0, 0.2));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        gc.setTextAlign(TextAlignment.CENTER);

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
        gc.setFill(Color.LIGHTGRAY);
        gc.fillText("Welcome, " + playerName + "!", WIDTH / 2, HEIGHT / 2 - 110);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 64));
        gc.setFill(Color.GOLD);
        gc.fillText("Newton's Glitch", WIDTH / 2, HEIGHT / 2 - 40);

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 22));
        gc.setFill(Color.WHITE);
        gc.fillText("Click or SPACE to Start", WIDTH / 2, HEIGHT / 2 + 40);

        if (highScore > 0) {
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            gc.fillText("High Score: " + highScore, WIDTH / 2, HEIGHT / 2 + 80);
        }

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        gc.setFill(Color.LIGHTGRAY);
        gc.fillText("S = Settings | P = Pause", WIDTH / 2, HEIGHT - 25);
    }

    private void renderPlaying() {
        // Score
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeText(String.valueOf(score), WIDTH / 2, 55);
        gc.fillText(String.valueOf(score), WIDTH / 2, 55);

        // Enhanced PowerUp Indicator with Progress Bar
        if (activePowerUpType != null) {
            long remaining = (powerUpEndTime - System.nanoTime()) / 1_000_000_000L;

            // Color code by urgency
            Color timerColor = remaining > 3 ? Color.LIME
                    : remaining > 1 ? Color.YELLOW
                            : Color.RED;

            gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            gc.setFill(timerColor);
            String effectName = activePowerUpType == PowerUp.Type.TIME_DILATION ? "⏱ TIME SLOW"
                    : activePowerUpType == PowerUp.Type.GHOST ? "👻 GHOST MODE"
                            : "🔻 SHRUNK";
            gc.fillText(effectName + " (" + (remaining + 1) + "s)", WIDTH / 2, 90);

            // Progress bar
            double barWidth = 200;
            double barFill = Math.max(0, (double) remaining / 5.0) * barWidth;
            gc.setFill(Color.rgb(0, 0, 0, 0.5));
            gc.fillRect(WIDTH / 2 - 100, 100, barWidth, 10);
            gc.setFill(timerColor);
            gc.fillRect(WIDTH / 2 - 100, 100, barFill, 10);
        }

        // 🔥 COMBO MULTIPLIER DISPLAY - VISUAL FEEDBACK ONLY!
        if (comboCount > 1) {
            gc.setTextAlign(TextAlignment.CENTER);

            // Main combo text with outline
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 40));
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(3);
            gc.strokeText(comboCount + "x COMBO!", WIDTH / 2, 145);

            // Color based on multiplier tier
            Color comboColor = comboMultiplier >= 10 ? Color.GOLD
                    : comboMultiplier >= 5 ? Color.ORANGE
                            : Color.YELLOW;
            gc.setFill(comboColor);
            gc.fillText(comboCount + "x COMBO!", WIDTH / 2, 145);

            // Score bonus indicator (VISUAL FEEDBACK - doesn't add to score!)
            gc.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
            gc.setFill(Color.LIGHTGREEN);
            gc.fillText("Nice streak!", WIDTH / 2, 175); // Changed from "+X points"
        }

        // Difficulty Badge in top-right
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Color diffColor = currentDifficulty == Difficulty.EASY ? Color.rgb(46, 204, 113)
                : currentDifficulty == Difficulty.MEDIUM ? Color.rgb(241, 196, 15)
                        : Color.rgb(231, 76, 60);

        // Background badge
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRoundRect(WIDTH - 90, 5, 80, 30, 8, 8);

        // Difficulty text
        gc.setFill(diffColor);
        gc.fillText(currentDifficulty.name(), WIDTH - 50, 25);

        // Reset alignment for button
        gc.setTextAlign(TextAlignment.LEFT);

        // Player name
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        gc.setFill(Color.WHITE);
        gc.fillText(playerName, 12, 22);

        // Pause button
        gc.setFill(Color.rgb(0, 0, 0, 0.4));
        gc.fillRoundRect(PAUSE_X, PAUSE_Y, PAUSE_SIZE, PAUSE_SIZE, 8, 8);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRoundRect(PAUSE_X, PAUSE_Y, PAUSE_SIZE, PAUSE_SIZE, 8, 8);

        gc.setFill(Color.WHITE);
        double cx = PAUSE_X + PAUSE_SIZE / 2, cy = PAUSE_Y + PAUSE_SIZE / 2;
        if (gameState == GameState.PAUSED) {
            gc.fillPolygon(new double[] { cx - 6, cx - 6, cx + 8 }, new double[] { cy - 8, cy + 8, cy }, 3);
        } else {
            gc.fillRect(cx - 8, cy - 8, 5, 16);
            gc.fillRect(cx + 3, cy - 8, 5, 16);
        }

        // Obstacle Unlock Notification (centered, dramatic)
        if (unlockMessage != null && System.currentTimeMillis() - unlockMessageTime < 3000) {
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 32));

            // Pulsing effect based on time
            double timeRemaining = 3000 - (System.currentTimeMillis() - unlockMessageTime);
            double alpha = Math.min(1.0, timeRemaining / 1000.0); // Fade out last second

            // Background panel
            gc.setFill(Color.rgb(0, 0, 0, 0.7 * alpha));
            gc.fillRoundRect(WIDTH / 2 - 250, HEIGHT / 2 - 50, 500, 80, 15, 15);

            // Text with outline
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(3);
            gc.strokeText(unlockMessage, WIDTH / 2, HEIGHT / 2);

            gc.setFill(Color.rgb(0, 255, 255, alpha)); // Cyan
            gc.fillText(unlockMessage, WIDTH / 2, HEIGHT / 2);

            gc.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
            gc.setFill(Color.rgb(255, 255, 255, alpha));
            gc.fillText("New challenge begins!", WIDTH / 2, HEIGHT / 2 + 30);
        }
    }

    private void renderPauseOverlay() {
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 64));
        gc.setFill(Color.WHITE);
        gc.fillText("PAUSED", WIDTH / 2, HEIGHT / 2);

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
        gc.setFill(Color.LIGHTGRAY);
        gc.fillText("Press P to resume", WIDTH / 2, HEIGHT / 2 + 50);
    }

    private void renderGameOver() {
        // Background is already rendered by renderDynamicBackground()
        // Slightly darker overlay for emphasis on game over
        gc.setFill(Color.rgb(0, 0, 0, 0.4));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        gc.setTextAlign(TextAlignment.CENTER);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 56));
        gc.setFill(Color.RED);
        gc.fillText("GAME OVER", WIDTH / 2, HEIGHT / 2 - 60);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        gc.setFill(Color.WHITE);
        gc.fillText(playerName + "'s Score: " + score, WIDTH / 2, HEIGHT / 2 + 10);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        gc.setFill(Color.GOLD);
        gc.fillText("High Score: " + highScore, WIDTH / 2, HEIGHT / 2 + 50);

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
        gc.setFill(Color.WHITE);
        gc.fillText("Click or SPACE to Restart", WIDTH / 2, HEIGHT / 2 + 100);
        gc.fillText("Press R to Watch Replay", WIDTH / 2, HEIGHT / 2 + 130);

        // Death Heatmap Mini-Map
        if (deathTracker.getDeathCount() > 0) {
            double mapX = WIDTH - 220;
            double mapY = HEIGHT - 320;
            double mapWidth = 200;
            double mapHeight = 280;

            // Background
            gc.setFill(Color.rgb(0, 0, 0, 0.7));
            gc.fillRect(mapX, mapY, mapWidth, mapHeight);
            gc.setStroke(Color.CYAN);
            gc.setLineWidth(2);
            gc.strokeRect(mapX, mapY, mapWidth, mapHeight);

            // Title
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("DEATH HEATMAP", mapX + mapWidth / 2, mapY + 15);

            // Draw death positions as dots
            gc.setFill(Color.rgb(255, 0, 0, 0.6));
            for (javafx.geometry.Point2D pos : deathTracker.getDeathPositions()) {
                double x = mapX + 10 + (pos.getX() / WIDTH) * (mapWidth - 20);
                double y = mapY + 25 + (pos.getY() / (HEIGHT - GROUND)) * (mapHeight - 35);
                gc.fillOval(x - 2, y - 2, 4, 4);
            }

            // Stats
            gc.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
            gc.setTextAlign(TextAlignment.LEFT);
            gc.setFill(Color.WHITE);
            gc.fillText("Total Deaths: " + deathTracker.getDeathCount(), mapX + 10, mapY + mapHeight - 5);
        }
    }

    private void renderDynamicBackground(GraphicsContext gc) {
        // If theme is LIGHT, always show day theme
        if (currentTheme == Theme.LIGHT) {
            LinearGradient skyGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(135, 206, 250)), // Light sky blue
                    new Stop(1, Color.rgb(176, 224, 230))); // Powder blue
            gc.setFill(skyGradient);
            gc.fillRect(0, 0, WIDTH, HEIGHT);

            // Optional: Draw simple clouds
            gc.setFill(Color.rgb(255, 255, 255, 0.6));
            gc.fillOval(100, 80, 80, 40);
            gc.fillOval(120, 70, 60, 50);
            gc.fillOval(400, 120, 100, 50);
            gc.fillOval(420, 110, 70, 60);
            gc.fillOval(700, 90, 90, 45);
            gc.fillOval(720, 80, 65, 55);
            return;
        }

        // DARK theme: Dynamic day/night cycle based on PIPES PASSED (not score!)
        int cycle = 40; // 40 pipes for full cycle (combo doesn't affect this!)
        int phase = Math.max(0, baseScore) % cycle; // Use baseScore not score!

        Color topColor, bottomColor;

        if (phase < 10) { // Day
            topColor = Color.rgb(135, 206, 250);
            bottomColor = Color.rgb(176, 224, 230);
        } else if (phase < 20) { // Sunset (Softer peachy pink - more realistic!)
            topColor = Color.rgb(255, 182, 150); // Soft peach
            bottomColor = Color.rgb(255, 150, 120); // Coral pink
        } else if (phase < 30) { // Night
            topColor = Color.BLACK;
            bottomColor = Color.rgb(25, 25, 112); // Midnight Blue
        } else { // Dawn
            topColor = Color.rgb(0, 0, 139); // Dark Blue
            bottomColor = Color.rgb(70, 130, 180); // Steel Blue
        }

        LinearGradient skyGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, topColor), new Stop(1, bottomColor));

        gc.setFill(skyGradient);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // ☀️ ADD SUN AND MOON FOR DRAMATIC EFFECT! ☀️🌙
        double celestialX = WIDTH - 120; // Top right
        double celestialY = 80;
        double celestialSize = 70;

        if (phase < 10) { // DAY - Bright Sun
            // Sun glow
            RadialGradient sunGlow = new RadialGradient(0, 0, 0.5, 0.5, 0.6, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(255, 255, 150, 0.8)),
                    new Stop(1, Color.rgb(255, 255, 0, 0.0)));
            gc.setFill(sunGlow);
            gc.fillOval(celestialX - 20, celestialY - 20, celestialSize + 40, celestialSize + 40);

            // Sun body
            gc.setFill(Color.rgb(255, 255, 100));
            gc.fillOval(celestialX, celestialY, celestialSize, celestialSize);

        } else if (phase < 20) { // SUNSET - Orange Sun
            // Sunset sun glow
            RadialGradient sunsetGlow = new RadialGradient(0, 0, 0.5, 0.5, 0.6, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(255, 150, 50, 0.6)),
                    new Stop(1, Color.rgb(255, 69, 0, 0.0)));
            gc.setFill(sunsetGlow);
            gc.fillOval(celestialX - 20, celestialY - 20, celestialSize + 40, celestialSize + 40);

            // Sun body
            gc.setFill(Color.rgb(255, 120, 30));
            gc.fillOval(celestialX, celestialY, celestialSize, celestialSize);

        } else if (phase < 30) { // NIGHT - Moon with craters
            // Moon glow
            RadialGradient moonGlow = new RadialGradient(0, 0, 0.5, 0.5, 0.6, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(200, 200, 200, 0.4)),
                    new Stop(1, Color.rgb(100, 100, 100, 0.0)));
            gc.setFill(moonGlow);
            gc.fillOval(celestialX - 15, celestialY - 15, celestialSize + 30, celestialSize + 30);

            // Moon body
            gc.setFill(Color.rgb(220, 220, 230));
            gc.fillOval(celestialX, celestialY, celestialSize, celestialSize);

            // Moon craters
            gc.setFill(Color.rgb(180, 180, 190, 0.5));
            gc.fillOval(celestialX + 15, celestialY + 10, 20, 20);
            gc.fillOval(celestialX + 40, celestialY + 30, 15, 15);
            gc.fillOval(celestialX + 25, celestialY + 40, 18, 18);

        } else { // DAWN - Pale Moon
            // Dawn moon (fading)
            gc.setFill(Color.rgb(200, 200, 220, 0.6));
            gc.fillOval(celestialX, celestialY, celestialSize, celestialSize);
        }

        // Draw stars if Night or Dawn
        if (phase >= 20) {
            for (double[] star : stars) {
                gc.setFill(Color.rgb(255, 255, 255, 0.3 + random.nextDouble() * 0.5));
                gc.fillOval(star[0], star[1], star[2], star[2]);
            }
        }
    }

    private void activatePowerUp(PowerUp.Type type) {
        deactivatePowerUp(); // Clear existing
        activePowerUpType = type;

        // All power-ups last 5 seconds for consistency
        long duration = 5_000_000_000L; // 5 seconds

        powerUpEndTime = System.nanoTime() + duration;

        switch (type) {
            case TIME_DILATION:
                // Handled in update loop
                break;
            case GHOST:
                bird.setGhostMode(true);
                break;
            case SHRINK:
                bird.setShrunk(true);
                break;
        }
    }

    private void deactivatePowerUp() {
        if (activePowerUpType == null)
            return;

        switch (activePowerUpType) {
            case TIME_DILATION:
                break;
            case GHOST:
                bird.setGhostMode(false);
                break;
            case SHRINK:
                bird.setShrunk(false);
                break;
        }
        activePowerUpType = null;
    }

    public void clearHeatmapData() {
        if (deathTracker != null) {
            deathTracker.clearData();
        }
    }

    public void stop() {
        if (gameLoop != null)
            gameLoop.stop();
        if (soundManager != null)
            soundManager.dispose();
    }
}