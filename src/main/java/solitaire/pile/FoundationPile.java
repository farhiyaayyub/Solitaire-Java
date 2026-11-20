package solitaire.pile;

import solitaire.model.Card;

import solitaire.enums.Suit;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores one suit foundation.
 *
 * @author Farhiya Ayyub
 */
public final class FoundationPile {
    private final Suit suit;
    private final ArrayList<Card> cards = new ArrayList<>();

    /** Creates an empty foundation for the supplied suit. */
    public FoundationPile(Suit suit) {
        this.suit = suit;
    }

    /** Returns the number of cards in this pile or deck. */
    public int size() {
        return cards.size();
    }

    /** Returns a detached top-card snapshot, or null when empty. */
    public Card top() {
        return cards.isEmpty() ? null : cards.get(cards.size() - 1).copy();
    }

    /** Reports whether the supplied face-up card satisfies this pile's placement rule. */
    public boolean accepts(Card card) {
        return card != null && card.isFaceUp() && card.suit() == suit && card.rank().getValue() == size() + 1;
    }

    /** Adds the supplied card to the top of this pile. */
    public void push(Card card) {
        if (!accepts(card)) throw new IllegalArgumentException("Invalid foundation placement");
        cards.add(card);
    }

    /** Removes and returns the top card; callers must ensure the pile is not empty. */
    public Card pop() {
        return cards.remove(cards.size() - 1);
    }

    /** Returns an unmodifiable list of detached card snapshots. */
    public List<Card> cards() {
        return cards.stream().map(Card::copy).toList();
    }

    /** Returns a detached copy, including card visibility. */
    public FoundationPile copy() {
        FoundationPile pileCopy = new FoundationPile(suit);
        for (Card card : cards) pileCopy.cards.add(card.copy());
        return pileCopy;
    }
}
