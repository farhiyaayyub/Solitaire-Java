package solitaire.enums;

/**
 * The 13 card ranks from Ace to King. Each rank has a number value (Ace = 1, King = 13) used by the rules, and a symbol
 * (A, 2 to 10, J, Q, K) used in card files and on screen.
 *
 * @author Farhiya Ayyub
 */
public enum Rank {
    ACE(1, "A"), TWO(2, "2"), THREE(3, "3"), FOUR(4, "4"), FIVE(5, "5"),
    SIX(6, "6"), SEVEN(7, "7"), EIGHT(8, "8"), NINE(9, "9"), TEN(10, "10"),
    JACK(11, "J"), QUEEN(12, "Q"), KING(13, "K");

    private final int value;
    private final String symbol;

    /** Stores the number value and the symbol of one rank. */
    Rank(int value, String symbol) {
        this.value = value;
        this.symbol = symbol;
    }

    /**
     * Returns the rank as a number, 1 for Ace up to 13 for King. The rules use it to check "one rank higher" or "one
     * rank lower".
     */
    public int value() {
        return value;
    }

    /** Returns the short text of the rank: A, 2 to 10, J, Q, or K. */
    public String symbol() {
        return symbol;
    }
}
