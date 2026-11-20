package solitaire.move;

import solitaire.enums.MoveType;

/**
 * Move description with zero-based source/destination indexes and a card count.
 * @author Farhiya Ayyub
 */
public record Move(MoveType type, int source, int destination, int count) {
    /** Returns a readable description for console output. */
    public String toString() {
        return type + " source=" + (source + 1) + " destination=" + (destination + 1) + " cards=" + count;
    }
}
