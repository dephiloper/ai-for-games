package de.htw.aiforgames.algorithm;

import de.htw.aiforgames.Client;
import de.htw.aiforgames.GameState;
import de.htw.aiforgames.Utils;
import lenz.htw.sawhian.Move;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HumanAlgorithm implements DecisionAlgorithm {
    private int playerNumber;
    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    @Override
    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    @Override
    public long getNextMove(GameState state) {
        String[] input = null;

        System.out.println(state);

        long possibleMoves = state.getNextPossibleMoves();

        if (possibleMoves == 0L) {
            return Client.createMove(new Move(playerNumber, -1, -1));
        }

        long move;
        int x;
        int y;
        while (true) {
            System.out.print("Type your decision like x,y: ");

            try {
                input = (reader.readLine()).split(",");

                x = Integer.parseInt(input[0]) - 1;
                y = Integer.parseInt(input[1]) - 1;
            } catch (Exception e) {
                try {
                    reader.reset();
                } catch (IOException ignored) { }
                System.out.println("Invalid input, try again!");
                continue;
            }

            if ((x < 0) || (x > 6) || (y < 0) || (y > 6)) {
                System.out.println("Invalid input, try again!");
                continue;
            }

            move = 1L << (x + y*7);
            if ((move & possibleMoves) != 0) {
                break;
            } else {
                System.out.println("Invalid move!");
            }
        }

        return Client.createMove(new Move(playerNumber, x, y));
    }
}
