import java.util.Iterator;

public class Configurations {
    static final byte FIELD_SIZE = 7;
    private static final byte NUM_TOKENS = 7;
    private static final byte NUM_FIELDS = 49;
    static final long FIELD_BITMASK =            0b0000000000000001111111111111111111111111111111111111111111111111L;
    static final long TOKENS_TO_PLAY_BITMASK =   0b0000000000001110000000000000000000000000000000000000000000000000L;
    static final long[] BASE_LINES = new long[] {0b0000000000000000000000000000000000000000000000000000000001111111L,
                                                 0b0000000000000000000001000000100000010000001000000100000010000001L,
                                                 0b0000000000000001111111000000000000000000000000000000000000000000L,
                                                 0b0000000000000001000000100000010000001000000100000010000001000000L
    };

    static long setNumTokensToPlay(long targetPlayerState, byte numTokensToPlay) {
        return (targetPlayerState & ~TOKENS_TO_PLAY_BITMASK) | (((long) numTokensToPlay) << NUM_FIELDS);
    }

    static private class TokenIterator implements Iterator<Long> {
        private long configuration;

        private TokenIterator(long configuration) {
            this.configuration = configuration;
        }

        public boolean hasNext() {
            return this.configuration != 0;
        }

        public Long next() {
            /*
            See https://www.geeksforgeeks.org/count-set-bits-in-an-integer/ for inspiration.
             */
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

    public static byte getNumTokensFinished(long configuration) {
        long numTokensToPlay = configuration & TOKENS_TO_PLAY_BITMASK;
        return (byte)(NUM_TOKENS - (numTokensToPlay >> NUM_FIELDS));
    }

    static byte getNumTokensToPlay(long configuration) {
        long numTokensToPlay = configuration & TOKENS_TO_PLAY_BITMASK;
        return (byte)(numTokensToPlay >> NUM_FIELDS);
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
