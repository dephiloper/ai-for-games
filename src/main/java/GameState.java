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
    final private long[] configurations = new long[4];

    public GameState() {
    }

    public static Vector2 playerNumberToDirection(byte playerNumber) {
        switch (playerNumber) {
            case 0: return new Vector2((byte)0, (byte)1);
            case 1: return new Vector2((byte)1, (byte)0);
            case 2: return new Vector2((byte)0, (byte)-1);
            case 3: return new Vector2((byte)-1, (byte)0);
        }
        throw new RuntimeException(String.format("invalid player number: %d", playerNumber));
    }

    public long getNextPossibleMoves(byte playerNumber) {
        return 0;
    }
}
