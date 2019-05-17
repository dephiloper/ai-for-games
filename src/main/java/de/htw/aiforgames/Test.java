package de.htw.aiforgames;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Test {
    public static void main(String[] args) {
        testRateFunctions();
    }

    static void testRateFunctions() {
        GameState state = GameState.newEmptyGameState();
        Random random = new Random(System.currentTimeMillis());
        List<Long> moves = new ArrayList<>(13);

        while (!state.isGameOver()) {
            long nextPossibleMoves = state.getNextPossibleMoves();
            moves.clear();
            for (long move : new Configurations.MovePositions(nextPossibleMoves)) {
                moves.add(move);
            }

            long move = moves.get(random.nextInt(moves.size()));
            state = state.createStateFromMove(move);
            System.out.println(state);

            for (int playerIndex = 0; playerIndex < GameState.NUM_PLAYERS; playerIndex++) {
                analyseRateFunctions(state, playerIndex);
            }
        }
    }

    static void analyseRateFunctions(GameState gameState, int playerNumber) {
        System.out.println("\n---------------\n");
        System.out.println("state: " + gameState);
        System.out.println("playerNumber: " + playerNumber);
        long configuration = gameState.configurations[playerNumber];
        System.out.println("rate: " + gameState.getRate(configuration, playerNumber));
        System.out.println("rate by token positions: " + GameState.getRateByTokenPositions(configuration, 0));
        System.out.println("rate by token positions 2: " + GameState.getRateByTokenPositions2(configuration, 0));
        System.out.println("rate by token movable: " + gameState.getRateByTokenMovable(playerNumber));
        System.out.println("rate by token token finished: " + GameState.getRateByTokenFinished(configuration));

    }
}
