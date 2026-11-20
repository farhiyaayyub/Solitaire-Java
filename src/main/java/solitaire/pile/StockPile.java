package solitaire.pile;

import solitaire.model.Card;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Stores cards waiting to be drawn.
 *
 * @author Farhiya Ayyub
 */
public final class StockPile {
    private final Deque<Card> cards = new ArrayDeque<>();

    /** Returns the number of cards in this pile or deck. */
    public int size() {
        return cards.size();
    }

    /** Conceals the supplied card and places it at the bottom of the stock. */
    public void addBottom(Card card) {
        card.conceal();
        cards.addLast(card);
    }

    /** Transfers the next stock card to the waste, revealing it. */
    public void drawTo(WastePile waste) {
        waste.push(cards.removeFirst());
    }

    /** Restores waste cards to stock in draw order; requires empty stock and nonempty waste. */
    public void recycle(WastePile waste) {
        if (!cards.isEmpty() || waste.size() == 0) throw new IllegalStateException("Cannot recycle");
        while (waste.size() > 0) {
            Card card = waste.pop();
            card.conceal();
            cards.addFirst(card);
        }
    }

    /** Returns an unmodifiable list of detached card snapshots. */
    public List<Card> cards() {
        return cards.stream().map(Card::copy).toList();
    }

    /** Returns a detached copy, including card visibility. */
    public StockPile copy() {
        StockPile pileCopy = new StockPile();
        for (Card card : cards) pileCopy.cards.addLast(card.copy());
        return pileCopy;
    }
}
