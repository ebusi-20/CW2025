package com.comp2042;

import java.util.ArrayList;
import java.util.List;

public final class ViewData {

    private final int[][] brickData;
    private final int xPosition;
    private final int yPosition;
    private final List<int[][]> nextBricksData;
    private final List<Integer> nextBricksTextureTypes;
    private final int ghostXPosition;
    private final int ghostYPosition;
    private final int textureType; // 0=wood, 1=stone, 2=mixed

    public ViewData(int[][] brickData, int xPosition, int yPosition, List<int[][]> nextBricksData,
                    List<Integer> nextBricksTextureTypes, int ghostXPosition, int ghostYPosition, int textureType) {
        this.brickData = brickData;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.nextBricksData = nextBricksData == null ? List.of() : MatrixOperations.deepCopyList(nextBricksData);
        this.nextBricksTextureTypes = nextBricksTextureTypes == null ? List.of() : new ArrayList<>(nextBricksTextureTypes);
        this.ghostXPosition = ghostXPosition;
        this.ghostYPosition = ghostYPosition;
        this.textureType = textureType;
    }

    public int[][] getBrickData() {
        return MatrixOperations.copy(brickData);
    }

    public int getxPosition() {
        return xPosition;
    }

    public int getyPosition() {
        return yPosition;
    }

    public List<int[][]> getNextBricksData() {
        return MatrixOperations.deepCopyList(nextBricksData);
    }

    public List<Integer> getNextBricksTextureTypes() {
        return new ArrayList<>(nextBricksTextureTypes);
    }

    public int getGhostXPosition() {
        return ghostXPosition;
    }

    public int getGhostYPosition() {
        return ghostYPosition;
    }

    public int getTextureType() {
        return textureType;
    }
}
