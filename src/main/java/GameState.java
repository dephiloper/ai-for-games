import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// TODO: players, that are not playing anymore should be skipped

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
    private static final int NUM_PLAYERS = 4;
    private static final int MAX_NEXT_MOVES = 13;
    private static final long INVALID_MOVE = 0L;

    final static private int[] PLAYER_DIRECTIONS = new int[] {
            Configurations.FIELD_SIZE,
            1,
            -Configurations.FIELD_SIZE,
            -1
    };

    /**
     * An array of configurations defining the positions of the tokens
     */
    private long[] configurations;
    /**
     * The number of the player, who should do the next move.
     */
    private int currentPlayer;

    static void test() {
        // GameState state = GameState.newEmptyGameState();
        GameState state = new GameState(new long[] {
                Configurations.PLAYER_ACTIVE_BITMASK | 64L,
                Configurations.PLAYER_ACTIVE_BITMASK | 16L | 32L,
                0L,
                0L
        }, 1);

        test_state(state);
    }

    static void test_state(GameState state) {
        Random randomGenerator = new Random();

        for (int i = 0; i < 500; i++) {
            List<GameState> states = state.getNextPossibleStates();
            int index = randomGenerator.nextInt(states.size());
            System.out.println(String.format("player %d moves:", state.getCurrentPlayer()));
            state = states.get(index);
            System.out.println(state.toString());
        }
    }

    private GameState(long[] configurations) {
        this.configurations = configurations;
        this.currentPlayer = 0;
    }

    private GameState(long[] configurations, int currentPlayer) {
        this.configurations = configurations;
        this.currentPlayer = currentPlayer;
    }

    private static GameState newEmptyGameState() {
        return new GameState(new long[]{
                Configurations.PLAYER_INIT_CONFIGURATION,
                Configurations.PLAYER_INIT_CONFIGURATION,
                Configurations.PLAYER_INIT_CONFIGURATION,
                Configurations.PLAYER_INIT_CONFIGURATION,
        });
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public float calculateScore(int playerNumber) {
        // TODO
        return 0.f;
    }

    private static int playerNumberToDirection(int playerNumber) {
        return PLAYER_DIRECTIONS[playerNumber];
    }

    private long getEnemyConfiguration() {
        long enemyConfiguration = 0L;
        for (int playerIndex = 0; playerIndex < NUM_PLAYERS; playerIndex++) {
            if (playerIndex != currentPlayer) {
                enemyConfiguration = enemyConfiguration | configurations[playerIndex];
            }
        }
        return enemyConfiguration & Configurations.FIELD_BITMASK;
    }

    private long getPlayerConfiguration() {
        return configurations[currentPlayer] & Configurations.FIELD_BITMASK;
    }

    private long getGeneralConfiguration() {
        long generalConfiguration = 0L;
        for (long configuration : configurations) {
            generalConfiguration |= configuration;
        }
        return generalConfiguration & Configurations.FIELD_BITMASK;
    }

    private static long getBaseLine(int playerNumber) {
        return Configurations.BASE_LINES[playerNumber];
    }

    private long getNextPossibleMoves() {
        final long playerConfiguration = getPlayerConfiguration();
        final long generalConfiguration = getGeneralConfiguration();

        long nextPossibleMoves = 0L;
        for (long tokenPosition : new Configurations.TokenPositions(playerConfiguration)) {
            if (canTokenMove(tokenPosition, currentPlayer, playerConfiguration, generalConfiguration)) {
                nextPossibleMoves |= tokenPosition;
            }
        }

        final int numTokensToPlay = Configurations.getNumTokensToPlay(configurations[currentPlayer]);
        if (numTokensToPlay > 0) {
            long freeBaseLine = getBaseLine(currentPlayer) & ~generalConfiguration;
            nextPossibleMoves |= freeBaseLine;
        }

        return nextPossibleMoves;
    }

    /**
     * Returns the number of the next active player
     */
    private int getNextPlayerNumber() {
        int nextPlayerNumber = (currentPlayer + 1) % NUM_PLAYERS;
        for (int i = 0; i < 5; i++) {
            if (Configurations.isPlayerActive(configurations[nextPlayerNumber])) {
                return nextPlayerNumber;
            }
            nextPlayerNumber = (nextPlayerNumber + 1) % NUM_PLAYERS;
        }
        throw new IllegalStateException("no players are activ anymore");
    }

    /**
     * Returns a new State representing the state after moving the token of the player with playerNumber
     * @param move The move to apply. If move == 0, the currentPlayer is set to invalid.
     * @return A new GameState with move applied
     */
    public GameState createStateFromMove(long move) {
        final long[] targetConfigurations = configurations.clone();

        // handle null move
        if (move == INVALID_MOVE) {
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

    private List<GameState> getNextPossibleStates() {
        final long nextPossibleMoves = getNextPossibleMoves();

        final List<GameState> nextPossibleStates = new ArrayList<>(MAX_NEXT_MOVES);
        if (nextPossibleMoves == 0) {
            nextPossibleStates.add(createStateFromMove(INVALID_MOVE));
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
            final long nextPosition = nextPositionInDirection(position, playerNumber);

            if (jumping) {
                final long nextNextPosition = nextPositionInDirection(nextPosition, playerNumber);

                // if we are one field behind edge
                if (nextPosition == 0) {
                    targetPosition = position;
                    break;
                }

                // if we jump over an enemy into freedom
                if (nextNextPosition == 0) {
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
                    position = nextPositionInDirection(nextPosition, playerNumber);
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
        for (int playerNumber = 0; playerNumber < NUM_PLAYERS; playerNumber++) {
            if (Configurations.isPlayerActive(configurations[playerNumber])) {
                anyActive = true;
            }
        }

        if (!anyActive) return true;

        for (int playerNumber = 0; playerNumber < NUM_PLAYERS; playerNumber++) {
            if (Configurations.isConfigurationFinished(configurations[playerNumber])) {
                return true;
            }
        }

        return false;
    }

    private static long nextPositionInDirection(long position, int playerNumber) {
        int playerDirection = PLAYER_DIRECTIONS[playerNumber];

        // If token is on enemy baseline, move out of the field
        if ((position & Configurations.ENEMY_BASE_LINES[playerNumber]) != 0) {
            return 0L;
        }

        if (playerDirection < 0) {
            return (position >> -playerDirection) & Configurations.FIELD_BITMASK;
        } else {
            return (position << playerDirection) & Configurations.FIELD_BITMASK;
        }
    }

    private static boolean canTokenMove(
            long tokenPosition,
            int playerNumber,
            long playerConfiguration,
            long generalConfiguration)
    {
        // if nextPosition is occupied by myself, return false
        long nextPosition = nextPositionInDirection(tokenPosition, playerNumber);
        if ((nextPosition & playerConfiguration) != 0) {
            return false;
        }

        // if nextPosition is free
        if ((nextPosition & generalConfiguration) == 0) {
            return true;
        }

        // Return whether nextNextPosition is free
        // jumping out of field is possible (nextNextPosition == 0)
        long nextNextPosition = nextPositionInDirection(nextPosition, playerNumber);

        return (nextNextPosition & generalConfiguration) == 0;
    }

    public String toString() {
        StringBuilder result = new StringBuilder(51);
        final long generalConfiguration = getGeneralConfiguration();

        for (int y = Configurations.FIELD_SIZE-1; y >= 0; y--) {
            for (int x = 0; x < Configurations.FIELD_SIZE; x++) {
                final long position = (1L << (x + y * Configurations.FIELD_SIZE));
                if ((position & generalConfiguration) == 0) {
                    result.append('~');
                    continue;
                }
                for (int playerNumber = 0; playerNumber < NUM_PLAYERS; playerNumber++) {
                    if ((configurations[playerNumber] & position) != 0) {
                        result.append(playerNumberToChar(playerNumber));
                    }
                }
            }
            result.append('\n');
        }
        return result.toString();
    }

    private static char playerNumberToChar(int playerNumber) {
        switch (playerNumber) {
            case 0: return '0';
            case 1: return '1';
            case 2: return '2';
            case 3: return '3';
            default: throw new RuntimeException();
        }
    }
}
