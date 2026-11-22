package solitaire.pile;

import java.util.Objects;

import solitaire.enums.Suit;
import solitaire.model.Card;

/**
 * One foundation pile, built up from Ace to King in a single suit.
 *
 * <p>Type: final class, subclass of CardPile.
 * OOP: INHERITANCE (extends CardPile), POLYMORPHISM (overrides accepts and copy), ENCAPSULATION (private suit).
 *
 * @author Farhiya Ayyub
 */
public final class FoundationPile extends CardPile {
    // Field: the suit this foundation collects; set once in the constructor.
    private final Suit suit;

    /**
     * Constructor: Creates an empty foundation for the supplied suit.
     * Calls: Objects.requireNonNull()
     */
    public FoundationPile(Suit suit) {
        this.suit = Objects.requireNonNull(suit);
    }

    /**
     * Method (override): Accepts a face-up card of this suit exactly one rank higher than the current top.
     * Calls: Card.isFaceUp(), Card.suit(), Card.rank(), Rank.value(), size()
     */
    @Override
    public boolean accepts(Card card) {
        return card != null && card.isFaceUp() && card.suit() == suit && card.rank().value() == size() + 1;
    }

    /**
     * Method (override): Returns a detached copy, including card visibility.
     * Calls: copyCardsInto()
     */
    @Override
    public FoundationPile copy() {
        // Object: FoundationPile - a new empty foundation of the same suit that receives the copied cards.
        return copyCardsInto(new FoundationPile(suit));
    }
}
