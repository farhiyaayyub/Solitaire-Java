package solitaire.enums;

/**
 * The seven kinds of moves allowed in Klondike Solitaire.
 *
 * <p>Kind: enum.
 * <p>Purpose: every Move has a MoveType, which tells the validator, the game, and the display which piles it uses.
 * MoveGenerator lists legal moves in the order these constants are declared, so this order also fixes the order of
 * every generated move list.
 * <p>Data: none; only the constants.
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
    /** Turns 1 or 3 cards (the draw mode) from the stock to the waste; fewer if the stock has fewer. */
    DRAW,
    /** Turns the whole waste back into the stock; allowed only when the stock is empty. */
    RECYCLE;

    /**
     * {@return {@code true} if this kind of move puts cards on a tableau column}
     *
     * <p>Called by: ConsoleRenderer.describeMove().
     */
    public boolean endsOnTableau() {
        return this == TABLEAU_TO_TABLEAU || this == WASTE_TO_TABLEAU || this == FOUNDATION_TO_TABLEAU;
    }
}
