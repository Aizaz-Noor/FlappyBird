import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

public class InputManager {
    private final GameEngine gameEngine;
    private final Canvas canvas;

    public InputManager(GameEngine gameEngine, Canvas canvas) {
        this.gameEngine = gameEngine;
        this.canvas = canvas;
        setupInput();
    }

    private void setupInput() {
        canvas.setFocusTraversable(true);

        canvas.setOnKeyPressed(e -> {
            if (gameEngine.getGameState() == GameEngine.GameState.NAME_INPUT) {
                gameEngine.enqueueCommand(() -> gameEngine.handleNameInput(e.getCode(), e.getText()));
            } else if (gameEngine.getGameState() == GameEngine.GameState.DIFFICULTY_SELECT) {
                gameEngine.enqueueCommand(() -> gameEngine.handleDifficultyInput(e.getCode()));
            } else if (e.getCode() == KeyCode.SPACE) {
                if (gameEngine.getStorySystem().isIntroShowing()) { 
                    gameEngine.enqueueCommand(() -> gameEngine.getStorySystem().skipIntro()); 
                    return; 
                }
                gameEngine.enqueueCommand(() -> gameEngine.handleAction());
            } else if (e.getCode() == KeyCode.SHIFT) {
                if (gameEngine.getStorySystem().isIntroShowing()) { 
                    gameEngine.enqueueCommand(() -> gameEngine.getStorySystem().skipIntro()); 
                    return; 
                }
                gameEngine.enqueueCommand(() -> gameEngine.handleGravityFlip());
            } else if (e.getCode() == KeyCode.P) {
                gameEngine.enqueueCommand(() -> gameEngine.togglePause());
            } else if (e.getCode() == KeyCode.S) {
                gameEngine.enqueueCommand(() -> gameEngine.toggleSettings());
            } else if (e.getCode() == KeyCode.ESCAPE && gameEngine.getSettingsMenu().isOpen()) {
                gameEngine.enqueueCommand(() -> gameEngine.toggleSettings());
            } else if (e.getCode() == KeyCode.ESCAPE && gameEngine.getGameState() == GameEngine.GameState.GAME_OVER) {
                gameEngine.enqueueCommand(() -> {
                    gameEngine.uiManager.clearGameOverEffect();
                    gameEngine.gameState = GameEngine.GameState.MENU;
                });
            } else if (e.getCode() == KeyCode.ESCAPE && gameEngine.getGameState() == GameEngine.GameState.CREDITS) {
                gameEngine.enqueueCommand(() -> {
                    gameEngine.uiManager.clearGameOverEffect();
                    gameEngine.gameState = GameEngine.GameState.MENU;
                });
            } else if (e.getCode() == KeyCode.A) {
                gameEngine.enqueueCommand(() -> gameEngine.avatarManager.switchToNextAvatar());
            } else if (e.getCode() == KeyCode.C && gameEngine.getGameState() == GameEngine.GameState.MENU) {
                gameEngine.enqueueCommand(() -> gameEngine.gameState = GameEngine.GameState.CREDITS);
            } else if (e.getCode() == KeyCode.R) {
                if (gameEngine.getGameState() == GameEngine.GameState.GAME_OVER) {
                    gameEngine.enqueueCommand(() -> gameEngine.startReplay());
                } else if (gameEngine.getGameState() == GameEngine.GameState.MENU) {
                    gameEngine.enqueueCommand(() -> gameEngine.getRocketMode().toggle()); // Toggle rocket mode from menu
                }
            }
        });

        canvas.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                double mx = e.getX(), my = e.getY();

                // Check pause button
                double padding = 10;
                if ((gameEngine.getGameState() == GameEngine.GameState.PLAYING || gameEngine.getGameState() == GameEngine.GameState.PAUSED) &&
                        mx >= GameEngine.PAUSE_X - padding && mx <= GameEngine.PAUSE_X + GameEngine.PAUSE_SIZE + padding &&
                        my >= GameEngine.PAUSE_Y - padding && my <= GameEngine.PAUSE_Y + GameEngine.PAUSE_SIZE + padding) {
                    gameEngine.enqueueCommand(() -> gameEngine.togglePause());
                    return;
                }

                // Check setting gear icon
                if (gameEngine.getGameState() == GameEngine.GameState.MENU || gameEngine.getGameState() == GameEngine.GameState.GAME_OVER) {
                    double rx = GameEngine.WIDTH - 50;
                    double ry = 15;
                    if (mx >= rx && mx <= rx + 35 && my >= ry && my <= ry + 35) {
                        gameEngine.enqueueCommand(() -> gameEngine.toggleSettings());
                        return;
                    }
                }

                // Check settings menu (Now handled by JavaFX Nodes directly)

                // Check tutorial dismissal
                if (gameEngine.getTutorialOverlay().handleClick(mx, my))
                    return;

                // Check Copy Score button on game over screen
                if (gameEngine.getGameState() == GameEngine.GameState.GAME_OVER) {
                    double copyBtnX = GameEngine.WIDTH / 2 - 115;
                    double copyBtnY = GameEngine.HEIGHT / 2 + 78;
                    double copyBtnW = 230;
                    double copyBtnH = 34;
                    if (mx >= copyBtnX && mx <= copyBtnX + copyBtnW && my >= copyBtnY && my <= copyBtnY + copyBtnH) {
                        gameEngine.enqueueCommand(() -> gameEngine.copyScoreToClipboard());
                        return;
                    }
                }

                gameEngine.enqueueCommand(() -> gameEngine.handleAction());
            } else if (e.getButton() == MouseButton.SECONDARY) {
                gameEngine.enqueueCommand(() -> gameEngine.handleGravityFlip());
            }
        });

        // Mouse drag — runs instantly for responsiveness
        canvas.setOnMouseDragged(e -> {
            // Settings volume handled by JavaFX UI directly
        });
        canvas.setOnMouseReleased(e -> {
            // Settings volume handled by JavaFX UI directly
        });
    }
}
