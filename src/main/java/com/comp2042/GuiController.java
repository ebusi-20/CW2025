package com.comp2042;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.Reflection;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.application.Platform;

import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class GuiController implements Initializable {

    private static final int BRICK_SIZE = 20;
    private double tileSize = BRICK_SIZE;
    private int visibleRows;
    private int cols;
    

    @FXML
    private GridPane gamePanel;

    @FXML
    private Group groupNotification;

    @FXML
    private GridPane brickPanel;
    @FXML
    private GridPane ghostPanel;

    @FXML
    private GameOverPanel gameOverPanel;

    @FXML
    private BorderPane gameBoard;

    @FXML
    private Pane root;
    
    @FXML
    private javafx.scene.layout.VBox scoreBox;
    @FXML
    private javafx.scene.layout.VBox controlBox;
    @FXML
    private javafx.scene.layout.VBox nextBox;
    @FXML
    private javafx.scene.layout.VBox restartBox;
    @FXML
    private javafx.scene.layout.VBox exitBox;
    // Keep a handle to the pause/resume button so keyboard shortcuts can toggle it
    private javafx.scene.control.Button pauseToggleButton;
    // Keep a handle to the start/restart button so we can rename it after the first start
    private javafx.scene.control.Button restartButtonRef;

    // Keep references to framed containers so we can align widths
    private javafx.scene.layout.StackPane scoreFrameRef;
    private javafx.scene.layout.StackPane controlFrameRef;
    private javafx.scene.layout.StackPane nextFrameRef;
    private javafx.scene.layout.StackPane restartFrameRef;
    private javafx.scene.layout.StackPane exitFrameRef;
    
    // Track which themed background is currently applied (0=default, 1=1k score, 2=2k score)
    private int backgroundStage = -1;

    private Rectangle[][] displayMatrix;

    private InputEventListener eventListener;

    private Rectangle[][] rectangles;
    private Rectangle[][] ghostRectangles;

    private Timeline timeLine;

    private final BooleanProperty isPause = new SimpleBooleanProperty();

    private final BooleanProperty isGameOver = new SimpleBooleanProperty();
    private boolean hasStarted = false;

    private static final int NEXT_PREVIEW_GRID_SIZE = 4;
    private static final int HIDDEN_ROWS = 2;
    private static final double BOARD_BORDER_WIDTH = 12.0;
    private static final double BASE_FALL_MILLIS = 600.0;

    private final List<Rectangle[][]> nextBricksRectangles = new ArrayList<>();

    // Cached texture patterns (optional; falls back to colors if missing)
    private ImagePattern woodPattern;
    private ImagePattern stonePattern;
    
    // Video background
    private MediaPlayer backgroundMediaPlayer;
    private MediaView backgroundMediaView;
    
    // Background music
    private MediaPlayer backgroundMusicPlayer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Font.loadFont(getClass().getClassLoader().getResource("digital.ttf").toExternalForm(), 38);
        // Try to load a wooden texture image from common names under src/main/resources/images
        loadWoodPattern("images/wood.jpg", "images/wood.jpeg", "images/wood.png",
                "images/block.jpg", "images/block.jpeg", "images/block.png");
        // Load white stone texture
        loadStonePattern("images/whitebrick.jpg", "images/whitebrick.jpeg", "images/whitebrick.png");
        // Load and setup video background
        setupVideoBackground();
        // Background music will be started after scene is ready (see setupBackgroundMusicAfterSceneReady)
        // Ensure the board starts with the default background
        updateGameBoardBackgroundForScore(0);
        // Start paused until the player presses Enter
        isPause.set(true);
        gamePanel.setFocusTraversable(true);
        gamePanel.requestFocus();
        gamePanel.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (isPause.getValue() == Boolean.FALSE && isGameOver.getValue() == Boolean.FALSE) {
                    if (keyEvent.getCode() == KeyCode.LEFT || keyEvent.getCode() == KeyCode.A) {
                        refreshBrick(eventListener.onLeftEvent(new MoveEvent(EventType.LEFT, EventSource.USER)));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.D) {
                        refreshBrick(eventListener.onRightEvent(new MoveEvent(EventType.RIGHT, EventSource.USER)));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.W) {
                        refreshBrick(eventListener.onRotateEvent(new MoveEvent(EventType.ROTATE, EventSource.USER)));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.S) {
                        moveDown(new MoveEvent(EventType.DOWN, EventSource.USER));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.SPACE) {
                        hardDrop(new MoveEvent(EventType.HARD_DROP, EventSource.USER));
                        keyEvent.consume();
                    }
                }
                // Keyboard shortcut: Ctrl toggles pause/resume
                if (keyEvent.getCode() == KeyCode.CONTROL) {
                    togglePauseResume();
                    keyEvent.consume();
                }
                // Keyboard shortcut: Enter restarts the game
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    if (!hasStarted) {
                        startGame();
                    } else {
                        newGame(null);
                    }
                    keyEvent.consume();
                }
                // Keyboard shortcut: Esc exits the game
                if (keyEvent.getCode() == KeyCode.ESCAPE) {
                    exitGame();
                    keyEvent.consume();
                }
                if (keyEvent.getCode() == KeyCode.N) {
                    newGame(null);
                }
            }
        });
        gameOverPanel.setVisible(false);
        if (ghostPanel != null) {
            ghostPanel.setMouseTransparent(true);
            ghostPanel.setVisible(false);
        }

        final Reflection reflection = new Reflection();
        reflection.setFraction(0.8);
        reflection.setTopOpacity(0.9);
        reflection.setTopOffset(-12);
    }

    private void loadWoodPattern(String... resourceNames) {
        for (String name : resourceNames) {
            try {
                URL url = getClass().getClassLoader().getResource(name);
                if (url != null) {
                    Image img = new Image(url.toExternalForm());
                    woodPattern = new ImagePattern(img, 0, 0, 1, 1, true);
                    return; // Stop at the first found image
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void loadStonePattern(String... resourceNames) {
        for (String name : resourceNames) {
            try {
                URL url = getClass().getClassLoader().getResource(name);
                if (url != null) {
                    Image img = new Image(url.toExternalForm());
                    stonePattern = new ImagePattern(img, 0, 0, 1, 1, true);
                    return; // Stop at the first found image
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void setupVideoBackground() {
        // Defer video setup until scene is available
        Platform.runLater(() -> {
            try {
                // Try to load the video file
                URL videoUrl = getClass().getClassLoader().getResource("images/wooden-house-in-the-forest-moewalls-com.mp4");
                if (videoUrl != null) {
                    Media media = new Media(videoUrl.toExternalForm());
                    backgroundMediaPlayer = new MediaPlayer(media);
                    backgroundMediaView = new MediaView(backgroundMediaPlayer);
                    
                    // Set video to loop infinitely
                    backgroundMediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                    
                    // Make video fill the entire background
                    backgroundMediaView.setPreserveRatio(false);
                    
                    // Position video behind all other elements
                    backgroundMediaView.setMouseTransparent(true);
                    
                    // Wait a bit more to ensure scene is fully ready
                    Platform.runLater(() -> {
                        if (root != null) {
                            Scene scene = root.getScene();
                            if (scene != null) {
                                // Bind video size to scene size
                                backgroundMediaView.fitWidthProperty().bind(scene.widthProperty());
                                backgroundMediaView.fitHeightProperty().bind(scene.heightProperty());
                                
                                // Wrap the scene's root in a StackPane with video behind
                                javafx.scene.Parent currentRoot = scene.getRoot();
                                if (!(currentRoot instanceof javafx.scene.layout.StackPane) || 
                                    !((javafx.scene.layout.StackPane) currentRoot).getChildren().contains(backgroundMediaView)) {
                                    javafx.scene.layout.StackPane videoContainer = new javafx.scene.layout.StackPane();
                                    videoContainer.getChildren().addAll(backgroundMediaView, currentRoot);
                                    scene.setRoot(videoContainer);
                                }
                                
                                // Start playing
                                backgroundMediaPlayer.setMute(true); // Mute by default (can be changed)
                                backgroundMediaPlayer.play();
                            }
                        }
                    });
                } else {
                    System.err.println("Video file not found: images/wooden-house-in-the-forest-moewalls-com.mp4");
                }
            } catch (Exception e) {
                // If video fails to load, fall back to static background
                System.err.println("Failed to load video background: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    public void setupVideoBackgroundAfterSceneReady(Scene scene) {
        try {
            // Try to load the video file
            URL videoUrl = getClass().getClassLoader().getResource("images/wooden-house-in-the-forest-moewalls-com.mp4");
            if (videoUrl != null) {
                Media media = new Media(videoUrl.toExternalForm());
                backgroundMediaPlayer = new MediaPlayer(media);
                backgroundMediaView = new MediaView(backgroundMediaPlayer);
                
                // Set video to loop infinitely
                backgroundMediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                
                // Make video fill the entire background
                backgroundMediaView.setPreserveRatio(false);
                
                // Position video behind all other elements
                backgroundMediaView.setMouseTransparent(true);
                
                // Bind video size to scene size
                backgroundMediaView.fitWidthProperty().bind(scene.widthProperty());
                backgroundMediaView.fitHeightProperty().bind(scene.heightProperty());
                
                // Wrap the scene's root in a StackPane with video behind
                javafx.scene.Parent currentRoot = scene.getRoot();
                javafx.scene.layout.StackPane videoContainer = new javafx.scene.layout.StackPane();
                videoContainer.getChildren().addAll(backgroundMediaView, currentRoot);
                scene.setRoot(videoContainer);
                
                // Start playing
                backgroundMediaPlayer.setMute(true); // Mute by default (can be changed)
                backgroundMediaPlayer.play();
            } else {
                System.err.println("Video file not found: images/wooden-house-in-the-forest-moewalls-com.mp4");
            }
        } catch (Exception e) {
            // If video fails to load, fall back to static background
            System.err.println("Failed to load video background: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Helper method to find the first available audio resource file.
     * This method is separated to avoid lambda variable capture issues.
     */
    private String findAudioResource(String[] audioFormats) {
        for (String format : audioFormats) {
            InputStream musicStream = getClass().getClassLoader().getResourceAsStream(format);
            if (musicStream != null) {
                try {
                    musicStream.close();
                } catch (Exception e) {
                    // Ignore close errors
                }
                return format;
            }
        }
        return null;
    }
    
    public void setupBackgroundMusicAfterSceneReady(Scene scene) {
        // Use Platform.runLater to ensure we're on the JavaFX Application Thread
        Platform.runLater(() -> {
            try {
                // Try to load the background music file from resources
                // JavaFX MediaPlayer has better support for WAV files, so try WAV first, then MP3
                String musicUrlString = null;
                String[] audioFormats = {"audio/lofi.wav", "audio/lofi.mp3"};
                
                // Find the first available audio file (WAV preferred, then MP3)
                final String finalResourcePath = findAudioResource(audioFormats);
                
                if (finalResourcePath == null) {
                    System.err.println("ERROR: Background music file not found!");
                    System.err.println("Expected locations:");
                    System.err.println("  - src/main/resources/audio/lofi.wav");
                    System.err.println("  - src/main/resources/audio/lofi.mp3");
                    System.err.println("\nIf MP3 doesn't work, re-encode it to JavaFX-compatible format:");
                    System.err.println("  ffmpeg -i lofi.mp3 -acodec libmp3lame -b:a 128k -ar 44100 -ac 2 lofi_fixed.mp3");
                    return;
                }
                
                System.out.println("Found audio file: " + finalResourcePath);
                
                // Extract to temporary file (works better with JavaFX Media)
                try {
                    InputStream musicStream = getClass().getClassLoader().getResourceAsStream(finalResourcePath);
                    if (musicStream != null) {
                        // Get file extension from resource path
                        String extension = finalResourcePath.substring(finalResourcePath.lastIndexOf('.'));
                        
                        // Create a temporary file with the correct extension
                        File tempFile = File.createTempFile("lofi_bg_music", extension);
                        tempFile.deleteOnExit(); // Clean up on exit
                        
                        // Copy the resource to the temporary file
                        try (FileOutputStream out = new FileOutputStream(tempFile)) {
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = musicStream.read(buffer)) != -1) {
                                out.write(buffer, 0, bytesRead);
                            }
                        }
                        musicStream.close();
                        
                        // Use the temporary file path
                        musicUrlString = tempFile.toURI().toURL().toExternalForm();
                        System.out.println("Loaded background music to temporary file: " + musicUrlString);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to extract music to temp file, trying direct resource: " + e.getMessage());
                    e.printStackTrace();
                    // Fallback to direct resource URL
                    URL musicUrl = getClass().getClassLoader().getResource(finalResourcePath);
                    if (musicUrl != null) {
                        musicUrlString = musicUrl.toExternalForm();
                        System.out.println("Using direct resource URL: " + musicUrlString);
                    }
                }
                
                if (musicUrlString != null) {
                    try {
                        // Create Media object with the file URL
                        System.out.println("Creating Media object from: " + musicUrlString);
                        Media musicMedia = new Media(musicUrlString);
                        backgroundMusicPlayer = new MediaPlayer(musicMedia);
                        
                        // Set music to loop infinitely
                        backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                        
                        // Set volume to 60% (adjustable: 0.0 to 1.0)
                        backgroundMusicPlayer.setVolume(0.6);
                        
                        // Set auto-play callback - this is the key to starting playback
                        backgroundMusicPlayer.setOnReady(() -> {
                            System.out.println("Background music is READY, starting playback...");
                            Platform.runLater(() -> {
                                try {
                                    if (backgroundMusicPlayer != null && 
                                        backgroundMusicPlayer.getStatus() == MediaPlayer.Status.READY) {
                                        backgroundMusicPlayer.play();
                                        System.out.println("Background music playback STARTED. Status: " + backgroundMusicPlayer.getStatus());
                                        System.out.println("Music volume: " + (backgroundMusicPlayer.getVolume() * 100) + "%");
                                    }
                                } catch (Exception e) {
                                    System.err.println("Failed to start music in onReady: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            });
                        });
                        
                        // Backup: Handle end of media to restart if cycle count doesn't work
                        backgroundMusicPlayer.setOnEndOfMedia(() -> {
                            if (backgroundMusicPlayer != null) {
                                Platform.runLater(() -> {
                                    backgroundMusicPlayer.seek(javafx.util.Duration.ZERO);
                                    backgroundMusicPlayer.play();
                                    System.out.println("Music looped (end of media reached)");
                                });
                            }
                        });
                        
                        // Error handling - log errors but don't spam console
                        backgroundMusicPlayer.setOnError(() -> {
                            System.err.println("\n=== BACKGROUND MUSIC ERROR ===");
                            if (backgroundMusicPlayer.getError() != null) {
                                System.err.println("Error message: " + backgroundMusicPlayer.getError().getMessage());
                                System.err.println("Error type: " + backgroundMusicPlayer.getError().getType());
                            }
                            System.err.println("Media status: " + backgroundMusicPlayer.getStatus());
                            
                            if (finalResourcePath != null && finalResourcePath.endsWith(".mp3")) {
                                System.err.println("\nThe MP3 file may use an unsupported codec.");
                                System.err.println("The file has been re-encoded to a compatible format.");
                                System.err.println("If errors persist, try converting to WAV format.");
                            }
                        });
                        
                        // Status monitoring for debugging (only log important state changes)
                        backgroundMusicPlayer.statusProperty().addListener((obs, oldStatus, newStatus) -> {
                            // Only log significant state changes
                            if (oldStatus != newStatus && 
                                (newStatus == MediaPlayer.Status.READY || 
                                 newStatus == MediaPlayer.Status.PLAYING ||
                                 newStatus == MediaPlayer.Status.STOPPED ||
                                 newStatus == MediaPlayer.Status.HALTED)) {
                                System.out.println("Music status: " + oldStatus + " -> " + newStatus);
                            }
                            
                            // Auto-start when status becomes READY
                            if (newStatus == MediaPlayer.Status.READY && oldStatus == MediaPlayer.Status.UNKNOWN) {
                                // Small delay to ensure MediaPlayer is fully ready
                                Timeline playTimeline = new Timeline(new KeyFrame(Duration.millis(300), e -> {
                                    Platform.runLater(() -> {
                                        if (backgroundMusicPlayer != null && 
                                            backgroundMusicPlayer.getStatus() == MediaPlayer.Status.READY &&
                                            backgroundMusicPlayer.getCurrentRate() == 0.0) {
                                            try {
                                                backgroundMusicPlayer.play();
                                                System.out.println("Music auto-started (status listener)");
                                            } catch (Exception ex) {
                                                System.err.println("Error in auto-start: " + ex.getMessage());
                                            }
                                        }
                                    });
                                }));
                                playTimeline.play();
                            }
                        });
                        
                        System.out.println("Background music MediaPlayer created. Initial status: " + backgroundMusicPlayer.getStatus());
                        System.out.println("Waiting for media to be ready...");
                        
                    } catch (Exception e) {
                        System.err.println("Failed to create MediaPlayer: " + e.getMessage());
                        e.printStackTrace();
                        backgroundMusicPlayer = null;
                    }
                } else {
                    System.err.println("ERROR: Failed to load background music file!");
                    System.err.println("Tried to load: " + finalResourcePath);
                    System.err.println("Please ensure the file exists in src/main/resources/audio/ and rebuild the project.");
                }
            } catch (Exception e) {
                System.err.println("EXCEPTION loading background music: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void initGameView(int[][] boardMatrix, ViewData brick) {
        displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];

        // Step 1: Maximize board height by computing dynamic tile size from available height
        visibleRows = boardMatrix.length - 2;
        cols = boardMatrix[0].length;
        Scene scene = gamePanel.getScene();
        double vgap = gamePanel.getVgap();
        double hgap = gamePanel.getHgap();
        double sceneHeight = scene.getHeight();
        double availableHeight = (root != null && root.getHeight() > 0) ? root.getHeight() : sceneHeight;
        if (availableHeight > (2 * BOARD_BORDER_WIDTH)) {
            availableHeight -= (2 * BOARD_BORDER_WIDTH);
        }
        tileSize = Math.floor((availableHeight - vgap * (visibleRows - 1)) / visibleRows);
        if (tileSize < 1) {
            tileSize = BRICK_SIZE;
        }

        // Step 2: Size the board to match columns and center the whole board
        double boardWidth = cols * tileSize + (cols - 1) * hgap;
        double sceneWidth = scene.getWidth();
        double availableWidth = (root != null && root.getWidth() > 0) ? root.getWidth() : sceneWidth;
        if (availableWidth > (2 * BOARD_BORDER_WIDTH)) {
            availableWidth -= (2 * BOARD_BORDER_WIDTH);
        }
        double leftX = Math.max(0, (availableWidth - boardWidth) / 2);
        gameBoard.setPrefWidth(boardWidth);
        double boardHeight = visibleRows * tileSize + (visibleRows - 1) * vgap;
        gameBoard.setMinHeight(boardHeight + (2 * BOARD_BORDER_WIDTH));
        gameBoard.setPrefHeight(boardHeight + (2 * BOARD_BORDER_WIDTH));
        gameBoard.setLayoutX(leftX);
        gameBoard.setLayoutY(0);
        // The grid inside the board should not be translated
        gamePanel.setTranslateX(0);
        gamePanel.setTranslateY(0);

        for (int i = 2; i < boardMatrix.length; i++) {
            for (int j = 0; j < boardMatrix[i].length; j++) {
                Rectangle rectangle = new Rectangle(tileSize, tileSize);
                rectangle.setFill(Color.TRANSPARENT);
                displayMatrix[i][j] = rectangle;
                gamePanel.add(rectangle, j, i - 2);
            }
        }

        brickPanel.getChildren().clear();
        ghostPanel.getChildren().clear();
        // Hide active/ghost bricks until the player starts the game with Enter
        brickPanel.setVisible(hasStarted);
        ghostPanel.setVisible(false);

        rectangles = new Rectangle[brick.getBrickData().length][brick.getBrickData()[0].length];
        ghostRectangles = new Rectangle[brick.getBrickData().length][brick.getBrickData()[0].length];
        boolean showGhost = shouldDisplayGhost(brick);
        updateGhostVisibility(showGhost);
        for (int i = 0; i < brick.getBrickData().length; i++) {
            for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                Rectangle activeRectangle = new Rectangle(tileSize, tileSize);
                activeRectangle.setFill(getFillColorWithTexture(brick.getBrickData()[i][j], brick.getTextureType(), i, j));
                rectangles[i][j] = activeRectangle;
                brickPanel.add(activeRectangle, j, i);

                Rectangle ghostRectangle = new Rectangle(tileSize, tileSize);
                configureGhostRectangle(ghostRectangle);
                setGhostRectangleData(brick.getBrickData()[i][j], ghostRectangle, showGhost);
                ghostRectangles[i][j] = ghostRectangle;
                ghostPanel.add(ghostRectangle, j, i);
            }
        }
        double hgap2 = gamePanel.getHgap();
        double vgap2 = gamePanel.getVgap();
        updateActiveBrickPosition(brick, hgap2, vgap2);
        if (showGhost) {
            updateGhostBrickPosition(brick, hgap2, vgap2);
        }

        initNextBricksPanel();
        refreshNextBricks(brick.getNextBricksData(), brick.getNextBricksTextureTypes());

        // Stop any existing timeline before creating a new one to prevent multiple timelines
        if (timeLine != null) {
            timeLine.stop();
        }
        
        timeLine = new Timeline(new KeyFrame(
                Duration.millis(BASE_FALL_MILLIS),
                ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
        ));
        timeLine.setCycleCount(Timeline.INDEFINITE);
        // Apply initial fall speed based on current score (likely 0 at startup)
        updateFallSpeed(0);
        
        // Only play timeline if game is not over and not paused
        if (isGameOver.getValue() == Boolean.FALSE && isPause.getValue() == Boolean.FALSE) {
            timeLine.play();
        }

        // Initialize control UI once timeline exists
        initControls();
        initRestartButton();
        initExitButton();
    }

    private Paint getFillColor(int i) {
        // 0 means empty cell
        if (i == 0) {
            return Color.TRANSPARENT;
        }
        // Fallback to existing solid colors when texture is missing
        switch (i) {
            case 1: return Color.AQUA;
            case 2: return Color.BLUEVIOLET;
            case 3: return Color.DARKGREEN;
            case 4: return Color.YELLOW;
            case 5: return Color.RED;
            case 6: return Color.BEIGE;
            case 7: return Color.BURLYWOOD;
            default: return Color.WHITE;
        }
    }

    private Paint getFillColorWithTexture(int colorValue, int textureType, int row, int col) {
        // 0 means empty cell
        if (colorValue == 0) {
            return Color.TRANSPARENT;
        }
        
        // Apply texture based on texture type
        if (textureType == 0) {
            // Wood texture
            if (woodPattern != null) {
                return woodPattern;
            }
        } else if (textureType == 1) {
            // Stone texture
            if (stonePattern != null) {
                return stonePattern;
            }
        } else if (textureType == 2) {
            // Mixed texture - randomly assign per cell based on position
            boolean useStone = ((row + col) % 2 == 0);
            if (useStone && stonePattern != null) {
                return stonePattern;
            } else if (woodPattern != null) {
                return woodPattern;
            }
        }
        
        // Fallback to solid colors if textures not available
        return getFillColor(colorValue);
    }


    private void refreshBrick(ViewData brick) {
        if (isPause.getValue() == Boolean.FALSE && isGameOver.getValue() == Boolean.FALSE && brick != null) {
            // Step 3: Keep falling brick aligned with board after resizing/centering
            double hgap = gamePanel.getHgap();
            double vgap = gamePanel.getVgap();
            boolean showGhost = shouldDisplayGhost(brick);
            updateGhostVisibility(showGhost);
            updateActiveBrickPosition(brick, hgap, vgap);
            if (showGhost) {
                updateGhostBrickPosition(brick, hgap, vgap);
            }
            if (rectangles != null && brick.getBrickData() != null) {
                for (int i = 0; i < brick.getBrickData().length && i < rectangles.length; i++) {
                    for (int j = 0; j < brick.getBrickData()[i].length && j < rectangles[i].length; j++) {
                        rectangles[i][j].setFill(getFillColorWithTexture(brick.getBrickData()[i][j], brick.getTextureType(), i, j));
                        rectangles[i][j].setArcHeight(9);
                        rectangles[i][j].setArcWidth(9);
                        setGhostRectangleData(brick.getBrickData()[i][j], ghostRectangles[i][j], showGhost);
                    }
                }
            }
            refreshNextBricks(brick.getNextBricksData(), brick.getNextBricksTextureTypes());
        }
    }

    public void refreshGameBackground(int[][] board) {
        refreshGameBackground(board, null);
    }

    public void refreshGameBackground(int[][] board, int[][] textureMatrix) {
        // Safety check: ensure displayMatrix is initialized before trying to update it
        if (displayMatrix == null || board == null) {
            return;
        }
        for (int i = 2; i < board.length && i < displayMatrix.length; i++) {
            for (int j = 0; j < board[i].length && j < displayMatrix[i].length; j++) {
                if (displayMatrix[i][j] != null) {
                    int textureType = (textureMatrix != null && i < textureMatrix.length && j < textureMatrix[i].length) 
                        ? textureMatrix[i][j] : 0; // Default to wood (0)
                    displayMatrix[i][j].setFill(getFillColorWithTexture(board[i][j], textureType, i, j));
                    displayMatrix[i][j].setArcHeight(9);
                    displayMatrix[i][j].setArcWidth(9);
                }
            }
        }
    }

    private void setRectangleData(int color, Rectangle rectangle) {
        rectangle.setFill(getFillColor(color));
        rectangle.setArcHeight(9);
        rectangle.setArcWidth(9);
    }

    private void setGhostRectangleData(int color, Rectangle rectangle, boolean showGhost) {
        rectangle.setOpacity((showGhost && color != 0) ? 1 : 0);
    }

    private void configureGhostRectangle(Rectangle rectangle) {
        rectangle.setFill(Color.TRANSPARENT);
        rectangle.setStroke(Color.WHITE);
        rectangle.setStrokeWidth(1.5);
        rectangle.getStrokeDashArray().setAll(6.0, 6.0);
        rectangle.setArcHeight(9);
        rectangle.setArcWidth(9);
        rectangle.setOpacity(0);
        rectangle.setMouseTransparent(true);
    }

    private void updateActiveBrickPosition(ViewData brick, double hgap, double vgap) {
        brickPanel.setTranslateX(brick.getxPosition() * (hgap + tileSize));
        brickPanel.setTranslateY((brick.getyPosition() - HIDDEN_ROWS) * (vgap + tileSize));
    }

    private void updateGhostBrickPosition(ViewData brick, double hgap, double vgap) {
        ghostPanel.setTranslateX(brick.getGhostXPosition() * (hgap + tileSize));
        ghostPanel.setTranslateY((brick.getGhostYPosition() - HIDDEN_ROWS) * (vgap + tileSize));
    }

    private void moveDown(MoveEvent event) {
        // Check game state before processing - if game is over or paused, don't process
        // Also double-check that timeline should still be running
        if (isPause.getValue() == Boolean.FALSE && isGameOver.getValue() == Boolean.FALSE && eventListener != null && timeLine != null) {
            try {
                DownData downData = eventListener.onDownEvent(event);
                // Check again after onDownEvent in case game over was triggered during processing
                if (isGameOver.getValue() == Boolean.FALSE && downData != null) {
                    if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                        NotificationPanel notificationPanel = new NotificationPanel("+" + downData.getClearRow().getScoreBonus());
                        groupNotification.getChildren().add(notificationPanel);
                        notificationPanel.showScore(groupNotification.getChildren());
                    }
                    if (downData.getViewData() != null) {
                        refreshBrick(downData.getViewData());
                    }
                } else if (isGameOver.getValue() == Boolean.TRUE) {
                    // Game over was triggered, ensure timeline is stopped
                    if (timeLine != null) {
                        timeLine.stop();
                    }
                }
            } catch (Exception e) {
                // If any error occurs, stop the timeline to prevent further issues
                System.err.println("Error in moveDown: " + e.getMessage());
                e.printStackTrace();
                if (timeLine != null) {
                    timeLine.stop();
                }
            }
        } else if (isGameOver.getValue() == Boolean.TRUE && timeLine != null) {
            // Game is over, ensure timeline is stopped
            timeLine.stop();
        }
        gamePanel.requestFocus();
    }
    
    private void hardDrop(MoveEvent event) {
        // Rapidly drop the current brick to the bottom (Space bar)
        if (isPause.getValue() == Boolean.FALSE && isGameOver.getValue() == Boolean.FALSE && eventListener != null && timeLine != null) {
            try {
                DownData downData = eventListener.onHardDrop(event);
                if (isGameOver.getValue() == Boolean.FALSE && downData != null) {
                    if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                        NotificationPanel notificationPanel = new NotificationPanel("+" + downData.getClearRow().getScoreBonus());
                        groupNotification.getChildren().add(notificationPanel);
                        notificationPanel.showScore(groupNotification.getChildren());
                    }
                    if (downData.getViewData() != null) {
                        refreshBrick(downData.getViewData());
                    }
                } else if (isGameOver.getValue() == Boolean.TRUE) {
                    // Game is over, ensure timeline is stopped
                    if (timeLine != null) {
                        timeLine.stop();
                    }
                }
            } catch (Exception e) {
                // If any error occurs, stop the timeline to prevent further issues
                System.err.println("Error in hardDrop: " + e.getMessage());
                e.printStackTrace();
                if (timeLine != null) {
                    timeLine.stop();
                }
            }
        } else if (isGameOver.getValue() == Boolean.TRUE && timeLine != null) {
            // Game is over, ensure timeline is stopped
            timeLine.stop();
        }
        gamePanel.requestFocus();
    }

    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void bindScore(IntegerProperty integerProperty) {
        // Create a label, bind it to the live score, and place it inside the right-side score box
        javafx.scene.control.Label scoreLabel = new javafx.scene.control.Label();
        scoreLabel.getStyleClass().add("scoreLabel");
        scoreLabel.textProperty().bind(integerProperty.asString("Score: %d"));
        scoreLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        // Swap background images when the player crosses score milestones
        integerProperty.addListener((obs, oldVal, newVal) -> updateGameBoardBackgroundForScore(newVal.intValue()));
        updateGameBoardBackgroundForScore(integerProperty.get());
        // Adjust fall speed as score increases
        integerProperty.addListener((obs, oldVal, newVal) -> updateFallSpeed(newVal.intValue()));
        updateFallSpeed(integerProperty.get());
        if (scoreBox != null) {
            scoreBox.getChildren().clear();
            // Wrap the score label in its own framed container so the dark brown frame is visible
            javafx.scene.layout.StackPane scoreFrame = new javafx.scene.layout.StackPane();
            scoreFrame.getStyleClass().add("scoreFrame");
            scoreFrame.getChildren().add(scoreLabel);
            // Allow frame to expand so it aligns with control frame
            scoreFrame.setMaxWidth(Double.MAX_VALUE);
            javafx.scene.layout.StackPane.setAlignment(scoreLabel, javafx.geometry.Pos.CENTER);
            scoreBox.getChildren().add(scoreFrame);
            scoreFrameRef = scoreFrame;
            // If control frame already exists, ensure widths match via parent fillWidth
            if (controlFrameRef != null) {
                controlFrameRef.setMaxWidth(Double.MAX_VALUE);
            }
            if (restartFrameRef != null) {
                restartFrameRef.setMaxWidth(Double.MAX_VALUE);
            }
            if (exitFrameRef != null) {
                exitFrameRef.setMaxWidth(Double.MAX_VALUE);
            }
            // ScoreBox now lives outside the gameBoard; do not attach it to the BorderPane
        }
    }

    private void initControls() {
        if (controlBox == null) return;
        controlBox.getChildren().clear();
        // Create Stop/Resume toggle button with same font style as score
        javafx.scene.control.Button toggleButton = new javafx.scene.control.Button("Stop");
        pauseToggleButton = toggleButton;
        toggleButton.getStyleClass().add("scoreLabel");
        toggleButton.setTextFill(javafx.scene.paint.Color.WHITE);
        // Wrap in framed container to match score box look
        javafx.scene.layout.StackPane controlFrame = new javafx.scene.layout.StackPane();
        controlFrame.getStyleClass().add("scoreFrame");
        controlFrame.getChildren().add(toggleButton);
        controlFrame.setMaxWidth(Double.MAX_VALUE);
        javafx.scene.layout.StackPane.setAlignment(toggleButton, javafx.geometry.Pos.CENTER);
        controlBox.getChildren().add(controlFrame);
        controlFrameRef = controlFrame;
        if (nextFrameRef != null) {
            nextFrameRef.setMaxWidth(Double.MAX_VALUE);
        }
        if (restartFrameRef != null) {
            restartFrameRef.setMaxWidth(Double.MAX_VALUE);
        }
        if (exitFrameRef != null) {
            exitFrameRef.setMaxWidth(Double.MAX_VALUE);
        }

        toggleButton.setOnAction(e -> {
            togglePauseResume();
        });
    }

    private void startGame() {
        hasStarted = true;
        isPause.set(false);
        if (pauseToggleButton != null) {
            pauseToggleButton.setText("Stop");
        }
        if (restartButtonRef != null) {
            restartButtonRef.setText("Restart");
        }
        if (brickPanel != null) {
            brickPanel.setVisible(true);
        }
        if (ghostPanel != null) {
            ghostPanel.setVisible(eventListener != null && shouldDisplayGhost(eventListener.getCurrentViewData()));
        }
        // Refresh the current brick to ensure it appears when the game starts
        if (eventListener != null) {
            ViewData currentView = eventListener.getCurrentViewData();
            if (currentView != null) {
                refreshBrick(currentView);
                refreshNextBricks(currentView.getNextBricksData(), currentView.getNextBricksTextureTypes());
            }
        }
        if (timeLine != null) {
            timeLine.play();
        }
        gamePanel.requestFocus();
    }

    // Increase fall speed when score crosses thresholds (500, 1000, 1500, 2000, 2500, 3000, 3500)
    private void updateFallSpeed(int score) {
        if (timeLine == null) {
            return;
        }
        double targetDuration;
        if (score >= 3500) {
            targetDuration = 152.0;
        } else if (score >= 3000) {
            targetDuration = 216.0;
        } else if (score >= 2500) {
            targetDuration = 280.0;
        } else if (score >= 2000) {
            targetDuration = 344.0;
        } else if (score >= 1500) {
            targetDuration = 408.0;
        } else if (score >= 1000) {
            targetDuration = 472.0;
        } else if (score >= 500) {
            targetDuration = 536.0;
        } else {
            targetDuration = BASE_FALL_MILLIS;
        }
        // Adjust rate relative to the base duration
        double rate = BASE_FALL_MILLIS / targetDuration;
        timeLine.setRate(rate);
    }

    // Toggle pause/resume logic shared by keyboard (Ctrl) and the UI button
    private void togglePauseResume() {
        boolean paused = isPause.get();
        if (!paused) {
            if (timeLine != null) timeLine.stop();
            isPause.set(true);
            if (pauseToggleButton != null) {
                pauseToggleButton.setText("Resume");
            }
        } else {
            if (timeLine != null && isGameOver.get() == Boolean.FALSE) timeLine.play();
            isPause.set(false);
            if (!hasStarted) {
                hasStarted = true;
                if (restartButtonRef != null) {
                    restartButtonRef.setText("Restart");
                }
            }
            if (pauseToggleButton != null) {
                pauseToggleButton.setText("Stop");
            }
        }
        gamePanel.requestFocus();
    }

    /**
     * Switch the board background image based on score milestones (0, 1000, 2000+).
     * Images live under src/main/resources/images.
     */
    private void updateGameBoardBackgroundForScore(int score) {
        int nextStage;
        String imagePath;
        if (score >= 1000) {
            nextStage = 2;
            imagePath = "images/dusk.jpg";
        } else if (score >= 500) {
            nextStage = 1;
            imagePath = "images/sky.jpg";
        } else {
            nextStage = 0;
            imagePath = "images/morning.jpg";
        }

        if (nextStage == backgroundStage || gameBoard == null) {
            return;
        }
        backgroundStage = nextStage;

        try {
            URL imageUrl = getClass().getClassLoader().getResource(imagePath);
            if (imageUrl == null) {
                System.err.println("Background image not found: " + imagePath);
                return;
            }
            final String style = String.format(
                    "-fx-background-image: url('%s');"
                    + " -fx-background-repeat: no-repeat;"
                    + " -fx-background-position: center center;"
                    + " -fx-background-size: cover;",
                    imageUrl.toExternalForm());
            Platform.runLater(() -> gameBoard.setStyle(style));
        } catch (Exception e) {
            System.err.println("Failed to apply background image: " + imagePath);
            e.printStackTrace();
        }
    }

    private void initRestartButton() {
        if (restartBox == null) return;
        restartBox.getChildren().clear();
        // Create Start/Restart button with same font style as score and control buttons
        javafx.scene.control.Button restartButton = new javafx.scene.control.Button(hasStarted ? "Restart" : "Start");
        restartButtonRef = restartButton;
        restartButton.getStyleClass().add("scoreLabel");
        restartButton.setTextFill(javafx.scene.paint.Color.WHITE);
        // Wrap in framed container to match score box look
        javafx.scene.layout.StackPane restartFrame = new javafx.scene.layout.StackPane();
        restartFrame.getStyleClass().add("scoreFrame");
        restartFrame.getChildren().add(restartButton);
        restartFrame.setMaxWidth(Double.MAX_VALUE);
        javafx.scene.layout.StackPane.setAlignment(restartButton, javafx.geometry.Pos.CENTER);
        restartBox.getChildren().add(restartFrame);
        restartFrameRef = restartFrame;
        if (scoreFrameRef != null) {
            scoreFrameRef.setMaxWidth(Double.MAX_VALUE);
        }
        if (controlFrameRef != null) {
            controlFrameRef.setMaxWidth(Double.MAX_VALUE);
        }

        restartButton.setOnAction(e -> {
            if (!hasStarted) {
                startGame();
            } else {
                newGame(null);
            }
            gamePanel.requestFocus();
        });
    }

    private void initExitButton() {
        if (exitBox == null) return;
        exitBox.getChildren().clear();
        // Create Exit button with same font style as score and control buttons
        javafx.scene.control.Button exitButton = new javafx.scene.control.Button("Exit Game");
        exitButton.getStyleClass().add("scoreLabel");
        exitButton.setTextFill(javafx.scene.paint.Color.WHITE);
        // Wrap in framed container to match score box look
        javafx.scene.layout.StackPane exitFrame = new javafx.scene.layout.StackPane();
        exitFrame.getStyleClass().add("scoreFrame");
        exitFrame.getChildren().add(exitButton);
        exitFrame.setMaxWidth(Double.MAX_VALUE);
        javafx.scene.layout.StackPane.setAlignment(exitButton, javafx.geometry.Pos.CENTER);
        exitBox.getChildren().add(exitFrame);
        exitFrameRef = exitFrame;
        if (scoreFrameRef != null) {
            scoreFrameRef.setMaxWidth(Double.MAX_VALUE);
        }
        if (controlFrameRef != null) {
            controlFrameRef.setMaxWidth(Double.MAX_VALUE);
        }
        if (restartFrameRef != null) {
            restartFrameRef.setMaxWidth(Double.MAX_VALUE);
        }

        exitButton.setOnAction(e -> {
            exitGame();
        });
    }

    public void gameOver() {
        // Stop timeline immediately to prevent further moveDown events
        if (timeLine != null) {
            timeLine.stop();
        }
        // Set game over flag first to prevent any pending events from processing
        isGameOver.setValue(Boolean.TRUE);
        // Show game over panel
        gameOverPanel.setVisible(true);
    }

    public void newGame(ActionEvent actionEvent) {
        // Stop timeline first and ensure it's completely stopped
        // This is critical to prevent race conditions
        if (timeLine != null) {
            timeLine.stop();
        }
        
        // Reset game state flags before creating new game
        // Order is important: set flags first to prevent any processing during reset
        isPause.setValue(Boolean.FALSE);
        isGameOver.setValue(Boolean.FALSE);
        gameOverPanel.setVisible(false);
        hasStarted = true;
        if (restartButtonRef != null) {
            restartButtonRef.setText("Restart");
        }
        
        // Create new game - this will reset the board and create a new brick
        if (eventListener != null) {
            try {
                eventListener.createNewGame();
                
                // Get the current view data and ensure everything is properly refreshed
                ViewData currentView = eventListener.getCurrentViewData();
                if (currentView != null) {
                    // Refresh the brick view with the new game state
                    refreshBrick(currentView);
                }
                
                // Double-check that game state is still valid after creating new game
                // This prevents starting timeline if something went wrong
                if (isGameOver.getValue() == Boolean.TRUE) {
                    // Something went wrong, don't start timeline
                    return;
                }
            } catch (Exception e) {
                // If error occurs during game creation, don't start timeline
                System.err.println("Error creating new game: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }
        
        // Only restart timeline if game is not over, not paused, and we have a valid timeline
        // Use a small delay to ensure all state updates are complete
        if (timeLine != null && isGameOver.getValue() == Boolean.FALSE && isPause.getValue() == Boolean.FALSE) {
            // Use Platform.runLater with a small delay to ensure view updates are complete
            Platform.runLater(() -> {
                // Double-check conditions before starting timeline
                if (timeLine != null && isGameOver.getValue() == Boolean.FALSE && isPause.getValue() == Boolean.FALSE && eventListener != null) {
                    timeLine.play();
                }
            });
        }
        gamePanel.requestFocus();
    }

    private void exitGame() {
        // Stop timeline and media players before exiting
        if (timeLine != null) {
            timeLine.stop();
        }
        if (backgroundMediaPlayer != null) {
            backgroundMediaPlayer.stop();
            backgroundMediaPlayer.dispose();
        }
        // Stop and dispose background music
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
            backgroundMusicPlayer.dispose();
        }
        // Get the stage and close it
        if (exitBox != null && !exitBox.getChildren().isEmpty()) {
            javafx.scene.Node node = exitBox.getChildren().get(0);
            javafx.scene.Scene scene = node.getScene();
            if (scene != null) {
                javafx.stage.Window window = scene.getWindow();
                if (window instanceof javafx.stage.Stage) {
                    ((javafx.stage.Stage) window).close();
                    return;
                }
            }
        }
        // Fallback: use Platform.exit() if stage is not available
        Platform.exit();
    }

    public void pauseGame(ActionEvent actionEvent) {
        gamePanel.requestFocus();
    }

    private void initNextBricksPanel() {
        if (nextBox == null) {
            return;
        }
        nextBox.getChildren().clear();
        javafx.scene.layout.StackPane previewFrame = new javafx.scene.layout.StackPane();
        previewFrame.getStyleClass().add("scoreFrame");
        previewFrame.setMaxWidth(Double.MAX_VALUE);
        previewFrame.setMinHeight(tileSize * 6);
        javafx.scene.layout.VBox container = new javafx.scene.layout.VBox(6);
        container.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        container.setPadding(new javafx.geometry.Insets(6, 0, 6, 0));
        previewFrame.getChildren().add(container);
        nextBox.getChildren().add(previewFrame);
        nextFrameRef = previewFrame;

        if (scoreFrameRef != null) {
            scoreFrameRef.setMaxWidth(Double.MAX_VALUE);
        }
        if (controlFrameRef != null) {
            controlFrameRef.setMaxWidth(Double.MAX_VALUE);
        }
        if (restartFrameRef != null) {
            restartFrameRef.setMaxWidth(Double.MAX_VALUE);
        }
        if (exitFrameRef != null) {
            exitFrameRef.setMaxWidth(Double.MAX_VALUE);
        }

        nextBricksRectangles.clear();
        double previewTileSize = Math.max(10, tileSize * 0.75);
        for (int index = 0; index < 3; index++) {
            GridPane gridPane = new GridPane();
            gridPane.setHgap(1);
            gridPane.setVgap(1);
            gridPane.setAlignment(javafx.geometry.Pos.CENTER);
            Rectangle[][] previewCells = new Rectangle[NEXT_PREVIEW_GRID_SIZE][NEXT_PREVIEW_GRID_SIZE];
            for (int row = 0; row < NEXT_PREVIEW_GRID_SIZE; row++) {
                for (int col = 0; col < NEXT_PREVIEW_GRID_SIZE; col++) {
                    Rectangle rectangle = new Rectangle(previewTileSize, previewTileSize);
                    setRectangleData(0, rectangle);
                    previewCells[row][col] = rectangle;
                    gridPane.add(rectangle, col, row);
                }
            }
            nextBricksRectangles.add(previewCells);
            container.getChildren().add(gridPane);
        }
    }

    private void refreshNextBricks(List<int[][]> nextBricksData, List<Integer> textureTypes) {
        if (nextBox == null) {
            return;
        }
        if (nextBricksRectangles.isEmpty()) {
            initNextBricksPanel();
        }
        for (int index = 0; index < nextBricksRectangles.size(); index++) {
            Rectangle[][] previewCells = nextBricksRectangles.get(index);
            int[][] data = (nextBricksData != null && index < nextBricksData.size()) ? nextBricksData.get(index) : null;
            int textureType = (textureTypes != null && index < textureTypes.size()) ? textureTypes.get(index) : 0;
            for (int row = 0; row < NEXT_PREVIEW_GRID_SIZE; row++) {
                for (int col = 0; col < NEXT_PREVIEW_GRID_SIZE; col++) {
                    int color = 0;
                    if (hasStarted && data != null && row < data.length && col < data[row].length) {
                        color = data[row][col];
                    }
                    previewCells[row][col].setFill(getFillColorWithTexture(color, textureType, row, col));
                    previewCells[row][col].setArcHeight(9);
                    previewCells[row][col].setArcWidth(9);
                }
            }
        }
    }

    private boolean shouldDisplayGhost(ViewData brick) {
        return brick.getGhostYPosition() > brick.getyPosition();
    }

    private void updateGhostVisibility(boolean showGhost) {
        if (ghostPanel != null) {
            ghostPanel.setVisible(showGhost && hasStarted);
        }
    }
}
