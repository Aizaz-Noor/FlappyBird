import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Main JavaFX Application for Flappy Bird Game
 */
public class FlappyBirdGame extends Application {

    private static final double WINDOW_WIDTH = 800;
    private static final double WINDOW_HEIGHT = 600;

    private GameEngine gameEngine;

    @Override
    public void start(Stage primaryStage) {
        // Create canvas
        Canvas canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);

        // Create game engine
        gameEngine = new GameEngine(canvas);

        // Setup scene
        StackPane root = new StackPane();
        root.getChildren().add(canvas);

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        // Setup stage
        primaryStage.setTitle("Flappy Bird - JavaFX Game");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // Request focus for canvas to receive keyboard input
        canvas.requestFocus();
    }

    @Override
    public void stop() {
        if (gameEngine != null) {
            gameEngine.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
