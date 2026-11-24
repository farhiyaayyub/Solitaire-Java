package solitaire.pile;

import java.util.Locale;
import java.util.Objects;

import solitaire.enums.Suit;
import solitaire.model.Card;

/**
 * One of the four foundation piles.
 *
 * <p>Kind: final class, extends CardPile.
 * <p>Purpose: it collects one suit in order from Ace to King; at 13 cards that suit is complete.
 * <p>Data: the suit it collects, as a final Suit field; the cards are kept in CardPile's list.
 * <p>OOP: Inheritance - it reuses CardPile's card handling. Polymorphism - it overrides accepts(), copy(), and
 * name().
 *
 * @author Farhiya Ayyub
 */
public final class FoundationPile extends CardPile {
    private final Suit suit;

    /**
     * Creates an empty foundation for one suit.
     *
     * <p>Called by: GameState.deal(), copy().
     *
     * @param suit the suit this foundation collects
     * @throws NullPointerException if suit is {@code null}
     */
    public FoundationPile(Suit suit) {
        this.suit = Objects.requireNonNull(suit);
    }

    /**
     * Accepts only a face-up card of this suit whose rank value is size() + 1: an Ace on an empty foundation, a 2 on
     * the Ace, and so on up to the King. Returns {@code false} for {@code null}.
     *
     * <p>Key idea: a foundation holds Ace, 2, 3, ... in order, so its size is also the rank of its top card. The next
     * rank needed is therefore {@code size() + 1}, without looking at the top card.
     *
     * <p>Called by: MoveValidator.isLegalSingleCardMove() and CardPile.place() (through CardPile).
     *
     * @param card the card to check
     * @return {@code true} if the card is the next one for this foundation
     */
    @Override
    public boolean accepts(Card card) {
        return card != null && card.isFaceUp() && card.suit() == suit && card.rank().value() == size() + 1;
    }

    /** {@return a new foundation of the same suit holding copies of these cards} Called by: GameState.copy(). */
    @Override
    public FoundationPile copy() {
        return copyCardsInto(new FoundationPile(suit));
    }

    /**
     * {@return "the clubs foundation", "the hearts foundation", and so on}
     *
     * <p>Called by: ConsoleRenderer.describeMove() (through CardPile).
     *
     * @param position not used; a foundation is named after its suit
     */
    @Override
    public String name(int position) {
        return "the " + suit.name().toLowerCase(Locale.ROOT) + " foundation";
    }
}
