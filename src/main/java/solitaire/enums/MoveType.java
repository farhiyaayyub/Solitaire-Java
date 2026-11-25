package solitaire.enums;

/**
 * The seven kinds of moves in Klondike. The other classes use it to pick the right rule or action for each move.
 *
 * @author Farhiya Ayyub
 */
public enum MoveType {
    /** One or more face-up cards from one tableau column to another. */
    TABLEAU_TO_TABLEAU,
    TABLEAU_TO_FOUNDATION,
    WASTE_TO_TABLEAU,
    WASTE_TO_FOUNDATION,
    FOUNDATION_TO_TABLEAU,
    /** Turns 1 or 3 cards (the draw mode) from the stock to the waste. */
    DRAW,
    /** Turns the whole waste back into the stock. */
    RECYCLE;

    /**
     * Returns true if this kind of move puts cards on a tableau column (tableau-to-tableau, waste-to-tableau, or
     * foundation-to-tableau).
     */
    public boolean endsOnTableau() {
        // Only these three move types put cards on a column.
        return this == TABLEAU_TO_TABLEAU || this == WASTE_TO_TABLEAU || this == FOUNDATION_TO_TABLEAU;
    }
}
