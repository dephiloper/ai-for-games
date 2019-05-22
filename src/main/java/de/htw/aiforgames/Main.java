package de.htw.aiforgames;

import de.htw.aiforgames.algorithm.*;

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
        //new Thread(() -> Server.runOnceAndReturnTheWinner(60)).start();
        //Thread.sleep(1000);

        ClientWrapper client0 = new ClientWrapper(
                null,
                "Bot learned 4",
                new DecisionRuleAlgorithm(7, Rater.withLearned4(), false)
        );
        ClientWrapper client1 = new ClientWrapper(
                null,
                "Bot learned 3",
                // new DecisionRuleAlgorithm(7, Rater.withLearned3(), false)
                new RandomAlgorithm()
        );
        ClientWrapper client2 = new ClientWrapper(
                null,
                "Bot default",
                // new DecisionRuleAlgorithm(7, Rater.withDefaults(), false)
                new RandomAlgorithm()
        );
        ClientWrapper client3 = new ClientWrapper(
                null,
                "Bot learned 2",
                // new DecisionRuleAlgorithm(7, Rater.withLearned2(), false)
                new RandomAlgorithm()
        );

        new Thread(client0).start();
        new Thread(client1).start();
        new Thread(client2).start();
        new Thread(client3).start();
    }
}
