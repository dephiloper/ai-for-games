import java.util.Iterator;

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
    final public byte FIELD_SIZE = 7;
    final public byte NUM_TOKENS = 7;
    final public byte NUM_FIELDS = FIELD_SIZE * FIELD_SIZE;
    final private long[] configurations = new long[4];
    final static private Vector2[] playerDirections = new Vector2[] {
        new Vector2((byte)0, (byte)1),
        new Vector2((byte)1, (byte)0),
        new Vector2((byte)0, (byte)-1),
        new Vector2((byte)-1, (byte)0)
    };

    private class TokenIterator implements Iterator<Long> {
        private long configuration;

        public TokenIterator(long configuration) {
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

    public void test(long configuration) {
        TokenIterator iter = new TokenIterator(configuration);
        while (iter.hasNext()) {
             long n = iter.next();
             System.out.println(Long.toBinaryString(n));
        }
    }

    public GameState() {
    }

    private static Vector2 playerNumberToDirection(byte playerNumber) {
        return playerDirections[playerNumber];
    }

    public byte getNextPossibleMoves(byte playerNumber, long[] next_moves) {
        long player_configuration = configurations[playerNumber];
        Vector2 player_direction = playerNumberToDirection(playerNumber);

        TokenIterator tokenIterator = new TokenIterator(player_configuration);
        long tokenPosition = 0;
        while ((tokenPosition = tokenIterator.next()) != 0) {

        }
        return 0;
    }

}
