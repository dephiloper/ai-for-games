package de.htw.aiforgames.algorithm;

import de.htw.aiforgames.Client;
import de.htw.aiforgames.GameState;
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

        System.out.print("Type your decision like x,y: ");

        try {
            input = (reader.readLine()).split(",");
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert input != null;
        int[] position = {Integer.parseInt(input[0]),Integer.parseInt(input[1])};

        return Client.createMove(new Move(playerNumber, position[0], position[1]));
    }
}
