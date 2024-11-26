import java.util.ArrayList;

public class Player {
    private boolean folded;
    private int Card1;
    private int Card2;
    private int Card3;
    private int Card4;
    private double equity;

    public Player(int Card1, int Card2) {
        this.Card1 = Card1;
        this.Card2 = Card2;
        this.Card3 = 0;
        this.Card4 = 0;
        this.folded = false;
    }

    public void fold() {
        this.folded = true;
    }

    public boolean getFolded(){
        return this.folded;
    }

    public Player(int Card1, int Card2, int Card3, int Card4) {
        this.Card1 = Card1;
        this.Card2 = Card2;
        this.Card3 = Card3;
        this.Card4 = Card4;
    }

    public void setEquity (double equity) {
        this.equity = equity;
    }

    public double getEquity() {
        return equity;
    }

    public int[] getCards() {
        if(Card3 != 0 && Card4 != 0){
            return new int[]{Card1,Card2,Card3, Card4};
        }
        return new int[]{Card1,Card2};
    }

    public void setCards(int card1,int card2) {
        Card1 = card1;
        Card2 = card2;
    }
}
