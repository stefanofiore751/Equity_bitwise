import java.util.ArrayList;

public class Player {
    private int Card1;
    private int Card2;
    private int wins;
    private int ties;

    public Player(int Card1, int Card2) {
        this.Card1 = Card1;
        this.Card2 = Card2;
    }

    public int getCard1() {
        return Card1;
    }

    public int getCard2() {
        return Card2;
    }

    public void addWin(){
        wins++;
    }

    public int getWins() {
        return wins;
    }

    public int[] getCards() {
        int[] cards = new int[2];
        cards[0] = Card1;
        cards[1] = Card2;
        return cards;
    }
}
