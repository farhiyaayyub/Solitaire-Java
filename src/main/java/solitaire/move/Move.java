package solitaire.move;

import solitaire.enums.MoveType;

/**
 * One move: its type, zero-based source and destination indexes, and the number of cards moved.
 * Draw and recycle moves do not use the indexes.
 *
 * <p>Type: record (an immutable data class; Java creates its fields, constructor, getters type(), source(),
 * destination(), count(), and equals/hashCode).
 * OOP: ENCAPSULATION (a move cannot be changed after it is created).
 *
 * @param type        the kind of move
 * @param source      the source column or foundation index
 * @param destination the destination column or foundation index
 * @param count       the number of cards moved
 * @author Farhiya Ayyub
 */
public record Move(MoveType type, int source, int destination, int count) {

    /** Method (override): Returns a short technical description, used in error messages. */
    @Override
    public String toString() {
        return type + " source=" + (source + 1) + " destination=" + (destination + 1) + " cards=" + count;
    }
}
