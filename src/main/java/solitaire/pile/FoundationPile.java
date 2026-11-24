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
     * <p>Key idea: a foundation holds Ace, 2, 3, ... in order, so its size is also the rank of its top card. The next
     * rank needed is therefore {@code size() + 1}, without looking at the top card.
     *
     * <p>Implements {@link CardPile#accepts}.
     *
     * <p>Called by: {@code MoveValidator.isLegalSingleCardMove()} (through CardPile),
     * {@link CardPile#place} (through CardPile).
     *
     * @param card the card to check
     * @return {@code true} if the card is the next one for this foundation
     */
    @Override
    public boolean accepts(Card card) {
        return card != null && card.isFaceUp() && card.suit() == suit && card.rank().value() == size() + 1;
    }

    /**
     * Returns a new foundation of the same suit holding copies of these cards.
     *
     * <p>Implements {@link CardPile#copy}.
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
