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
    public static final byte NUM_PLAYERS = 4;
    public static final byte MAX_NEXT_MOVES = 13;

    final static private byte[] PLAYER_DIRECTIONS = new byte[] {Configurations.FIELD_SIZE, -1, -Configurations.FIELD_SIZE, 1};

    private long[] configurations = new long[NUM_PLAYERS];

    void test(long configuration) {
        for (int playerIndex = 0; playerIndex < NUM_PLAYERS; playerIndex++) {
            System.out.println(String.format("Baseline for player %d", playerIndex));
            System.out.println(configurationToString(Configurations.BASE_LINES[playerIndex]));
        }
    }

    private GameState(long[] configurations) {
        this.configurations = configurations;
    }

    public static GameState newEmptyGameState() {
        return new GameState(new long[]{
                Configurations.TOKENS_TO_PLAY_BITMASK,
                Configurations.TOKENS_TO_PLAY_BITMASK,
                Configurations.TOKENS_TO_PLAY_BITMASK,
                Configurations.TOKENS_TO_PLAY_BITMASK,
        });
    }

    private static byte playerNumberToDirection(byte playerNumber) {
        return PLAYER_DIRECTIONS[playerNumber];
    }

    private long getEnemyConfiguration(byte playerNumber) {
        long enemyConfiguration = 0L;
        for (byte playerIndex = 0; playerIndex < NUM_PLAYERS; playerIndex++) {
            if (playerIndex != playerNumber) {
                enemyConfiguration = enemyConfiguration | configurations[playerIndex];
            }
        }
        return enemyConfiguration & Configurations.FIELD_BITMASK;
    }

    private long getPlayerConfiguration(byte playerNumber) {
        return configurations[playerNumber] & Configurations.FIELD_BITMASK;
    }

    private long getGeneralConfiguration() {
        long generalConfiguration = 0L;
        for (long configuration : configurations) {
            generalConfiguration |= configuration;
        }
        return generalConfiguration & Configurations.FIELD_BITMASK;
    }

    private long getBaseLine(byte playerNumber) {
        return Configurations.BASE_LINES[playerNumber];
    }

    private long getNextPossibleMoves(byte playerNumber) {
        final long playerConfiguration = getPlayerConfiguration(playerNumber);
        final long generalConfiguration = getGeneralConfiguration();

        final byte playerDirection = playerNumberToDirection(playerNumber);

        long nextPossibleMoves = 0L;
        for (long tokenPosition : new Configurations.TokenPositions(playerConfiguration)) {
            if (canTokenMove(tokenPosition, playerDirection, playerConfiguration, generalConfiguration)) {
                nextPossibleMoves |= tokenPosition;
            }
        }

        final byte numTokensToPlay = Configurations.getNumTokensToPlay(playerConfiguration);
        if (numTokensToPlay > 0) {
            long freeBaseLine = getBaseLine(playerNumber) & ~generalConfiguration;
            nextPossibleMoves |= freeBaseLine;
        }

        return nextPossibleMoves;
    }

    public List<GameState> getNextPossibleStates(byte playerNumber) {
        final long nextPossibleMoves = getNextPossibleMoves(playerNumber);
        final long playerConfiguration = getPlayerConfiguration(playerNumber);

        final List<GameState> nextPossibleStates = new ArrayList<>(MAX_NEXT_MOVES);
        for (long move : new Configurations.TokenPositions(nextPossibleMoves)) {
            final long targetPosition = getTargetPosition(playerNumber, move);
            final long targetPlayerState = (playerConfiguration & ~move) | targetPosition;

            final long[] target_configurations = configurations.clone();
            target_configurations[playerNumber] = targetPlayerState;
            nextPossibleStates.add(new GameState(target_configurations));
        }

        return nextPossibleStates;
    }

    private long getTargetPosition(byte playerNumber, long move) {
        byte playerDirection = PLAYER_DIRECTIONS[playerNumber];
        final long playerConfiguration = getPlayerConfiguration(playerNumber);
        final long enemyConfiguration = getEnemyConfiguration(playerNumber);
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

    private static long nextPositionInDirection(long position, byte playerDirection) {
        if (playerDirection < 0) {
            return (position >> -playerDirection) & Configurations.FIELD_BITMASK;
        } else {
            return (position << playerDirection) & Configurations.FIELD_BITMASK;
        }
    }

    private static boolean canTokenMove(
            long tokenPosition,
            byte playerDirection,
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

    public static String configurationToString(long configuration) {
        StringBuilder result = new StringBuilder(51);
        for (int y = Configurations.FIELD_SIZE-1; y >= 0; y--) {
            for (int x = 0; x < Configurations.FIELD_SIZE; x++) {
                if ((configuration & (1L << (x + y* Configurations.FIELD_SIZE))) != 0) {
                    result.append('1');
                } else {
                    result.append('0');
                }
            }
            result.append('\n');
        }
        return result.toString();
    }
}
