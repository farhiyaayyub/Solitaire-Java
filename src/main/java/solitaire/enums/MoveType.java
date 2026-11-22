package solitaire.enums;

/**
 * The kinds of moves allowed in Klondike Solitaire.
 *
 * <p>Type: enum; used by Move, MoveValidator, and MoveGenerator.
 *
 * @author Farhiya Ayyub
 */
public enum MoveType {
    // Constants: one for each kind of move.
    TABLEAU_TO_TABLEAU,
    TABLEAU_TO_FOUNDATION,
    WASTE_TO_TABLEAU,
    WASTE_TO_FOUNDATION,
    FOUNDATION_TO_TABLEAU,
    DRAW,
    RECYCLE
}
