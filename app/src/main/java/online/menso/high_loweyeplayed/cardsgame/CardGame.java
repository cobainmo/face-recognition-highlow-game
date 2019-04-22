package online.menso.high_loweyeplayed.cardsgame;


import online.menso.high_loweyeplayed.MainActivity;

public class CardGame {

    private int deck_number;
    private CardsDeck[] card_deck;
    private int current_streak;
    private int long_streak;
    private int previous_card_value;
    private int current_card_value;
    private int current_deck;
    private boolean gameActive;

    public CardGame(int num_decks){
        this.startGame(num_decks);
    }

    public int getNumDecksLeft() {
        return (deck_number-1)-current_deck;
    }

    public int getCardsLeft() {
        return card_deck[current_deck].cardsLeftInDeck() + (this.getNumDecksLeft()*52);
    }

    public boolean getGameIsActive() {
        return gameActive;
    }

    public void dealCard() {
        previous_card_value = card_deck[current_deck].topCard().get_value();

        if (card_deck[current_deck].cardsLeftInDeck()>0) {
            card_deck[current_deck].dealCard();
        } else if (this.getNumDecksLeft()>0) {
            // Go to the next deck....
            current_deck++;
            card_deck[current_deck].dealCard();
        } else {
            endGame();
        }

        current_card_value = card_deck[current_deck].topCard().get_value();
    }

    public CardModel topCard(){
        return card_deck[current_deck].topCard();
    }

    public int getCurrentStreak(){
        return current_streak;
    }

    public int checkUserGuess(int guess) {

        int intAnswer=compareCurToPrevCard();

        if (intAnswer==0) {
            return MainActivity.CARD_GUESS_EQUAL;
        } else {
            if (guess== MainActivity.LOWER_CARD_MESSAGE) {
                intAnswer--;
            } else {
                intAnswer++;
            }

            if (intAnswer==0) {
                current_streak=0;
                return MainActivity.CARD_GUESS_WRONG;
            } else {
                current_streak++;
                if (current_streak>long_streak) {
                    long_streak=current_streak;
                }
                return MainActivity.CARD_GUESS_RIGHT;
            }
        }
    }

    public int getTotalStreak() {
        return long_streak;
    }

    private int compareCurToPrevCard(){
        if(current_card_value>previous_card_value){
            return 1;
        } else if (current_card_value<previous_card_value) {
            return -1;
        } else{
            return 0;
        }
    }

    private void endGame(){
        gameActive = false;
    }

    private void startGame(int num_decks){
        deck_number = num_decks;
        current_streak = 0;
        long_streak = 0;
        previous_card_value = 0;
        current_card_value = 0;

        card_deck = new CardsDeck[deck_number];

        for(int i=0; i<=deck_number-1; i++) {
            card_deck[i] = new CardsDeck();
            card_deck[i].randomizeCard();
        }

        current_deck = 0;

        gameActive = true;
    }
}
