package de.htw.aiforgames.algorithm;

import de.htw.aiforgames.Configurations;
import de.htw.aiforgames.GameState;
import de.htw.aiforgames.Utils;

import static de.htw.aiforgames.Utils.BASELINE_POS;
import static de.htw.aiforgames.Utils.PLAYER_NUMBER_TO_XY_POSITIONS;

public class Rater {
    private float tokenPositionsWeight = 1.f;
    private float tokenFinishedWeight = 8.f;
    private float tokenToMoveWeight = 0.5f;
    private float winWeight = 100.f;
    private float inactiveWeight = -100.f;

    public static Rater withDefaults() {
        return new Rater();
    }

    /**
     * Calculates the score of the configuration from the view of the player with playerNumber.
     * @param playerNumber The number of the player from whose point of view the score is assessed.
     * @return The score of the configurations
     */
    public float calculateScore(GameState gameState, int playerNumber) {
        float enemyRate = 0.f;
        float playerRate = 0.f;
        for (int playerIndex = 0; playerIndex < Utils.NUM_PLAYERS; playerIndex++) {
            float rate = getRate(gameState, playerIndex);
            if (playerIndex == playerNumber) {
                playerRate = rate;
            } else {
                if (enemyRate < rate) {
                    enemyRate = rate;
                }
            }
        }
        return playerRate - enemyRate;
    }

    public float getRate(GameState gameState, int playerNumber) {
        long configuration = gameState.configurations[playerNumber];

        return tokenPositionsWeight * getRateByTokenPositions(configuration, playerNumber) +
                tokenFinishedWeight * getRateByTokenFinished(configuration) +
                tokenToMoveWeight * getRateByTokenMovable(gameState, playerNumber) +
                winWeight * getRateByWon(configuration) +
                inactiveWeight * getRateByInactive(configuration);
    }

    /**
     * Returns the Rating for the configuration by investigating only the progress of the tokens.
     * @param configuration The configuration to rate
     * @param playerNumber The Number of the player which controls the tokens in position.
     * @return The score
     */
    public static float getRateByTokenPositions(long configuration, int playerNumber) {
        int sum = 0;
        final int base = BASELINE_POS[playerNumber];
        int[] positions = PLAYER_NUMBER_TO_XY_POSITIONS[playerNumber];

        for (long tokenPosition : new Configurations.TokenPositions(configuration)) {
            int pos = Utils.floorLog2(tokenPosition);
            int xyPos = positions[pos];
            sum += Math.abs(xyPos - base) + 1;
        }

        return sum;
    }

    @SuppressWarnings("unused")
    public static float getRateByTokenPositions2(long configuration, int playerNumber) {
        long mask = Utils.BASE_LINES[playerNumber];
        int playerDirection = Utils.PLAYER_DIRECTIONS[playerNumber];
        int sum = 0;

        for (int i = 1; i < 8; i++) {
            sum += i * Configurations.getNumTokens(configuration & mask);
            if (playerDirection < 0) {
                mask >>= -playerDirection;
            } else {
                mask <<= playerDirection;
            }
        }

        return sum;
    }

    public static float getRateByTokenFinished(long configuration) {
        return Configurations.getNumTokensFinished(configuration);
    }

    public float getRateByTokenMovable(GameState state, int playerNumber) {
        long possibleMoves = state.getNextPossibleMoves(playerNumber);
        int numTokensMovable = Configurations.getNumTokens(possibleMoves);
        return (float)numTokensMovable;
    }

    public static float getRateByInactive(long configuration) {
        if (!Configurations.isPlayerActive(configuration)) {
            return 1.f;
        }
        return 0.f;
    }

    public static float getRateByWon(long configuration) {
        if ((configuration & Utils.WON_BITMASK) == 0) {
            return 1.f;
        }
        return 0.f;
    }
}
