package online.menso.high_loweyeplayed.cardsgame;

public class CardImage {


    public static String[] card_value = new String[]{"two","three","four", "five", "six", "seven", "eight","nine","ten","jack","queen","king","ace"};
    public static String[] card_suit = new String[]{"clubs","diamonds","hearts","spades"};



    public static String getCardName(int suit, int value){

        return card_suit[suit]+"_"+card_value[value]+".png";

    }


}
