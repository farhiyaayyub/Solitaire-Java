package solitaire.enums;

/**
 * The four card suits.
 *
 * <p>Kind: enum.
 * <p>Purpose: it gives every card its suit and color, and each suit has its own foundation. The declaration order
 * matters: a suit's ordinal (0 to 3) is also its foundation index in moves and its block in Card.id().
 * <p>Data: each constant stores a one-letter file code, a display symbol, and whether it is red.
 * <p>OOP: Encapsulation - code, symbol, and color are private final fields, read through methods.
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

    /**
     * Runs once for each constant above.
     *
     * @param code the one-letter code used in card files
     * @param symbol the symbol shown on screen
     * @param red {@code true} for Hearts and Diamonds
     */
    Suit(String code, String symbol, boolean red) {
        this.code = code;
        this.symbol = symbol;
        this.red = red;
    }

    /** {@return H, D, C, or S, as used in card files} Called by: CardFileLoader.parseSuit(), Card.code(). */
    public String code() {
        return code;
    }

    /** {@return ♥, ♦, ♣, or ♠, as printed on screen} Called by: ConsoleRenderer.render(). */
    public String symbol() {
        return symbol;
    }

    /**
     * {@return {@code true} for Hearts and Diamonds, {@code false} for Clubs and Spades}
     *
     * <p>Called by: Card.isRed(), ObservableBoard.VisibleCard.isRed(), VisibleSolver.foundationScore().
     */
    public boolean isRed() {
        return red;
    }
}
