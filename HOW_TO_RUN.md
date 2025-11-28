# How to Run the Game 🎮

Hey! Here's how you can get the game running on your machine. I've made it as simple as possible.

## The Easy Way (Recommended) 🚀

If you're on Windows, just double-click the `setup-and-run.ps1` file. It'll handle everything for you—downloading JavaFX, setting up the paths, and launching the game.

If that doesn't work or you prefer doing things manually, check out the options below.

## Running via IntelliJ IDEA 💻

1.  Open the project folder in IntelliJ.
2.  Go to **File > Project Structure > Libraries**.
3.  Add the `lib` folder from the JavaFX SDK (it's inside the `javafx-sdk-23.0.1` folder).
4.  Run `FlappyBirdGame.java`.

**Note:** You might need to add these VM options in your Run Configuration if it complains about modules:
```
--module-path "javafx-sdk-23.0.1\lib" --add-modules javafx.controls,javafx.graphics,javafx.media
```

## Running via Command Line ⌨️

If you're a terminal person, you can use the `run.bat` script I included. Just type:
```cmd
.\run.bat
```

Or if you want to compile it yourself manually:

```cmd
javac --module-path "javafx-sdk-23.0.1\lib" --add-modules javafx.controls,javafx.graphics,javafx.media -d out\production\FlappyBird src\*.java

java --module-path "javafx-sdk-23.0.1\lib" --add-modules javafx.controls,javafx.graphics,javafx.media -cp out\production\FlappyBird FlappyBirdGame
```

## Controls 🕹️

-   **Space / Click**: Jump
-   **S**: Open Settings
-   **A**: Change Avatar (put your friends' faces in!)
-   **1, 2, 3**: Switch between sound effects
-   **R**: Restart when you die
-   **Esc**: Quit

## Troubleshooting 🔧

-   **No Sound?** Make sure your audio files are in `resources/sounds/`.
-   **No Avatars?** Check `resources/avatars/`. The game looks for images there.
-   **Weird Errors?** Usually it's JavaFX not being found. Double-check the path in the run command.

Enjoy the game! Let me know if you run into any issues.
