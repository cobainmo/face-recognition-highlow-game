package online.menso.high_loweyeplayed.cardsgame;

public class CardModel {

    private int _suit;
    private int _value;

    public CardModel(int suit, int value){
        _suit=suit;
        _value=value;
    }

    public int get_suit(){
        return _suit;
    }

    public int get_value(){
        return _value;
    }

}
