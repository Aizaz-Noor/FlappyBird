import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Advanced sound manager with multiple switchable sound categories
 */
public class SoundManager {
    // Background music
    private MediaPlayer backgroundMusic;

    // Game over sounds (2 options)
    private List<MediaPlayer> gameOverSounds;
    private int currentGameOverIndex;

    // Danger/close call sounds (2 options)
    private List<MediaPlayer> dangerSounds;
    private int currentDangerIndex;

    // Safe/relief sounds (2 options)
    private List<MediaPlayer> safeSounds;
    private int currentSafeIndex;

    // Special sounds
    private MediaPlayer highAltitudeSound;
    private MediaPlayer milestoneSound;

    private boolean soundEnabled;

    private static final double BACKGROUND_VOLUME = 0.08; // Very low background music (8%)
    private static final double EFFECT_VOLUME = 0.7; // Meme sounds (70%)

    // Cooldown tracking
    private long lastDangerSound;
    private long lastSafeSound;
    private long lastHighAltitudeSound;
    private static final long DANGER_COOLDOWN = 2_000_000_000L; // 2 seconds
    private static final long SAFE_COOLDOWN = 3_000_000_000L; // 3 seconds
    private static final long HIGH_ALTITUDE_COOLDOWN = 5_000_000_000L; // 5 seconds

    public SoundManager() {
        this.gameOverSounds = new ArrayList<>();
        this.dangerSounds = new ArrayList<>();
        this.safeSounds = new ArrayList<>();
        this.currentGameOverIndex = 0;
        this.currentDangerIndex = 0;
        this.currentSafeIndex = 0;
        this.soundEnabled = true;
        this.lastDangerSound = 0;
        this.lastSafeSound = 0;
        this.lastHighAltitudeSound = 0;

        loadSounds();
    }

    /**
     * Load all sound files
     */
    private void loadSounds() {
        try {
            // Load background music (MP4 video file - JavaFX can extract audio)
            loadBackgroundMusic("resources/sounds/background.mp4");

            // Load game over sounds
            loadSound(gameOverSounds, "resources/sounds/gameover1.m4a", "Game Over Sound 1");
            loadSound(gameOverSounds, "resources/sounds/gameover2.m4a", "Game Over Sound 2");

            // Load danger sounds
            loadSound(dangerSounds, "resources/sounds/danger1.m4a", "Danger Sound 1");
            loadSound(dangerSounds, "resources/sounds/danger2.m4a", "Danger Sound 2");

            // Load safe sounds
            loadSound(safeSounds, "resources/sounds/safe1.m4a", "Safe Sound 1");
            loadSound(safeSounds, "resources/sounds/safe2.m4a", "Safe Sound 2");

            // Load special sounds
            highAltitudeSound = loadSingleSound("resources/sounds/high_altitude.m4a", "High Altitude Sound");
            if (highAltitudeSound != null) {
                highAltitudeSound.setVolume(1.0); // Max volume for this specific sound
            }
            milestoneSound = loadSingleSound("resources/sounds/milestone.m4a", "Milestone Sound");

        } catch (Exception e) {
            System.err.println("Error loading sounds: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load background music
     */
    private void loadBackgroundMusic(String path) {
        File bgFile = new File(path);
        if (bgFile.exists()) {
            try {
                Media bgMedia = new Media(bgFile.toURI().toString());
                backgroundMusic = new MediaPlayer(bgMedia);
                backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);
                backgroundMusic.setVolume(BACKGROUND_VOLUME);
                System.out.println("✓ Background music loaded: " + path);
            } catch (Exception e) {
                System.err.println("✗ Failed to load background music: " + e.getMessage());
            }
        } else {
            System.out.println("✗ Background music not found: " + bgFile.getAbsolutePath());
        }
    }

    /**
     * Load a sound into a list
     */
    private void loadSound(List<MediaPlayer> list, String path, String name) {
        File file = new File(path);
        if (file.exists()) {
            try {
                Media media = new Media(file.toURI().toString());
                MediaPlayer player = new MediaPlayer(media);
                player.setVolume(EFFECT_VOLUME);
                list.add(player);
                System.out.println("✓ " + name + " loaded");
            } catch (Exception e) {
                System.err.println("✗ Failed to load " + name + ": " + e.getMessage());
            }
        } else {
            System.out.println("✗ " + name + " not found: " + path);
        }
    }

    /**
     * Load a single sound file
     */
    private MediaPlayer loadSingleSound(String path, String name) {
        File file = new File(path);
        if (file.exists()) {
            try {
                Media media = new Media(file.toURI().toString());
                MediaPlayer player = new MediaPlayer(media);
                player.setVolume(EFFECT_VOLUME);
                System.out.println("✓ " + name + " loaded");
                return player;
            } catch (Exception e) {
                System.err.println("✗ Failed to load " + name + ": " + e.getMessage());
            }
        } else {
            System.out.println("✗ " + name + " not found: " + path);
        }
        return null;
    }

    /**
     * Start background music
     */
    public void playBackgroundMusic() {
        if (backgroundMusic != null && soundEnabled) {
            backgroundMusic.play();
        }
    }

    /**
     * Stop background music
     */
    public void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }

    /**
     * Play game over sound (when bird collides)
     */
    public void playGameOverSound() {
        if (!soundEnabled || gameOverSounds.isEmpty())
            return;

        try {
            MediaPlayer sound = gameOverSounds.get(currentGameOverIndex);
            sound.stop();
            sound.seek(Duration.ZERO);
            sound.play();
        } catch (Exception e) {
            System.err.println("Error playing game over sound: " + e.getMessage());
        }
    }

    /**
     * Play danger sound (when close to pillar or ground) with cooldown
     */
    public void playDangerSound() {
        long now = System.nanoTime();
        if (!soundEnabled || dangerSounds.isEmpty())
            return;
        if (now - lastDangerSound < DANGER_COOLDOWN)
            return;

        try {
            MediaPlayer sound = dangerSounds.get(currentDangerIndex);
            sound.stop();
            sound.seek(Duration.ZERO);
            sound.play();
            lastDangerSound = now;
        } catch (Exception e) {
            System.err.println("Error playing danger sound: " + e.getMessage());
        }
    }

    /**
     * Play safe sound (when passing obstacle safely) with cooldown
     */
    public void playSafeSound() {
        long now = System.nanoTime();
        if (!soundEnabled || safeSounds.isEmpty())
            return;
        if (now - lastSafeSound < SAFE_COOLDOWN)
            return;

        try {
            MediaPlayer sound = safeSounds.get(currentSafeIndex);
            sound.stop();
            sound.seek(Duration.ZERO);
            sound.play();
            lastSafeSound = now;
        } catch (Exception e) {
            System.err.println("Error playing safe sound: " + e.getMessage());
        }
    }

    /**
     * Play high altitude sound (when bird goes too high) with cooldown
     */
    public void playHighAltitudeSound() {
        long now = System.nanoTime();
        if (!soundEnabled || highAltitudeSound == null)
            return;
        if (now - lastHighAltitudeSound < HIGH_ALTITUDE_COOLDOWN)
            return;

        try {
            highAltitudeSound.stop();
            highAltitudeSound.seek(Duration.ZERO);
            highAltitudeSound.play();
            lastHighAltitudeSound = now;
        } catch (Exception e) {
            System.err.println("Error playing high altitude sound: " + e.getMessage());
        }
    }

    /**
     * Play milestone sound (every 5 pillars)
     */
    public void playMilestoneSound() {
        if (!soundEnabled || milestoneSound == null)
            return;

        try {
            milestoneSound.stop();
            milestoneSound.seek(Duration.ZERO);
            milestoneSound.play();
        } catch (Exception e) {
            System.err.println("Error playing milestone sound: " + e.getMessage());
        }
    }

    // ===== SOUND SWITCHING METHODS =====

    /**
     * Switch game over sound
     */
    public void switchGameOverSound() {
        if (gameOverSounds.isEmpty())
            return;
        currentGameOverIndex = (currentGameOverIndex + 1) % gameOverSounds.size();
        System.out.println("Switched to Game Over Sound " + (currentGameOverIndex + 1));
    }

    /**
     * Switch danger sound
     */
    public void switchDangerSound() {
        if (dangerSounds.isEmpty())
            return;
        currentDangerIndex = (currentDangerIndex + 1) % dangerSounds.size();
        System.out.println("Switched to Danger Sound " + (currentDangerIndex + 1));
        playDangerSound();
    }

    /**
     * Switch safe sound
     */
    public void switchSafeSound() {
        if (safeSounds.isEmpty())
            return;
        currentSafeIndex = (currentSafeIndex + 1) % safeSounds.size();
        System.out.println("Switched to Safe Sound " + (currentSafeIndex + 1));
        playSafeSound();
    }

    // ===== GETTERS =====

    public int getCurrentGameOverIndex() {
        return currentGameOverIndex;
    }

    public int getCurrentDangerIndex() {
        return currentDangerIndex;
    }

    public int getCurrentSafeIndex() {
        return currentSafeIndex;
    }

    public int getGameOverSoundCount() {
        return gameOverSounds.size();
    }

    public int getDangerSoundCount() {
        return dangerSounds.size();
    }

    public int getSafeSoundCount() {
        return safeSounds.size();
    }

    /**
     * Toggle sound on/off
     */
    public void toggleSound() {
        soundEnabled = !soundEnabled;
        if (!soundEnabled) {
            stopBackgroundMusic();
        } else {
            playBackgroundMusic();
        }
    }

    /**
     * Check if sounds are enabled
     */
    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    /**
     * Stop all sounds
     */
    public void stopAll() {
        stopBackgroundMusic();
        for (MediaPlayer sound : gameOverSounds) {
            if (sound != null)
                sound.stop();
        }
        for (MediaPlayer sound : dangerSounds) {
            if (sound != null)
                sound.stop();
        }
        for (MediaPlayer sound : safeSounds) {
            if (sound != null)
                sound.stop();
        }
        if (highAltitudeSound != null)
            highAltitudeSound.stop();
        if (milestoneSound != null)
            milestoneSound.stop();
    }

    /**
     * Cleanup resources
     */
    public void dispose() {
        stopAll();
        if (backgroundMusic != null)
            backgroundMusic.dispose();
        for (MediaPlayer sound : gameOverSounds) {
            if (sound != null)
                sound.dispose();
        }
        for (MediaPlayer sound : dangerSounds) {
            if (sound != null)
                sound.dispose();
        }
        for (MediaPlayer sound : safeSounds) {
            if (sound != null)
                sound.dispose();
        }
        if (highAltitudeSound != null)
            highAltitudeSound.dispose();
        if (milestoneSound != null)
            milestoneSound.dispose();
    }
}
