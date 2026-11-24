package solitaire.pile;

import solitaire.model.Card;

/**
 * The face-up cards drawn from the stock. Only the top card can be played.
 *
 * <p>Inheritance: it extends CardPile. Polymorphism: it overrides accepts() and copy().
 *
 * @author Farhiya Ayyub
 */
public final class WastePile extends CardPile {

    /**
     * Always returns {@code false}: cards reach the waste only by drawing, through receiveDrawn().
     *
     * <p>Implements {@link CardPile#accepts}.
     *
     * <p>Called by: {@code MoveValidator.isLegalSingleCardMove()} (through CardPile),
     * {@link CardPile#place} (through CardPile).
     *
     * @param card the card to check
     * @return {@code false}, always
     */
    @Override
    public boolean accepts(Card card) {
        return false;
    }

    /**
     * Returns a new waste holding copies of these cards in the same order.
     *
     * <p>Implements {@link CardPile#copy}.
     *
     * <p>Called by: {@code GameState.copy()}.
     *
     * @return a new waste holding copies of these cards in the same order
     */
    @Override
    public WastePile copy() {
        return copyCardsInto(new WastePile());
    }

    /**
     * Turns a card drawn from the stock face up and puts it on top. It has no access modifier (package-private), so
     * only classes in solitaire.pile, in practice StockPile.drawTo(), can call it.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link Card#reveal} - drawn cards are face up</li>
     *   <li>{@link CardPile#addToTop} - puts the card on the waste</li>
     * </ul>
     *
     * <p>Called by: {@link StockPile#drawTo}.
     *
     * @param card the card just taken from the stock
     */
    void receiveDrawn(Card card) {
        card.reveal();
        addToTop(card);
    }
}
