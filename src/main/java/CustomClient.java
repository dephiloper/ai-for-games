import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import lenz.htw.sawhian.Move;
import lenz.htw.sawhian.Server;
import lenz.htw.sawhian.net.NetworkClient;

public class CustomClient {
    public static void main(String[] args) throws IOException {
        Server server = new Server();
        BufferedImage logo = ImageIO.read(new File("meinBild.png"));
        NetworkClient client = new NetworkClient("ip adresse server (null = localhost)", "CLIENT 0", logo);

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