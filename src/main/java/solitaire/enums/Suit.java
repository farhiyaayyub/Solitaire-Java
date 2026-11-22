package solitaire.enums;

/**
 * The four card suits with their file code, display symbol, and color.
 *
 * <p>Type: enum with 4 constants; each constant is an object with its own code, symbol, and color.
 * OOP: ENCAPSULATION (private final fields, read through methods).
 *
 * @author Farhiya Ayyub
 */
public enum Suit {
    // Constants: each one calls the constructor below with its code, symbol, and color.
    HEARTS("H", "♥", true),
    DIAMONDS("D", "♦", true),
    CLUBS("C", "♣", false),
    SPADES("S", "♠", false);

    /** Constant: Number of foundation piles, one per suit. */
    public static final int FOUNDATION_COUNT = values().length;

    // Fields: the file code, display symbol, and color of this suit.
    private final String code;
    private final String symbol;
    private final boolean red;

    /** Constructor: Stores the code, symbol, and color of one suit. */
    Suit(String code, String symbol, boolean red) {
        this.code = code;
        this.symbol = symbol;
        this.red = red;
    }

    /** Method: Returns the one-letter suit code used in card files, such as H. */
    public String code() {
        return code;
    }

    /** Method: Returns the display symbol. */
    public String symbol() {
        return symbol;
    }

    /** Method: Reports whether the suit is red. */
    public boolean isRed() {
        return red;
    }
}
