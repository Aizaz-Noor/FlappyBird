import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class WeatherSystem {
    private List<Precipitation> precipitation;
    private Random random;
    private double width, height;
    private WeatherType currentWeather;
    private double windSpeed;
    private List<Cloud> clouds; // For CLOUDY weather

    public enum WeatherType {
        CLEAR, CLOUDY, RAIN, SNOW
    }

    public WeatherSystem(double width, double height) {
        this.width = width;
        this.height = height;
        this.random = new Random();
        this.precipitation = new ArrayList<>();
        this.clouds = new ArrayList<>();
        this.currentWeather = WeatherType.CLEAR;
        this.windSpeed = 0;
    }

    public void setWeather(WeatherType type) {
        this.currentWeather = type;
        precipitation.clear();

        // Generate clouds for CLOUDY weather
        if (type == WeatherType.CLOUDY) {
            clouds.clear();
            // Spawn 5-8 clouds
            int cloudCount = random.nextInt(4) + 5;
            for (int i = 0; i < cloudCount; i++) {
                double cx = random.nextDouble() * width;
                double cy = random.nextDouble() * 150 + 50; // Top half of screen
                double size = random.nextDouble() * 80 + 60; // 60-140px
                clouds.add(new Cloud(cx, cy, size, random.nextDouble() * 0.5 + 0.2));
            }
        }

        // Default wind based on weather
        if (type == WeatherType.RAIN) {
            windSpeed = -2; // Rain often falls slanted left due to bird moving right
        } else if (type == WeatherType.SNOW) {
            windSpeed = -1;
        } else {
            windSpeed = 0;
        }
    }

    public void setWindSpeed(double speed) {
        this.windSpeed = speed;
    }

    /**
     * Get wind force that affects bird physics
     */
    public double getWindForce() {
        if (currentWeather == WeatherType.RAIN) {
            return windSpeed * 0.15; // Rain pushes bird
        } else if (currentWeather == WeatherType.SNOW) {
            return windSpeed * 0.08; // Snow pushes less
        }
        return 0;
    }

    public void update() {
        // Update clouds (slow drift)
        for (Cloud cloud : clouds) {
            cloud.x -= 0.3; // Slow cloud movement
            if (cloud.x + cloud.size < 0) {
                cloud.x = width + random.nextDouble() * 100;
            }
        }

        if (currentWeather == WeatherType.CLEAR || currentWeather == WeatherType.CLOUDY)
            return;

        // Spawn precipitation
        int spawnRate = (currentWeather == WeatherType.RAIN) ? 8 : 3;
        for (int i = 0; i < spawnRate; i++) {
            double startX = random.nextDouble() * width;
            double startY = -10;

            // Adjust spawn for wind to cover screen
            if (windSpeed < 0) {
                startX = random.nextDouble() * (width + 200); // 200 extra buffer
            }

            double size = (currentWeather == WeatherType.RAIN)
                    ? random.nextDouble() * 15 + 15 // Rain: 15-30px
                    : random.nextDouble() * 4 + 8; // Snow: 8-12px
            double speed = (currentWeather == WeatherType.RAIN)
                    ? random.nextDouble() * 6 + 12 // Rain faster
                    : random.nextDouble() * 2 + 1; // Snow slower

            precipitation.add(new Precipitation(startX, startY, speed, size, currentWeather));
        }

        // Update existing particles
        Iterator<Precipitation> it = precipitation.iterator();
        while (it.hasNext()) {
            Precipitation p = it.next();
            p.update(windSpeed);

            // Remove if out of bounds
            if (p.y > height || p.x < -20 || p.x > width + 20) {
                it.remove();
            }
        }
    }

    // NOTE: spawnParticles() was removed - duplicate of code in update() method

    public void render(GraphicsContext gc) {
        // Render clouds first (if CLOUDY or RAIN - rain comes from clouds!)
        if (currentWeather == WeatherType.CLOUDY || currentWeather == WeatherType.RAIN) {
            for (Cloud cloud : clouds) {
                // Cloud shadow
                gc.setFill(Color.rgb(200, 200, 200, cloud.opacity * 0.8));
                gc.fillOval(cloud.x, cloud.y, cloud.size, cloud.size * 0.6);
                gc.fillOval(cloud.x - cloud.size * 0.3, cloud.y + cloud.size * 0.2, cloud.size * 0.7, cloud.size * 0.5);
                gc.fillOval(cloud.x + cloud.size * 0.3, cloud.y + cloud.size * 0.1, cloud.size * 0.6, cloud.size * 0.5);
            }
        }

        // Render precipitation if raining/snowing
        if (currentWeather == WeatherType.CLEAR || currentWeather == WeatherType.CLOUDY)
            return;

        if (currentWeather == WeatherType.RAIN) {
            // Brighter, more visible rain
            gc.setStroke(Color.rgb(180, 200, 255, 0.9)); // More opaque, bluer
            gc.setLineWidth(3); // Thicker rain lines (was 1.5)
        } else {
            // Brighter snow
            gc.setFill(Color.rgb(255, 255, 255, 0.95)); // Almost fully opaque
        }

        for (Precipitation p : precipitation) {
            if (currentWeather == WeatherType.RAIN) {
                // Draw line for rain
                // Calculate end point based on speed and wind
                double endX = p.x + windSpeed * 2;
                double endY = p.y + p.size;
                gc.strokeLine(p.x, p.y, endX, endY);
            } else {
                // Draw circle for snow
                gc.fillOval(p.x, p.y, p.size, p.size);
            }
        }
    }

    // Inner class for clouds
    private static class Cloud {
        double x, y;
        double size;
        double opacity;

        Cloud(double x, double y, double size, double opacity) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.opacity = opacity;
        }
    }

    private static class Precipitation {
        double x, y;
        double speed;
        double size; // Length for rain, Radius for snow
        WeatherType type;

        public Precipitation(double x, double y, double speed, double size, WeatherType type) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.size = size;
            this.type = type;
        }

        public void update(double wind) {
            y += speed;
            x += wind;

            if (type == WeatherType.SNOW) {
                // Add some sway to snow
                x += Math.sin(y * 0.05) * 0.5;
            }
        }
    }
}
