import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Card {
    static final int[] PRIMES = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41};
    private static final String[] RANK_STRINGS = {"2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A"};
    private static final String[] SUIT_STRINGS = {"s", "h", "d", "c"};
    public static final int CLUB = 0x8;
    public static final int DIAMOND = 0x4;
    public static final int HEART = 0x2;
    public static final int SPADE = 0x1;

    private static final int[] BITRANKS = {
            0x1,  // deuce
            0x1 << 1,  // three
            0x1 << 2,  // four
            0x1 << 3,  // five
            0x1 << 4,  // six
            0x1 << 5,  // seven
            0x1 << 6,  // eight
            0x1 << 7,  // nine
            0x1 << 8,  // ten
            0x1 << 9,  // jack
            0x1 << 10, // queen
            0x1 << 11, // king
            0x1 << 12  // ace
    };

    public static int cardFromChar(String cardCode) {
        int rank = parseRank(cardCode.charAt(0));
        int suit = parseSuit(cardCode.charAt(1));
        return createCard(rank, suit);
    }

    private static int parseRank(char rankChar) {
        return switch (rankChar) {
            case '2' -> 0;
            case '3' -> 1;
            case '4' -> 2;
            case '5' -> 3;
            case '6' -> 4;
            case '7' -> 5;
            case '8' -> 6;
            case '9' -> 7;
            case 'T' -> 8;
            case 'J' -> 9;
            case 'Q' -> 10;
            case 'K' -> 11;
            case 'A' -> 12;
            default -> throw new IllegalArgumentException("Invalid rank: " + rankChar);
        };
    }

    private static int parseSuit(char suitChar) {
        return switch (suitChar) {
            case 'c' -> CLUB;
            case 'd' -> DIAMOND;
            case 'h' -> HEART;
            case 's' -> SPADE;
            default -> throw new IllegalArgumentException("Invalid suit: " + suitChar);
        };
    }

    public static int createCard(int rank, int suit) {
        int prime = PRIMES[rank];
        return (prime | (rank << 8) | suit << 12 | ((1 << rank) << 16));
    }

    public static int getPrime(int card) {
        return card & 0x3F;
    }

    public static int primeProductFromRankBits(int rankBits) {
        int product = 1;

        for (int i = 0; i < BITRANKS.length; i++) {
          if ((rankBits & BITRANKS[i]) != 0) {
                product *= PRIMES[i];
            }
        }
        return product;
    }


    public static int getRank(int card) {
        return (card >> 8) & 0xF;
    }

    public static char getRankChar(int card) {
        return (char) (getRank(card) & 0x3F);
    }

    public static int getRankBit(int card) {
        int rank = getRank(card); // Extract rank from the card
        return 1 << rank;        // Return the bitmask for this rank
    }

    public static int getSuit(int card) {
        return (card >> 12) & 0xF;
    }

    public static List<Integer> getRankListDescending() {
        List<Integer> ranks = new ArrayList<>();
        for (int i = PRIMES.length - 1; i >= 0; i--) {
            ranks.add(i);
        }
        return ranks;
    }

    public static List<Integer> getRankListExcluding(int excludeRank) {
        List<Integer> ranks = new ArrayList<>();
        for (int i = PRIMES.length - 1; i >= 0; i--) {
            if (i != excludeRank) {
                ranks.add(i);
            }
        }
        return ranks;
    }
    public static String cardToString(int card) {
        int rank = getRank(card);
        int suit = getSuit(card);
        String rankString = RANK_STRINGS[rank];
        String suitString = switch (suit) {
            case SPADE -> "s";
            case HEART -> "h";
            case DIAMOND -> "d";
            case CLUB -> "c";
            default -> "?";
        };
        return rankString + suitString;
    }
}
