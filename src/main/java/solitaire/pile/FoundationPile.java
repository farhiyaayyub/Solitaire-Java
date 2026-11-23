package solitaire.pile;

import java.util.Objects;

import solitaire.enums.Suit;
import solitaire.model.Card;

/**
 * One of the four foundation piles. It collects a single suit in order from Ace to King; when it holds 13 cards,
 * that suit is complete.
 *
 * <p>Inheritance: it extends CardPile. Polymorphism: it overrides accepts() and copy().
 *
 * @author Farhiya Ayyub
 */
public final class FoundationPile extends CardPile {
    private final Suit suit;

    /**
     * Creates an empty foundation for one suit.
     *
     * @param suit the suit this foundation collects
     * @throws NullPointerException if suit is {@code null}
     */
    public FoundationPile(Suit suit) {
        this.suit = Objects.requireNonNull(suit);
    }

    /**
     * Returns {@code true} only for a face-up card of this suit whose rank value is size() + 1: an Ace on an empty
     * foundation, a 2 on the Ace, and so on up to the King.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link Card#isFaceUp} - only face-up cards may move</li>
     *   <li>{@link Card#suit()} - must match this foundation's suit</li>
     *   <li>{@link Card#rank()} - gets the rank to compare</li>
     *   <li>{@code Rank.value()} - gives the rank as a number</li>
     *   <li>{@link CardPile#size} - the next rank needed is size() + 1</li>
     * </ul>
     *
     * <p>Called by: {@code MoveValidator.isLegalSingleCardMove()} (through CardPile),
     * {@link CardPile#place} (through CardPile).
     *
     * @param card the card to check
     * @return {@code true} only for a face-up card of this suit whose rank value is size() + 1: an Ace on an empty
     *     foundation, a 2 on the Ace, and so on up to the King
     */
    @Override
    public boolean accepts(Card card) {
        return card != null && card.isFaceUp() && card.suit() == suit && card.rank().value() == size() + 1;
    }

    /**
     * Returns a new foundation of the same suit holding copies of these cards.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link CardPile#copyCardsInto} - fills the new pile with copies</li>
     * </ul>
     *
     * <p>Called by: {@code GameState.copy()}.
     *
     * @return a new foundation of the same suit holding copies of these cards
     */
    @Override
    public FoundationPile copy() {
        return copyCardsInto(new FoundationPile(suit));
    }
}
