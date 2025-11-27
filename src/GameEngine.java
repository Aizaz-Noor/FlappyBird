import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Main game engine with advanced sound system and avatar support
 */
public class GameEngine {
    private Canvas canvas;
    private GraphicsContext gc;
    private AnimationTimer gameLoop;

    private Bird bird;
    private List<Pipe> pipes;
    private ParticleEffect particleEffect;
    private Random random;

    // Managers
    private SoundManager soundManager;
    private AvatarManager avatarManager;
    private SettingsMenu settingsMenu;

    private GameState gameState;
    private int score;
    private int highScore;

    // Sound trigger tracking
    private boolean wasInDanger;
    private boolean wasTooHigh;
    private int lastMilestoneScore;

    private long lastPipeTime;
    private static final long PIPE_SPAWN_INTERVAL = 2_000_000_000L;

    // Game zones and thresholds
    private static final double DANGER_PROXIMITY = 80;
    private static final double SAFE_PROXIMITY = 150;
    private static final double HIGH_ALTITUDE_THRESHOLD = 100;
    private static final int MILESTONE_INTERVAL = 5;

    private static final double CANVAS_WIDTH = 800;
    private static final double CANVAS_HEIGHT = 600;
    private static final double GROUND_HEIGHT = 50;

    private enum GameState {
        MENU, PLAYING, GAME_OVER
    }

    public GameEngine(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.random = new Random();

        bird = new Bird(150, CANVAS_HEIGHT / 2);
        pipes = new ArrayList<>();
        particleEffect = new ParticleEffect();

        soundManager = new SoundManager();
        avatarManager = new AvatarManager();
        settingsMenu = new SettingsMenu(CANVAS_WIDTH, CANVAS_HEIGHT, avatarManager, soundManager);

        gameState = GameState.MENU;
        score = 0;
        highScore = 0;
        lastPipeTime = 0;
        wasInDanger = false;
        wasTooHigh = false;
        lastMilestoneScore = 0;

        setupInput();
        startGameLoop();
        soundManager.playBackgroundMusic();
    }

    private void setupInput() {
        canvas.setFocusTraversable(true);

        canvas.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                handleJump();
            } else if (event.getCode() == KeyCode.DIGIT1) {
                soundManager.switchDangerSound();
            } else if (event.getCode() == KeyCode.DIGIT2) {
                soundManager.switchSafeSound();
            } else if (event.getCode() == KeyCode.DIGIT3) {
                soundManager.switchGameOverSound();
            } else if (event.getCode() == KeyCode.A) {
                avatarManager.switchToNextAvatar();
            } else if (event.getCode() == KeyCode.S) {
                settingsMenu.toggle();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                if (settingsMenu.isOpen()) {
                    settingsMenu.close();
                }
            }
        });

        canvas.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                if (settingsMenu.isOpen()) {
                    if (settingsMenu.handleClick(event.getX(), event.getY())) {
                        return;
                    }
                }
                handleJump();
            }
        });
    }

    private void handleJump() {
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
                restartGame();
                break;
        }
    }

    private void startGame() {
        gameState = GameState.PLAYING;
        score = 0;
        pipes.clear();
        particleEffect.clear();
        bird.reset(150, CANVAS_HEIGHT / 2);
        lastPipeTime = System.nanoTime();
        wasInDanger = false;
        wasTooHigh = false;
        lastMilestoneScore = 0;
    }

    private void restartGame() {
        startGame();
    }

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update(now);
                render();
            }
        };
        gameLoop.start();
    }

    private void update(long currentTime) {
        if (gameState != GameState.PLAYING) {
            return;
        }

        bird.update();
        particleEffect.update();

        checkHighAltitude();
        checkMilestone();

        if (currentTime - lastPipeTime > PIPE_SPAWN_INTERVAL) {
            double gapY = random.nextDouble() * (CANVAS_HEIGHT - GROUND_HEIGHT - 300) + 200;
            pipes.add(new Pipe(CANVAS_WIDTH, gapY));
            lastPipeTime = currentTime;
        }

        boolean inDangerNow = false;
        boolean justPassedSafe = false;

        Iterator<Pipe> iterator = pipes.iterator();
        while (iterator.hasNext()) {
            Pipe pipe = iterator.next();
            pipe.update();

            // Pipe collision - play game over sound
            if (pipe.collidesWith(bird.getX(), bird.getY(), bird.getRadius())) {
                gameOver(true); // true = play sound
            }

            if (pipe.isNearBird(bird.getX(), DANGER_PROXIMITY)) {
                inDangerNow = true;
                if (!wasInDanger) {
                    soundManager.playDangerSound();
                    wasInDanger = true;
                }
            }

            if (pipe.isPassed(bird.getX()) && !pipe.isOffScreen()) {
                double distance = Math.abs((pipe.getX() + Pipe.getWidth() / 2) - bird.getX());
                if (distance < SAFE_PROXIMITY) {
                    justPassedSafe = true;
                }
            }

            if (pipe.isPassed(bird.getX())) {
                pipe.setScored();
                score++;
                if (score > highScore) {
                    highScore = score;
                }
            }

            if (pipe.isOffScreen()) {
                iterator.remove();
            }
        }

        if (!inDangerNow && wasInDanger) {
            wasInDanger = false;
        }

        if (justPassedSafe) {
            soundManager.playSafeSound();
        }

        // Check boundary collisions
        if (bird.getY() - bird.getRadius() < 0) {
            // Ceiling collision - play high altitude sound
            soundManager.playHighAltitudeSound();
            gameOver(false); // false = no game over sound (already played high altitude)
        } else if (bird.getY() + bird.getRadius() > CANVAS_HEIGHT - GROUND_HEIGHT) {
            // Ground collision - play game over sound
            gameOver(true); // true = play sound
        }
    }

    private void checkHighAltitude() {
        // Warn when flying too high (but not colliding yet)
        if (bird.getY() < HIGH_ALTITUDE_THRESHOLD) {
            if (!wasTooHigh) {
                soundManager.playHighAltitudeSound();
                wasTooHigh = true;
            }
        } else {
            wasTooHigh = false;
        }
    }

    private void checkMilestone() {
        if (score > 0 && score % MILESTONE_INTERVAL == 0 && score != lastMilestoneScore) {
            soundManager.playMilestoneSound();
            lastMilestoneScore = score;
        }
    }

    /**
     * Handle game over
     * 
     * @param playSound true to play game over sound, false otherwise
     */
    private void gameOver(boolean playSound) {
        gameState = GameState.GAME_OVER;
        particleEffect.createExplosionParticles(bird.getX(), bird.getY());
        if (playSound) {
            soundManager.playGameOverSound();
        }
    }

    private void render() {
        LinearGradient skyGradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(135, 206, 250)),
                new Stop(1, Color.rgb(0, 191, 255)));
        gc.setFill(skyGradient);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        for (Pipe pipe : pipes) {
            pipe.render(gc, CANVAS_HEIGHT - GROUND_HEIGHT);
        }

        LinearGradient groundGradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(139, 69, 19)),
                new Stop(1, Color.rgb(101, 67, 33)));
        gc.setFill(groundGradient);
        gc.fillRect(0, CANVAS_HEIGHT - GROUND_HEIGHT, CANVAS_WIDTH, GROUND_HEIGHT);

        bird.render(gc, avatarManager);
        particleEffect.render(gc);

        switch (gameState) {
            case MENU:
                renderMenu();
                break;
            case PLAYING:
                renderScore();
                break;
            case GAME_OVER:
                renderGameOver();
                break;
        }

        settingsMenu.render(gc);
    }

    private void renderMenu() {
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        gc.setTextAlign(TextAlignment.CENTER);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 72));
        gc.setFill(Color.GOLD);
        gc.fillText("Flappy Bird", CANVAS_WIDTH / 2 + 3, CANVAS_HEIGHT / 2 - 47);
        gc.setFill(Color.rgb(255, 140, 0));
        gc.fillText("Flappy Bird", CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 - 50);

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 24));
        gc.setFill(Color.WHITE);
        gc.fillText("Click or Press SPACE to Start", CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 + 50);

        if (highScore > 0) {
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            gc.fillText("High Score: " + highScore, CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 + 100);
        }

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        gc.setFill(Color.rgb(200, 200, 200));
        gc.fillText("Press S-Settings • 1-Danger • 2-Safe • 3-GameOver • A-Avatar",
                CANVAS_WIDTH / 2, CANVAS_HEIGHT - 30);
    }

    private void renderScore() {
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 48));

        String scoreText = String.valueOf(score);
        gc.strokeText(scoreText, CANVAS_WIDTH / 2, 60);
        gc.fillText(scoreText, CANVAS_WIDTH / 2, 60);
    }

    private void renderGameOver() {
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        gc.setTextAlign(TextAlignment.CENTER);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 64));
        gc.setFill(Color.RED);
        gc.setStroke(Color.DARKRED);
        gc.setLineWidth(3);
        gc.strokeText("GAME OVER", CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 - 80);
        gc.fillText("GAME OVER", CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 - 80);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        gc.setFill(Color.WHITE);
        gc.fillText("Score: " + score, CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        gc.setFill(Color.GOLD);
        gc.fillText("High Score: " + highScore, CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 + 50);

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 24));
        gc.setFill(Color.WHITE);
        gc.fillText("Click or Press SPACE to Restart", CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 + 120);
    }

    public void stop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (soundManager != null) {
            soundManager.dispose();
        }
    }
}
