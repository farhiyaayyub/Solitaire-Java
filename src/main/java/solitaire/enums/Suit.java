package solitaire.enums;

/**
 * The four card suits. Each suit has a one-letter file code, a display symbol, and a color. The declaration order
 * matters: a suit's position (ordinal 0 to 3) is also its foundation index in moves and its block in Card.id().
 *
 * <p>Encapsulation: each suit keeps its code, symbol, and color in private final fields.
 *
 * @author Farhiya Ayyub
 */
public enum Suit {
    /** Hearts: code H, red. Foundation index 0. */
    HEARTS("H", "♥", true),
    /** Diamonds: code D, red. Foundation index 1. */
    DIAMONDS("D", "♦", true),
    /** Clubs: code C, black. Foundation index 2. */
    CLUBS("C", "♣", false),
    /** Spades: code S, black. Foundation index 3. */
    SPADES("S", "♠", false);

    /** Number of foundation piles; there is one foundation per suit, so this is 4. */
    public static final int FOUNDATION_COUNT = values().length;

    private final String code;
    private final String symbol;
    private final boolean red;

    /**
     * Runs once for each constant above and stores its code, symbol, and color.
     *
     * @param code the one-letter code used in card files
     * @param symbol the text shown for this value
     * @param red {@code true} for Hearts and Diamonds
     */
    Suit(String code, String symbol, boolean red) {
        this.code = code;
        this.symbol = symbol;
        this.red = red;
    }

    /**
     * Returns the one-letter code used in card files: H, D, C, or S.
     *
     * <p>Called by: {@code CardFileLoader.parseSuit()}, {@code Card.code()}.
     *
     * @return the one-letter code used in card files: H, D, C, or S
     */
    public String code() {
        return code;
    }

    /**
     * Returns the symbol printed on screen: ♥, ♦, ♣, or ♠.
     *
     * <p>Called by: {@code ConsoleRenderer.render()}.
     *
     * @return the symbol printed on screen: ♥, ♦, ♣, or ♠
     */
    public String symbol() {
        return symbol;
    }

    /**
     * Returns {@code true} for Hearts and Diamonds, {@code false} for Clubs and Spades.
     *
     * <p>Called by: {@code Card.isRed()}, {@code ObservableBoard.VisibleCard.isRed()},
     * {@code VisibleSolver.foundationScore()}.
     *
     * @return {@code true} for Hearts and Diamonds, {@code false} for Clubs and Spades
     */
    public boolean isRed() {
        return red;
    }
}
