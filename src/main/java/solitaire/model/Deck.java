package solitaire.model;

import solitaire.enums.Rank;
import solitaire.enums.Suit;
import solitaire.enums.ShuffleType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import java.util.Set;

/**
 * Owns a deck whose next card is stored at the end for efficient removal.
 * @author Farhiya Ayyub
 */
public final class Deck {
    public static final int CARD_COUNT = Suit.values().length * Rank.values().length;
    private final ArrayList<Card> cards = new ArrayList<>();

    /** Creates a standard deck in its default draw order. */
    public Deck() {
        for (Suit suit : Suit.values()) for (Rank rank : Rank.values()) cards.add(new Card(suit, rank));
    }

    /** Creates a deck from the supplied top-first card order. */
    public Deck(List<Card> drawOrder) {
        Set<Card> uniqueCards = new HashSet<>();
        for (Card card : drawOrder) {
            if (!uniqueCards.add(card)) throw new IllegalArgumentException("Duplicate card");
            cards.add(card.copy());
        }
        if (cards.size() != CARD_COUNT) throw new IllegalArgumentException("Expected " + CARD_COUNT + " cards");
        Collections.reverse(cards);
    }

    /** Reorders cards using the selected shuffle strategy or seed. */
    public void shuffle(long seed) {
        shuffle(ShuffleType.RANDOM, seed);
    }

    /** Returns an unmodifiable, detached snapshot in top-first draw order. */
    public List<Card> drawOrder() {
        List<Card> drawOrderSnapshot = new ArrayList<>();
        for (int cardIndex = cards.size() - 1; cardIndex >= 0; cardIndex--) drawOrderSnapshot.add(cards.get(cardIndex).copy());
        return List.copyOf(drawOrderSnapshot);
    }

    /** Reorders cards using the selected shuffle strategy or seed. */
    public void shuffle(ShuffleType shuffleType, long seed) {
        verify();
        Deck shuffledDeck = new Deck(shuffleType.shuffle(drawOrder(), seed));
        cards.clear();
        cards.addAll(shuffledDeck.cards);
    }

    /** Throws IllegalStateException unless exactly 52 distinct card identities are present. */
    public void verify() {
        if (cards.size() != CARD_COUNT || new HashSet<>(cards).size() != CARD_COUNT)
            throw new IllegalStateException("Deck must contain 52 unique cards");
    }
    /** Removes and returns the next card; throws IllegalStateException when empty. */
    public Card draw() {
        if (cards.isEmpty()) throw new IllegalStateException("Empty deck");
        return cards.remove(cards.size() - 1);
    }

    /** Returns the number of cards in this pile or deck. */
    public int size() {
        return cards.size();
    }
}
