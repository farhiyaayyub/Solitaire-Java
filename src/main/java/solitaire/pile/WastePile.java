package solitaire.pile;

import solitaire.model.Card;

/**
 * The waste: the face-up cards drawn from the stock.
 *
 * <p>Kind: final class, extends CardPile.
 * <p>Purpose: it holds the drawn cards; only its top card can be played to the tableau or a foundation.
 * <p>Data: none of its own; the cards are kept in CardPile's list.
 * <p>OOP: Inheritance - it reuses CardPile's card handling. Polymorphism - it overrides accepts(), copy(), and
 * name(). Encapsulation - only classes in this package can add cards, through receiveDrawn().
 *
 * @author Farhiya Ayyub
 */
public final class WastePile extends CardPile {

    /**
     * {@return {@code false}, always} Cards reach the waste only by drawing, through receiveDrawn().
     *
     * <p>Called by: MoveValidator.isLegalSingleCardMove() and CardPile.place() (through CardPile).
     *
     * @param card the card to check
     */
    @Override
    public boolean accepts(Card card) {
        return false;
    }

    /** {@return a new waste holding copies of these cards in the same order} Called by: GameState.copy(). */
    @Override
    public WastePile copy() {
        return copyCardsInto(new WastePile());
    }

    /**
     * {@return "the waste"}
     *
     * <p>Called by: ConsoleRenderer.describeMove() (through CardPile).
     *
     * @param position not used; there is only one waste
     */
    @Override
    public String name(int position) {
        return "the waste";
    }

    /**
     * Turns a card drawn from the stock face up and puts it on top. It is package-private, so code outside
     * solitaire.pile cannot add cards to the waste.
     *
     * <p>Called by: StockPile.drawTo().
     *
     * @param card the card just taken from the stock
     */
    void receiveDrawn(Card card) {
        card.reveal();
        addToTop(card);
    }
}
