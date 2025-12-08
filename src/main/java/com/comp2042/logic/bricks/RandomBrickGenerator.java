package com.comp2042.logic.bricks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class RandomBrickGenerator implements BrickGenerator {

    private final List<Brick> brickList;

    private final Deque<Brick> nextBricks = new ArrayDeque<>();

    public RandomBrickGenerator() {
        brickList = new ArrayList<>();
        brickList.add(new IBrick());
        brickList.add(new JBrick());
        brickList.add(new LBrick());
        brickList.add(new OBrick());
        brickList.add(new SBrick());
        brickList.add(new TBrick());
        brickList.add(new ZBrick());
        ensureNextBricks(3);
    }

    @Override
    public Brick getBrick() {
        ensureNextBricks(3);
        Brick next = nextBricks.pollFirst();
        ensureNextBricks(3);
        return next;
    }

    @Override
    public Brick getNextBrick() {
        ensureNextBricks(1);
        return nextBricks.peekFirst();
    }

    @Override
    public List<Brick> peekNextBricks(int count) {
        ensureNextBricks(count);
        return nextBricks.stream().limit(count).collect(Collectors.toList());
    }

    private void ensureNextBricks(int count) {
        while (nextBricks.size() < count) {
            nextBricks.addLast(brickList.get(ThreadLocalRandom.current().nextInt(brickList.size())));
        }
    }
}
