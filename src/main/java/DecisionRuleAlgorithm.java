import java.util.ArrayList;
import java.util.Random;

public class DecisionRuleAlgorithm {

    private int initialDepth;
    private int playerNumber;
    private long selectedMove = 0L;

    public float minmax(GameState state, int depth) {
        var moves = state.getNextPossibleMoves();
        if (depth == 0 || moves == 0 || state.isGameOver())
            return state.calculateScore(playerNumber);

        var topScore = playerNumber == state.getCurrentPlayer() ? -Float.MAX_VALUE : Float.MAX_VALUE;

        for (var move : new Configurations.TokenPositions(moves)) {
            var newState = state.createStateFromMove(move);
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
        Random randomGenerator = new Random();
        var dra = new DecisionRuleAlgorithm();
        dra.playerNumber = 0;
        dra.initialDepth = 5;
        var state = GameState.newEmptyGameState();

        while (true) {
            var score = dra.minmax(state, dra.initialDepth);
            System.out.println(String.format("player: %d", state.getCurrentPlayer()));
            System.out.println(String.format("selected move: %d", log2(dra.selectedMove)));
            state = state.createStateFromMove(dra.selectedMove);
            System.out.println(state);

            for (var i = 0; i < 3; i++) {
                var moves = new ArrayList<Long>();
                for (var move : new Configurations.TokenPositions(state.getNextPossibleMoves()))
                    moves.add(move);

                System.out.println(String.format("player %s", state.getCurrentPlayer()));
                if (moves.size() == 0) {
                    System.out.println(String.format("selected move: %d", 0L));
                    state = state.createStateFromMove(0L);
                }
                else {
                    var index = randomGenerator.nextInt(moves.size());
                    var move = moves.get(index);
                    System.out.println(String.format("selected move: %d", log2(move)));
                    state = state.createStateFromMove(move);
                }
                System.out.println(state);
            }
        }


    }

    private static int log2(long number)
    {
        return (int)(Math.log(number) / Math.log(2));
    }
}
