package de.htw.aiforgames.algorithm;

import de.htw.aiforgames.GameState;

public interface DecisionAlgorithm {
    long getNextMove(GameState state);
    default void setPlayerNumber(int playerNumber) {}
}
