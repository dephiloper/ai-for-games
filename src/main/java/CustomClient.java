import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

import lenz.htw.sawhian.Move;
// import lenz.htw.sawhian.Server;
import lenz.htw.sawhian.net.NetworkClient;

public class CustomClient {
    public static void main(String[] args) throws IOException {
        /*
        new Thread(() -> { runRandomClient("Client 0", "test1.png"); }).start();
        new Thread(() -> { runRandomClient("Client 1", "test1.png"); }).start();
        new Thread(() -> { runRandomClient("Client 2", "test1.png"); }).start();
        new Thread(() -> { runRandomClient("Client 3", "test1.png"); }).start();
         */
        GameState.test();
    }

    private static void runRandomClient(final String clientName, final String imagePath) {
        BufferedImage logo;
        try {
            logo = ImageIO.read(CustomClient.class.getResourceAsStream(imagePath));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        NetworkClient client = new NetworkClient(null, clientName, logo);

        client.getMyPlayerNumber();
        client.getTimeLimitInSeconds();
        client.getExpectedNetworkLatencyInMilliseconds();

        while (true) {
            Move move = client.receiveMove();
            if (move == null) {
                //move = meinTotalClevererZug();
                //uncleverer Zug:
                move = new Move(client.getMyPlayerNumber(),0,0);
                client.sendMove(move);
            } else {
                // moveInMeinSpielfeldIntegrieren(move);
            }
        }
    }
}