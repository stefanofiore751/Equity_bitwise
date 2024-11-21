import java.util.*;

public class LookupTable {

    // Poker hand ranks
    public static final int MAX_ROYAL_FLUSH = 1;
    public static final int MAX_STRAIGHT_FLUSH = 10;
    public static final int MAX_FOUR_OF_A_KIND = 166;
    public static final int MAX_FULL_HOUSE = 322;
    public static final int MAX_FLUSH = 1599;
    public static final int MAX_STRAIGHT = 1609;
    public static final int MAX_THREE_OF_A_KIND = 2467;
    public static final int MAX_TWO_PAIR = 3325;
    public static final int MAX_PAIR = 6185;
    public static final int MAX_HIGH_CARD = 7462;

    private final Map<Integer, Integer> flushLookup = new HashMap<>();
    private final Map<Integer, Integer> unsuitedLookup = new HashMap<>();

    public LookupTable() {
        populateFlushes();
        populateMultiples();
    }

    private void populateFlushes() {
        // Straight flushes in rank order
        List<Integer> straightFlushes = List.of(7936, 3968, 1984, 992, 496, 248, 124, 62, 31, 4111);
        List<Integer> flushes = new ArrayList<>();

        // Generate all flush combinations except straight flushes
        Iterator<Integer> generator = getLexographicallyNextBitSequence();
        while (flushes.size() < 1277) {
            int f = generator.next();
            if (straightFlushes.stream().noneMatch(sf -> (f ^ sf) == 0)) {
                flushes.add(f);
            }
        }

        Collections.reverse(flushes);

        // Populate straight flush ranks
        int rank = 1;
        for (int sf : straightFlushes) {
            int primeProduct = Card.primeProductFromRankBits(sf);
            flushLookup.put(primeProduct, rank++);
        }

        // Populate flush ranks
        rank = MAX_FULL_HOUSE + 1;
        for (int f : flushes) {
            int primeProduct = Card.primeProductFromRankBits(f);
            flushLookup.put(primeProduct, rank++);
        }

        populateStraightsAndHighCards(straightFlushes, flushes);
    }

    private void populateStraightsAndHighCards(List<Integer> straights, List<Integer> highCards) {
        int rank = MAX_FLUSH + 1;
        for (int s : straights) {
            int primeProduct = Card.primeProductFromRankBits(s);
            unsuitedLookup.put(primeProduct, rank++);
        }

        rank = MAX_PAIR + 1;
        for (int h : highCards) {
            int primeProduct = Card.primeProductFromRankBits(h);
            unsuitedLookup.put(primeProduct, rank++);
        }
    }

    private void populateMultiples() {
        List<Integer> ranksDescending = Card.getRankListDescending();

        // 1) Four of a Kind
        int rank = MAX_STRAIGHT_FLUSH + 1;
        for (int i : ranksDescending) {
            for (int kicker : Card.getRankListExcluding(i)) {
                int product = Card.PRIMES[i] * Card.PRIMES[i] * Card.PRIMES[i] * Card.PRIMES[i] * Card.PRIMES[kicker];
                unsuitedLookup.put(product, rank++);
            }
        }

        // 2) Full House
        rank = MAX_FOUR_OF_A_KIND + 1;
        for (int i : ranksDescending) {
            for (int p : Card.getRankListExcluding(i)) {
                int product = Card.PRIMES[i] * Card.PRIMES[i] * Card.PRIMES[i] * Card.PRIMES[p] * Card.PRIMES[p];
                unsuitedLookup.put(product, rank++);
            }
        }

        // 3) Three of a Kind
        rank = MAX_STRAIGHT + 1;
        for (int i : ranksDescending) {
            List<Integer> kickers = Card.getRankListExcluding(i);
            for (int j = 0; j < kickers.size() - 1; j++) {
                for (int k = j + 1; k < kickers.size(); k++) {
                    int product = Card.PRIMES[i] * Card.PRIMES[i] * Card.PRIMES[i] * Card.PRIMES[kickers.get(j)] * Card.PRIMES[kickers.get(k)];
                    unsuitedLookup.put(product, rank++);
                }
            }
        }

        // 4) Two Pair
        rank = MAX_THREE_OF_A_KIND + 1;
        for (int i = 0; i < ranksDescending.size() - 1; i++) {
            for (int j = i + 1; j < ranksDescending.size(); j++) {
                int pair1 = ranksDescending.get(i), pair2 = ranksDescending.get(j);

                List<Integer> kickers = Card.getRankListExcluding(pair1);
                kickers.remove(Integer.valueOf(pair2));

                for (int kicker : kickers) {
                    int product = Card.PRIMES[pair1] * Card.PRIMES[pair1] * Card.PRIMES[pair2] * Card.PRIMES[pair2] * Card.PRIMES[kicker];
                    unsuitedLookup.put(product, rank++);
                }
            }
        }

        // 5) Pair
        rank = MAX_TWO_PAIR + 1;
        for (int i : ranksDescending) {
            List<Integer> kickers = Card.getRankListExcluding(i);
            for (int j = 0; j < kickers.size() - 2; j++) {
                for (int k = j + 1; k < kickers.size() - 1; k++) {
                    for (int l = k + 1; l < kickers.size(); l++) {
                        int product = Card.PRIMES[i] * Card.PRIMES[i] * Card.PRIMES[kickers.get(j)] * Card.PRIMES[kickers.get(k)] * Card.PRIMES[kickers.get(l)];
                        unsuitedLookup.put(product, rank++);
                    }
                }
            }
        }
    }

    private Iterator<Integer> getLexographicallyNextBitSequence() {
        return new Iterator<>() {
            private int current = 31;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Integer next() {
                int t = (current | (current - 1)) + 1;
                current = t | ((((t & -t) / (current & -current)) >> 1) - 1);
                return current;
            }
        };
    }

    public int evaluateHand(int[] hand) {
        int primeProduct = 1;
        int firstSuit = Card.getSuit(hand[0]);
        boolean isFlush = true;

        for (int card : hand) {
            primeProduct *= Card.getPrime(card);
            if (Card.getSuit(card) != firstSuit) {
                isFlush = false;
            }
        }

        if (isFlush) {
            return flushLookup.getOrDefault(primeProduct, MAX_HIGH_CARD);
        } else {
            return unsuitedLookup.getOrDefault(primeProduct, MAX_HIGH_CARD);
        }
    }



}
