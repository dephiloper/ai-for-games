package de.htw.aiforgames;

import de.htw.aiforgames.algorithm.DecisionAlgorithm;

import java.util.ArrayList;
import java.util.Random;

public class DecisionRuleAlgorithm implements DecisionAlgorithm {

    private int initialDepth;
    final private int playerNumber;
    private long selectedMove = 0L;

    public DecisionRuleAlgorithm(int initialDepth, int playerNumber) {
        this.initialDepth = initialDepth;
        this.playerNumber = playerNumber;
    }

    /**
     * Depending on the moves we could take minmax returns the score of the current state.
     * lowerBound  @min: if we get something lower than lowerBound --> break;
     * upperBound @max: if we get something higher than upperBound --> break;
     * @param state The state to rate
     * @param depth The current depth
     * @return A score for the given state
     */
    public float minmax(GameState state, int depth, float lowerBound, float upperBound) {
        var moves = state.getNextPossibleMoves();

        if (depth == 0 || state.isGameOver()) {
            return state.calculateScore(playerNumber);
        }

        var topScore = playerNumber == state.getCurrentPlayer() ? lowerBound : upperBound;

        for (var move : new Configurations.MovePositions(moves)) {
            var newState = state.createStateFromMove(move);

            if (playerNumber == state.getCurrentPlayer()) {
                var score = minmax(newState, depth - 1, topScore, upperBound);
                if (score > topScore) {
                    topScore = score;

                    if (depth == initialDepth) {
                        selectedMove = move;
                        System.out.println(String.format("minmax move: %d", log2(selectedMove)));
                    }

                    if (topScore > upperBound)
                        break;
                }
            } else {
                var score = minmax(newState, depth - 1, lowerBound, topScore);
                if (score < topScore) {
                    topScore = score;

                    if (topScore < lowerBound)
                        break;
                }
            }
        }

        return topScore;
    }

    public static void main(String[] args) {
        Random randomGenerator = new Random();
        var dra = new DecisionRuleAlgorithm(9, 0);

        var state = GameState.newEmptyGameState();

        while (!state.isGameOver()) {
            if (state.getCurrentPlayer() == dra.playerNumber) {
                dra.minmax(state, dra.initialDepth, -Float.MAX_VALUE, Float.MAX_VALUE);
                System.out.println(String.format("player: %d", state.getCurrentPlayer()));
                System.out.println("-----------------------------");
                System.out.println(String.format("this is the move we take: %d", log2(dra.selectedMove)));
                state = state.checkedCreateStateFromMove(dra.selectedMove);
                System.out.println(state);
            } else {
                var moves = new ArrayList<Long>();
                for (var move : new Configurations.TokenPositions(state.getNextPossibleMoves()))
                    moves.add(move);

                System.out.println(String.format("player %s", state.getCurrentPlayer()));
                if (moves.size() == 0) {
                    System.out.println(String.format("selected move: %d", 0L));
                    state = state.checkedCreateStateFromMove(0L);
                } else {
                    var index = randomGenerator.nextInt(moves.size());
                    var move = moves.get(index);
                    System.out.println(String.format("selected move: %d", log2(move)));
                    state = state.checkedCreateStateFromMove(move);
                }
                System.out.println(state);
            }
        }
    }

    private static int log2(long number) {
        return (int) (Math.log(number) / Math.log(2));
    }

    public long getNextMove(GameState state) {
        minmax(state, initialDepth, -Float.MAX_VALUE, Float.MAX_VALUE);

        return selectedMove;
    }
}
