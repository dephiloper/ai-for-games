package de.htw.aiforgames;

import java.util.Iterator;

public class Configurations {
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
            this.isInvalidMove = configuration == Utils.INVALID_MOVE;
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
                return Utils.INVALID_MOVE;
            }
            long old_configuration = configuration;
            this.configuration = this.configuration & (this.configuration - 1);
            return old_configuration ^ this.configuration;
        }
    }

    static public class TokenPositions implements Iterable<Long> {
        private long configuration;

        public TokenPositions(long configuration) {
            this.configuration = configuration & Utils.FIELD_BITMASK;
        }
        public Iterator<Long> iterator() {
            return new TokenIterator(this.configuration);
        }
    }

    static public class MovePositions implements Iterable<Long> {
        private long configuration;

        public MovePositions(long configuration) {
            this.configuration = configuration & Utils.FIELD_BITMASK;
        }
        public Iterator<Long> iterator() {
            return new MoveIterator(this.configuration);
        }
    }

    static public String configurationToString(long configuration, int playerNumber) {
        StringBuilder result = new StringBuilder(51);

        for (int y = Utils.FIELD_SIZE-1; y >= 0; y--) {
            for (int x = 0; x < Utils.FIELD_SIZE; x++) {
                final long position = (1L << (x + y * Utils.FIELD_SIZE));
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
        configuration = configuration & Utils.FIELD_BITMASK;
        int count = 0;
        while (configuration != 0) {
            configuration &= (configuration - 1);
            count++;
        }
        return count;
    }

    public static boolean isConfigurationFinished(long configuration) {
        return (getNumTokensToPlay(configuration) == 0) && ((configuration & Utils.FIELD_BITMASK) == 0);
    }

    public static int getNumTokensFinished(long configuration) {
        long numTokensInGame = (configuration & Utils.TOKENS_TO_PLAY_BITMASK) >> Utils.NUM_FIELDS;
        numTokensInGame += getNumTokens(configuration);
        return (int)(Utils.NUM_TOKENS - numTokensInGame);
    }

    static int getNumTokensToPlay(long configuration) {
        long numTokensToPlay = configuration & Utils.TOKENS_TO_PLAY_BITMASK;
        return (int)(numTokensToPlay >> Utils.NUM_FIELDS);
    }

    static long setNumTokensToPlay(long targetPlayerState, int numTokensToPlay) {
        return (targetPlayerState & ~Utils.TOKENS_TO_PLAY_BITMASK) | (((long) numTokensToPlay) << Utils.NUM_FIELDS);
    }

    public static boolean isPlayerActive(long configuration) {
        return (configuration & Utils.PLAYER_ACTIVE_BITMASK) != 0;
    }

    static long setPlayerInactive(long configuration) {
        return configuration & ~Utils.PLAYER_ACTIVE_BITMASK;
    }

    static long setPlayerActive(long configuration) {
        return configuration | Utils.PLAYER_ACTIVE_BITMASK;
    }

    @SuppressWarnings("unused")
    static String configurationToString(long configuration) {
        StringBuilder result = new StringBuilder(51);
        for (int y = Utils.FIELD_SIZE-1; y >= 0; y--) {
            for (int x = 0; x < Utils.FIELD_SIZE; x++) {
                if ((configuration & (1L << (x + y* Utils.FIELD_SIZE))) != 0) {
                    result.append('X');
                } else {
                    result.append('~');
                }
            }
            result.append('\n');
        }
        return result.toString();
    }
}
