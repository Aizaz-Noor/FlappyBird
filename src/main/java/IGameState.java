
public interface IGameState {
    GameEngine.GameState getGameState();
    GameEngine.Theme getCurrentTheme();
    StringBuilder getNameInput();
    GameEngine.Difficulty getCurrentDifficulty();
    String getPlayerName();
    int getHighScore();
    RocketMode getRocketMode();
    int getScore();
    boolean isNewPersonalBest();
    int getPersonalBest();
    int getGravityFlipCount();
    long getRunStartTime();
    long getCopyFeedbackTime();
    StorySystem getStorySystem();
    DeathTracker getDeathTracker();
    SettingsMenu getSettingsMenu();
    TutorialOverlay getTutorialOverlay();
    
    // Narrative HUD support
    double getStability();
    String getDeathCause();
    int getSimulationAttempts();
    StageManager getStageManager();
    AvatarManager getAvatarManager();
    double getComboMultiplier();
    boolean isGlitchWarningActive();
}
