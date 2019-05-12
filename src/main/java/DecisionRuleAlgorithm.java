public class DecisionRuleAlgorithm {

    private int initialDepth;
    private int playerNumber;
    private long selectedMove = 0L;

    public int minmax(GameState state, int depth) {
        var moves = state.getNextPossibleMoves(((byte) state.getCurrentPlayer()));
        if (depth == 0 || moves == 0 || state.isGameOver())
            return state.calculateScore(playerNumber);

        var topScore = playerNumber == state.getCurrentPlayer() ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (var move : new Configurations.TokenPositions(moves)) {
            var newState = state.createStateFromMove(currentPlayer, move);
            var score = minmax(newState, depth - 1);

            if (playerNumber == state.getCurrentPlayer()) {
                if (score > topScore) {
                    topScore = score;

                    if (depth == initialDepth) {
                        selectedMove = move;
                    }
                }
            } else {
                if (score < topScore) {
                    topScore = score;
                }
            }
        }

        return topScore;
    }

    public static void main(String[] args) {
        var dra = new DecisionRuleAlgorithm();
        dra.playerNumber = 0;
        dra.initialDepth = 3;
        var state = GameState.newEmptyGameState();
        var score = dra.minmax(state, dra.initialDepth);
        System.out.println(dra.selectedMove);

    }
}
