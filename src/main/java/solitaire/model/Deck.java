package solitaire.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import solitaire.enums.Rank;
import solitaire.enums.Suit;

/**
 * A full 52-card deck. Cards are stored with the next card to draw at the end, so drawing is constant time.
 *
 * <p>Type: final class.
 * OOP: ENCAPSULATION (private card list; outside code only gets copies from drawOrder()).
 *
 * @author Farhiya Ayyub
 */
public final class Deck {
    /** Constant: Number of cards in a standard deck (4 suits x 13 ranks = 52). */
    public static final int CARD_COUNT = Suit.values().length * Rank.values().length;

    // Field: the cards, with the next card to draw at the end of the list.
    private final List<Card> cards = new ArrayList<>();

    /** Constructor: Creates a standard deck in its default order. */
    public Deck() {
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                // Object: Card - one for each suit and rank, 52 in total.
                cards.add(new Card(suit, rank));
            }
        }
    }

    /**
     * Constructor: Creates a deck from a top-first card list; requires exactly 52 unique cards.
     * Calls: Set.add() (finds duplicates), Card.code(), Card.copy(), Collections.reverse()
     */
    public Deck(List<Card> drawOrder) {
        Set<Card> uniqueCards = new HashSet<>();
        for (Card card : drawOrder) {
            if (!uniqueCards.add(card)) {
                throw new IllegalArgumentException("Duplicate card " + card.code());
            }
            cards.add(card.copy());
        }
        if (cards.size() != CARD_COUNT) {
            throw new IllegalArgumentException("Expected " + CARD_COUNT + " cards, found " + cards.size());
        }
        Collections.reverse(cards);
    }

    /**
     * Method: Returns an unmodifiable list of detached card copies in top-first draw order.
     * Calls: Card.copy(), List.copyOf()
     */
    public List<Card> drawOrder() {
        List<Card> drawOrderSnapshot = new ArrayList<>();
        for (int cardIndex = cards.size() - 1; cardIndex >= 0; cardIndex--) {
            drawOrderSnapshot.add(cards.get(cardIndex).copy());
        }
        return List.copyOf(drawOrderSnapshot);
    }

    /** Method: Throws IllegalStateException unless exactly 52 distinct cards are present. */
    public void verify() {
        if (cards.size() != CARD_COUNT || new HashSet<>(cards).size() != CARD_COUNT) {
            throw new IllegalStateException("Deck must contain " + CARD_COUNT + " unique cards");
        }
    }

    /** Method: Removes and returns the next card; throws IllegalStateException when the deck is empty. */
    public Card draw() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Empty deck");
        }
        return cards.remove(cards.size() - 1);
    }

    /** Method: Returns the number of cards left in the deck. */
    public int size() {
        return cards.size();
    }
}
