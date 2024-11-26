import java.util.*;


public class OmahaGame extends Game {
    @Override
    public void generatePlayerCards() {
        List<Integer> deckList = new ArrayList<>();
        for (int card : deck) {
            deckList.add(card);
        }
        Collections.shuffle(deckList);
        for(int i = 0; i < players.length; i++) {
            players[i] = new Player(deckList.removeFirst(), deckList.removeFirst(), deckList.removeFirst(), deckList.removeFirst());
        }
        deck = deckList.stream().mapToInt(Integer::intValue).toArray();
    }

    @Override
    public void insertPlayerCards() {
        players[0] = new Player(deck[12], deck[45], deck[8], deck[29]);
        players[1] = new Player(deck[3], deck[18], deck[22], deck[40]);
        players[2] = new Player(deck[9], deck[31], deck[0], deck[47]);
        players[3] = new Player(deck[11], deck[25], deck[4], deck[36]);
        players[4] = new Player(deck[7], deck[28], deck[17], deck[42]);
        players[5] = new Player(deck[1], deck[34], deck[19], deck[50]);

        Set<Integer> usedCards = new HashSet<>();
        for (Player player : players) {
            for (int card : player.getCards()) {
                usedCards.add(card);
            }
        }
        deck = Arrays.stream(deck).filter(card -> !usedCards.contains(card)).toArray();
    }

    @Override
    public int bestPlay(int[] playerCards, int[] communityCards) {
        // Generate all combinations of 2 hole cards from 4 hole cards
        List<int[]> holeCardCombos = generateCombinations(playerCards, 2);

        // Generate all combinations of 3 community cards from 5 community cards
        List<int[]> communityCardCombos = generateCombinations(communityCards, 3);

        int bestRank = Integer.MAX_VALUE;

        // Combine 2 hole cards with 3 community cards
        for (int[] holeCombo : holeCardCombos) {
            for (int[] communityCombo : communityCardCombos) {
                int[] fullHand = new int[5];
                System.arraycopy(holeCombo, 0, fullHand, 0, 2);
                System.arraycopy(communityCombo, 0, fullHand, 2, 3);

                // Evaluate the rank of this hand
                int rank = lookupTable.evaluateHand(fullHand);
                bestRank = Math.min(bestRank, rank);
            }
        }

        return bestRank;
    }

    @Override
    public void play() throws InterruptedException {
        deck = Deck.fullDeckArray();
        insertPlayerCards();
        // Pre-flop equity calculation
        calculatePreFlopEquity();
        //fold(0);
        // Draw flop cards and calculate flop equity
        calculateFlopEquity();
        //fold(1);
        calculateTurnEquity();
        //fold(2);
        calculateRiverEquity();
    }

    @Override
    void printResults(double[] totalWins, int totalCombinations) {
        for (int i = 0; i < players.length; i++) {
            double winPercentage = totalWins[i] / totalCombinations * 100;
            System.out.println("Player " + i + " total wins = " + totalWins[i] + " percentage = " + String.format("%.2f", winPercentage) + "%");
        }

        System.out.println("\nPlayer Cards:");
        for (int i = 0; i < players.length; i++) {
            int[] playerCards = players[i].getCards();
            System.out.println("Player " + i + ": " + Card.cardToString(playerCards[0]) + " " + Card.cardToString(playerCards[1]) + " " + Card.cardToString(playerCards[2])+ " " + Card.cardToString(playerCards[3]));
        }
    }

    public static void main(String[] args) throws InterruptedException {
        OmahaGame game = new OmahaGame();
        game.play();
    }
}

