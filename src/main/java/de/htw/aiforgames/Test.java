package de.htw.aiforgames;

import de.htw.aiforgames.algorithm.Rater;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

class Test {
    public static void main(String[] args) {
        // testRateFunctions();
        testRateFunctions2();
    }

    static void testRateFunctions2() {
        Rater rater = Rater.withDefaults();

        GameState state = GameState.fromString(
                "~~~~~~~" +
                "~~~~~~~" +
                "~~~~~0~" +
                "~~~~~~~" +
                "~~~~~~~" +
                "~~~~~~~" +
                "~~~~~~~",
                new int[] {0, 0, 0, 0}
        );

        analyseRateFunctions(state, 1, null, rater);
    }

    static void testRateFunctions() {
        Rater rater = Rater.withDefaults();
        Scanner scanner = new Scanner(System.in);
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

            for (int playerIndex = 0; playerIndex < Utils.NUM_PLAYERS; playerIndex++) {
                analyseRateFunctions(state, playerIndex, scanner, rater);
            }
        }
    }

    static void analyseRateFunctions(GameState gameState, int playerNumber, Scanner scanner, Rater rater) {
        System.out.println("\n---------------\n");
        System.out.println("state:\n" + gameState);
        System.out.println("playerNumber: " + playerNumber);
        long configuration = gameState.configurations[playerNumber];
        System.out.println("rate: " + rater.getRate(gameState, playerNumber));
        System.out.println("rate by token positions: " + Rater.getRateByTokenPositions(configuration, playerNumber));
        System.out.println("rate by token positions 2: " + Rater.getRateByTokenPositions2(configuration, playerNumber));
        System.out.println("rate by token movable: " + rater.getRateByTokenMovable(gameState, playerNumber));
        System.out.println("rate by token finished: " + Rater.getRateByTokenFinished(configuration));
        System.out.println("rate by win: " + Rater.getRateByWon(configuration));
        System.out.println("rate by inactive: " + Rater.getRateByInactive(configuration));

        if (scanner != null) {
            scanner.next();
        }
    }

    static void testLog2() {
        long l = 1L;
        for (int i = 0; i < 64; i++) {
            System.out.println(Utils.floorLog2(l));
            l <<= 1;
        }
        System.out.println(Utils.floorLog2(0L));
    }
}
