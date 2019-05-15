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

    public static void main(String[] args) throws InterruptedException {
        // new Thread(() -> Server.runOnceAndReturnTheWinner(5)).start();
        // Thread.sleep(2000);

        ClientWrapper decisionClient = new ClientWrapper(null, "Team A", new DecisionRuleAlgorithm(9));
        ClientWrapper randomClient0 = new ClientWrapper(null, "Team B", new RandomAlgorithm());
        ClientWrapper randomClient1 = new ClientWrapper(null, "Team C", new RandomAlgorithm());
        ClientWrapper randomClient2 = new ClientWrapper(null, "Team D", new RandomAlgorithm());

        new Thread(decisionClient).start();
        new Thread(randomClient0).start();
        new Thread(randomClient1).start();
        new Thread(randomClient2).start();
    }
}
