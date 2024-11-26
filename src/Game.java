import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Game {
        Player[] players = new Player[6];
         int[] deck = Deck.fullDeckArray();
         final LookupTable lookupTable = new LookupTable();
         int[] table;
        public void generatePlayerCards() {
                int index = 0;
                for (int i = 0; i < players.length; i++) {
                        players[i] = new Player(deck[index++], deck[index++]);
                }
        }

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
                fold(0);
                // Draw flop cards and calculate flop equity
                calculateFlopEquity();
                fold(1);
                calculateTurnEquity();
                fold(2);
                calculateRiverEquity();

        }

        public void fold(int i){

                int activePlayerCount = players.length;
                                // Shift all elements to the left, overwriting the current player
                                for (int j = i; j < activePlayerCount - 1; j++) {
                                        players[j] = players[j + 1];
                                }
                                players[activePlayerCount - 1] = null; // Clear the last element
                                activePlayerCount--; // Decrease the count of active players
                                i--; // Decrement index to handle the shifted element



                // Create a trimmed array to display the remaining players
                Player[] remainingPlayers = new Player[activePlayerCount];
                System.arraycopy(players, 0, remainingPlayers, 0, activePlayerCount);
                players = remainingPlayers;
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
                                        totalWins[i] += threadWins[i];
                                }
                        } catch (ExecutionException e) {
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
                // Randomly draw a flop (3 cards) from the deck
                List<Integer> deckList = new ArrayList<>();
                for (int card : deck) {
                        deckList.add(card);
                }
                Collections.shuffle(deckList); // Shuffle the deck
                table = new int[]{deckList.removeFirst(), deckList.removeFirst(), deckList.removeFirst(),0,0}; // Draw 3 cards for the flop

                // Remove the flop cards from the remaining deck
                int[] remainingDeck = deckList.stream().mapToInt(Integer::intValue).toArray();

                // Generate all turn and river combinations
                List<int[]> turnRiverCombinations = generateCombinations(remainingDeck, 2);
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
                for (int card : deck) {
                        if (Arrays.stream(table).noneMatch(fc -> fc == card)) { // Exclude flop cards
                                deckList.add(card);
                        }
                }
                Collections.shuffle(deckList);

                // Draw the turn card
                table[3] = deckList.removeFirst();

                // Generate all river combinations from the remaining deck
                int[] remainingDeck = deckList.stream().mapToInt(Integer::intValue).toArray();
                List<int[]> riverCombinations = generateCombinations(remainingDeck, 1);

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
                for (int card : deck) {
                        if (Arrays.stream(table).noneMatch(fc -> fc == card)) { // Exclude flop and turn cards
                                deckList.add(card);
                        }
                }
                Collections.shuffle(deckList);

                // Draw the river card
                table[4] = deckList.removeFirst();

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
                        double winPercentage = totalWins[i] / totalCombinations * 100;
                        players[i].setEquity(winPercentage);
                        System.out.println("Player " + i + " total wins = " + totalWins[i] + " percentage = " + String.format("%.2f", winPercentage) + "%");
                }

                System.out.println("\nPlayer Cards:");
                for (int i = 0; i < players.length; i++) {
                        int[] playerCards = players[i].getCards();
                        System.out.println("Player " + i + ": " + Card.cardToString(playerCards[0]) + " " + Card.cardToString(playerCards[1]));
                }
        }
}
