package solitaire.move;

import solitaire.enums.MoveType;

/**
 * One Solitaire move.
 *
 * <p>Kind: record, uses MoveType.
 * <p>Purpose: it describes a move by its type, where it starts, where it ends, and how many cards it moves. Indexes
 * start at 0: a column is 0 to 6 (T1 to T7), a foundation is the suit's position in Suit (0 Hearts to 3 Spades), and
 * draw and recycle moves ignore both.
 * <p>Data: four record fields (a MoveType and three ints). A record was chosen because its generated equals() and
 * hashCode() let moves be stored in a HashSet.
 * <p>OOP: Encapsulation - a move cannot change after it is made.
 *
 * @param type        the kind of move
 * @param source      the column or foundation the cards leave
 * @param destination the column or foundation the cards go to
 * @param count       cards moved: 1 for single-card moves and recycle, 1 to 3 for a draw, 1 or more between columns
 * @author Farhiya Ayyub
 */
public record Move(MoveType type, int source, int destination, int count) {

    /**
     * {@return a technical description with one-based indexes, such as "TABLEAU_TO_TABLEAU source=3 destination=6
     * cards=2"} It is only used in error messages; the screen uses ConsoleRenderer.describeMove(), which names the
     * card.
     *
     * <p>Called by: SolitaireGame.apply() (through string concatenation).
     */
    @Override
    public String toString() {
        return type + " source=" + (source + 1) + " destination=" + (destination + 1) + " cards=" + count;
    }
}
