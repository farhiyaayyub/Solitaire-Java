package solitaire.enums;

/**
 * Defines the four card suits and their display properties.
 *
 * @author Farhiya Ayyub
 */
public enum Suit {
    HEARTS("H", "\u2665", true),
    DIAMONDS("D", "\u2666", true),
    CLUBS("C", "\u2663", false),
    SPADES("S", "\u2660", false);

    private final boolean red;
    private final String code;
    private final String symbol;

    Suit(String code, String symbol, boolean red) {
        this.code = code;
        this.symbol = symbol;
        this.red = red;
    }

    public static final int FOUNDATION_COUNT = values().length;

    /** Returns the one-letter suit code. */
    public String code() {
        return code;
    }

    /** Returns the display symbol. */
    public String symbol() {
        return symbol;
    }

    /** Reports whether the suit is red. */
    public boolean isRed() {
        return red;
    }

    /** Reports whether the suit is black. */
    public boolean isBlack() {
        return !red;
    }
}
