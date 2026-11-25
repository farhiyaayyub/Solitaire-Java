package solitaire.pile;

import java.util.Locale;
import java.util.Objects;

import solitaire.enums.Suit;
import solitaire.model.Card;

/**
 * One of the four foundation piles, one for each suit. Cards are built up from Ace to King in the same suit, and the
 * game is won when all four are full.
 *
 * @author Farhiya Ayyub
 */
public final class FoundationPile extends CardPile {
    private final Suit suit;

    /** Creates an empty foundation for the given suit. */
    public FoundationPile(Suit suit) {
        this.suit = Objects.requireNonNull(suit);
    }

    /**
     * Accepts only a face-up card of this suit that is exactly one rank higher than the top card: an Ace on an empty
     * foundation, then 2, 3, and so on up to the King.
     */
    @Override
    public boolean accepts(Card card) {
        return card != null && card.isFaceUp() && card.suit() == suit && card.rank().value() == size() + 1;
    }

    /** Returns a new foundation of the same suit with copies of these cards. */
    @Override
    public FoundationPile copy() {
        return copyCardsInto(new FoundationPile(suit));
    }

    /** Returns the foundation's name for move sentences, such as "the clubs foundation". */
    @Override
    public String name(int position) {
        return "the " + suit.name().toLowerCase(Locale.ROOT) + " foundation";
    }
}
