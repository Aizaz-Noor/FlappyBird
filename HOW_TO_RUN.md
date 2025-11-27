# IntelliJ IDEA Setup & Run Instructions

## Method 1: Run via IntelliJ IDEA

### 1. Open Project in IntelliJ
1. Open IntelliJ IDEA
2. Click "Open" and select the `FlappyBird` folder

### 2. Configure JavaFX
1. Go to **File → Project Structure** (Ctrl+Alt+Shift+S)
2. Click "Libraries" on the left
3. Click the "+ (minus if you fail this time delete it first by click on minus )" button and select "Java"
4. Browse to your JavaFX SDK: `FlappyBird/javafx-sdk-23.0.1/lib`
5. Select all JAR files and click OK
6. Click "Apply" and "OK"

### 3. Configure Run Configuration
1. Go to **Run → Edit Configurations**
2. Click the "+" button → "Application"
3. Set the following:
   - **Name**: Flappy Bird Game
   - **Main class**: `FlappyBirdGame`
   - **VM options**: 
     ```
     --module-path "javafx-sdk-23.0.1\lib" --add-modules javafx.controls,javafx.graphics,javafx.media
     ```
   - **Working directory**: Your FlappyBird project root folder
4. Click "Apply" and "OK"

### 4. Run the Game
- Click the green "Run" button (Shift+F10)
- Or use **Run → Run 'FlappyBirdGame'**

---

## Method 2: Run via Script (Command Line)

### Windows (run.bat)
```batch
.\run.bat
```

**If run.bat doesn't work:**
1. Make sure you're in the FlappyBird directory
2. Check that `javafx-sdk-23.0.1` folder exists
3. Try running commands manually:
```batch
javac --module-path "javafx-sdk-23.0.1\lib" --add-modules javafx.controls,javafx.graphics,javafx.media -d out\production\FlappyBird src\*.java

java --module-path "javafx-sdk-23.0.1\lib" --add-modules javafx.controls,javafx.graphics,javafx.media -cp out\production\FlappyBird FlappyBirdGame
```

---

## Game Controls

| Key | Action |
|-----|--------|
| SPACE / CLICK | Jump |
| 1 | Switch Danger Sound |
| 2 | Switch Safe Sound |
| 3 | Switch Game Over Sound |
| A | Switch Avatar |
| S | Settings Menu |
| ESC | Close Settings |

---

## Troubleshooting

### "Module not found" error
- Make sure JavaFX SDK is in the correct location
- Verify all three modules are included: `javafx.controls`, `javafx.graphics`, `javafx.media`

### No sound
- Check that `.m4a` and `.mp4` files are in `resources/sounds/`
- Verify `javafx.media` module is included in VM options

### Avatars not showing
- Check that image files (`.png`, `.jpg`) are in `resources/avatars/`
- Restart the game after adding new images

---

## Project Structure
```
FlappyBird/
├── src/
│   ├── FlappyBirdGame.java (Main class)
│   ├── GameEngine.java
│   ├── Bird.java
│   ├── Pipe.java
│   ├── SoundManager.java
│   ├── AvatarManager.java
│   └── ...
├── resources/
│   ├── sounds/
│   └── avatars/
├── javafx-sdk-23.0.1/
├── out/production/FlappyBird/ (compiled classes)
└── run.bat
```
