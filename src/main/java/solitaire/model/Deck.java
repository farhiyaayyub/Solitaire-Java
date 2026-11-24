package solitaire.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import solitaire.enums.Rank;
import solitaire.enums.Suit;

/**
 * A deck of exactly 52 different cards.
 *
 * <p>Kind: final class.
 * <p>Purpose: it is the source of the cards for a new game: it is built (sorted, from a file, or shuffled), checked,
 * and then dealt one card at a time onto the board.
 * <p>Data: the cards in a private ArrayDeque, next card at the front: a deck is filled once and then only drawn from
 * the front, both O(1), and never needs an index.
 * <p>OOP: Encapsulation - the deque is private; outside code only gets copies through drawOrder().
 *
 * @author Farhiya Ayyub
 */
public final class Deck {
    /** 4 suits x 13 ranks = 52 cards. */
    public static final int CARD_COUNT = Suit.values().length * Rank.values().length;

    private final Deque<Card> cards = new ArrayDeque<>();

    /**
     * Creates the 52 cards sorted by suit (Hearts, Diamonds, Clubs, Spades), Ace to King within each suit. Each card
     * is put on the front, so the King of Spades, created last, is drawn first.
     *
     * <p>Called by: ShuffleConfiguration.prepareDeck(), GameController.automatic(), GameController.playFromMenu().
     */
    public Deck() {
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.addFirst(new Card(suit, rank));
            }
        }
    }

    /**
     * Creates a deck from a list whose first card is drawn first, storing copies so later changes to the list's cards
     * do not affect the deck.
     *
     * <p>Key idea: {@code HashSet.add()} returns {@code false} when an equal card is already in the set, so a
     * duplicate is found in one pass. This works because Card's equals() and hashCode() compare suit and rank only.
     *
     * <p>Called by: CardFileLoader.load(), ShuffleConfiguration.prepareDeck(Deck), GameController.simulate(),
     * SolvableDealProvider.create().
     *
     * @param drawOrder the cards in draw order; the first element is the first card to be drawn
     * @throws IllegalArgumentException for a duplicate card or a card count other than 52
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

    /**
     * {@return unmodifiable copies of the cards in draw order, next card first} Changing the copies does not affect
     * the deck.
     *
     * <p>Called by: ShuffleConfiguration.prepareDeck(Deck), GameController.simulate().
     */
    public List<Card> drawOrder() {
        return cards.stream().map(Card::copy).toList();
    }

    /**
     * Checks that the deck holds exactly 52 different cards.
     *
     * <p>Called by: ShuffleConfiguration.prepareDeck(Deck).
     *
     * @throws IllegalStateException unless the deck holds exactly 52 cards and no two are equal
     */
    public void verify() {
        if (cards.size() != CARD_COUNT || new HashSet<>(cards).size() != CARD_COUNT) {
            throw new IllegalStateException("Deck must contain " + CARD_COUNT + " unique cards");
        }
    }

    /**
     * Removes and returns the next card.
     *
     * <p>Called by: GameState.deal().
     *
     * @return the card that was removed
     * @throws IllegalStateException if the deck is empty
     */
    public Card draw() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Empty deck");
        }
        return cards.removeFirst();
    }

    /** {@return how many cards have not been drawn yet} Called by: GameState.deal(). */
    public int size() {
        return cards.size();
    }
}
