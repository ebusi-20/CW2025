package com.comp2042;

public class GameController implements InputEventListener {

    private Board board = new SimpleBoard(25, 10);

    private final GuiController viewGuiController;

    public GameController(GuiController c) {
        viewGuiController = c;
        board.createNewBrick();
        viewGuiController.setEventListener(this);
        viewGuiController.initGameView(board.getBoardMatrix(), board.getViewData());
        viewGuiController.bindScore(board.getScore().scoreProperty());
    }

    @Override
    public DownData onDownEvent(MoveEvent event) {
        boolean canMove = board.moveBrickDown();
        ClearRow clearRow = null;
        if (!canMove) {
            board.mergeBrickToBackground();
            clearRow = board.clearRows();
            if (clearRow.getLinesRemoved() > 0) {
                board.getScore().add(clearRow.getScoreBonus());
            }
            if (board.createNewBrick()) {
                viewGuiController.gameOver();
            }

            if (board instanceof SimpleBoard) {
                viewGuiController.refreshGameBackground(board.getBoardMatrix(), ((SimpleBoard) board).getTextureMatrix());
            } else {
                viewGuiController.refreshGameBackground(board.getBoardMatrix());
            }

        }
        return new DownData(clearRow, board.getViewData());
    }

    @Override
    public ViewData onLeftEvent(MoveEvent event) {
        board.moveBrickLeft();
        return board.getViewData();
    }

    @Override
    public ViewData onRightEvent(MoveEvent event) {
        board.moveBrickRight();
        return board.getViewData();
    }

    @Override
    public ViewData onRotateEvent(MoveEvent event) {
        board.rotateLeftBrick();
        return board.getViewData();
    }

    @Override
    public DownData onHardDrop(MoveEvent event) {
        // Move the brick straight down until it collides
        while (board.moveBrickDown()) {
            // keep dropping
        }
        ClearRow clearRow = null;
        // Merge to background and handle clears/new brick same as onDownEvent
        board.mergeBrickToBackground();
        clearRow = board.clearRows();
        if (clearRow.getLinesRemoved() > 0) {
            board.getScore().add(clearRow.getScoreBonus());
        }
        if (board.createNewBrick()) {
            viewGuiController.gameOver();
        }

        if (board instanceof SimpleBoard) {
            viewGuiController.refreshGameBackground(board.getBoardMatrix(), ((SimpleBoard) board).getTextureMatrix());
        } else {
            viewGuiController.refreshGameBackground(board.getBoardMatrix());
        }

        return new DownData(clearRow, board.getViewData());
    }


    @Override
    public void createNewGame() {
        // Reset the board state
        board.newGame();
        // Immediately refresh the game background to ensure view is synchronized
        if (board instanceof SimpleBoard) {
            viewGuiController.refreshGameBackground(board.getBoardMatrix(), ((SimpleBoard) board).getTextureMatrix());
        } else {
            viewGuiController.refreshGameBackground(board.getBoardMatrix());
        }
        // The view data will be refreshed by the caller (GuiController.newGame)
    }
    
    @Override
    public ViewData getCurrentViewData() {
        return board.getViewData();
    }
}
