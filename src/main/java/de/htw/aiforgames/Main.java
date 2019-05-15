package de.htw.aiforgames;

import de.htw.aiforgames.algorithm.DecisionRuleAlgorithm;
import de.htw.aiforgames.algorithm.RandomAlgorithm;
import lenz.htw.sawhian.Server;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> Server.runOnceAndReturnTheWinner(5)).start();
        Thread.sleep(2000);

        Client decisionClient = Client.create(null, "Team 0", new DecisionRuleAlgorithm(10));
        Client randomClient0 = Client.create(null, "Team 1", new RandomAlgorithm());
        Client randomClient1 = Client.create(null, "Team 2", new RandomAlgorithm());
        Client randomClient2 = Client.create(null, "Team 3", new RandomAlgorithm());

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
        System.out.println("client 2 started");
    }
}
