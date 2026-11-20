package solitaire.pile;

import solitaire.model.Card;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Stores cards drawn from the stock.
 *
 * @author Farhiya Ayyub
 */
public final class WastePile {
    private final Deque<Card> cards = new ArrayDeque<>();

    /** Returns the number of cards in this pile or deck. */
    public int size() {
        return cards.size();
    }

    /** Returns a detached top-card snapshot, or null when empty. */
    public Card top() {
        return cards.isEmpty() ? null : cards.peekFirst().copy();
    }

    /** Adds the supplied card to the top of this pile. */
    public void push(Card card) {
        card.reveal();
        cards.addFirst(card);
    }

    /** Removes and returns the top card; callers must ensure the pile is not empty. */
    public Card pop() {
        return cards.removeFirst();
    }

    /** Returns an unmodifiable list of detached card snapshots. */
    public List<Card> cards() {
        return cards.stream().map(Card::copy).toList();
    }

    /** Returns a detached copy, including card visibility. */
    public WastePile copy() {
        WastePile pileCopy = new WastePile();
        for (Card card : cards) pileCopy.cards.addLast(card.copy());
        return pileCopy;
    }
}
