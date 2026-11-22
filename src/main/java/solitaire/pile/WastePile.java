package solitaire.pile;

import solitaire.model.Card;

/**
 * Face-up cards drawn from the stock. Only its top card can be played.
 *
 * <p>Type: final class, subclass of CardPile.
 * OOP: INHERITANCE (extends CardPile), POLYMORPHISM (overrides accepts and copy).
 *
 * @author Farhiya Ayyub
 */
public final class WastePile extends CardPile {

    /** Method (override): Players never place cards on the waste; cards arrive only by drawing from the stock. */
    @Override
    public boolean accepts(Card card) {
        return false;
    }

    /**
     * Method (override): Returns a detached copy, including card visibility.
     * Calls: copyCardsInto()
     */
    @Override
    public WastePile copy() {
        // Object: WastePile - a new empty waste that receives the copied cards.
        return copyCardsInto(new WastePile());
    }

    /**
     * Method: Receives a card drawn from the stock and turns it face up. Package-private: only classes in the
     * pile package (StockPile) can call it.
     * Calls: Card.reveal(), addToTop()
     */
    void receiveDrawn(Card card) {
        card.reveal();
        addToTop(card);
    }
}
