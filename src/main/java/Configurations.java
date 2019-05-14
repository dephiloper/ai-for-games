import java.util.Iterator;

public class Configurations {
    static final int FIELD_SIZE = 7;
    private static final int NUM_TOKENS = 7;
    private static final int NUM_FIELDS = 49;
    static final long FIELD_BITMASK =            0b0000000000000001111111111111111111111111111111111111111111111111L;
    static final long TOKENS_TO_PLAY_BITMASK =   0b0000000000001110000000000000000000000000000000000000000000000000L;
    static final long[] BASE_LINES = new long[] {0b0000000000000000000000000000000000000000000000000000000001111111L,
                                                 0b0000000000000000000001000000100000010000001000000100000010000001L,
                                                 0b0000000000000001111111000000000000000000000000000000000000000000L,
                                                 0b0000000000000001000000100000010000001000000100000010000001000000L
    };
    static final long[] ENEMY_BASE_LINES = new long[] {BASE_LINES[2], BASE_LINES[3], BASE_LINES[0], BASE_LINES[1]};
    static final long PLAYER_ACTIVE_BITMASK =    0b0000000000010000000000000000000000000000000000000000000000000000L;
    static final long PLAYER_INIT_CONFIGURATION = PLAYER_ACTIVE_BITMASK | TOKENS_TO_PLAY_BITMASK;

    static private class TokenIterator implements Iterator<Long> {
        private long configuration;

        private TokenIterator(long configuration) {
            this.configuration = configuration;
        }

        public boolean hasNext() {
            return this.configuration != 0;
        }

        /**
         * See https://www.geeksforgeeks.org/count-set-bits-in-an-integer/ Brian Kernighan’s Algorithm for inspiration.
         */
        public Long next() {
            long old_configuration = configuration;
            this.configuration = this.configuration & (this.configuration - 1);
            return old_configuration ^ this.configuration;
        }
    }

    static private class MoveIterator implements Iterator<Long> {
        private long configuration;
        private boolean isInvalidMove;

        private MoveIterator(long configuration) {
            this.configuration = configuration;
            this.isInvalidMove = configuration == GameState.INVALID_MOVE;
        }

        public boolean hasNext() {
            if (isInvalidMove) {
                return this.configuration == 0;
            }
            return this.configuration != 0;
        }

        /**
         * See https://www.geeksforgeeks.org/count-set-bits-in-an-integer/ Brian Kernighan’s Algorithm for inspiration.
         */
        public Long next() {
            if (isInvalidMove) {
                this.configuration = 1L;
                return GameState.INVALID_MOVE;
            }
            long old_configuration = configuration;
            this.configuration = this.configuration & (this.configuration - 1);
            return old_configuration ^ this.configuration;
        }
    }

    static public class TokenPositions implements Iterable<Long> {
        private long configuration;

        TokenPositions(long configuration) {
            this.configuration = configuration & Configurations.FIELD_BITMASK;
        }
        public Iterator<Long> iterator() {
            return new TokenIterator(this.configuration);
        }
    }

    static public class MovePositions implements Iterable<Long> {
        private long configuration;

        MovePositions(long configuration) {
            this.configuration = configuration & Configurations.FIELD_BITMASK;
        }
        public Iterator<Long> iterator() {
            return new MoveIterator(this.configuration);
        }
    }

    static public String configurationToString(long configuration, int playerNumber) {
        StringBuilder result = new StringBuilder(51);

        for (int y = Configurations.FIELD_SIZE-1; y >= 0; y--) {
            for (int x = 0; x < Configurations.FIELD_SIZE; x++) {
                final long position = (1L << (x + y * Configurations.FIELD_SIZE));
                if ((position & configuration) == 0) {
                    result.append('~');
                } else {
                    result.append(GameState.playerNumberToChar(playerNumber));
                }
            }
            result.append('\n');
        }
        return result.toString();
    }

    /**
     * See https://www.geeksforgeeks.org/count-set-bits-in-an-integer/ Brian Kernighan’s Algorithm.
     */
    static public int getNumTokens(long configuration) {
        int count = 0;
        while (configuration != 0) {
            configuration &= (configuration - 1);
            count++;
        }
        return count;
    }

    public static boolean isConfigurationFinished(long configuration) {
        return (getNumTokensToPlay(configuration) == 0) && ((configuration & FIELD_BITMASK) == 0);
    }

    public static int getNumTokensFinished(long configuration) {
        long numTokensToPlay = configuration & TOKENS_TO_PLAY_BITMASK;
        return (int)(NUM_TOKENS - (numTokensToPlay >> NUM_FIELDS));
    }

    static int getNumTokensToPlay(long configuration) {
        long numTokensToPlay = configuration & TOKENS_TO_PLAY_BITMASK;
        return (int)(numTokensToPlay >> NUM_FIELDS);
    }

    static long setNumTokensToPlay(long targetPlayerState, int numTokensToPlay) {
        return (targetPlayerState & ~TOKENS_TO_PLAY_BITMASK) | (((long) numTokensToPlay) << NUM_FIELDS);
    }

    static boolean isPlayerActive(long configuration) {
        return (configuration & PLAYER_ACTIVE_BITMASK) != 0;
    }

    static long setPlayerInactive(long configuration) {
        return configuration & ~PLAYER_ACTIVE_BITMASK;
    }

    static long setPlayerActive(long configuration) {
        return configuration | PLAYER_ACTIVE_BITMASK;
    }

    static String configurationToString(long configuration) {
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
