package de.htw.aiforgames.algorithm;

import de.htw.aiforgames.Configurations;
import de.htw.aiforgames.GameState;

import java.util.ArrayList;
import java.util.Random;

public class DecisionRuleAlgorithm implements DecisionAlgorithm {
    public static final int DEFAULT_DEPTH = 9;

    private int initialDepth;
    private int playerNumber = -1;
    private long selectedMove = 0L;

    public DecisionRuleAlgorithm(int initialDepth) {
        this.initialDepth = initialDepth;
    }

    @Override
    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    /**
     * Depending on the moves we could take minmax returns the score of the current state.
     * lowerBound  @min: if we get something lower than lowerBound --> break;
     * upperBound @max: if we get something higher than upperBound --> break;
     *
     * Beispiel: Man nehme an wir sind in max und haben bereits einen topScore, dann interessieren uns nur Werte die höher sind als dieser topScore.
     * Deshalb gibt man in der max funktion den topScore als lowerBound an die min funktion. Sobald in der min funktion ein score ermittelt wird,
     * der kleiner ist als der lowerBound (topScore der aufrufenden max funktion) kann die weitere Berechnung abgebrochen werden.
     * vice versa gilt das auch für die min funktion
     *
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
        var dra = new DecisionRuleAlgorithm(DEFAULT_DEPTH);

        dra.setPlayerNumber(1);

        var state = GameState.newEmptyGameState();

        float predictedScore = 0.f;

        // TODO: check player number == 1

        while (!state.isGameOver()) {
            if (state.getCurrentPlayer() == dra.playerNumber) {
                System.out.println("-----------------------------");
                System.out.println(String.format("player: %d", state.getCurrentPlayer()));
                System.out.println("Last predicted score " + predictedScore);
                for (int i = 0; i < GameState.NUM_PLAYERS; i++) {
                    System.out.println("score for player " + i + ": " + state.calculateScore(i));
                }
                predictedScore = dra.minmax(state, dra.initialDepth, -Float.MAX_VALUE, Float.MAX_VALUE);
                System.out.println(String.format("this is the move we take: %d", log2(dra.selectedMove)));
                state = state.checkedCreateStateFromMove(dra.selectedMove);
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
            }
            System.out.println(state);
        }
    }

    private static int log2(long number) {
        return (int) (Math.log(number) / Math.log(2));
    }

    @Override
    public long getNextMove(GameState state) {
        if (playerNumber == -1) {
            throw new IllegalStateException("Player number == -1");
        }
        selectedMove = GameState.INVALID_MOVE;
        minmax(state, initialDepth, -Float.MAX_VALUE, Float.MAX_VALUE);

        return selectedMove;
    }
}
