# Customizing Your Game 🎨

One of the coolest things about this version is that you can make it your own. Want to fly as your cat? Want the game to scream at you when you crash? You can do that.

Here's how to add your own stuff.

## Adding Custom Avatars 🐦

You can replace the bird with any image you want.

1.  **Find an image**: Square images work best (like 1:1 ratio). Close-ups of faces are hilarious.
2.  **Format**: Make sure it's a `.png`, `.jpg`, or `.jpeg`.
3.  **Drop it in**:
    Go to `FlappyBird/resources/avatars/` and paste your image there.
    
    That's it! The game automatically finds all images in that folder.
    
    **In-Game:** Press **A** to cycle through them until you find your new masterpiece.

## Adding Custom Sounds 🎵

You can change the funny sounds that play during the game.

### The Meme Sounds
There are 3 slots for "meme" sounds (the ones that play when you're in danger or crash).

1.  **Get your audio**: Short clips (1-3 seconds) work best. MP3 or WAV.
2.  **Rename them**: You *must* name them exactly like this:
    -   `meme1.mp3`
    -   `meme2.mp3`
    -   `meme3.mp3`
3.  **Drop them in**:
    Put them inside `FlappyBird/resources/sounds/`.

### Background Music
Want different music?
1.  Find an MP3 or WAV file.
2.  Rename it to `background.mp3`.
3.  Replace the existing one in `FlappyBird/resources/sounds/`.

## Troubleshooting

-   **Game crashing?** Check if your image files are actually images and not corrupted.
-   **Sound not playing?** JavaFX can be picky about audio formats. If an MP3 doesn't work, try converting it to WAV.
-   **Changes not showing up?** You usually need to restart the game for it to pick up new files.

Have fun customizing!
