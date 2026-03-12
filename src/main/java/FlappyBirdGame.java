import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Main JavaFX Application Entry point for Flappy Bird Game
 * This class extends Application which is required for all JavaFX apps
 */
public class FlappyBirdGame extends Application {

    // Window dimensions - these are constants (final = cannot change)
    private static final double WINDOW_WIDTH = 1000;
    private static final double WINDOW_HEIGHT = 700;

    // Reference to the game engine
    private GameEngine gameEngine;

    // start() is called automatically by JavaFX after launch()
    // This is where we set up the window and start the game

    @Override
    public void start(Stage primaryStage) {
        // Canvas is a drawing surface for graphics
        Canvas canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);

        // Create the game engine which handles all game logic
        gameEngine = new GameEngine(canvas);

        // StackPane is a container that centers its children
        StackPane root = new StackPane();
        root.getChildren().add(canvas);

        // Scene contains all the visual content
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        // Stage is the window itself
        primaryStage.setTitle("Newton's Glitch");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true); // Allow window resizing
        primaryStage.show();

        // Canvas needs focus to receive keyboard input
        canvas.requestFocus();
    }

    // stop() is called when the application closes
    // We use this to clean up resources like sounds

    @Override
    public void stop() {
        if (gameEngine != null) {
            gameEngine.stop();
        }
    }

    // main() is the entry point it calls launch() to start JavaFX
    public static void main(String[] args) {
        launch(args);
    }
}
