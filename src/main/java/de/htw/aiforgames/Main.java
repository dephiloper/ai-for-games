package de.htw.aiforgames;

import de.htw.aiforgames.algorithm.DecisionAlgorithm;
import de.htw.aiforgames.algorithm.DecisionRuleAlgorithm;
import de.htw.aiforgames.algorithm.HumanAlgorithm;
import de.htw.aiforgames.algorithm.RandomAlgorithm;
import de.htw.aiforgames.algorithm.Rater;
import lenz.htw.sawhian.Server;

import java.util.Collections;
import java.util.Random;

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
                "Bot Learned3",
                new DecisionRuleAlgorithm(7, Rater.withLearned3(), false)
        );
        ClientWrapper client1 = new ClientWrapper(
                null,
                "Bot Learned1",
                new DecisionRuleAlgorithm(7, Rater.withLearned(), false)
        );
        ClientWrapper client2 = new ClientWrapper(
                null,
                "Bot Default",
                new DecisionRuleAlgorithm(7, Rater.withDefaults(), false)
        );
        ClientWrapper client3 = new ClientWrapper(
                null,
                "Bot Random",
                new RandomAlgorithm()
        );

        new Thread(client0).start();
        new Thread(client1).start();
        new Thread(client2).start();
        new Thread(client3).start();
    }
}
