package solitaire.move;

import solitaire.enums.MoveType;

/**
 * One move in the game, stored as four values: the move type, the source index, the destination index, and how many
 * cards move. Indexes count from 0, so column 0 is T1.
 *
 * @author Farhiya Ayyub
 */
public record Move(MoveType type, int source, int destination, int count) {

    /**
     * Returns a technical description of the move with indexes counted from 1, such as "TABLEAU_TO_TABLEAU source=3
     * destination=6 cards=2".
     */
    @Override
    public String toString() {
        return type + " source=" + (source + 1) + " destination=" + (destination + 1) + " cards=" + count;
    }
}
