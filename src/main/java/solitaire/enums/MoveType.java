package solitaire.enums;

/**
 * The seven kinds of moves allowed in Klondike Solitaire. MoveGenerator lists legal moves in the order these
 * constants are declared, so this order also fixes the order of every generated move list.
 *
 * @author Farhiya Ayyub
 */
public enum MoveType {
    /** One or more face-up cards from one tableau column to another. */
    TABLEAU_TO_TABLEAU,
    /** The top card of a tableau column to its suit's foundation. */
    TABLEAU_TO_FOUNDATION,
    /** The top waste card to a tableau column. */
    WASTE_TO_TABLEAU,
    /** The top waste card to its suit's foundation. */
    WASTE_TO_FOUNDATION,
    /** The top foundation card back to a tableau column. */
    FOUNDATION_TO_TABLEAU,
    /** Turns 1 or 3 cards (the draw mode) from the stock to the waste; fewer if the stock has fewer. */
    DRAW,
    /** Turns the whole waste back into the stock; allowed only when the stock is empty. */
    RECYCLE
}
