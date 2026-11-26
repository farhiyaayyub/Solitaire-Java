package solitaire.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import solitaire.enums.Rank;
import solitaire.enums.Suit;

/**
 * A deck of exactly 52 different cards, kept in the order they will be drawn.
 *
 * @author Farhiya Ayyub
 */
public final class Deck {
    /** 4 suits x 13 ranks = 52 cards. */
    public static final int CARD_COUNT = Suit.values().length * Rank.values().length;

    private final Deque<Card> cards = new ArrayDeque<>();

    /**
     * Creates all 52 cards, one for each suit and rank. Each new card is added to the front, so the last card created
     * (the King of Spades) is drawn first.
     */
    public Deck() {
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.addFirst(new Card(suit, rank));
            }
        }
    }

    /**
     * Creates a deck from a list of cards, where the first card in the list is drawn first. It stores copies of the
     * cards and throws an error on a duplicate card or if there are not exactly 52.
     */
    public Deck(List<Card> drawOrder) {
        Set<Card> uniqueCards = new HashSet<>();
        for (Card card : drawOrder) {
            if (!uniqueCards.add(card)) {
                throw new IllegalArgumentException("Duplicate card " + card.code());
            }
            cards.addLast(card.copy());
        }
        if (cards.size() != CARD_COUNT) {
            throw new IllegalArgumentException("Expected " + CARD_COUNT + " cards, found " + cards.size());
        }
    }

    /** Returns copies of the cards in draw order, next card first, without removing them from the deck. */
    public List<Card> drawOrder() {
        return cards.stream().map(Card::copy).toList();
    }

    /** Checks that the deck holds exactly 52 different cards, and throws an error if not. */
    public void verify() {
        if (cards.size() != CARD_COUNT || new HashSet<>(cards).size() != CARD_COUNT) {
            throw new IllegalStateException("Deck must contain " + CARD_COUNT + " unique cards");
        }
    }

    /** Removes the next card from the deck and returns it. Throws an error if the deck is empty. */
    public Card draw() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Empty deck");
        }
        return cards.removeFirst();
    }

    /** Returns how many cards are still in the deck. */
    public int size() {
        return cards.size();
    }
}
