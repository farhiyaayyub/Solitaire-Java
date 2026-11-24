package solitaire.enums;

/**
 * The 13 card ranks.
 *
 * <p>Kind: enum.
 * <p>Purpose: it gives every card its rank, used by the placement rules, the card files, and the screen.
 * <p>Data: each constant stores a number value from 1 (Ace) to 13 (King), which the rules compare, and the symbol
 * used in card files and on screen.
 * <p>OOP: Encapsulation - value and symbol are private final fields, read through value() and symbol().
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
     * Runs once for each constant above.
     *
     * @param value the rank number, 1 (Ace) to 13 (King)
     * @param symbol the text used in card files and on screen
     */
    Rank(int value, String symbol) {
        this.value = value;
        this.symbol = symbol;
    }

    /**
     * {@return 1 for Ace up to 13 for King} The rules compare these numbers ("one rank lower/higher").
     *
     * <p>Called by: Card.id(), FoundationPile.accepts(), TableauPile.fitsBelow(), ObservableBoard.observe().
     */
    public int value() {
        return value;
    }

    /** {@return A, 2 to 10, J, Q, or K} Called by: CardFileLoader.parseRank(), Card.code(). */
    public String symbol() {
        return symbol;
    }
}
