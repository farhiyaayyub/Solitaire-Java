package solitaire.pile;

import solitaire.model.Card;

/**
 * The waste: the face-up cards drawn from the stock. Only its top card can be played.
 *
 * @author Farhiya Ayyub
 */
public final class WastePile extends CardPile {

    /** Always returns false, because cards reach the waste only by drawing from the stock, never by a move onto it. */
    @Override
    public boolean accepts(Card card) {
        // No card can ever be placed on the waste by a move; it only gets cards drawn from the stock.
        return false;
    }

    /** Returns a new waste with copies of these cards in the same order. */
    @Override
    public WastePile copy() {
        // Make an empty waste and fill it with copies of this waste's cards.
        return copyCardsInto(new WastePile());
    }

    /** Returns "the waste" for move sentences. */
    @Override
    public String name(int position) {
        return "the waste";
    }

    /** Turns a card drawn from the stock face up and puts it on top of the waste. */
    void receiveDrawn(Card card) {
        // A drawn card is always shown face up.
        card.reveal();
        // Put it on top, so it becomes the card that can be played next.
        addToTop(card);
    }
}
