package solitaire.move;

import solitaire.enums.MoveType;

/**
 * One move: its type, where it starts, where it ends, and how many cards it moves. Indexes start at 0: a column
 * index is 0 to 6 (T1 to T7), and a foundation index is the suit's position in Suit (0 Hearts to 3 Spades). Draw and
 * recycle moves ignore both indexes.
 *
 * <p>It is a record, so a move cannot change after it is created, and Java generates its constructor, getters,
 * equals(), and hashCode(). That lets moves be stored in a Set.
 *
 * @param type        the kind of move
 * @param source      the column or foundation the cards leave
 * @param destination the column or foundation the cards go to
 * @param count       cards moved: 1 for single-card moves and recycle, 1 to 3 for a draw, 1 or more between columns
 * @author Farhiya Ayyub
 */
public record Move(MoveType type, int source, int destination, int count) {

    /**
     * Returns a technical description with one-based indexes, such as "TABLEAU_TO_TABLEAU source=3 destination=6
     * cards=2", used in error messages. The screen uses ConsoleRenderer.describeMove(), which also names the card.
     *
     * <p>Overrides the {@code toString()} that Java generates for a record.
     *
     * <p>Called by: string concatenation in the error message of {@code SolitaireGame.apply()}.
     *
     * @return a technical description with one-based indexes, such as "TABLEAU_TO_TABLEAU source=3 destination=6
     *     cards=2", used in error messages
     */
    @Override
    public String toString() {
        return type + " source=" + (source + 1) + " destination=" + (destination + 1) + " cards=" + count;
    }
}
