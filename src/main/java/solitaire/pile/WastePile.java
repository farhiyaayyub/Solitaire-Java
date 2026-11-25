package solitaire.pile;

import solitaire.model.Card;

/**
 * The waste: the face-up cards drawn from the stock. Only its top card can be played, onto a tableau column or a
 * foundation.
 *
 * @author Farhiya Ayyub
 */
public final class WastePile extends CardPile {

    /** Always returns false, because cards reach the waste only by drawing from the stock, never by a move onto it. */
    @Override
    public boolean accepts(Card card) {
        return false;
    }

    /** Returns a new waste with copies of these cards in the same order. */
    @Override
    public WastePile copy() {
        return copyCardsInto(new WastePile());
    }

    /** Returns "the waste" for move sentences. */
    @Override
    public String name(int position) {
        return "the waste";
    }

    /** Turns a card drawn from the stock face up and puts it on top of the waste. */
    void receiveDrawn(Card card) {
        card.reveal();
        addToTop(card);
    }
}
