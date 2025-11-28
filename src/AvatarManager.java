import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.shape.ArcType;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages custom avatar face images for the bird
 */
public class AvatarManager {
    private List<Image> avatars;
    private List<String> avatarNames;
    private int currentAvatarIndex;
    private boolean avatarsLoaded;

    public AvatarManager() {
        this.avatars = new ArrayList<>();
        this.avatarNames = new ArrayList<>();
        this.currentAvatarIndex = 0;
        this.avatarsLoaded = false;

        loadAvatars();
    }

    /**
     * Load all avatar images from resources/avatars folder
     */
    private void loadAvatars() {
        try {
            File avatarDir = new File("resources/avatars");

            if (!avatarDir.exists() || !avatarDir.isDirectory()) {
                System.out.println("✗ Avatars folder not found: " + avatarDir.getAbsolutePath());
                System.out.println("  Please create 'resources/avatars/' folder and add your face images");
                return;
            }

            File[] files = avatarDir.listFiles((dir, name) -> {
                String nameLower = name.toLowerCase();
                return nameLower.endsWith(".png") ||
                        nameLower.endsWith(".jpg") ||
                        nameLower.endsWith(".jpeg");
            });

            if (files == null || files.length == 0) {
                System.out.println("⚠ No avatar images found in resources/avatars/");
                System.out.println("  Please add PNG or JPG images of faces to the avatars folder");
                return;
            }

            for (File file : files) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    Image avatar = new Image(fis);
                    avatars.add(avatar);
                    avatarNames.add(file.getName().replaceAll("\\.[^.]+$", "")); // Remove extension
                    System.out.println("✓ Loaded avatar: " + file.getName());
                } catch (Exception e) {
                    System.err.println("✗ Failed to load avatar: " + file.getName());
                }
            }

            if (!avatars.isEmpty()) {
                avatarsLoaded = true;
                System.out.println("✓ Total avatars loaded: " + avatars.size());
            }

        } catch (Exception e) {
            System.err.println("Error loading avatars: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Render the current avatar on the bird with rotation
     */
    public void renderAvatar(GraphicsContext gc, double x, double y, double rotation, double size) {
        if (!avatarsLoaded || avatars.isEmpty()) {
            // Fallback to default bird rendering
            renderDefaultBird(gc, size);
            return;
        }

        gc.save();

        // Create circular clipping path for realistic face display
        gc.beginPath();
        gc.arc(0, 0, size / 2, size / 2, 0, 360);
        gc.closePath();
        gc.clip();

        // Draw avatar image with smart cropping to avoid distortion
        Image currentAvatar = avatars.get(currentAvatarIndex);
        double imgW = currentAvatar.getWidth();
        double imgH = currentAvatar.getHeight();

        // Calculate source crop (square)
        double cropSize = Math.min(imgW, imgH);
        double sx, sy;

        if (imgW > imgH) {
            // Landscape: crop center
            sx = (imgW - cropSize) / 2;
            sy = 0;
        } else {
            // Portrait: crop top-center (faces are usually higher up)
            sx = 0;
            sy = (imgH - cropSize) / 4; // Offset slightly from top, but not center
        }

        double avatarSize = size * 1.1; // Slightly larger to fill the circle

        // Draw cropped image
        gc.drawImage(currentAvatar,
                sx, sy, cropSize, cropSize, // Source: crop square
                -avatarSize / 2, -avatarSize / 2, // Dest: centered
                avatarSize, avatarSize); // Dest: scaled to bird size

        gc.restore();
    }

    /**
     * Fallback default bird face when no avatars are loaded
     */
    private void renderDefaultBird(GraphicsContext gc, double size) {
        // Simple circle face
        gc.setFill(javafx.scene.paint.Color.rgb(255, 215, 0));
        gc.fillOval(-size / 2, -size / 2, size, size);

        // Eye
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.fillOval(size / 4 - 8, -size / 4 - 4, 8, 8);
        gc.setFill(javafx.scene.paint.Color.BLACK);
        gc.fillOval(size / 4 - 6, -size / 4 - 2, 4, 4);
    }

    /**
     * Switch to the next avatar
     */
    public void switchToNextAvatar() {
        if (!avatarsLoaded || avatars.isEmpty()) {
            System.out.println("No avatars available to switch");
            return;
        }

        currentAvatarIndex = (currentAvatarIndex + 1) % avatars.size();
        System.out.println("Switched to avatar: " + getCurrentAvatarName());
    }

    /**
     * Switch to a specific avatar by index
     */
    public void switchToAvatar(int index) {
        if (index >= 0 && index < avatars.size()) {
            currentAvatarIndex = index;
            System.out.println("Switched to avatar: " + getCurrentAvatarName());
        }
    }

    /**
     * Get the current avatar image
     */
    public Image getCurrentAvatar() {
        if (avatarsLoaded && !avatars.isEmpty()) {
            return avatars.get(currentAvatarIndex);
        }
        return null;
    }

    /**
     * Get the current avatar name
     */
    public String getCurrentAvatarName() {
        if (avatarsLoaded && !avatarNames.isEmpty()) {
            return avatarNames.get(currentAvatarIndex);
        }
        return "Default";
    }

    /**
     * Get the current avatar index
     */
    public int getCurrentAvatarIndex() {
        return currentAvatarIndex;
    }

    /**
     * Get all avatar images
     */
    public List<Image> getAllAvatars() {
        return avatars;
    }

    /**
     * Get all avatar names
     */
    public List<String> getAllAvatarNames() {
        return avatarNames;
    }

    /**
     * Get total number of avatars
     */
    public int getAvatarCount() {
        return avatars.size();
    }

    /**
     * Check if avatars are loaded
     */
    public boolean hasAvatars() {
        return avatarsLoaded && !avatars.isEmpty();
    }
}
