# 🎮 Flappy Bird - Enhanced JavaFX Game

An attractive and feature-rich Flappy Bird game built with JavaFX, featuring custom sound effects, background music, and personalized avatars!

## ✨ Features

### Core Game Features
- **Smooth Physics**: Realistic gravity and jump mechanics
- **Beautiful Graphics**: Gradient-based visuals with particle effects
- **Score Tracking**: Keep track of your high scores
- **Responsive Controls**: Mouse click or spacebar to jump

### 🎵 Sound System
- **Background Music**: Continuous low-volume music during gameplay
- **3 Switchable Meme Sounds**: Choose from 3 different meme sounds
- **Proximity Trigger**: Meme sound plays when you get close to pipes
- **Game Over Sound**: Meme sound plays on collision

### 👤 Avatar System
- **Custom Face Images**: Replace the bird with your own photos
- **Multiple Avatars**: Add as many avatar images as you want
- **Easy Switching**: Cycle through avatars with a keypress
- **Realistic Rendering**: Faces are rendered with proper rotation and circular clipping

### ⚙️ Settings Menu
- **Visual Interface**: Beautiful settings overlay
- **Avatar Selection**: Click to select any loaded avatar
- **Sound Selection**: Click to select from 3 meme sounds
- **Preview System**: See all avatars and sounds at a glance

## 🎮 Controls

| Key | Action |
|-----|--------|
| **SPACE** or **MOUSE CLICK** | Jump / Start Game / Restart |
| **S** | Toggle Settings Menu |
| **M** | Switch to Next Meme Sound |
| **A** | Switch to Next Avatar |
| **ESC** | Close Settings Menu |

## 📁 Adding Your Custom Resources

### Step 1: Prepare Your Sound Files

1. **Get your meme sound files** (3 files)
   - Format: MP3 or WAV
   - Recommended duration: 1-5 seconds
   
2. **Get background music** (1 file)
   - Format: MP3 or WAV
   - The game will automatically loop it

3. **Rename your files:**
   - `meme1.mp3` - Your first meme sound
   - `meme2.mp3` - Your second meme sound
   - `meme3.mp3` - Your third meme sound
   - `background.mp3` - Background music

4. **Copy to resources folder:**
   ```
   FlappyBird/resources/sounds/
   ├── background.mp3
   ├── meme1.mp3
   ├── meme2.mp3
   └── meme3.mp3
   ```

### Step 2: Prepare Your Avatar Images

1. **Get face photos** of you and your friends
   - Format: PNG, JPG, or JPEG
   - Recommended: Square images (1:1 ratio)
   - **Best results**: Cropped close-up face photos

2. **Name your files:** (any names work)
   - `avatar1.png` - Your face
   - `avatar2.png` - Friend #1
   - `avatar3.png` - Friend #2
   - ...and so on

3. **Copy to avatars folder:**
   ```
   FlappyBird/resources/avatars/
   ├── avatar1.png
   ├── avatar2.png
   ├── avatar3.png
   └── ...
   ```

### File Structure Example
```
FlappyBird/
├── resources/
│   ├── sounds/
│   │   ├── background.mp3      ✓ Add your background music here
│   │   ├── meme1.mp3           ✓ Add your first meme sound
│   │   ├── meme2.mp3           ✓ Add your second meme sound
│   │   └── meme3.mp3           ✓ Add your third meme sound
│   └── avatars/
│       ├── avatar1.png         ✓ Add your face images here
│       ├── avatar2.png
│       ├── avatar3.png
│       └── ...
├── src/
│   ├── FlappyBirdGame.java
│   ├── GameEngine.java
│   ├── Bird.java
│   ├── Pipe.java
│   ├── ParticleEffect.java
│   ├── SoundManager.java        [NEW]
│   ├── AvatarManager.java       [NEW]
│   └── SettingsMenu.java        [NEW]
└── ...
```

## 🚀 How to Run

### Option 1: Using PowerShell Script (Recommended)
```powershell
.\setup-and-run.ps1
```

### Option 2: Using Batch File
```cmd
run.bat
```

### Option 3: Manual Compilation
```bash
# Compile
javac --module-path "javafx-sdk-23.0.1\lib" --add-modules javafx.controls,javafx.media -d out\production\FlappyBird src\*.java

# Run
java --module-path "javafx-sdk-23.0.1\lib" --add-modules javafx.controls,javafx.media -cp out\production\FlappyBird FlappyBirdGame
```

## 🎯 Gameplay Tips

1. **Start the Game**: Click anywhere or press SPACE
2. **Avoid Pipes**: Navigate through the gaps
3. **Listen for Sounds**: The meme sound warns you when you're close to a pipe
4. **Customize**: Press S to open settings and change your avatar or meme sound
5. **Challenge Friends**: Share the game and compete for high scores!

## 🔊 Sound Features Explained

### Background Music
- Plays continuously from game start
- Low volume (20%) to not interfere with gameplay
- Loops automatically

### Meme Sounds
- **Proximity Trigger**: Plays when you get within 100 pixels of a pipe
- **Cooldown System**: 1-second cooldown to prevent sound spam
- **Game Over**: Plays on collision
- **3 Options**: Switch between 3 different meme sounds anytime

### Switching Sounds
1. **Keyboard**: Press **M** to cycle through sounds
2. **Settings Menu**: Press **S** then click on a sound icon
3. **Preview**: Each selection plays a preview of the sound

## 👤 Avatar Features Explained

### How Avatars Work
- Faces are rendered in a **circular shape** for realistic bird appearance
- Avatars **rotate** with the bird for smooth animation
- A **beak** is added on top for theFlapyBird look

### Switching Avatars
1. **Keyboard**: Press **A** to cycle through avatars
2. **Settings Menu**: Press **S** then click on an avatar
3. **Unlimited**: Add as many avatar images as you want!

## ⚠️ Important Notes

### Game Will Run Without Resources
- If you haven't added sound files, the game will still work (no sounds)
- If you haven't added avatars, you'll see the default golden bird
- Console messages will notify you about missing resources

### Supported Formats
- **Sound**: MP3, WAV
- **Images**: PNG, JPG, JPEG

### Recommendations
- **Square images** (1:1 ratio) work best for avatars
- **Close-up face photos** with the face centered
- **Short sound clips** (1-5 seconds) for meme sounds
- **Moderate volume** for background music (game sets it to 20%)

## 🐛 Troubleshooting

### No Sound Playing?
- Check that files are named correctly: `meme1.mp3`, `meme2.mp3`, `meme3.mp3`, `background.mp3`
- Check that files are in `resources/sounds/` folder
- Ensure files are MP3 or WAV format
- Check console for error messages

### Avatars Not Showing?
- Check that image files are in `resources/avatars/` folder
- Ensure files are PNG, JPG, or JPEG format
- Check console for loading messages
- Try with square (1:1 ratio) images

### Game Won't Start?
- Make sure JavaFX is properly installed
- Check that you're using Java 11 or higher
- Verify the `javafx-sdk-23.0.1` folder exists

## 📝 Technical Details

### New Classes
- **SoundManager.java**: Handles all audio (background music + meme sounds)
- **AvatarManager.java**: Manages face image loading and rendering
- **SettingsMenu.java**: Interactive settings UI overlay

### Modified Classes
- **Bird.java**: Integrated avatar rendering system
- **Pipe.java**: Added proximity detection for sound triggers
- **GameEngine.java**: Integrated all new features with keyboard shortcuts

### Technologies
- **JavaFX**: UI and graphics
- **JavaFX Media**: Audio playback
- **Java 22**: Core language

## 🎨 Customization Ideas

- Add more meme sounds (keep only 3 active at a time)
- Create themed avatar sets (e.g., all superheroes, all animals)
- Mix serious photos with funny edited faces
- Use voice recordings as meme sounds
- Try different background music for different moods

## 📜 License

Feel free to use, modify, and share this game with friends!

## 🙏 Credits

Enhanced with custom sound system and avatar features by your friendly AI assistant!

---

**Have fun playing and customizing your Flappy Bird! 🐦🎮**
