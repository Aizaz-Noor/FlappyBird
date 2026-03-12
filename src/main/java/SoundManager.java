import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * SoundManager - Optimized Audio System
 * Uses AudioClip for low-latency SFX and MediaPlayer for Music.
 */
public class SoundManager {
    // Music (Long streaming audio)
    private MediaPlayer backgroundMusic;

    // SFX (Short memory-resident audio)
    private List<AudioClip> gameOverSounds;
    private List<AudioClip> dangerSounds;
    private List<AudioClip> safeSounds;
    private AudioClip highAltitudeSound;
    private AudioClip milestoneSound;

    // Current sound indices
    private int currentGameOverIndex = 0;
    private int currentDangerIndex = 0;
    private int currentSafeIndex = 0;

    // Flags
    private boolean soundEnabled;

    // Volume constants
    private static final double BACKGROUND_VOLUME = 0.08;
    private static final double EFFECT_VOLUME = 0.7;

    // Cooldowns (nanoseconds) - Fixed to 3 seconds to prevent repetition
    private static final long DANGER_COOLDOWN = 3_000_000_000L; // 3s - prevents annoying repetition
    private static final long SAFE_COOLDOWN = 200_000_000L; // 0.2s - allows quick positive feedback
    private static final long HIGH_ALTITUDE_COOLDOWN = 3_000_000_000L; // 3s - consistent with other sounds

    private long lastDangerSound = 0;
    private long lastSafeSound = 0;
    private long lastHighAltitudeSound = 0;

    public SoundManager() {
        this.soundEnabled = true;
        this.gameOverSounds = new ArrayList<>();
        this.dangerSounds = new ArrayList<>();
        this.safeSounds = new ArrayList<>();

        loadSounds();
    }

    private void loadSounds() {
        System.out.println("=== Loading Sound System (Optimized) ===");

        // Load Background Music (MediaPlayer)
        loadBackgroundMusic("/sounds/background.mp4");

        // Load SFX (AudioClip)
        loadAudioClip(gameOverSounds, "/sounds/gameover1.m4a", "Game Over 1");
        loadAudioClip(gameOverSounds, "/sounds/gameover2.m4a", "Game Over 2");

        loadAudioClip(dangerSounds, "/sounds/danger1.m4a", "Danger 1");
        loadAudioClip(dangerSounds, "/sounds/danger2.m4a", "Danger 2");

        loadAudioClip(safeSounds, "/sounds/safe1.m4a", "Safe 1");
        loadAudioClip(safeSounds, "/sounds/safe2.m4a", "Safe 2");

        highAltitudeSound = loadSingleClip("/sounds/high_altitude.m4a", "High Altitude");
        milestoneSound = loadSingleClip("/sounds/milestone.m4a", "Milestone");

        System.out.println("===========================");
    }

    private void loadBackgroundMusic(String path) {
        try {
            java.net.URL resource = getClass().getResource(path);
            if (resource != null) {
                Media bgMedia = new Media(resource.toExternalForm());
                backgroundMusic = new MediaPlayer(bgMedia);
                backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);
                backgroundMusic.setVolume(BACKGROUND_VOLUME);
                System.out.println("✓ Background music loaded");
            } else {
                System.out.println("✗ Background music not found: " + path);
            }
        } catch (Exception e) {
            System.err.println("✗ Failed to load background music: " + e.getMessage());
        }
    }

    private void loadAudioClip(List<AudioClip> list, String path, String name) {
        try {
            java.net.URL resource = getClass().getResource(path);
            if (resource != null) {
                AudioClip clip = new AudioClip(resource.toExternalForm());
                clip.setVolume(EFFECT_VOLUME);
                list.add(clip);
                System.out.println("✓ " + name + " loaded (AudioClip)");
            } else {
                System.out.println("✗ " + name + " not found: " + path);
            }
        } catch (Exception e) {
            System.err.println("✗ Failed to load " + name + ": " + e.getMessage());
        }
    }

    private AudioClip loadSingleClip(String path, String name) {
        try {
            java.net.URL resource = getClass().getResource(path);
            if (resource != null) {
                AudioClip clip = new AudioClip(resource.toExternalForm());
                clip.setVolume(EFFECT_VOLUME);
                System.out.println("✓ " + name + " loaded (AudioClip)");
                return clip;
            }
        } catch (Exception e) {
            System.err.println("✗ Failed to load " + name + ": " + e.getMessage());
        }
        return null;
    }

    // Playback Methods

    public void playBackgroundMusic() {
        if (backgroundMusic != null && soundEnabled) {
            // Ensure it's not already playing or stuck
            if (backgroundMusic.getStatus() != MediaPlayer.Status.PLAYING) {
                backgroundMusic.play();
            }
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }

    // Optimized: No seeking needed for AudioClip, it handles concurrent playback
    // automatically
    public void playGameOverSound() {
        if (!soundEnabled || gameOverSounds.isEmpty())
            return;
        try {
            gameOverSounds.get(currentGameOverIndex).play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playDangerSound() {
        long now = System.nanoTime();
        if (!soundEnabled || dangerSounds.isEmpty())
            return;
        if (now - lastDangerSound < DANGER_COOLDOWN)
            return;

        try {
            dangerSounds.get(currentDangerIndex).play();
            lastDangerSound = now;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playSafeSound() {
        long now = System.nanoTime();
        if (!soundEnabled || safeSounds.isEmpty())
            return;
        if (now - lastSafeSound < SAFE_COOLDOWN)
            return;

        try {
            safeSounds.get(currentSafeIndex).play();
            lastSafeSound = now;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playHighAltitudeSound() {
        long now = System.nanoTime();
        if (!soundEnabled || highAltitudeSound == null)
            return;
        if (now - lastHighAltitudeSound < HIGH_ALTITUDE_COOLDOWN)
            return;

        try {
            highAltitudeSound.play(1.0); // Max volume
            lastHighAltitudeSound = now;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playMilestoneSound() {
        if (!soundEnabled || milestoneSound == null)
            return;
        try {
            milestoneSound.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Switching
    public void switchGameOverSound(boolean forward) {
        if (gameOverSounds.isEmpty())
            return;
        int delta = forward ? 1 : -1;
        currentGameOverIndex = (currentGameOverIndex + delta + gameOverSounds.size()) % gameOverSounds.size();
    }

    public void switchDangerSound(boolean forward) {
        if (dangerSounds.isEmpty())
            return;
        int delta = forward ? 1 : -1;
        currentDangerIndex = (currentDangerIndex + delta + dangerSounds.size()) % dangerSounds.size();
        playDangerSound();
    }

    public void switchSafeSound(boolean forward) {
        if (safeSounds.isEmpty())
            return;
        int delta = forward ? 1 : -1;
        currentSafeIndex = (currentSafeIndex + delta + safeSounds.size()) % safeSounds.size();
        playSafeSound();
    }

    // Getters

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

    public void toggleSound() {
        soundEnabled = !soundEnabled;
        if (!soundEnabled)
            stopBackgroundMusic();
        else
            playBackgroundMusic();
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    /**
     * Reset all sound cooldowns (called when starting new game)
     */
    public void resetCooldowns() {
        lastDangerSound = 0;
        lastSafeSound = 0;
        lastHighAltitudeSound = 0;
    }

    public void dispose() {
        stopBackgroundMusic();
        if (backgroundMusic != null)
            backgroundMusic.dispose();
        // AudioClips don't need explicit disposal, but good to clear refs
        gameOverSounds.clear();
        dangerSounds.clear();
        safeSounds.clear();
    }
}
