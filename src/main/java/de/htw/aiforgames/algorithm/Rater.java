package de.htw.aiforgames.algorithm;

import de.htw.aiforgames.Configurations;
import de.htw.aiforgames.GameState;
import de.htw.aiforgames.Utils;

import static de.htw.aiforgames.Utils.BASELINE_POS;
import static de.htw.aiforgames.Utils.PLAYER_NUMBER_TO_XY_POSITIONS;

public class Rater {
    public static final float DEFAULT_TOKEN_POSITIONS_WEIGHT = 1.f;
    public static final float DEFAULT_TOKEN_FINISHED_WEIGHT = 9.0f;
    public static final float DEFAULT_TOKEN_TO_MOVE_WEIGHT = 0.5f;
    public static final float DEFAULT_WIN_WEIGHT = 100.F;
    public static final float DEFAULT_INACTIVE_WEIGHT = -100.f;

    private float[] weights;

    public static Rater withDefaults() {
        float[] weights = new float[] {
                DEFAULT_TOKEN_POSITIONS_WEIGHT,
                DEFAULT_TOKEN_FINISHED_WEIGHT,
                DEFAULT_TOKEN_TO_MOVE_WEIGHT,
                DEFAULT_WIN_WEIGHT,
                DEFAULT_INACTIVE_WEIGHT
        };
        return new Rater(weights);
    }

    private Rater(float[] weights) {
        this.weights = weights;
    }

    /**
     * Calculates the score of the configuration from the view of the player with playerNumber.
     * @param playerNumber The number of the player from whose point of view the score is assessed.
     * @return The score of the configurations
     */
    public float calculateScore(GameState gameState, int playerNumber) {
        float maxEnemyRate = 0.f;
        float enemyRate = 0.f;
        float playerRate = 0.f;
        for (int playerIndex = 0; playerIndex < Utils.NUM_PLAYERS; playerIndex++) {
            float rate = getRate(gameState, playerIndex);
            if (playerIndex == playerNumber) {
                playerRate = rate;
            } else {
                enemyRate += rate;
                if (maxEnemyRate < rate) {
                    maxEnemyRate = rate;
                }
            }
        }
        return playerRate - (maxEnemyRate + enemyRate) / 4.f;
    }

    public float getRate(GameState gameState, int playerNumber) {
        long configuration = gameState.configurations[playerNumber];

        float[] attributes = new float[] {
                getRateByTokenPositions(configuration, playerNumber),
                getRateByTokenFinished(configuration),
                getRateByTokenMovable(gameState, playerNumber),
                getRateByWon(configuration),
                getRateByInactive(configuration)
        };

        float rate = 0.f;

        for (int i = 0; i < attributes.length; i++) {
            rate += weights[i] * attributes[i];
        }

        return rate;
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
