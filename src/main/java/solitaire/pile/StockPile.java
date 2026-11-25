package solitaire.pile;

import java.util.List;

import solitaire.model.Card;

/**
 * The stock: the face-down cards that have not been drawn yet. Cards are drawn from it to the waste, and when it is
 * empty the waste can be turned back into it.
 *
 * @author Farhiya Ayyub
 */
public final class StockPile extends CardPile {

    /** Creates an empty stock. */
    public StockPile() {
    }

    /**
     * Creates the stock from the cards left after the deal. It turns every card face down and stacks them so the first
     * card in the list is on top and is drawn first.
     */
    public StockPile(List<Card> drawOrder) {
        for (int cardIndex = drawOrder.size() - 1; cardIndex >= 0; cardIndex--) {
            Card card = drawOrder.get(cardIndex);
            card.conceal();
            addToTop(card);
        }
    }

    /** Always returns false, because no move may place a card on the stock. */
    @Override
    public boolean accepts(Card card) {
        return false;
    }

    /** Returns a new stock with copies of these cards in the same order. */
    @Override
    public StockPile copy() {
        return copyCardsInto(new StockPile());
    }

    /** Returns "the stock" for move sentences. */
    @Override
    public String name(int position) {
        return "the stock";
    }

    /** Takes the top card of the stock and puts it face up on the waste. Throws an error if the stock is empty. */
    public void drawTo(WastePile waste) {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot draw from an empty stock");
        }
        waste.receiveDrawn(takeTop());
    }

    /**
     * When the stock is empty, moves every waste card back to the stock face down, so the cards can be drawn again in
     * the same order.
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
