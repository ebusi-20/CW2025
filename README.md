COMP2042 Coursework – Tetris Maintenance & Extension

Author: Qian Chengyu
Module: COMP2042 – Software Maintenance
Project: TetrisJFX (maintained and extended)

1. GitHub Repository

The coursework code is hosted at:

https://github.com/ebusi-20/CW2025

Please clone this repository to obtain the latest version of the game.

2. Compilation & Execution Instructions

This project is a Maven-based JavaFX application.

Java version: 23

IDE used: IntelliJ IDEA

Main class: com.comp2042.Main

Build tool: Maven

2.1 Run from IntelliJ IDEA (recommended)

Clone the repository:

git clone https://github.com/ebusi-20/CW2025.git

Open the project folder (the one containing pom.xml) in IntelliJ IDEA as a Maven project.

Let Maven resolve all dependencies automatically.

Ensure the project SDK is set to Java 23.

Run the application:

Open src/main/java/com/comp2042/Main.java.

Right-click the Main class and choose Run 'Main.main()'.

2.2 Run using Maven from the command line

From the project root (the folder containing pom.xml):

mvn clean install
mvn javafx:run

The javafx-maven-plugin is configured to launch com.comp2042.Main as the entry point.

3. Implemented and Working Properly

The following maintenance and extension features have been implemented and, to the best of my knowledge from testing, are working as intended.

3.1 Gameplay Enhancements

Hard Drop mechanic (SPACE key)

A new event type HARD_DROP was added to the EventType enum.

InputEventListener, GameController, and GuiController were extended to support hard drop.

When the player presses SPACE, the current tetromino is dropped instantly to the lowest possible position, greatly speeding up gameplay.

Ghost Piece (landing preview)

SimpleBoard now computes a ghost position for the current piece using a helper method (calculateGhostPosition), which simulates falling until collision.

ViewData carries the ghost position (ghostXPosition, ghostYPosition).

GuiController uses a separate ghostPanel and ghostRectangles to render a ghost representation of the piece at its landing position.

This helps the player see where the piece will land before dropping it.

Multiple “Next” Piece Preview

BrickGenerator was extended with a new method peekNextBricks(int count).

RandomBrickGenerator maintains a queue (nextBricks) and uses ensureNextBricks(count) to guarantee there are always enough upcoming bricks.

SimpleBoard requests the next three bricks and passes the data through ViewData.

ViewData now stores List<int[][]> nextBricksData and corresponding texture types for the upcoming pieces.

GuiController renders a preview of multiple next pieces in a VBox called nextBox, instead of only one.

Improved keyboard controls
The following keys are supported:

Move left: Left arrow or A

Move right: Right arrow or D

Rotate: Up arrow or W

Soft drop: Down arrow or S

Hard drop: SPACE

Pause / resume: CONTROL

Start / restart: ENTER (start if first time, otherwise restart)

Start new game: N

Exit game: ESCAPE

These controls make the game more responsive and convenient to play.

3.2 Visual Enhancements (Textures and Backgrounds)

Per-cell texture matrix for blocks

SimpleBoard now maintains a textureMatrix, which stores a texture type for each occupied cell (for example: wood, stone, mixed).

A currentBrickTextureType is associated with the active tetromino, and a nextBricksTextureTypes list is maintained for the preview bricks.

MatrixOperations gains a new method mergeTexture(...) that merges the active brick into the texture matrix in parallel with the game matrix, and ensures textures remain in sync when rows are cleared.

ViewData includes the textureType of the current brick as well as texture types for upcoming bricks.

GuiController uses this information in getFillColorWithTexture(...) to render blocks with different visual textures, giving each piece a distinct style.

Dynamic board background based on score

GuiController introduces updateGameBoardBackgroundForScore(int score).

Background images change at score thresholds (for example, different images for low, medium, and high scores).

The method loads images from src/main/resources/images and updates the style of the board using CSS-like properties, creating a feeling of progression as the player scores more points.

3.3 Video Background and Background Music

Video background

GuiController uses JavaFX Media, MediaPlayer, and MediaView to display a looping background video behind the game.

The video file (images/wooden-house-in-the-forest-moewalls-com.mp4) is loaded from the classpath and bound to the scene’s width and height so that it scales with the window.

The video is placed behind game elements and marked mouse-transparent so it does not interfere with gameplay.

If the video cannot be found or loaded, error messages are printed to the console.

Background music

A second MediaPlayer (backgroundMusicPlayer) is configured to play background music from the audio resource folder (for example, audio/lofi.wav or audio/lofi.mp3).

The music is set to loop indefinitely, and error messages are logged if suitable audio resources are not found or if playback fails.

Music is initialized once the scene is ready, after the stage is shown.

3.4 Window and Lifecycle Improvements

Window setup

In Main, the window title is changed from the original TetrisJFX title to a localized Tetris title (this does not affect gameplay logic).

The application uses Screen.getPrimary().getVisualBounds() to maximize the window and size it to the available screen area.

Game lifecycle and robustness

GuiController carefully manages the JavaFX Timeline used for the game loop:

The timeline is stopped on game over, on restart, and when exiting.

Game state flags (such as pause and game-over) are checked before processing input events.

Platform.runLater is used in several places to ensure that UI updates happen on the JavaFX Application Thread.

Errors during hard drop or media initialization are caught and logged; in critical cases the timeline is stopped to prevent inconsistent state.

4. Implemented but Not Working Properly

At the time of submission:

No partially implemented or intentionally broken features are known.

All features listed in Section 3 have been implemented and behaved as expected during manual testing. If any issues are discovered in the marking environment (for example, media files not loading on certain systems), they are not intentional and are due to environment differences rather than deliberately incomplete work.

5. Features Not Implemented

All features that were planned for this coursework have been implemented in the final version.

There are no additional planned gameplay or visual features that were started and then intentionally left unfinished.

6. New Java Classes

No new top-level Java classes were added.

All changes were made by modifying and extending the existing classes provided in the original coursework skeleton.

7. Modified Java Classes

The following existing classes were modified. This section gives a high-level overview of the changes; detailed behavior can be seen in the source code and Javadoc.

7.1 com.comp2042.EventType

Added a new enum constant: HARD_DROP to represent the hard drop action.

7.2 com.comp2042.GameController

Updated to handle the new HARD_DROP event:

Repeatedly calls moveBrickDown() until the piece can no longer move.

Applies score bonuses from ClearRow after the hard drop.

Requests a new brick and checks for game over when necessary.

When refreshing the view, if the board is an instance of SimpleBoard, passes both the game matrix and the texture matrix to the GUI so textured blocks can be rendered.

Implements getCurrentViewData() to return board.getViewData(), allowing the GUI to query the latest view state.

Ensures the game background is refreshed after relevant actions.

7.3 com.comp2042.GuiController

Added extended keyboard support:

Arrow keys and WASD for movement and rotation.

SPACE for hard drop.

CONTROL for pause and resume.

ENTER and N for starting or restarting a game.

ESCAPE for exiting the game.

Integrated ghost piece rendering via:

A separate GridPane ghostPanel.

A 2D array ghostRectangles for ghost cells.

Visibility handling so the ghost appears only when relevant.

Implemented multiple “next piece” previews in nextBox, using a small grid per preview and aligning them in a vertical container.

Added texture-based block rendering using getFillColorWithTexture(...), which uses:

The block color from the board matrix.

The texture type from the texture matrix.

Added dynamic board backgrounds based on score using updateGameBoardBackgroundForScore(int score) and images from the resources folder.

Introduced a video background and background music:

Loads a looping MP4 video and binds it to the scene size.

Loads and loops background audio from the audio folder.

Contains logging and fallback behavior when resources cannot be found.

Improved game lifecycle and error handling:

Ensures the timeline is stopped in game over and error cases.

Uses JavaFX thread-safe updates via Platform.runLater.

7.4 com.comp2042.InputEventListener

Interface extended with:

DownData onHardDrop(MoveEvent event);

ViewData getCurrentViewData();

These methods allow the GUI controller to:

Perform a hard drop via the controller.

Retrieve the latest ViewData when needed.

7.5 com.comp2042.Main

Changed the window title to a localized Tetris title.

Uses Screen.getPrimary().getVisualBounds() to:

Size the stage to the full available screen.

Maximize the window on startup.

After calling primaryStage.show(), invokes:

setupVideoBackgroundAfterSceneReady(scene);

setupBackgroundMusicAfterSceneReady(scene);
in the GuiController to ensure media components are initialized only after the scene is ready.

7.6 com.comp2042.MatrixOperations

Fixed the intersect method:

Previously used mismatched indices (brick[j][i]) and coordinate calculations.

Now uses row and col indices properly (brick[row][col]) and maps them to targetX and targetY.

This improves collision detection accuracy and clarity.

Introduced mergeTexture(int[][] textureFields, int[][] brick, int x, int y, int textureType):

Merges the current brick into a texture matrix at the given offset.

Ensured that row clearing logic (checkRemoving) is compatible with the texture matrix, so textures are cleared consistently along with the blocks.

Added deepCopyList(List<int[][]> list) to safely copy lists of matrices when constructing ViewData.

7.7 com.comp2042.SimpleBoard

Added fields:

int[][] textureMatrix to store a texture type per cell.

int currentBrickTextureType to identify the texture of the active tetromino.

List<Integer> nextBricksTextureTypes to store texture types for upcoming bricks.

In the constructor:

Initializes currentGameMatrix and textureMatrix.

Initializes brickGenerator as RandomBrickGenerator.

Initializes brickRotator and score.

In createNewBrick():

Uses the pre-generated texture type for the first next brick as the current brick’s texture type when available.

Ensures exactly three texture types are generated and stored for upcoming bricks.

Maintains nextBricks and texture types in sync with the preview list.

Extends getViewData() to return a ViewData that includes:

The current piece’s matrix and position.

The list of next bricks’ matrices.

The list of next bricks’ texture types.

The ghost piece position.

The texture type of the current brick.

Adds a getTextureMatrix() method so the GUI can render textured backgrounds.

Implements calculateGhostPosition() to compute the ghost piece position by simulating falling until intersection.

In merge and clear operations:

Uses MatrixOperations.mergeTexture(...) and MatrixOperations.checkRemoving(...) to keep the texture matrix consistent with the game matrix.

newGame() resets:

currentGameMatrix

textureMatrix

score and other relevant fields

7.8 com.comp2042.ViewData

Extended to include:

List<int[][]> nextBricksData

List<Integer> nextBricksTextureTypes

int ghostXPosition, int ghostYPosition

int textureType

Provides corresponding getters so the GUI can:

Render the active piece.

Render multiple next pieces.

Render the ghost piece.

Apply the correct texture appearance.

7.9 com.comp2042.logic.bricks.BrickGenerator

Interface now includes:

List<Brick> peekNextBricks(int count);

This allows callers (such as SimpleBoard) to preview a configurable number of upcoming bricks without consuming them.

7.10 com.comp2042.logic.bricks.RandomBrickGenerator

Added import for java.util.stream.Collectors.

Ensures the nextBricks deque is always populated with enough upcoming bricks using ensureNextBricks(int count).

Implements peekNextBricks(int count) by returning a copy (using stream().limit(count).collect(Collectors.toList())) of the next bricks without modifying the queue.

Preserves the original behavior of randomly selecting bricks from brickList.

8. Unexpected Problems

No single critical unexpected problem blocked development, but there were several challenges:

Understanding the original code structure in enough detail to extend it without breaking existing behavior.

Keeping the game matrix, texture matrix, ghost piece, and multiple next previews all synchronized between SimpleBoard, GameController, ViewData, and GuiController.

Ensuring video and audio resources load correctly from the classpath, and handling cases where files are missing or unsupported on certain systems.

These issues were addressed through incremental refactoring, additional helper methods, and defensive checks with console logging.

9. How to Play

Use arrow keys or WASD to move and rotate the pieces.

Press DOWN or S to soft drop the current piece.

Press SPACE to perform a hard drop.

Press CONTROL to pause or resume the game.

Press ENTER or N to start or restart a game.

Press ESCAPE to exit the application.

Clear full horizontal lines to gain points and prevent the blocks from stacking up to the top of the board.

10. Documentation and Javadoc

This project includes generated Javadoc in the Javadoc folder, documenting the main classes and key methods used in the implementation.

The class diagram in Design.pdf reflects the final structure of the implementation, including the relationships between SimpleBoard, GameController, GuiController, ViewData, and the brick-related classes.