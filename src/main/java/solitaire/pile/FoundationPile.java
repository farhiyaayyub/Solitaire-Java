package solitaire.pile;

import java.util.Locale;
import java.util.Objects;

import solitaire.enums.Suit;
import solitaire.model.Card;

/**
 * One of the four foundation piles. It accepts only the next card of its suit, from Ace up to King.
 *
 * @author Farhiya Ayyub
 */
public final class FoundationPile extends CardPile {
    private final Suit suit;

    /** Creates an empty foundation for the given suit. */
    public FoundationPile(Suit suit) {
        // A foundation must belong to a suit, so a null suit stops with an error.
        this.suit = Objects.requireNonNull(suit);
    }

    /**
     * Accepts only a face-up card of this suit that is exactly one rank higher than the top card: an Ace on an empty
     * foundation, then 2, 3, and so on up to the King.
     */
    @Override
    public boolean accepts(Card card) {
        // All four checks must be true: the card exists, it is face up, it has this suit,
        // and its rank is the next one (an empty pile has size 0, so it needs rank 1, the Ace).
        return card != null && card.isFaceUp() && card.suit() == suit && card.rank().value() == size() + 1;
    }

    /** Returns a new foundation of the same suit with copies of these cards. */
    @Override
    public FoundationPile copy() {
        // Make an empty foundation of the same suit and fill it with copies of these cards.
        return copyCardsInto(new FoundationPile(suit));
    }

    /** Returns the foundation's name for move sentences, such as "the clubs foundation". */
    @Override
    public String name(int position) {
        // Turn the suit name (for example CLUBS) into lower case: "the clubs foundation".
        return "the " + suit.name().toLowerCase(Locale.ROOT) + " foundation";
    }
}
