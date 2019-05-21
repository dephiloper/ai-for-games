package de.htw.aiforgames.algorithm;

import de.htw.aiforgames.GameState;
import de.htw.aiforgames.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Train {
    static private class AlgorithmWrapper {
        DecisionRuleAlgorithm decisionAlgorithm;
        int numPlays;
        int numWins;
        int playerPos;

        AlgorithmWrapper(DecisionRuleAlgorithm decisionAlgorithm, int playerPos) {
            this.decisionAlgorithm = decisionAlgorithm;
            this.numPlays = 0;
            this.numWins = 0;
            this.playerPos = playerPos;
        }

        float getWinRate() {
            if (numPlays == 0) {
                return 0.f;
            }
            return numWins / (float)numPlays;
        }

        public AlgorithmWrapper mutate() {
            DecisionRuleAlgorithm alg = new DecisionRuleAlgorithm(
                    decisionAlgorithm.initialDepth,
                    decisionAlgorithm.getRater().mutate(MUTATION_RATE),
                    decisionAlgorithm.silent);
            return new AlgorithmWrapper(alg, -1);
        }

        public void reset() {
            numPlays = 0;
            numWins = 0;
        }

        @Override
        public String toString() {
            return "AlgorithmWrapper{" +
                    "decisionAlgorithm=" + decisionAlgorithm +
                    ", winRate=" + getWinRate() +
                    ", numPlays=" + numPlays +
                    ", numWins=" + numWins +
                    ", playerPos=" + playerPos +
                    '}';
        }
    }

    private static final int NUM_EPOCHS = 20;
    private static final float MUTATION_RATE = 0.5f;
    private static final int GENERATION_SIZE = 4*3;
    private static final int NUM_TRAININGS = GENERATION_SIZE*4;
    private static final int TRAIN_DEPTH = 5;

    private ArrayList<AlgorithmWrapper> generation;

    public static void main(String[] args) {
        Train train = new Train();
        train.start();
    }

    public Train() {
        generation = createGeneration(GENERATION_SIZE);
    }

    private void start() {
        for (int i = 0; i < NUM_EPOCHS-1; i++) {
            playEpoch();
            System.out.println(generation.get(generation.size()-1));

            nextGeneration();
        }
        playEpoch();

        generation.sort(Comparator.comparingDouble(algorithmWrapper -> -algorithmWrapper.getWinRate()));
        System.out.println("\nbest player:\n" + generation.get(0));
    }

    private void nextGeneration() {
        generation.sort(Comparator.comparingInt(algorithmWrapper -> -algorithmWrapper.numWins));
        int numSources = GENERATION_SIZE / 4;
        ArrayList<AlgorithmWrapper> nextGen = new ArrayList<>(GENERATION_SIZE);

        for (int i = 0; i < numSources; i++) {
            for (int j = 0; j < 3; j++) {
                nextGen.add(generation.get(i).mutate());
            }
            nextGen.add(generation.get(i));
        }

        for (int i = 0; i < nextGen.size(); i++) {
            nextGen.get(i).reset();
            nextGen.get(i).playerPos = i % 4;
        }

        generation = nextGen;
    }

    public void playEpoch() {
        for (AlgorithmWrapper w : generation) {
            if (w.numPlays != 0)
                throw new IllegalStateException("hey");
        }

        Utils.printProgress(-1, NUM_TRAININGS);
        for (int i = 0; i < NUM_TRAININGS; i++) {
            playWithWrappers(chooseRandom());
            Utils.printProgress(i, NUM_TRAININGS);
        }

        generation.sort(Comparator.comparingDouble(algorithmWrapper -> (double)algorithmWrapper.getWinRate()));
    }

    public AlgorithmWrapper[] chooseRandom() {
        AlgorithmWrapper[] selectedWrappers = new AlgorithmWrapper[4];

        Collections.shuffle(generation);
        generation.sort(Comparator.comparingInt(algorithmWrapper -> algorithmWrapper.numPlays));

        for (int playerIndex = 0; playerIndex < Utils.NUM_PLAYERS; playerIndex++) {
            for (AlgorithmWrapper w : generation) {
                if (w.playerPos == playerIndex) {
                    selectedWrappers[playerIndex] = w;
                    break;
                }
            }
        }

        return selectedWrappers;
    }

    static public ArrayList<AlgorithmWrapper> createGeneration(int size) {
        ArrayList<AlgorithmWrapper> generation = new ArrayList<>(size);
        Rater defaultRater = Rater.withDefaults();
        for (int i = 0; i < size; i++) {
            AlgorithmWrapper wrapper = new AlgorithmWrapper(
                    new DecisionRuleAlgorithm(TRAIN_DEPTH, defaultRater.mutate(MUTATION_RATE), true),
                    i%Utils.NUM_PLAYERS
            );
            generation.add(wrapper);
        }

        return generation;
    }

    public static void playWithWrappers(AlgorithmWrapper[] wrappers) {
        int winner = playGame(wrappers);
        for (AlgorithmWrapper wrapper : wrappers) {
            wrapper.numPlays++;
            wrapper.playerPos = (wrapper.playerPos + 1) % Utils.NUM_PLAYERS;
        }
        if (winner == -1) {
            return;
        }
        wrappers[winner].numWins++;
    }

    public static int playGame(AlgorithmWrapper[] algorithms) {
        for (int i = 0; i < algorithms.length; i++) {
            algorithms[i].decisionAlgorithm.setPlayerNumber(i);
        }

        GameState state = GameState.newEmptyGameState();

        while (!state.isGameOver()) {
            long move = algorithms[state.getCurrentPlayer()].decisionAlgorithm.getNextMove(state);
            // System.out.println(state);
            state = state.createStateFromMove(move);
        }

        return state.getWinner();
    }
}
