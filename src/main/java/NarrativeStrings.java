
import javafx.scene.paint.Color;

/**
 * NarrativeStrings — Central repository for all lore and story text.
 * Organized by game state and event type.
 */
public class NarrativeStrings {
    
    // --- Colors (Narrative Palette) ---
    public static final Color COLOR_SYSTEM = Color.rgb(0, 255, 100); // Green
    public static final Color COLOR_LORE = Color.rgb(255, 255, 255);   // White
    public static final Color COLOR_WARNING = Color.rgb(255, 100, 0);  // Orange
    public static final Color COLOR_CRITICAL = Color.rgb(255, 45, 107); // Red/Pink
    public static final Color COLOR_CYAN = Color.rgb(0, 229, 255);    // Cyan
    public static final Color COLOR_GRAY = Color.rgb(160, 175, 195);   // Secondary info

    // --- Fonts ---
    public static final String FONT_NARRATIVE = "Courier New";
    public static final String FONT_UI = "Arial";
    
    // --- Boot Sequence (Phase 1) ---
    public static final String[] BOOT_SEQUENCE = {
        "INITIATING SIMULATION...",
        "STABILIZING REALITY CORES",
        "WARNING: GRAVITY_VAR UNSTABLE",
        "SYNCING NEURAL FEED: NEWTON #4471",
        "SIMULATION LOADED. GOOD LUCK."
    };

    // --- HUD Labels ---
    public static final String HUD_INTEGRITY = "SYSTEM STABILITY";
    public static final String HUD_NODES = "NODES PROCESSED: ";
    public static final String HUD_SECTOR = "LAYER_";
    public static final String HUD_CORRUPTED = " // COMPROMISED";
    public static final String HUD_INSTABILITY = " // CRITICAL ANOMALY";

    // --- Stage Transitions ---
    public static final String TRANSITION_1_2 = "SECURITY BREACH. DEPLOYING COUNTERMEASURES.";
    public static final String TRANSITION_2_3 = "STILL PERSISTING? ACTIVATING LASER GRIDS.";
    public static final String TRANSITION_3_4 = "REALITY DISSOLVING. PREPARE FOR DEFORMATION.";
    public static final String TRANSITION_4_5 = "ABORT SIMULATION. TOTAL SYSTEM COLLAPSE.";

    // --- Glitch Mode Entry ---
    public static final String GLITCH_DOUBLE_GRAVITY = "FATAL ERROR: GRAVITY OVERLOAD";
    public static final String GLITCH_INVISIBLE_PIPES = "// RENDERING FAILURE";
    public static final String GLITCH_REVERSE_GRAVITY = "// VERTIGO DISRUPTION";
    public static final String GLITCH_SPEED_BURST = "// TEMPORAL ACCELERATION";
    public static final String GLITCH_WARNING = "CRITICAL INSTABILITY";
    public static final String GLITCH_STABILIZING = "SIMULATION STABILIZING...";

    // --- Death Screen ---
    public static final String DEATH_TITLE = "NEWTON DESTABILIZED";
    public static final String DEATH_REINITIALIZE = "[ REINITIALIZE ]";
    public static final String DEATH_ABANDON = "[ ABANDON SIM ]";
    
    public static final String DEATH_CAUSE_PIPE = "Shattered against Corrupted Column";
    public static final String DEATH_CAUSE_CEILING = "Simulation Boundary Breach (Upper)";
    public static final String DEATH_CAUSE_FLOOR = "Simulation Boundary Breach (Lower)";
    public static final String DEATH_CAUSE_LASER = "Incinerated by Security Laser";
    public static final String DEATH_CAUSE_CRUSHER = "Crushed by Containment Protocols";

    // --- Power-up Pickups ---
    public static final String PU_TIME = "CLOCK SUSPENDED";
    public static final String PU_GHOST = "PHASE SHIFT ACTIVE";
    public static final String PU_SHRINK = "MASS COMPRESSION ONLINE";

    // --- Rocket Mode Unlock ---
    public static final String ROCKET_UNLOCK_MSG1 = "ANOMALY DETECTED: PROPULSION FRAGMENT RECOVERED";
    public static final String ROCKET_UNLOCK_MSG2 = "HOLD SHIFT TO ACTIVATE ROCKET PROTOCOL";
}
