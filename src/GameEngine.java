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
 * Main game engine that manages game state, logic, and rendering
 */
public class GameEngine {
    private Canvas canvas;
    private GraphicsContext gc;
    private AnimationTimer gameLoop;

    private Bird bird;
    private List<Pipe> pipes;
    private ParticleEffect particleEffect;
    private Random random;

    private GameState gameState;
    private int score;
    private int highScore;

    private long lastPipeTime;
    private static final long PIPE_SPAWN_INTERVAL = 2_000_000_000L; // 2 seconds in nanoseconds

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

        gameState = GameState.MENU;
        score = 0;
        highScore = 0;
        lastPipeTime = 0;

        setupInput();
        startGameLoop();
    }

    /**
     * Setup keyboard and mouse input handlers
     */
    private void setupInput() {
        canvas.setFocusTraversable(true);

        canvas.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                handleJump();
            }
        });

        canvas.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                handleJump();
            }
        });
    }

    /**
     * Handle jump input based on game state
     */
    private void handleJump() {
        switch (gameState) {
            case MENU:
                startGame();
                break;
            case PLAYING:
                bird.jump();
                particleEffect.createJumpParticles(bird.getX(), bird.getY());
                break;
            case GAME_OVER:
                restartGame();
                break;
        }
    }

    /**
     * Start a new game
     */
    private void startGame() {
        gameState = GameState.PLAYING;
        score = 0;
        pipes.clear();
        particleEffect.clear();
        bird.reset(150, CANVAS_HEIGHT / 2);
        lastPipeTime = System.nanoTime();
    }

    /**
     * Restart game after game over
     */
    private void restartGame() {
        startGame();
    }

    /**
     * Start the main game loop
     */
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

    /**
     * Update game logic
     */
    private void update(long currentTime) {
        if (gameState != GameState.PLAYING) {
            return;
        }

        // Update bird
        bird.update();

        // Update particles
        particleEffect.update();

        // Spawn pipes
        if (currentTime - lastPipeTime > PIPE_SPAWN_INTERVAL) {
            double gapY = random.nextDouble() * (CANVAS_HEIGHT - GROUND_HEIGHT - 300) + 200;
            pipes.add(new Pipe(CANVAS_WIDTH, gapY));
            lastPipeTime = currentTime;
        }

        // Update pipes and check collisions
        Iterator<Pipe> iterator = pipes.iterator();
        while (iterator.hasNext()) {
            Pipe pipe = iterator.next();
            pipe.update();

            // Check collision
            if (pipe.collidesWith(bird.getX(), bird.getY(), bird.getRadius())) {
                gameOver();
            }

            // Check scoring
            if (pipe.isPassed(bird.getX())) {
                pipe.setScored();
                score++;
                if (score > highScore) {
                    highScore = score;
                }
            }

            // Remove off-screen pipes
            if (pipe.isOffScreen()) {
                iterator.remove();
            }
        }

        // Check boundary collisions
        if (bird.getY() - bird.getRadius() < 0 ||
                bird.getY() + bird.getRadius() > CANVAS_HEIGHT - GROUND_HEIGHT) {
            gameOver();
        }
    }

    /**
     * Handle game over
     */
    private void gameOver() {
        gameState = GameState.GAME_OVER;
        particleEffect.createExplosionParticles(bird.getX(), bird.getY());
    }

    /**
     * Render all game elements
     */
    private void render() {
        // Clear canvas with sky gradient
        LinearGradient skyGradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(135, 206, 250)), // Light sky blue
                new Stop(1, Color.rgb(0, 191, 255)) // Deep sky blue
        );
        gc.setFill(skyGradient);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        // Render pipes
        for (Pipe pipe : pipes) {
            pipe.render(gc, CANVAS_HEIGHT - GROUND_HEIGHT);
        }

        // Render ground
        LinearGradient groundGradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(139, 69, 19)), // Saddle brown
                new Stop(1, Color.rgb(101, 67, 33)) // Dark brown
        );
        gc.setFill(groundGradient);
        gc.fillRect(0, CANVAS_HEIGHT - GROUND_HEIGHT, CANVAS_WIDTH, GROUND_HEIGHT);

        // Render bird
        bird.render(gc);

        // Render particles
        particleEffect.render(gc);

        // Render UI based on game state
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
    }

    /**
     * Render menu screen
     */
    private void renderMenu() {
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);

        // Title
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 72));
        gc.setFill(Color.GOLD);
        gc.fillText("Flappy Bird", CANVAS_WIDTH / 2 + 3, CANVAS_HEIGHT / 2 - 47);
        gc.setFill(Color.rgb(255, 140, 0));
        gc.fillText("Flappy Bird", CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 - 50);

        // Instructions
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 24));
        gc.setFill(Color.WHITE);
        gc.fillText("Click or Press SPACE to Start", CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 + 50);

        // High score
        if (highScore > 0) {
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            gc.fillText("High Score: " + highScore, CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 + 100);
        }
    }

    /**
     * Render current score during gameplay
     */
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

    /**
     * Render game over screen
     */
    private void renderGameOver() {
        // Semi-transparent overlay
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        gc.setTextAlign(TextAlignment.CENTER);

        // Game Over text
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 64));
        gc.setFill(Color.RED);
        gc.setStroke(Color.DARKRED);
        gc.setLineWidth(3);
        gc.strokeText("GAME OVER", CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 - 80);
        gc.fillText("GAME OVER", CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 - 80);

        // Score
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        gc.setFill(Color.WHITE);
        gc.fillText("Score: " + score, CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2);

        // High score
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        gc.setFill(Color.GOLD);
        gc.fillText("High Score: " + highScore, CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 + 50);

        // Restart instruction
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 24));
        gc.setFill(Color.WHITE);
        gc.fillText("Click or Press SPACE to Restart", CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 + 120);
    }

    /**
     * Stop the game loop
     */
    public void stop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }
}
