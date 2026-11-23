package solitaire.enums;

/**
 * The 13 card ranks. Each rank has a numeric value from 1 (Ace) to 13 (King), used by the placement rules, and a
 * symbol used in card files and on screen.
 *
 * <p>Encapsulation: each rank keeps its value and symbol in private final fields, read through value() and symbol().
 *
 * @author Farhiya Ayyub
 */
public enum Rank {
    ACE(1, "A"), TWO(2, "2"), THREE(3, "3"), FOUR(4, "4"), FIVE(5, "5"),
    SIX(6, "6"), SEVEN(7, "7"), EIGHT(8, "8"), NINE(9, "9"), TEN(10, "10"),
    JACK(11, "J"), QUEEN(12, "Q"), KING(13, "K");

    private final int value;
    private final String symbol;

    /**
     * Runs once for each constant above and stores its value and symbol.
     *
     * @param value the rank number, 1 (Ace) to 13 (King)
     * @param symbol the text shown for this value
     */
    Rank(int value, String symbol) {
        this.value = value;
        this.symbol = symbol;
    }

    /**
     * Returns 1 for Ace up to 13 for King; rules compare these numbers ("one rank lower/higher").
     *
     * <p>Called by: {@code Card.id()}, {@code FoundationPile.accepts()}, {@code TableauPile.fitsBelow()},
     * {@code ObservableBoard.observe()}.
     *
     * @return 1 for Ace up to 13 for King
     */
    public int value() {
        return value;
    }

    /**
     * Returns the symbol used in card files and on screen: A, 2 to 10, J, Q, or K.
     *
     * <p>Called by: {@code CardFileLoader.parseRank()}, {@code Card.code()}.
     *
     * @return the symbol used in card files and on screen: A, 2 to 10, J, Q, or K
     */
    public String symbol() {
        return symbol;
    }
}
