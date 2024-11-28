import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Game {
        Player[] players = new Player[6];
         int[] deck = Deck.fullDeckArray();
         final LookupTable lookupTable = new LookupTable();
         int[] table = new int[5];


        public Player[] getPlayers() {
                return players;
        }

        public int[] getTable(){
                return table;
        }

        public void insertPlayerCards() {
                players[0] = new Player(deck[24], deck[12]);
                players[1] = new Player(deck[2], deck[17]);
                players[2] = new Player(deck[25], deck[40]);
                players[3] = new Player(deck[11], deck[50]);
                players[4] = new Player(deck[46], deck[47]);
                players[5] = new Player(deck[48], deck[49]);

                Set<Integer> usedCards = new HashSet<>();
                for (Player player : players) {
                        for (int card : player.getCards()) {
                                usedCards.add(card);
                        }
                }
                deck = Arrays.stream(deck).filter(card -> !usedCards.contains(card)).toArray();
        }

        public void play() throws InterruptedException {
                deck = Deck.fullDeckArray();
                insertPlayerCards();
                // Pre-flop equity calculation
                calculatePreFlopEquity();
                players[0].fold();
                // Draw flop cards and calculate flop equity
                calculateFlopEquity();
                calculateTurnEquity();
                calculateRiverEquity();

        }

        void calculatePreFlopEquity() throws InterruptedException {
                List<int[]> communityCardCombinations = generateCombinations(deck, 5);

                // Multithreading setup
                int numThreads = Runtime.getRuntime().availableProcessors();
                int batchSize = (int) Math.ceil((double) communityCardCombinations.size() / numThreads);
                List<List<int[]>> batches = new ArrayList<>();
                for (int i = 0; i < communityCardCombinations.size(); i += batchSize) {
                        int end = Math.min(i + batchSize, communityCardCombinations.size());
                        batches.add(communityCardCombinations.subList(i, end));
                }

                ExecutorService executor = Executors.newFixedThreadPool(numThreads);
                List<Future<double[]>> futures = new ArrayList<>();
                for (List<int[]> batch : batches) {
                        futures.add(executor.submit(() -> simulateGamesForBatch(batch)));
                }

                // Aggregate results from threads
                double[] totalWins = new double[players.length];
                for (Future<double[]> future : futures) {
                        try {
                                double[] threadWins = future.get();
                                for (int i = 0; i < totalWins.length; i++) {
                                        if (!players[i].getFolded()) { // Exclude folded players
                                                totalWins[i] += threadWins[i];
                                        }
                                }
                        } catch (ExecutionException e) {
                            //noinspection CallToPrintStackTrace
                            e.printStackTrace();
                        }
                }

                executor.shutdown();

                // Print pre-flop results
                System.out.println("Pre-flop results:");
                printResults(totalWins, communityCardCombinations.size());
        }

        double[] simulateGamesForBatch(List<int[]> batch) {
                double[] wins = new double[players.length];
                for (int[] communityCards : batch) {
                        simulateGameForCommunityCombination(communityCards, wins);
                }
                return wins;
        }

        void calculateFlopEquity() {
                List<Integer> deckList = new ArrayList<>();
                // Randomly draw a flop (3 cards) from the deck
                if(!(tableCard() == 3)){
                        for (int card : deck) {
                                deckList.add(card);
                        }
                        Collections.shuffle(deckList); // Shuffle the deck
                        table = new int[]{deckList.removeFirst(), deckList.removeFirst(), deckList.removeFirst(),0,0}; // Draw 3 cards for the flop
                        deck = deckList.stream().mapToInt(Integer::intValue).toArray();
                }

                // Remove the flop cards from the remaining deck


                // Generate all turn and river combinations
                List<int[]> turnRiverCombinations = generateCombinations(deck, 2);
                double[] totalWins = new double[players.length];

                // Simulate games for this specific flop
                for (int[] turnRiver : turnRiverCombinations) {
                        int[] communityCards = new int[5];
                        System.arraycopy(table, 0, communityCards, 0, 3);
                        System.arraycopy(turnRiver, 0, communityCards, 3, 2);

                        simulateGameForCommunityCombination(communityCards, totalWins);
                }

                // Print flop results
                System.out.println("Flop results (for specific flop):");
                int totalCombinations = turnRiverCombinations.size();
                printResults(totalWins, totalCombinations);

                // Print the drawn flop cards
                System.out.println("Drawn Flop: " + Card.cardToString(table[0]) + ", " + Card.cardToString(table[1]) + ", " + Card.cardToString(table[2]));
        }

        void calculateTurnEquity() {
                // Convert remaining deck into a list for shuffling
                List<Integer> deckList = new ArrayList<>();
                if(!(tableCard() == 4)){
                        for (int card : deck)
                                // Exclude flop cards
                                deckList.add(card);

                        Collections.shuffle(deckList);

                        // Draw the turn card
                        table[3] = deckList.removeFirst();
                        deck = deckList.stream().mapToInt(Integer::intValue).toArray();
                }

                // Generate all river combinations from the remaining deck
                List<int[]> riverCombinations = generateCombinations(deck, 1);

                double[] totalWins = new double[players.length];

                // Simulate games for this specific turn
                for (int[] river : riverCombinations) {
                        int[] communityCards = new int[5];
                        System.arraycopy(table, 0, communityCards, 0, 3);
                        communityCards[3] = table[3];
                        communityCards[4] = river[0];

                        simulateGameForCommunityCombination(communityCards, totalWins);
                }

                // Print turn results
                System.out.println("Turn results (for specific turn):");
                int totalCombinations = riverCombinations.size();
                printResults(totalWins, totalCombinations);

                // Print the drawn turn card
                System.out.println("Drawn Turn: " + Card.cardToString(table[0])+ ", "+ Card.cardToString(table[1])+ ", "+ Card.cardToString(table[2])+ ", " + Card.cardToString(table[3]));
        }

        void calculateRiverEquity() {
                // Convert remaining deck into a list for shuffling
                List<Integer> deckList = new ArrayList<>();
                if(!(tableCard() == 5)){
                        for (int card : deck)
                                deckList.add(card);
                        Collections.shuffle(deckList);

                        // Draw the river card
                        table[4] = deckList.removeFirst();
                }


                // Form the full community cards
                int[] communityCards = new int[5];
                System.arraycopy(table, 0, communityCards, 0, 5);

                double[] totalWins = new double[players.length];

                // Simulate the game for this specific river
                simulateGameForCommunityCombination(communityCards, totalWins);

                // Print river results
                System.out.println("River results (for specific river):");
                printResults(totalWins, 1);

                // Print the drawn river card
                System.out.println("Drawn river: " + Card.cardToString(table[0])+ ", "+ Card.cardToString(table[1])+ ", "+ Card.cardToString(table[2])+ ", " + Card.cardToString(table[3]) +  ", " + Card.cardToString(table[4])) ;
        }

        private void simulateGameForCommunityCombination(int[] communityCards, double[] wins) {
                int bestRank = Integer.MAX_VALUE;
                List<Integer> winners = new ArrayList<>();

                for (int i = 0; i < players.length; i++) {
                        if (players[i].getFolded()) continue; // Skip folded players

                        int rank = bestPlay(players[i].getCards(), communityCards);
                        if (rank < bestRank) {
                                bestRank = rank;
                                winners.clear();
                                winners.add(i);
                        } else if (rank == bestRank) {
                                winners.add(i);
                        }
                }

                double splitPoints = 1.0 / winners.size();
                for (int winner : winners) {
                        wins[winner] += splitPoints;
                }
        }

        public int bestPlay(int[] playerCards, int[] communityCards) {
                int[] allCards = new int[playerCards.length + communityCards.length];
                System.arraycopy(playerCards, 0, allCards, 0, playerCards.length);
                System.arraycopy(communityCards, 0, allCards, playerCards.length, communityCards.length);

                List<int[]> allHands = generateCombinations(allCards, 5);
                int bestRank = Integer.MAX_VALUE;

                for (int[] hand : allHands) {
                        int rank = lookupTable.evaluateHand(hand);
                        bestRank = Math.min(bestRank, rank);
                }

                return bestRank;
        }

        List<int[]> generateCombinations(int[] cards, int k) {
                List<int[]> combinations = new ArrayList<>();
                int[] indices = new int[k];
                for (int i = 0; i < k; i++) {
                        indices[i] = i;
                }

                while (true) {
                        int[] combination = new int[k];
                        for (int i = 0; i < k; i++) {
                                combination[i] = cards[indices[i]];
                        }
                        combinations.add(combination);

                        int i = k - 1;
                        while (i >= 0 && indices[i] == cards.length - k + i) {
                                i--;
                        }
                        if (i < 0) break;
                        indices[i]++;
                        for (int j = i + 1; j < k; j++) {
                                indices[j] = indices[j - 1] + 1;
                        }
                }

                return combinations;
        }

        void printResults(double[] totalWins, int totalCombinations) {
                for (int i = 0; i < players.length; i++) {
                        if (!players[i].getFolded()) { // Print only active players
                                double winPercentage = totalWins[i] / totalCombinations * 100;
                                players[i].setEquity(winPercentage);
                                System.out.println("Player " + i + " total wins = " + totalWins[i] + " percentage = " + String.format("%.2f", winPercentage) + "%");
                        }
                }

                System.out.println("\nPlayer Cards:");
                for (int i = 0; i < players.length; i++) {
                        if (!players[i].getFolded()) { // Print cards only for active players
                                int[] playerCards = players[i].getCards();
                                System.out.println("Player " + i + ": " + Card.cardToString(playerCards[0]) + " " + Card.cardToString(playerCards[1]));
                        }
                }
        }

        public boolean addTableCard(int card,boolean random){
                if(!random)
                        if(!presentInDeck(card))
                                return false;
                for (int i = 0; i < table.length; i++) {
                        if (table[i] == 0) {
                                table[i] = card;
                                removeFromDeck(card);
                                return true;
                        }
                }
                return false;
        }

        public boolean presentInDeck(int card){
            for (int j : deck) {
                if (j == card)
                    return true;
            }
                return false;
        }

        public int randomCard() {
                int i = (int) (Math.random() * deck.length);
                int temp = deck[i];
               removeFromDeck(temp);
                return temp;
        }

        public void removeFromDeck(int t){
                Set<Integer> usedCards = new HashSet<>();
                usedCards.add(t);
                deck = Arrays.stream(deck).filter(card -> !usedCards.contains(card)).toArray();
        }

        public int tableCard(){
                int i = 0;
                for(int t : table)
                        if(t != 0)
                                i++;
                return i;
        }
}
