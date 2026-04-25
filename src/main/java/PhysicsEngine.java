

/**
 * PhysicsEngine - De-coupled collision and physics manager for Newton's Glitch.
 * Moves complexity out of GameEngine.
 */
public class PhysicsEngine {
    
    public enum CollisionResult {
        NONE,
        PIPE,
        LASER,
        CRUSHER,
        CEILING,
        FLOOR
    }

    /**
     * Central collision hub. Returns the cause of death if a collision occurred.
     */
    public CollisionResult checkCollisions(Bird bird, java.util.List<Pipe> pipes, java.util.List<LaserGate> lasers, java.util.List<Crusher> crushers) {
        double bx = bird.getX();
        double by = bird.getY();
        double br = bird.getRadius();

        // 1. Level Bounds
        if (by - br < 0) return CollisionResult.CEILING;
        if (by + br > 700 - 50) return CollisionResult.FLOOR; // 700 = Height, 50 = Ground

        // 2. Pipes
        for (Pipe pipe : pipes) {
            if (pipe.isPoolActive() && pipe.collidesWith(bx, by, br)) {
                return CollisionResult.PIPE;
            }
        }

        // 3. Laser Gates
        for (LaserGate laser : lasers) {
            if (laser.isPoolActive() && laser.collidesWith(bx, by, br)) {
                return CollisionResult.LASER;
            }
        }

        // 4. Crushers
        for (Crusher crusher : crushers) {
            if (crusher.isPoolActive() && crusher.collidesWith(bx, by, br)) {
                return CollisionResult.CRUSHER;
            }
        }

        return CollisionResult.NONE;
    }

    /**
     * Checks if bird is in a "danger zone" (near obstacles)
     */
    public boolean checkDanger(Bird bird, java.util.List<Pipe> pipes, java.util.List<LaserGate> lasers, java.util.List<Crusher> crushers, double dangerRadius) {
        double bx = bird.getX();
        double by = bird.getY();
        
        for (Pipe pipe : pipes) {
            if (pipe.isPoolActive() && pipe.isCloseBy(bx, by, dangerRadius)) return true;
        }
        
        for (LaserGate laser : lasers) {
            if (laser.isPoolActive() && laser.isActive() && laser.isCloseBy(bx, by, dangerRadius)) return true; // Note: added isCloseBy to LaserGate
        }

        return false;
    }
}
