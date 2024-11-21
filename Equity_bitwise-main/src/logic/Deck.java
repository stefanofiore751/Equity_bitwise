package logic;
import java.util.ArrayList;
import java.util.List;

public class Deck {

    public static ArrayList<Integer> fullDeck() {
        int[] rankPrimes = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41}; // Valori primi per i ranghi delle carte
        ArrayList<Integer> result = new ArrayList<>();

        // Ciclo su tutti i ranghi (0 = deuce, 12 = ace)
        for (int rank = 0; rank < 13; rank++) {
            // Semi rappresentati in bit: 8 = fiori, 4 = quadri, 2 = cuori, 1 = picche
            for (int suit : new int[]{8, 4, 2, 1}) {
                // Genera la rappresentazione intera a 32 bit della carta
                int card = rankPrimes[rank]            // Valore primo
                        | (rank << 8)               // Rango della carta
                        | (suit << 12)              // Seme in bit
                        | ((1 << rank) << 16);      // Bitrank della carta
                result.add(card);
            }
        }

        // Converte la lista a un array di int
        return result;
    }

    public static int[] fullDeckArray(){
        int[] rankPrimes = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41}; // Valori primi per i ranghi delle carte
        int[] result = new int[52];
        int i = 0;
        // Ciclo su tutti i ranghi (0 = deuce, 12 = ace)
        for (int rank = 0; rank < 13; rank++) {
            // Semi rappresentati in bit: 8 = fiori, 4 = quadri, 2 = cuori, 1 = picche
            for (int suit : new int[]{8, 4, 2, 1}) {
                // Genera la rappresentazione intera a 32 bit della carta
                int card = rankPrimes[rank]            // Valore primo
                        | (rank << 8)               // Rango della carta
                        | (suit << 12)              // Seme in bit
                        | ((1 << rank) << 16);      // Bitrank della carta
                result[i++] = card;
            }
        }

        // Converte la lista a un array di int
        return result;
    }



}
