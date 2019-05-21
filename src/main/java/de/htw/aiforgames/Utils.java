package de.htw.aiforgames;

public class Utils {
    public static final int FIELD_SIZE = 7;
    public static final int NUM_TOKENS = 7;
    static final int NUM_FIELDS = 49;
    static final long FIELD_BITMASK =            0b0000000000000001111111111111111111111111111111111111111111111111L;
    static final long TOKENS_TO_PLAY_BITMASK =   0b0000000000001110000000000000000000000000000000000000000000000000L;
    public static final long WON_BITMASK = FIELD_BITMASK | TOKENS_TO_PLAY_BITMASK;
    public static final long[] BASE_LINES = new long[] {0b0000000000000000000000000000000000000000000000000000000001111111L,
            0b0000000000000000000001000000100000010000001000000100000010000001L,
            0b0000000000000001111111000000000000000000000000000000000000000000L,
            0b0000000000000001000000100000010000001000000100000010000001000000L
    };
    static final long[] ENEMY_BASE_LINES = new long[] {BASE_LINES[2], BASE_LINES[3], BASE_LINES[0], BASE_LINES[1]};
    static final long PLAYER_ACTIVE_BITMASK =    0b0000000000010000000000000000000000000000000000000000000000000000L;
    static final long PLAYER_INIT_CONFIGURATION = PLAYER_ACTIVE_BITMASK | TOKENS_TO_PLAY_BITMASK;

    public static final int NUM_PLAYERS = 4;
    public static final int MAX_NEXT_MOVES = 13;
    public static final long INVALID_MOVE = 0L;

    public static final int PROGRESS_BAR_SIZE = 100;

    final static public int[] PLAYER_DIRECTIONS = new int[] {
            FIELD_SIZE,
            1,
            -FIELD_SIZE,
            -1
    };
    final static public int[] BASELINE_POS = new int[] {0, 0, FIELD_SIZE-1, FIELD_SIZE-1};
    final static public int[][] PLAYER_NUMBER_TO_XY_POSITIONS = new int[4][];

    static {
        int[] XPOSITIONS = new int[NUM_FIELDS];
        int[] YPOSITIONS = new int[NUM_FIELDS];

        for (int y = 0; y < FIELD_SIZE; y++) {
            for (int x = 0; x < FIELD_SIZE; x++) {
                int pos = x + y * FIELD_SIZE;
                XPOSITIONS[pos] = x;
                YPOSITIONS[pos] = y;
            }
        }

        PLAYER_NUMBER_TO_XY_POSITIONS[0] = YPOSITIONS;
        PLAYER_NUMBER_TO_XY_POSITIONS[1] = XPOSITIONS;
        PLAYER_NUMBER_TO_XY_POSITIONS[2] = YPOSITIONS;
        PLAYER_NUMBER_TO_XY_POSITIONS[3] = XPOSITIONS;
    }

    public static int floorLog2(long n){
        return 63 - Long.numberOfLeadingZeros(n);
    }

    public static void printProgress(int n, int max) {
        n++;

        StringBuilder sb = new StringBuilder();

        sb.append('|');

        float ratio = n / (float)max;

        for (int i = 0; i < (int)(ratio*PROGRESS_BAR_SIZE); i++) {
            sb.append('#');
        }

        for (int i = (int)(ratio*PROGRESS_BAR_SIZE); i < PROGRESS_BAR_SIZE; i++) {
            sb.append('~');
        }

        sb.append("|[");
        sb.append(n);
        sb.append('/');
        sb.append(max);
        if (n == max) {
            sb.append("]\n");
        } else {
            sb.append("]\r");
        }

        System.out.print(sb);
    }
}
