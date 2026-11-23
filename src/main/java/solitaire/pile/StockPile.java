package solitaire.pile;

import java.util.List;

import solitaire.model.Card;

/**
 * The face-down cards that have not been drawn yet. A draw moves its top card to the waste; when it is empty, a
 * recycle turns the whole waste back into the stock.
 *
 * <p>Inheritance: it extends CardPile. Polymorphism: it overrides accepts() and copy().
 *
 * @author Farhiya Ayyub
 */
public final class StockPile extends CardPile {

    /** Creates an empty stock; copy() fills it with copied cards. */
    public StockPile() {
    }

    /**
     * Turns every card face down and stacks them so the first card in the list ends on top and is drawn first.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link Card#conceal} - stock cards are face down</li>
     *   <li>{@link CardPile#addToTop} - stacks the cards</li>
     * </ul>
     *
     * @param drawOrder the cards in draw order; the first element is the first card to be drawn
     */
    public StockPile(List<Card> drawOrder) {
        for (int cardIndex = drawOrder.size() - 1; cardIndex >= 0; cardIndex--) {
            Card card = drawOrder.get(cardIndex);
            card.conceal();
            addToTop(card);
        }
    }

    /**
     * Always returns {@code false}: players never place cards on the stock; only recycle() refills it.
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
     * Returns a new stock holding copies of these cards in the same order.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link CardPile#copyCardsInto} - fills the new pile with copies</li>
     * </ul>
     *
     * <p>Called by: {@code GameState.copy()}.
     *
     * @return a new stock holding copies of these cards in the same order
     */
    @Override
    public StockPile copy() {
        return copyCardsInto(new StockPile());
    }

    /**
     * Moves one card from the top of the stock to the top of the waste and turns it face up. A Draw 3 move calls this
     * three times.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link CardPile#takeTop} - removes the top stock card</li>
     *   <li>{@code WastePile.receiveDrawn()} - puts it face up on the waste</li>
     * </ul>
     *
     * <p>Called by: {@code SolitaireGame.applyLegalMove()}.
     *
     * @param waste the waste that receives the card
     */
    public void drawTo(WastePile waste) {
        waste.receiveDrawn(takeTop());
    }

    /**
     * Moves every waste card back to the stock face down. Taking the waste from the top reverses it again, so the stock
     * is drawn in the same order as before.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link CardPile#isEmpty} - the stock must be empty and the waste must have cards</li>
     *   <li>{@link CardPile#takeTop} - takes the waste cards one by one</li>
     *   <li>{@link Card#conceal} - turns them face down</li>
     *   <li>{@link CardPile#addToTop} - stacks them back in the stock</li>
     * </ul>
     *
     * <p>Called by: {@code SolitaireGame.applyLegalMove()}.
     *
     * @param waste the waste to turn back into the stock
     * @throws IllegalStateException unless the stock is empty and the waste is not
     */
    public void recycle(WastePile waste) {
        if (!isEmpty() || waste.isEmpty()) {
            throw new IllegalStateException("Recycle needs an empty stock and a nonempty waste");
        }
        while (!waste.isEmpty()) {
            Card card = waste.takeTop();
            card.conceal();
            addToTop(card);
        }
    }
}
