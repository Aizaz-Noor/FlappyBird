import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;

/**
 * DeathTracker - Tracks and persists death locations for heatmap analytics
 */
public class DeathTracker {
    // We store simple Double arrays [x, y] because Point2D is NOT Serializable
    private List<double[]> deathCoordinates;
    private static final String DIR_NAME = ".flappybird_data";
    private static final String FILE_NAME = "death_heatmap.dat";
    private static final int MAX_DEATHS = 500;

    public DeathTracker() {
        deathCoordinates = new ArrayList<>();
        loadFromFile();
    }

    public void recordDeath(double x, double y) {
        deathCoordinates.add(new double[] { x, y });

        if (deathCoordinates.size() > MAX_DEATHS) {
            deathCoordinates.remove(0);
        }

        saveToFile();
    }

    public List<Point2D> getDeathPositions() {
        List<Point2D> points = new ArrayList<>();
        for (double[] coord : deathCoordinates) {
            points.add(new Point2D(coord[0], coord[1]));
        }
        return points;
    }

    public void clearData() {
        deathCoordinates.clear();
        saveToFile();
    }

    public int getDeathCount() {
        return deathCoordinates.size();
    }

    private Path getSavePath() {
        String userHome = System.getProperty("user.home");
        Path dir = Paths.get(userHome, DIR_NAME);
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            return dir.resolve(FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
            return Paths.get(FILE_NAME); // Fallback
        }
    }

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getSavePath().toFile()))) {
            oos.writeObject(deathCoordinates);
        } catch (IOException e) {
            System.err.println("Failed to save death data: " + e.getMessage());
        }
    }

    // Suppress unchecked cast warning - we control the serialization format
    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        File file = getSavePath().toFile();
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            deathCoordinates = (List<double[]>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load death data: " + e.getMessage());
            deathCoordinates = new ArrayList<>();
        }
    }
}
