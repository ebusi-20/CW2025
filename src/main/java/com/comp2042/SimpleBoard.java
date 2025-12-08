package com.comp2042;

import com.comp2042.logic.bricks.Brick;
import com.comp2042.logic.bricks.BrickGenerator;
import com.comp2042.logic.bricks.RandomBrickGenerator;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleBoard implements Board {

    private final int width;
    private final int height;
    private final BrickGenerator brickGenerator;
    private final BrickRotator brickRotator;
    private int[][] currentGameMatrix;
    private int[][] textureMatrix; // Stores texture type per cell: 0=wood, 1=stone, 2=mixed
    private Point currentOffset;
    private final Score score;
    private int currentBrickTextureType; // 0=wood, 1=stone, 2=mixed
    private List<Integer> nextBricksTextureTypes; // Store texture types for next 3 bricks (consistent until new brick is created)

    public SimpleBoard(int width, int height) {
        this.width = width;
        this.height = height;
        currentGameMatrix = new int[width][height];
        textureMatrix = new int[width][height]; // Initialize texture matrix
        brickGenerator = new RandomBrickGenerator();
        brickRotator = new BrickRotator();
        score = new Score();
    }

    @Override
    public boolean moveBrickDown() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        Point p = new Point(currentOffset);
        p.translate(0, 1);
        boolean conflict = MatrixOperations.intersect(currentMatrix, brickRotator.getCurrentShape(), (int) p.getX(), (int) p.getY());
        if (conflict) {
            return false;
        } else {
            currentOffset = p;
            return true;
        }
    }


    @Override
    public boolean moveBrickLeft() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        Point p = new Point(currentOffset);
        p.translate(-1, 0);
        boolean conflict = MatrixOperations.intersect(currentMatrix, brickRotator.getCurrentShape(), (int) p.getX(), (int) p.getY());
        if (conflict) {
            return false;
        } else {
            currentOffset = p;
            return true;
        }
    }

    @Override
    public boolean moveBrickRight() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        Point p = new Point(currentOffset);
        p.translate(1, 0);
        boolean conflict = MatrixOperations.intersect(currentMatrix, brickRotator.getCurrentShape(), (int) p.getX(), (int) p.getY());
        if (conflict) {
            return false;
        } else {
            currentOffset = p;
            return true;
        }
    }

    @Override
    public boolean rotateLeftBrick() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        NextShapeInfo nextShape = brickRotator.getNextShape();
        boolean conflict = MatrixOperations.intersect(currentMatrix, nextShape.getShape(), (int) currentOffset.getX(), (int) currentOffset.getY());
        if (conflict) {
            return false;
        } else {
            brickRotator.setCurrentShape(nextShape.getPosition());
            return true;
        }
    }

    @Override
    public boolean createNewBrick() {
        Brick currentBrick = brickGenerator.getBrick();
        brickRotator.setBrick(currentBrick);
        
        // Use the texture type that was already assigned to this brick in the preview
        // (it was the first brick in the nextBricksTextureTypes list)
        if (nextBricksTextureTypes != null && !nextBricksTextureTypes.isEmpty()) {
            // Use the texture type that was assigned to the first next brick (which is now current)
            currentBrickTextureType = nextBricksTextureTypes.remove(0);
        } else {
            // Fallback: randomly assign if no preview texture types exist (first brick or after restart)
            currentBrickTextureType = (int)(Math.random() * 3);
        }
        
        // Always generate and store texture types for the NEXT 3 bricks (the ones that will come after this one)
        // These will be shown in the preview and used when those bricks become current
        List<Brick> nextBrickObjects = brickGenerator.peekNextBricks(3);
        nextBricksTextureTypes = new ArrayList<>();
        for (int i = 0; i < nextBrickObjects.size() && i < 3; i++) {
            nextBricksTextureTypes.add((int)(Math.random() * 3));
        }
        // Ensure we have exactly 3 texture types
        while (nextBricksTextureTypes.size() < 3) {
            nextBricksTextureTypes.add((int)(Math.random() * 3));
        }
        // Spawn at the top visible row (accounting for 2 hidden rows)
        currentOffset = new Point(4, 2);
        return MatrixOperations.intersect(currentGameMatrix, brickRotator.getCurrentShape(), (int) currentOffset.getX(), (int) currentOffset.getY());
    }

    @Override
    public int[][] getBoardMatrix() {
        return currentGameMatrix;
    }

    public int[][] getTextureMatrix() {
        return textureMatrix;
    }

    @Override
    public ViewData getViewData() {
        // Get next 3 bricks
        List<Brick> nextBrickObjects = brickGenerator.peekNextBricks(3);
        List<int[][]> nextBricks = nextBrickObjects.stream()
                .map(brick -> brick.getShapeMatrix().get(0))
                .collect(Collectors.toList());
        // Use stored texture types (generated in createNewBrick, consistent until next brick is created)
        if (nextBricksTextureTypes == null || nextBricksTextureTypes.size() != nextBricks.size()) {
            // Fallback: generate if not initialized
            nextBricksTextureTypes = new ArrayList<>();
            for (int i = 0; i < nextBrickObjects.size(); i++) {
                nextBricksTextureTypes.add((int)(Math.random() * 3));
            }
        }
        Point ghostPosition = calculateGhostPosition();
        return new ViewData(
                brickRotator.getCurrentShape(),
                (int) currentOffset.getX(),
                (int) currentOffset.getY(),
                nextBricks,
                nextBricksTextureTypes,
                (int) ghostPosition.getX(),
                (int) ghostPosition.getY(),
                currentBrickTextureType);
    }

    @Override
    public void mergeBrickToBackground() {
        currentGameMatrix = MatrixOperations.merge(currentGameMatrix, brickRotator.getCurrentShape(), (int) currentOffset.getX(), (int) currentOffset.getY());
        // Also merge texture information
        textureMatrix = MatrixOperations.mergeTexture(textureMatrix, brickRotator.getCurrentShape(), (int) currentOffset.getX(), (int) currentOffset.getY(), currentBrickTextureType);
    }

    @Override
    public ClearRow clearRows() {
        ClearRow clearRow = MatrixOperations.checkRemoving(currentGameMatrix);
        currentGameMatrix = clearRow.getNewMatrix();
        // Also clear texture matrix rows
        textureMatrix = MatrixOperations.checkRemoving(textureMatrix).getNewMatrix();
        return clearRow;

    }

    @Override
    public Score getScore() {
        return score;
    }


    @Override
    public void newGame() {
        currentGameMatrix = new int[width][height];
        textureMatrix = new int[width][height];
        score.reset();
        // Reset texture types to ensure clean state
        nextBricksTextureTypes = null;
        // Create new brick (this will initialize texture types)
        createNewBrick();
    }

    private Point calculateGhostPosition() {
        Point ghost = new Point(currentOffset);
        int[][] background = MatrixOperations.copy(currentGameMatrix);
        while (!MatrixOperations.intersect(background, brickRotator.getCurrentShape(),
                (int) ghost.getX(), (int) ghost.getY() + 1)) {
            ghost.translate(0, 1);
        }
        return ghost;
    }
}
