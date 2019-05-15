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
        while (true) {
            var serverMove = this.networkClient.receiveMove(); // blocking
            if (serverMove == null) { // our turn
                var move = this.decisionAlgorithm.getNextMove(this.gameState);
                serverMove = createServerMove(move);
                this.networkClient.sendMove(serverMove);

            } else {
                var move = createMove(serverMove);
                this.gameState = this.gameState.checkedCreateStateFromMove(move);
            }
        }
    }

    private static long createMove(Move serverMove) {

    }

    private static Move createServerMove(int playerNumber, long move) {
        var hrMove = Utils.floorLog2(move);
        var x = hrMove % 7;
        var y = hrMove / 7;

        return new Move(playerNumber, x, y);
    }
}
