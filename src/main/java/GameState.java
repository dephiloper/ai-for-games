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

    final static private int[] PLAYER_DIRECTIONS = new int[] {Configurations.FIELD_SIZE, 1, -Configurations.FIELD_SIZE, -1};

    /**
     * An array of configurations defining the positions of the tokens
     */
    private long[] configurations;
    /**
     * The number of the player, who should do the next move.
     */
    private int currentPlayer;

    static void test() {
        Random randomGenerator = new Random();

        GameState state = GameState.newEmptyGameState();

        for (int i = 0; i < 50; i++) {
            List<GameState> states = state.getNextPossibleStates();
            int index = randomGenerator.nextInt(states.size());
            state = states.get(index);
            System.out.println(String.format("player %d moves:", state.getCurrentPlayer()));
            System.out.println(state.toString());
        }
    }

    private GameState(long[] configurations) {
        this.configurations = configurations;
        this.currentPlayer = 0;
    }

    private static GameState newEmptyGameState() {
        return new GameState(new long[]{
                Configurations.TOKENS_TO_PLAY_BITMASK,
                Configurations.TOKENS_TO_PLAY_BITMASK,
                Configurations.TOKENS_TO_PLAY_BITMASK,
                Configurations.TOKENS_TO_PLAY_BITMASK,
        });
    }

    public int getCurrentPlayer() {
        return currentPlayer;
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

        final int playerDirection = playerNumberToDirection(currentPlayer);

        long nextPossibleMoves = 0L;
        for (long tokenPosition : new Configurations.TokenPositions(playerConfiguration)) {
            if (canTokenMove(tokenPosition, playerDirection, playerConfiguration, generalConfiguration)) {
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
     * Returns a new State representing the state after moving the token of the player with playerNumber
     * @param move The move to apply
     * @return A new GameState with move applied
     */
    public GameState createStateFromMove(long move) {
        long playerConfiguration = getPlayerConfiguration();
        long targetPosition = getTargetPosition(move, currentPlayer);
        // long targetPlayerState = ()

        // TODO
        return this;
    }

    private List<GameState> getNextPossibleStates() {
        final long nextPossibleMoves = getNextPossibleMoves();
        final long playerConfiguration = getPlayerConfiguration();

        final List<GameState> nextPossibleStates = new ArrayList<>(MAX_NEXT_MOVES);
        for (long move : new Configurations.TokenPositions(nextPossibleMoves)) {
            final long targetPosition = getTargetPosition(move, currentPlayer);
            long targetPlayerState = (playerConfiguration & ~move) | targetPosition;

            int numTokensToPlay = Configurations.getNumTokensToPlay(configurations[currentPlayer]);

            // if is new token
            if (move == targetPosition) {
                targetPlayerState = Configurations.setNumTokensToPlay(targetPlayerState, numTokensToPlay-1);
            } else {
                targetPlayerState = Configurations.setNumTokensToPlay(targetPlayerState, numTokensToPlay);
            }

            final long[] target_configurations = configurations.clone();
            target_configurations[currentPlayer] = targetPlayerState;
            nextPossibleStates.add(new GameState(target_configurations));
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
        int playerDirection = PLAYER_DIRECTIONS[playerNumber];
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
            final long nextPosition = nextPositionInDirection(position, playerDirection);

            if (jumping) {
                final long nextNextPosition = nextPositionInDirection(nextPosition, playerDirection);

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
                    position = nextPositionInDirection(nextPosition, playerDirection);
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

    private static long nextPositionInDirection(long position, int playerDirection) {
        if (playerDirection < 0) {
            return (position >> -playerDirection) & Configurations.FIELD_BITMASK;
        } else {
            return (position << playerDirection) & Configurations.FIELD_BITMASK;
        }
    }

    private static boolean canTokenMove(
            long tokenPosition,
            int playerDirection,
            long playerConfiguration,
            long generalConfiguration)
    {
        // if nextPosition is occupied by myself, return false
        long nextPosition = nextPositionInDirection(tokenPosition, playerDirection);
        if ((nextPosition & playerConfiguration) != 0) {
            return false;
        }

        // Return whether nextNextPosition is free
        long nextNextPosition = nextPositionInDirection(nextPosition, playerDirection);
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
