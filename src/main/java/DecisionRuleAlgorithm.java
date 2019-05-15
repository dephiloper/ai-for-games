import java.util.ArrayList;
import java.util.Random;

public class DecisionRuleAlgorithm {

    private int initialDepth;
    private int playerNumber;
    private long selectedMove = 0L;

    /**
     * Depending on the moves we could take minmax returns the score of the current state.
     * lowerBound  @min: if we get something lower than lowerBound --> break;
     * upperBound @max: if we get something higher than upperBound --> break;
     *
     * Beispiel: Man nehme an wir sind in max und haben bereits einen topScore, dann interessieren uns nur Werte die höher sind als dieser topScore.
     * Deshalb gibt man in der max funktion den topScore als lowerBound an die min funktion. Sobald in der min funktion ein score ermittelt wird,
     * der kleiner ist als der lowerBound (topScore der aufrufenden max funktion) kann die weitere Berechnung abgebrochen werden.
     * vice versa gilt das auch für die min funktion
     *
     * @param state
     * @param depth
     * @return
     */
    public float minmax(GameState state, int depth, float lowerBound, float upperBound) {
        var moves = state.getNextPossibleMoves();

        if (depth == 0 || state.isGameOver()) {
            return state.calculateScore(playerNumber);
        }

        var topScore = playerNumber == state.getCurrentPlayer() ? lowerBound : upperBound;

        for (var move : new Configurations.MovePositions(moves)) {
            var newState = state.checkedCreateStateFromMove(move);

            if (playerNumber == state.getCurrentPlayer()) {
                var score = minmax(newState, depth - 1, topScore, upperBound);
                if (score > topScore) {
                    topScore = score;

                    if (depth == initialDepth) {
                        selectedMove = move;
                        System.out.println(String.format("minmax move: %d", log2(selectedMove)));
                    }

                    if (topScore > upperBound)
                        break;
                }
            } else {
                var score = minmax(newState, depth - 1, lowerBound, topScore);
                if (score < topScore) {
                    topScore = score;

                    if (topScore < lowerBound)
                        break;
                }
            }
        }

        return topScore;
    }

    public static void main(String[] args) {
        Random randomGenerator = new Random();
        var dra = new DecisionRuleAlgorithm();
        dra.playerNumber = 0;
        dra.initialDepth = 9;
        var state = GameState.newEmptyGameState();

        while (!state.isGameOver()) {
            if (state.getCurrentPlayer() == dra.playerNumber) {
                var score = dra.minmax(state, dra.initialDepth, -Float.MAX_VALUE, Float.MAX_VALUE);
                System.out.println(String.format("player: %d", state.getCurrentPlayer()));
                System.out.println("-----------------------------");
                System.out.println(String.format("this is the move we take: %d", log2(dra.selectedMove)));
                state = state.checkedCreateStateFromMove(dra.selectedMove);
                System.out.println(state);
            } else {
                var moves = new ArrayList<Long>();
                for (var move : new Configurations.TokenPositions(state.getNextPossibleMoves()))
                    moves.add(move);

                System.out.println(String.format("player %s", state.getCurrentPlayer()));
                if (moves.size() == 0) {
                    System.out.println(String.format("selected move: %d", 0L));
                    state = state.checkedCreateStateFromMove(0L);
                } else {
                    var index = randomGenerator.nextInt(moves.size());
                    var move = moves.get(index);
                    System.out.println(String.format("selected move: %d", log2(move)));
                    state = state.checkedCreateStateFromMove(move);
                }
                System.out.println(state);
            }
        }


    }

    private static int log2(long number) {
        return (int) (Math.log(number) / Math.log(2));
    }
}
