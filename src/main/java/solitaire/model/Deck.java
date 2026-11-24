package solitaire.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import solitaire.enums.Rank;
import solitaire.enums.Suit;

/**
 * A deck of exactly 52 different cards. The next card to draw is kept at the end of the list, because removing
 * the last element of an ArrayList is instant while removing the first would shift every other card.
 *
 * <p>Encapsulation: the card list is private; outside code only gets copies through drawOrder().
 *
 * @author Farhiya Ayyub
 */
public final class Deck {
    /** 4 suits x 13 ranks = 52 cards. */
    public static final int CARD_COUNT = Suit.values().length * Rank.values().length;

    private final List<Card> cards = new ArrayList<>();

    /**
     * Creates the 52 cards in suit order (Hearts, Diamonds, Clubs, Spades), Ace to King within each suit. The King of
     * Spades is drawn first, because the next card to draw is at the end of the list.
     */
    public Deck() {
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
    }

    /**
     * Creates a deck from a list whose first card is the first to be drawn. Stores copies, so later changes to the
     * list's cards do not affect the deck.
     *
     * <p>Key idea: {@code HashSet.add()} returns {@code false} when an equal card is already in the set, which is how a
     * duplicate is found in one pass. It works because Card overrides equals() and hashCode() to compare suit and rank
     * only.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link Card#code} - names the duplicate in the error</li>
     *   <li>{@link Card#copy} - stores copies, not the caller's cards</li>
     * </ul>
     *
     * @param drawOrder the cards in draw order; the first element is the first card to be drawn
     * @throws IllegalArgumentException for a duplicate card or a card count other than 52
     */
    public Deck(List<Card> drawOrder) {
        Set<Card> uniqueCards = new HashSet<>();
        for (Card card : drawOrder) {
            // Set.add returns false when an equal card (same suit and rank) is already in the set.
            if (!uniqueCards.add(card)) {
                throw new IllegalArgumentException("Duplicate card " + card.code());
            }
            cards.add(card.copy());
        }
        if (cards.size() != CARD_COUNT) {
            throw new IllegalArgumentException("Expected " + CARD_COUNT + " cards, found " + cards.size());
        }
        // Reversed so the first card of drawOrder is at the end of the list, where draw() takes it.
        Collections.reverse(cards);
    }

    /**
     * Returns copies of the cards in the order they will be drawn (first element = next card). The list cannot be
     * modified, and changing the copied cards does not affect the deck.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link Card#copy} - protects the deck's own cards</li>
     * </ul>
     *
     * <p>Called by: {@code ShuffleConfiguration.prepareDeck()}.
     *
     * @return copies of the cards, next card first
     */
    public List<Card> drawOrder() {
        List<Card> drawOrderSnapshot = new ArrayList<>();
        for (int cardIndex = cards.size() - 1; cardIndex >= 0; cardIndex--) {
            drawOrderSnapshot.add(cards.get(cardIndex).copy());
        }
        return List.copyOf(drawOrderSnapshot);
    }

    /**
     * Checks that the deck holds exactly 52 different cards.
     *
     * <p>Called by: {@code ShuffleConfiguration.prepareDeck()}.
     *
     * @throws IllegalStateException unless the deck holds exactly 52 cards and no two are equal
     */
    public void verify() {
        if (cards.size() != CARD_COUNT || new HashSet<>(cards).size() != CARD_COUNT) {
            throw new IllegalStateException("Deck must contain " + CARD_COUNT + " unique cards");
        }
    }

    /**
     * Removes and returns the next card (the end of the list).
     *
     * <p>Called by: {@code GameState.deal()}.
     *
     * @return the card that was removed
     * @throws IllegalStateException if the deck is empty
     */
    public Card draw() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Empty deck");
        }
        return cards.removeLast();
    }

    /**
     * Returns how many cards have not been drawn yet.
     *
     * <p>Called by: {@code GameState.deal()}.
     *
     * @return how many cards have not been drawn yet
     */
    public int size() {
        return cards.size();
    }
}
