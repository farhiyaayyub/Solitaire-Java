package solitaire.enums;

/**
 * Card ranks with their numeric value and display symbol.
 *
 * <p>Type: enum with 13 constants; each constant is an object with its own value and symbol.
 * OOP: ENCAPSULATION (private final fields, read through value() and symbol()).
 *
 * @author Farhiya Ayyub
 */
public enum Rank {
    // Constants: each one calls the constructor below with its value and symbol.
    ACE(1, "A"), TWO(2, "2"), THREE(3, "3"), FOUR(4, "4"), FIVE(5, "5"),
    SIX(6, "6"), SEVEN(7, "7"), EIGHT(8, "8"), NINE(9, "9"), TEN(10, "10"),
    JACK(11, "J"), QUEEN(12, "Q"), KING(13, "K");

    // Fields: the numeric value and the display symbol of this rank.
    private final int value;
    private final String symbol;

    /** Constructor: Stores the value and symbol of one rank. */
    Rank(int value, String symbol) {
        this.value = value;
        this.symbol = symbol;
    }

    /** Method: Returns the numeric value used by placement rules (Ace = 1, King = 13). */
    public int value() {
        return value;
    }

    /** Method: Returns the display symbol, such as A, 10, or K. */
    public String symbol() {
        return symbol;
    }
}
