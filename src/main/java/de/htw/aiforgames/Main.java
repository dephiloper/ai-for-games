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
        new Thread(() -> Server.runOnceAndReturnTheWinner(5)).start();
        Thread.sleep(2000);

        ClientWrapper decisionClient = new ClientWrapper(null, "Team 0", new DecisionRuleAlgorithm(7));
        ClientWrapper randomClient0 = new ClientWrapper(null, "Team 1", new RandomAlgorithm());
        ClientWrapper randomClient1 = new ClientWrapper(null, "Team 2", new RandomAlgorithm());
        ClientWrapper randomClient2 = new ClientWrapper(null, "Team 3", new RandomAlgorithm());

        new Thread(decisionClient).start();
        System.out.println("client 0 started");
        Thread.sleep(1000);

        new Thread(randomClient0).start();
        System.out.println("client 1 started");
        Thread.sleep(1000);

        new Thread(randomClient1).start();
        System.out.println("client 2 started");
        Thread.sleep(1000);

        new Thread(randomClient2).start();
        System.out.println("client 3 started");
    }
}
