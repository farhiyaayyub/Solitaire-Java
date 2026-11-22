package solitaire.pile;

import java.util.List;

import solitaire.model.Card;

/**
 * Face-down cards waiting to be drawn to the waste.
 *
 * <p>Type: final class, subclass of CardPile.
 * OOP: INHERITANCE (extends CardPile), POLYMORPHISM (overrides accepts and copy).
 *
 * @author Farhiya Ayyub
 */
public final class StockPile extends CardPile {

    /** Constructor: Creates an empty stock. */
    public StockPile() {
    }

    /**
     * Constructor: Creates a face-down stock; the first card in the list is the first card drawn.
     * Calls: Card.conceal(), addToTop()
     */
    public StockPile(List<Card> drawOrder) {
        for (int cardIndex = drawOrder.size() - 1; cardIndex >= 0; cardIndex--) {
            Card card = drawOrder.get(cardIndex);
            card.conceal();
            addToTop(card);
        }
    }

    /** Method (override): Players never place cards on the stock; it is refilled only by recycling the waste. */
    @Override
    public boolean accepts(Card card) {
        return false;
    }

    /**
     * Method (override): Returns a detached copy, including card visibility.
     * Calls: copyCardsInto()
     */
    @Override
    public StockPile copy() {
        // Object: StockPile - a new empty stock that receives the copied cards.
        return copyCardsInto(new StockPile());
    }

    /**
     * Method: Moves the top stock card to the waste, where it is turned face up.
     * Calls: takeTop(), WastePile.receiveDrawn()
     */
    public void drawTo(WastePile waste) {
        waste.receiveDrawn(takeTop());
    }

    /**
     * Method: Turns the whole waste back into the stock in its original draw order; requires an empty stock.
     * Calls: isEmpty(), WastePile.isEmpty(), WastePile.takeTop(), Card.conceal(), addToTop()
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
