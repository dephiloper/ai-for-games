package de.htw.aiforgames;

import de.htw.aiforgames.algorithm.DecisionAlgorithm;
import lenz.htw.sawhian.Move;
import lenz.htw.sawhian.net.NetworkClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigInteger;

public class Client {
    private int playerNumber;
    private NetworkClient networkClient;
    private DecisionAlgorithm decisionAlgorithm;
    private GameState gameState;

    private Client(int playerNumber, NetworkClient networkClient, DecisionAlgorithm decisionAlgorithm) {
        this.playerNumber = playerNumber;
        this.networkClient = networkClient;
        this.decisionAlgorithm = decisionAlgorithm;
        this.gameState = GameState.newEmptyGameState();
    }

    public static Client create(String serverAddress, String teamName, DecisionAlgorithm decisionAlgorithm) {
        return create(serverAddress, teamName, "test1.png", decisionAlgorithm);
    }

    public static Client create(String serverAddress, String teamName, String logoPath, DecisionAlgorithm decisionAlgorithm) {
        BufferedImage logo;
        try {
            logo = ImageIO.read(Client.class.getResourceAsStream(logoPath));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        var networkClient = new NetworkClient(serverAddress, teamName, logo);
        return new Client(networkClient.getMyPlayerNumber(), networkClient, decisionAlgorithm);
    }

    void run() {
        while (!gameState.isGameOver()) {
            var serverMove = this.networkClient.receiveMove(); // blocking
            if (serverMove == null) { // our turn
                var move = this.decisionAlgorithm.getNextMove(this.gameState);
                serverMove = createServerMove(this.playerNumber, move);
                this.networkClient.sendMove(serverMove);
                this.gameState = this.gameState.checkedCreateStateFromMove(move);
            } else { // enemy turn
                var move = createMove(serverMove);
                this.gameState = this.gameState.checkedCreateStateFromMove(move);
            }
        }
    }

    private static long createMove(Move serverMove) {
        return BigInteger.valueOf(2).pow(serverMove.y).longValue() + serverMove.x;
    }

    private static Move createServerMove(int playerNumber, long move) {
        var serverMove = Utils.floorLog2(move);
        var x = serverMove % 7;
        var y = serverMove / 7;

        return new Move(playerNumber, x, y);
    }
}
