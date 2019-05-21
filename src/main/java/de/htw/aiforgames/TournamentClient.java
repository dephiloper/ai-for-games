package de.htw.aiforgames;

import de.htw.aiforgames.algorithm.DecisionRuleAlgorithm;
import de.htw.aiforgames.algorithm.Rater;

public class TournamentClient {
    public static void main(String[] args) {
        if (args.length == 1) {
            Client client = Client.create(args[0], "Team Human", "/test1.png", new DecisionRuleAlgorithm(7, Rater.withLearned1(), false));
            client.execute();
        } else {
            System.out.println("usage: client <hostname>");
        }
    }
}
