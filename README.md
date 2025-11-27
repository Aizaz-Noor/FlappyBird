# FlappyBird 🐦

A Java-based Flappy Bird game using JavaFX with smooth gameplay and particle effects.

## Features ✨

- **Smooth Gameplay**: 60 FPS game loop for responsive controls
- **Particle Effects**: Beautiful particle explosions on collision
- **Dynamic Obstacles**: Randomly generated pipes with varying gaps
- **Score Tracking**: Real-time score display
- **Pause/Resume**: Space bar to pause and resume gameplay
- **Game Over Screen**: Restart option after collision

## Prerequisites 📋

- **Java 22** or higher
- **JavaFX SDK 23.0.1** (will be downloaded automatically by setup script)

## Quick Start 🚀

### Windows

#### Option 1: Automated Setup (Recommended)
```powershell
.\setup-and-run.ps1
```

#### Option 2: Manual Run (if JavaFX already set up)
```cmd
.\run.bat
```

### Manual Setup

1. **Download JavaFX SDK**:
   - Download from: https://gluonhq.com/products/javafx/
   - Extract to project root as `javafx-sdk-23.0.1`

2. **Compile**:
   ```cmd
   mkdir out
   javac -d out --module-path javafx-sdk-23.0.1\lib --add-modules javafx.controls src\*.java
   ```

3. **Run**:
   ```cmd
   java -cp out --module-path javafx-sdk-23.0.1\lib --add-modules javafx.controls FlappyBirdGame
   ```

## Controls 🎮

- **SPACE** - Flap / Start Game / Pause
- **R** - Restart after Game Over
- **ESC** - Exit game

## Project Structure 📁

```
FlappyBird/
├── src/
│   ├── Bird.java           # Bird entity with physics
│   ├── Pipe.java           # Obstacle generation
│   ├── GameEngine.java     # Core game loop and rendering
│   ├── ParticleEffect.java # Particle system
│   └── FlappyBirdGame.java # Main entry point
├── run.bat                 # Quick run script
├── setup-and-run.ps1       # Automated setup script
└── README.md
```

## How It Works 🔧

- **Physics**: Custom gravity and jump mechanics
- **Collision Detection**: Rectangle-based collision system
- **Rendering**: JavaFX Canvas for smooth 2D graphics
- **Game Loop**: Delta time-based updates for consistent gameplay

## Author 👨‍💻

**Aizaz Noor**

## License 📄

This project is open source and available for educational purposes.
