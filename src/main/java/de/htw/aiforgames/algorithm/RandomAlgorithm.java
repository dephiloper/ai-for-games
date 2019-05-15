package de.htw.aiforgames.algorithm;

import de.htw.aiforgames.Configurations;
import de.htw.aiforgames.GameState;

import java.util.Random;

public class RandomAlgorithm implements DecisionAlgorithm {
    private Random randomGenerator;

    public RandomAlgorithm() {
        this(System.currentTimeMillis());
    }

    public RandomAlgorithm(long seed) {
        randomGenerator = new Random(seed);
    }

    @Override
    public long getNextMove(GameState state) {
        long nextPossibleMoves = state.getNextPossibleMoves();

        int numMoves = Configurations.getNumTokens(nextPossibleMoves);
        if (numMoves == 0) {
            return GameState.INVALID_MOVE;
        }
        int moveIndex = randomGenerator.nextInt(numMoves);

        int index = 0;
        for (long move : new Configurations.TokenPositions(nextPossibleMoves)) {
            if (index == moveIndex) {
                return move;
            }
        }

        return GameState.INVALID_MOVE;
    }
}
