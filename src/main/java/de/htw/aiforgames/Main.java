package de.htw.aiforgames;

import de.htw.aiforgames.algorithm.DecisionAlgorithm;
import de.htw.aiforgames.algorithm.DecisionRuleAlgorithm;
import de.htw.aiforgames.algorithm.RandomAlgorithm;
import lenz.htw.sawhian.Server;

public class Main {

    private static class ClientWrapper implements Runnable {
        private String serverAddress;
        private String teamName;
        private DecisionAlgorithm algorithm;

        public ClientWrapper(String serverAddress, String teamName, DecisionAlgorithm algorithm) {
            this.serverAddress = serverAddress;
            this.teamName = teamName;
            this.algorithm = algorithm;
        }

        @Override
        public void run() {
            Client client = Client.create(serverAddress, teamName, algorithm);
            client.execute();
        }
    }

    // TODO: make server address editable
    public static void main(String[] args) throws InterruptedException {
        // new Thread(() -> Server.runOnceAndReturnTheWinner(10)).start();
        // Thread.sleep(1000);

        ClientWrapper decisionClient = new ClientWrapper(null, "Team Bot 9", new DecisionRuleAlgorithm(9));
        ClientWrapper randomClient0 = new ClientWrapper(null, "Team Bot 8", new DecisionRuleAlgorithm(8));
        ClientWrapper randomClient1 = new ClientWrapper(null, "Team Bot 7", new DecisionRuleAlgorithm(7));
        ClientWrapper randomClient2 = new ClientWrapper(null, "Team Bot 6", new DecisionRuleAlgorithm(6));

        new Thread(decisionClient).start();
        new Thread(randomClient0).start();
        new Thread(randomClient1).start();
        new Thread(randomClient2).start();
    }
}
