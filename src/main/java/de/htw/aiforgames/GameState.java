package de.htw.aiforgames;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    /*
     * A configuration is a bitmask defining the tokens of a player.
     * The least significant bit of a configuration is associated with the bottom left token
     * The most significant bit of a configuration is associated with the top right token
     * The nth bit defines the position (x, y) = (n % 7, n / 7).
     * The first 49 bits are used for the token masks.
     *
     * The first player has the ground line (0, 0) to (6, 0).
     * The second player has the ground line (0, 0) to (0, 6).
     * The following players change clockwise.
     */

    /**
     * An array of configurations defining the positions of the tokens
     */
    public long[] configurations;
    /**
     * The number of the player, who should do the next move.
     */
    private int currentPlayer;

    private GameState(long[] configurations) {
        this.configurations = configurations;
        this.currentPlayer = 0;
    }

    private GameState(long[] configurations, int currentPlayer) {
        this.configurations = configurations;
        this.currentPlayer = currentPlayer;
    }

    public static GameState newEmptyGameState() {
        return new GameState(new long[]{
                Utils.PLAYER_INIT_CONFIGURATION,
                Utils.PLAYER_INIT_CONFIGURATION,
                Utils.PLAYER_INIT_CONFIGURATION,
                Utils.PLAYER_INIT_CONFIGURATION,
        });
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public long getEnemyConfiguration() {
        return getEnemyConfiguration(currentPlayer);
    }

    public long getEnemyConfiguration(int playerNumber) {
        long enemyConfiguration = 0L;
        for (int playerIndex = 0; playerIndex < Utils.NUM_PLAYERS; playerIndex++) {
            if (playerIndex != playerNumber) {
                enemyConfiguration = enemyConfiguration | configurations[playerIndex];
            }
        }
        return enemyConfiguration & Utils.FIELD_BITMASK;
    }

    private long getPlayerConfiguration(int playerNumber) {
        return configurations[playerNumber] & Utils.FIELD_BITMASK;
    }

    private long getPlayerConfiguration() {
        return getPlayerConfiguration(currentPlayer);
    }

    private long getGeneralConfiguration() {
        long generalConfiguration = 0L;
        for (long configuration : configurations) {
            generalConfiguration |= configuration;
        }
        return generalConfiguration & Utils.FIELD_BITMASK;
    }

    private static long getBaseLine(int playerNumber) {
        return Utils.BASE_LINES[playerNumber];
    }

    public long getNextPossibleMoves() {
        return getNextPossibleMoves(currentPlayer);
    }

    public long getNextPossibleMoves(int playerNumber) {
        final long playerConfiguration = getPlayerConfiguration(playerNumber);
        final long generalConfiguration = getGeneralConfiguration();

        long nextPossibleMoves = 0L;
        for (long tokenPosition : new Configurations.TokenPositions(playerConfiguration)) {
            if (canTokenMove(tokenPosition, playerNumber, playerConfiguration, generalConfiguration)) {
                nextPossibleMoves |= tokenPosition;
            }
        }

        final int numTokensToPlay = Configurations.getNumTokensToPlay(configurations[playerNumber]);
        if (numTokensToPlay > 0) {
            long freeBaseLine = getBaseLine(playerNumber) & ~generalConfiguration;
            nextPossibleMoves |= freeBaseLine;
        }

        return nextPossibleMoves;
    }

    /**
     * Returns the number of the next active player
     */
    private int getNextPlayerNumber() {
        int nextPlayerNumber = (currentPlayer + 1) % Utils.NUM_PLAYERS;
        for (int i = 0; i < 5; i++) {
            if (Configurations.isPlayerActive(configurations[nextPlayerNumber])) {
                return nextPlayerNumber;
            }
            nextPlayerNumber = (nextPlayerNumber + 1) % Utils.NUM_PLAYERS;
        }
        throw new IllegalStateException("no players are active anymore");
    }

    /**
     * Returns a new State representing the state after moving the token of the player with playerNumber
     * @param move The move to apply. If move == 0, the currentPlayer is set to invalid.
     * @return A new GameState with move applied
     */
    public GameState createStateFromMove(long move) {
        final long[] targetConfigurations = configurations.clone();

        // handle null move
        if (move == Utils.INVALID_MOVE) {
            targetConfigurations[currentPlayer] = Configurations.setPlayerInactive(configurations[currentPlayer]);
        } else {
            long playerConfiguration = getPlayerConfiguration();
            long targetPosition = getTargetPosition(move, currentPlayer);
            long targetPlayerConfiguration = (playerConfiguration & ~move) | targetPosition;

            int numTokensToPlay = Configurations.getNumTokensToPlay(configurations[currentPlayer]);

            // if is new token
            if (move == targetPosition) {
                targetPlayerConfiguration = Configurations.setNumTokensToPlay(targetPlayerConfiguration, numTokensToPlay-1);
            } else {
                targetPlayerConfiguration = Configurations.setNumTokensToPlay(targetPlayerConfiguration, numTokensToPlay);
            }

            targetPlayerConfiguration = Configurations.setPlayerActive(targetPlayerConfiguration);

            targetConfigurations[currentPlayer] = targetPlayerConfiguration;
        }

        int nextPlayerNumber = getNextPlayerNumber();

        return new GameState(targetConfigurations, nextPlayerNumber);
    }

    /**
     * Returns a new State representing the state after moving the token of the player with playerNumber.
     * This function also checks if the given move is possible to execute.
     * @param move The move to apply. If move == 0, the currentPlayer is set to invalid.
     * @return A new GameState with move applied
     */
    public GameState checkedCreateStateFromMove(long move) {
        long possibleMoves = getNextPossibleMoves();
        if (((move & possibleMoves) == 0) && (move != Utils.INVALID_MOVE) || Configurations.getNumTokens(move) > 1) {
            System.err.println(String.format(
                    "invalid move given: \n%s in state: \n%s",
                    Configurations.configurationToString(move, currentPlayer),
                    this)
            );
            move = Utils.INVALID_MOVE;
        }
        return createStateFromMove(move);
    }

    @SuppressWarnings("unused")
    private List<GameState> getNextPossibleStates() {
        final long nextPossibleMoves = getNextPossibleMoves();

        final List<GameState> nextPossibleStates = new ArrayList<>(Utils.MAX_NEXT_MOVES);
        if (nextPossibleMoves == 0) {
            nextPossibleStates.add(createStateFromMove(Utils.INVALID_MOVE));
        } else {
            for (long move : new Configurations.TokenPositions(nextPossibleMoves)) {
                nextPossibleStates.add(createStateFromMove(move));
            }
        }

        return nextPossibleStates;
    }

    /**
     * Moves the token at the position given by move. The token has to be of the player with playerNumber
     * @param playerNumber The number of the player, for which to move the token
     * @param move Defines the token to move
     * @return the positions of the token after moving
     */
    private long getTargetPosition(long move, int playerNumber) {
        final long playerConfiguration = getPlayerConfiguration();
        final long enemyConfiguration = getEnemyConfiguration();
        final long generalConfiguration = enemyConfiguration | playerConfiguration;

        // check if field is empty
        if ((playerConfiguration & move) == 0)
            return move;

        long targetPosition = 0L;

        boolean jumping = false;
        long position = move;
        while (true) {
            final long nextPosition = Configurations.nextPositionInDirection(position, playerNumber);

            if (jumping) {
                final long nextNextPosition = Configurations.nextPositionInDirection(nextPosition, playerNumber);

                // if we are one field behind edge
                if (nextPosition == 0) {
                    targetPosition = position;
                    break;
                }

                // if we jump over an enemy into freedom
                if ((nextNextPosition == 0) && ((nextPosition & enemyConfiguration) != 0)) {
                    break;
                }

                // if the next jump place is occupied
                if ((nextNextPosition & generalConfiguration) != 0) {
                    targetPosition = position;
                    break;
                }

                // if the field over which to jump is occupied by us
                if ((nextPosition & playerConfiguration) != 0) {
                    targetPosition = position;
                    break;
                }

                // if the field over which to jump is free
                if ((nextPosition & enemyConfiguration) == 0) {
                    targetPosition = position;
                    break;
                }

                // if the field over which to jump is occupied by an enemy
                position = nextNextPosition;
            } else {
                // if we moved outside the field
                if (nextPosition == 0) {
                    return 0L;
                }

                // if there is an enemy in front of us
                if ((nextPosition & enemyConfiguration) != 0)
                {
                    // we assume that the field behind the enemy player is free,
                    // because otherwise the move would be invalid
                    position = Configurations.nextPositionInDirection(nextPosition, playerNumber);
                    jumping = true;
                }
                else // if there is no enemy in front of us
                {
                    targetPosition = nextPosition;
                    break;
                }
            }
        }

        return targetPosition;
    }

    public boolean isGameOver() {
        boolean anyActive = false;
        for (int playerNumber = 0; playerNumber < Utils.NUM_PLAYERS; playerNumber++) {
            if (Configurations.isPlayerActive(configurations[playerNumber])) {
                anyActive = true;
            }
        }

        if (!anyActive) return true;

        for (int playerNumber = 0; playerNumber < Utils.NUM_PLAYERS; playerNumber++) {
            if (Configurations.isConfigurationFinished(configurations[playerNumber])) {
                return true;
            }
        }

        return false;
    }

    public int getWinner() {
        if (!isGameOver()) {
            throw new IllegalStateException("Game is not over, so no winner is possible");
        }
        int tokensFinishedMax = -1;
        int bestPlayer = -1;
        for (int playerIndex = 0; playerIndex < Utils.NUM_PLAYERS; playerIndex++) {
            int tokensFinished = Configurations.getNumTokensFinished(configurations[playerIndex]);
            if (tokensFinished > tokensFinishedMax) {
                tokensFinishedMax = tokensFinished;
                bestPlayer = playerIndex;
            } else if (tokensFinished == tokensFinishedMax) {
                bestPlayer = -1;
            }
        }

        return bestPlayer;
    }

    private static boolean canTokenMove(
            long tokenPosition,
            int playerNumber,
            long playerConfiguration,
            long generalConfiguration)
    {
        // if nextPosition is occupied by myself, return false
        long nextPosition = Configurations.nextPositionInDirection(tokenPosition, playerNumber);
        if ((nextPosition & playerConfiguration) != 0) {
            return false;
        }

        // if nextPosition is free
        if ((nextPosition & generalConfiguration) == 0) {
            return true;
        }

        // Return whether nextNextPosition is free
        // jumping out of field is possible (nextNextPosition == 0)
        long nextNextPosition = Configurations.nextPositionInDirection(nextPosition, playerNumber);

        return (nextNextPosition & generalConfiguration) == 0;
    }

    static private boolean applyChar(GameState state, long pos, char c) {
        switch (c) {
            case '~':
                return true;
            case '0':
                state.configurations[0] = state.configurations[0] | pos;
                return true;
            case '1':
                state.configurations[1] = state.configurations[1] | pos;
                return true;
            case '2':
                state.configurations[2] = state.configurations[2] | pos;
                return true;
            case '3':
                state.configurations[3] = state.configurations[3] | pos;
                return true;
        }
        return false;
    }

    public static GameState fromString(String s, int[] tokensToPlay) {
        GameState state = GameState.newEmptyGameState();

        int index = 0;


        int y = 6;
        while ((y >= 0) && (s.length() != index)) {

            int x = 0;
            while ((x < 7) && (s.length() != index)) {
                long pos = 1L << (x + y*7);
                if (applyChar(state, pos, s.charAt(index))) {
                    x++;
                }
                index++;
            }
            y--;
        }

        for (int playerNumber = 0; playerNumber < 4; playerNumber++) {
            long conf = state.configurations[playerNumber];
            int t2p = Utils.NUM_TOKENS - Configurations.getNumTokens(conf);
            if (playerNumber < tokensToPlay.length) {
                t2p = tokensToPlay[playerNumber];
            }
            state.configurations[playerNumber] = Configurations.setNumTokensToPlay(conf, t2p);
        }

        return state;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(51);
        final long generalConfiguration = getGeneralConfiguration();

        for (int y = Utils.FIELD_SIZE-1; y >= 0; y--) {
            for (int x = 0; x < Utils.FIELD_SIZE; x++) {
                final long position = (1L << (x + y * Utils.FIELD_SIZE));
                if ((position & generalConfiguration) == 0) {
                    result.append('~');
                    continue;
                }
                for (int playerNumber = 0; playerNumber < Utils.NUM_PLAYERS; playerNumber++) {
                    if ((configurations[playerNumber] & position) != 0) {
                        result.append(playerNumberToChar(playerNumber));
                    }
                }
            }
            result.append('\n');
        }
        return result.toString();
    }

    public static char playerNumberToChar(int playerNumber) {
        switch (playerNumber) {
            case 0: return '0';
            case 1: return '1';
            case 2: return '2';
            case 3: return '3';
            default: throw new RuntimeException();
        }
    }
}
