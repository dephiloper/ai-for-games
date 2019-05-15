package de.htw.aiforgames.algorithm;

import de.htw.aiforgames.Configurations;
import de.htw.aiforgames.GameState;
import de.htw.aiforgames.Utils;
import lenz.htw.sawhian.Move;

public interface DecisionAlgorithm {
    long getNextMove(GameState state);
    default void setPlayerNumber(int playerNumber) {}

    static Move moveToServerMove(long move, int playerNumber) {
        int tokenPos = Utils.floorLog2(move);
        int x = tokenPos / Configurations.FIELD_SIZE;
        int y = tokenPos % Configurations.FIELD_SIZE;
        return new Move(playerNumber, x, y);
    }
}
