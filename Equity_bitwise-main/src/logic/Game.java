package logic;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Game {
        private final Player[] players = new Player[6];
        private int[] deck;
        private final LookupTable lookupTable = new LookupTable();

        public void generatePlayerCards() {
                int index = 0;
                for (int i = 0; i < players.length; i++) {
                        players[i] = new Player(deck[index++], deck[index++]);
                }
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
                fold();
                // Draw flop cards and calculate flop equity
                int[] flopCard = calculateFlopEquity();

                int turnCard = calculateTurnEquity(flopCard);
                calculateRiverEquity(flopCard,turnCard);

        }

        public void fold(){
                int i = 0;
                for (Player player : players) {
                        System.out.println("player " + i++ + "want to fold? ");
                }
        }

        private void calculatePreFlopEquity() throws InterruptedException {
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

        private double[] simulateGamesForBatch(List<int[]> batch) {
                double[] wins = new double[players.length];
                for (int[] communityCards : batch) {
                        simulateGameForCommunityCombination(communityCards, wins);
                }
                return wins;
        }

        private int[] calculateFlopEquity() {
                // Randomly draw a flop (3 cards) from the deck
                List<Integer> deckList = new ArrayList<>();
                for (int card : deck) {
                        deckList.add(card);
                }
                Collections.shuffle(deckList); // Shuffle the deck
                int[] flop = {deckList.removeFirst(), deckList.removeFirst(), deckList.removeFirst()}; // Draw 3 cards for the flop

                // Remove the flop cards from the remaining deck
                int[] remainingDeck = deckList.stream().mapToInt(Integer::intValue).toArray();

                // Generate all turn and river combinations
                List<int[]> turnRiverCombinations = generateCombinations(remainingDeck, 2);
                double[] totalWins = new double[players.length];

                // Simulate games for this specific flop
                for (int[] turnRiver : turnRiverCombinations) {
                        int[] communityCards = new int[5];
                        System.arraycopy(flop, 0, communityCards, 0, 3);
                        System.arraycopy(turnRiver, 0, communityCards, 3, 2);

                        simulateGameForCommunityCombination(communityCards, totalWins);
                }

                // Print flop results
                System.out.println("Flop results (for specific flop):");
                int totalCombinations = turnRiverCombinations.size();
                printResults(totalWins, totalCombinations);

                // Print the drawn flop cards
                System.out.println("Drawn Flop: " + Card.cardToString(flop[0]) + ", " + Card.cardToString(flop[1]) + ", " + Card.cardToString(flop[2]));
                return flop;
        }

        private int calculateTurnEquity(int[] flop) {
                // Convert remaining deck into a list for shuffling
                List<Integer> deckList = new ArrayList<>();
                for (int card : deck) {
                        if (Arrays.stream(flop).noneMatch(fc -> fc == card)) { // Exclude flop cards
                                deckList.add(card);
                        }
                }
                Collections.shuffle(deckList);

                // Draw the turn card
                int turnCard = deckList.removeFirst();

                // Generate all river combinations from the remaining deck
                int[] remainingDeck = deckList.stream().mapToInt(Integer::intValue).toArray();
                List<int[]> riverCombinations = generateCombinations(remainingDeck, 1);

                double[] totalWins = new double[players.length];

                // Simulate games for this specific turn
                for (int[] river : riverCombinations) {
                        int[] communityCards = new int[5];
                        System.arraycopy(flop, 0, communityCards, 0, 3);
                        communityCards[3] = turnCard;
                        communityCards[4] = river[0];

                        simulateGameForCommunityCombination(communityCards, totalWins);
                }

                // Print turn results
                System.out.println("Turn results (for specific turn):");
                int totalCombinations = riverCombinations.size();
                printResults(totalWins, totalCombinations);

                // Print the drawn turn card
                System.out.println("Drawn Turn: " + Card.cardToString(flop[0])+ ", "+ Card.cardToString(flop[1])+ ", "+ Card.cardToString(flop[2])+ ", " + Card.cardToString(turnCard));
                return turnCard;
        }

        private void calculateRiverEquity(int[] flop, int turnCard) {
                // Convert remaining deck into a list for shuffling
                List<Integer> deckList = new ArrayList<>();
                for (int card : deck) {
                        if (Arrays.stream(flop).noneMatch(fc -> fc == card) && card != turnCard) { // Exclude flop and turn cards
                                deckList.add(card);
                        }
                }
                Collections.shuffle(deckList);

                // Draw the river card
                int riverCard = deckList.removeFirst();

                // Form the full community cards
                int[] communityCards = new int[5];
                System.arraycopy(flop, 0, communityCards, 0, 3);
                communityCards[3] = turnCard;
                communityCards[4] = riverCard;

                double[] totalWins = new double[players.length];

                // Simulate the game for this specific river
                simulateGameForCommunityCombination(communityCards, totalWins);

                // Print river results
                System.out.println("River results (for specific river):");
                printResults(totalWins, 1);

                // Print the drawn river card
                System.out.println("Drawn river: " + Card.cardToString(flop[0])+ ", "+ Card.cardToString(flop[1])+ ", "+ Card.cardToString(flop[2])+ ", " + Card.cardToString(turnCard) +  ", " + Card.cardToString(riverCard)) ;
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

        private List<int[]> generateCombinations(int[] cards, int k) {
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

        private void printResults(double[] totalWins, int totalCombinations) {
                for (int i = 0; i < players.length; i++) {
                        double winPercentage = totalWins[i] / totalCombinations * 100;
                        System.out.println("Player " + i + " total wins = " + totalWins[i] + " percentage = " + String.format("%.2f", winPercentage) + "%");
                }

                System.out.println("\nPlayer Cards:");
                for (int i = 0; i < players.length; i++) {
                        int[] playerCards = players[i].getCards();
                        System.out.println("Player " + i + ": " + Card.cardToString(playerCards[0]) + " " + Card.cardToString(playerCards[1]));
                }
        }
}
