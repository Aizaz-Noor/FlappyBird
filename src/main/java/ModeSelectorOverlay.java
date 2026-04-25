import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ModeSelectorOverlay {
    private final VBox overlay;
    private final GameEngine gameEngine;
    private final Button quantumBtn;
    private final Button classicBtn;

    public ModeSelectorOverlay(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        
        overlay = new VBox(25);
        overlay.setAlignment(Pos.CENTER);
        overlay.setVisible(false);
        overlay.setPickOnBounds(false);

        HBox buttonBox = new HBox(30);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPickOnBounds(false);

        classicBtn = createStyledButton("CLASSIC MODE\nStandard Gameplay", "#2ecc71");
        classicBtn.setOnAction(e -> selectMode(GameEngine.GameMode.CLASSIC));

        quantumBtn = createStyledButton("QUANTUM MODE\nDual-Bird Entanglement", "#9b59b6");
        
        buttonBox.getChildren().addAll(classicBtn, quantumBtn);
        overlay.getChildren().addAll(buttonBox);
        
        overlay.setTranslateY(80);
    }

    private Button createStyledButton(String text, String colorHex) {
        Button btn = new Button(text);
        btn.setPrefSize(280, 80);
        btn.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: " + colorHex + "; -fx-text-fill: white; -fx-background-radius: 10px; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setOpacity(0.8));
        btn.setOnMouseExited(e -> btn.setOpacity(1.0));
        return btn;
    }

    public void updateState() {
        if (gameEngine.getHighScore() < 15) {
            quantumBtn.setText("QUANTUM MODE\n(Locked - Reach 15 pts)");
            quantumBtn.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: #555555; -fx-text-fill: #aaaaaa; -fx-background-radius: 10px;");
            quantumBtn.setDisable(true);
        } else {
            quantumBtn.setText("QUANTUM MODE\nDual-Bird Entanglement");
            quantumBtn.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: #9b59b6; -fx-text-fill: white; -fx-background-radius: 10px; -fx-cursor: hand;");
            quantumBtn.setDisable(false);
            quantumBtn.setOnAction(e -> selectMode(GameEngine.GameMode.QUANTUM));
        }
        
        boolean isC = gameEngine.activeMode == GameEngine.GameMode.CLASSIC;
        classicBtn.setEffect(isC ? new javafx.scene.effect.DropShadow(20, javafx.scene.paint.Color.web("#2ecc71")) : null);
        quantumBtn.setEffect((gameEngine.activeMode == GameEngine.GameMode.QUANTUM) ? new javafx.scene.effect.DropShadow(20, javafx.scene.paint.Color.web("#9b59b6")) : null);
    }

    private void selectMode(GameEngine.GameMode mode) {
        gameEngine.activeMode = mode;
        updateState();
        gameEngine.startGame();
    }

    public VBox getNode() {
        return overlay;
    }

    public void show() {
        updateState();
        overlay.setVisible(true);
        overlay.toFront();
    }

    public void hide() {
        overlay.setVisible(false);
    }
}
