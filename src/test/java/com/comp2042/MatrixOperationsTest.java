package com.comp2042;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatrixOperationsTest {

    @Test
    void intersectHandlesNegativeOffsetGracefully() {
        int[][] board = new int[2][2];
        int[][] brick = new int[][]{{1}};

        assertTrue(MatrixOperations.intersect(board, brick, 0, -1), "Out-of-bounds bricks should be treated as conflicts, not crashes");
    }

    @Test
    void mergeSupportsRectangularBricks() {
        int[][] board = new int[4][4];
        int[][] brick = new int[][]{
                {1, 2, 0},
                {0, 3, 4}
        };

        int[][] merged = MatrixOperations.merge(board, brick, 0, 0);

        assertEquals(1, merged[0][0]);
        assertEquals(2, merged[0][1]);
        assertEquals(3, merged[1][1]);
        assertEquals(4, merged[1][2]);
    }
}
