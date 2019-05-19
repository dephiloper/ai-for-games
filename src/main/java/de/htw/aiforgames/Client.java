package de.htw.aiforgames;

import de.htw.aiforgames.algorithm.DecisionAlgorithm;
import lenz.htw.sawhian.Move;
import lenz.htw.sawhian.net.NetworkClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

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

        this.decisionAlgorithm.setPlayerNumber(playerNumber);
    }

    public static Client create(String serverAddress, String teamName, DecisionAlgorithm decisionAlgorithm) {
        return create(serverAddress, teamName, "/test1.png", decisionAlgorithm);
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

    public void execute() {
        while (!gameState.isGameOver()) {
            Move serverMove;
            try {
                serverMove = this.networkClient.receiveMove(); // blocking
            } catch (RuntimeException ex) {
                System.out.println("client " + playerNumber + " is out.");
                break;
            }
            if (serverMove == null) { // our turn
                while (playerNumber != gameState.getCurrentPlayer()) {
                    gameState = gameState.createStateFromMove(Utils.INVALID_MOVE);
                }
                var move = this.decisionAlgorithm.getNextMove(this.gameState);
                serverMove = createServerMove(this.playerNumber, move);
                this.networkClient.sendMove(serverMove);
            } else { // enemy turn
                while (serverMove.player != gameState.getCurrentPlayer()) {
                    gameState = gameState.createStateFromMove(Utils.INVALID_MOVE);
                }
                var move = createMove(serverMove);
                this.gameState = this.gameState.checkedCreateStateFromMove(move);
            }
        }
    }

    public static long createMove(Move serverMove) {
        return 1L << (serverMove.y * Utils.FIELD_SIZE + serverMove.x);
    }

    private static Move createServerMove(int playerNumber, long move) {
        var serverMove = Utils.floorLog2(move);
        var x = serverMove % Utils.FIELD_SIZE;
        var y = serverMove / Utils.FIELD_SIZE;
        return new Move(playerNumber, x, y);
    }
}
