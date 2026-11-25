package solitaire.enums;

/**
 * The four card suits. Each suit has a one-letter code for card files, a symbol for the screen, and a color: Hearts and
 * Diamonds are red, Clubs and Spades are black.
 *
 * @author Farhiya Ayyub
 */
public enum Suit {
    HEARTS("H", "♥", true),
    DIAMONDS("D", "♦", true),
    CLUBS("C", "♣", false),
    SPADES("S", "♠", false);

    /** One foundation per suit, so 4. */
    public static final int FOUNDATION_COUNT = values().length;

    private final String code;
    private final String symbol;
    private final boolean red;

    /** Stores the code, the symbol, and the color of one suit. */
    Suit(String code, String symbol, boolean red) {
        this.code = code;
        this.symbol = symbol;
        this.red = red;
    }

    /** Returns the letter used in card files: H, D, C, or S. */
    public String code() {
        return code;
    }

    /** Returns the symbol printed on screen: ♥, ♦, ♣, or ♠. */
    public String symbol() {
        return symbol;
    }

    /**
     * Returns true for Hearts and Diamonds and false for Clubs and Spades. The tableau rule uses this to check
     * alternating colors.
     */
    public boolean isRed() {
        return red;
    }
}
