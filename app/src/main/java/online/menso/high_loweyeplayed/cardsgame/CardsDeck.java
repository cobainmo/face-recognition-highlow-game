package online.menso.high_loweyeplayed.cardsgame;

import java.util.Random;

public class CardsDeck {

    private CardModel[] cards_deck;
    private CardModel[] temp_deck1;
    private CardModel[] temp_deck2;
    private int top_value;
    private int deck_size;

    public CardsDeck(){
        cards_deck = new CardModel[52];
        top_value = 0;
        deck_size = 52;

        int k = 0;

        for(int i=3;i>=0;i--)
        {
            for(int j=0; j<=12; j++, k++)
            {
                cards_deck[k] = new CardModel(i, j);
            }
        }
    }

    public void randomizeCard()
    {
        for(int count = 0; count<102; count++)
        {
            Random num = new Random();
            int num1 = (num.nextInt(deck_size) + top_value);
            int num2 = (num.nextInt(deck_size) + top_value);
            temp_deck1 = new CardModel[1];
            temp_deck2 = new CardModel[1];
            temp_deck1[0] = cards_deck[num1];
            temp_deck2[0] = cards_deck[num2];
            cards_deck[num2] = temp_deck1[0];
            cards_deck[num1] = temp_deck2[0];
        }
    }


    public CardModel dealCard(){
        top_value+=1;
        deck_size-=1;
        return cards_deck[top_value];
    }

    public CardModel topCard(){
        return cards_deck[top_value];
    }

    public int cardsLeftInDeck(){
        return (cards_deck.length-top_value-1);
    }
}
