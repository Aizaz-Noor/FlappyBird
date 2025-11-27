import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Manages particle effects for visual enhancement
 */
public class ParticleEffect {
    private List<Particle> particles;
    private Random random;

    public ParticleEffect() {
        particles = new ArrayList<>();
        random = new Random();
    }

    /**
     * Create jump particles when bird flaps
     */
    public void createJumpParticles(double x, double y) {
        for (int i = 0; i < 8; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = random.nextDouble() * 3 + 1;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;

            Color color = Color.rgb(
                    200 + random.nextInt(55),
                    200 + random.nextInt(55),
                    100 + random.nextInt(155));

            particles.add(new Particle(x, y, vx, vy, color, 30));
        }
    }

    /**
     * Create explosion particles on collision
     */
    public void createExplosionParticles(double x, double y) {
        for (int i = 0; i < 20; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = random.nextDouble() * 5 + 2;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;

            Color color = Color.rgb(
                    200 + random.nextInt(55),
                    random.nextInt(100),
                    random.nextInt(100));

            particles.add(new Particle(x, y, vx, vy, color, 50));
        }
    }

    /**
     * Update all particles
     */
    public void update() {
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle p = iterator.next();
            p.update();
            if (p.isDead()) {
                iterator.remove();
            }
        }
    }

    /**
     * Render all particles
     */
    public void render(GraphicsContext gc) {
        for (Particle p : particles) {
            p.render(gc);
        }
    }

    /**
     * Clear all particles
     */
    public void clear() {
        particles.clear();
    }

    /**
     * Individual particle class
     */
    private static class Particle {
        private double x, y;
        private double vx, vy;
        private Color color;
        private int life;
        private int maxLife;

        public Particle(double x, double y, double vx, double vy, Color color, int maxLife) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.life = maxLife;
            this.maxLife = maxLife;
        }

        public void update() {
            x += vx;
            y += vy;
            vy += 0.2; // Gravity effect
            life--;
        }

        public void render(GraphicsContext gc) {
            double alpha = (double) life / maxLife;
            gc.setFill(Color.color(
                    color.getRed(),
                    color.getGreen(),
                    color.getBlue(),
                    alpha));
            gc.fillOval(x - 3, y - 3, 6, 6);
        }

        public boolean isDead() {
            return life <= 0;
        }
    }
}
