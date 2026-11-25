package solitaire.pile;

import java.util.List;

import solitaire.model.Card;

/**
 * The stock: the face-down cards that have not been drawn yet. It draws cards to the waste and takes the waste back
 * when it is empty.
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
        // Go through the list from the last card to the first, so the first card ends up on top.
        for (int cardIndex = drawOrder.size() - 1; cardIndex >= 0; cardIndex--) {
            Card card = drawOrder.get(cardIndex);
            // Stock cards are always face down.
            card.conceal();
            addToTop(card);
        }
    }

    /** Always returns false, because no move may place a card on the stock. */
    @Override
    public boolean accepts(Card card) {
        // No card can ever be placed on the stock by a move.
        return false;
    }

    /** Returns a new stock with copies of these cards in the same order. */
    @Override
    public StockPile copy() {
        // Make an empty stock and fill it with copies of this stock's cards.
        return copyCardsInto(new StockPile());
    }

    /** Returns "the stock" for move sentences. */
    @Override
    public String name(int position) {
        return "the stock";
    }

    /** Takes the top card of the stock and puts it face up on the waste. Throws an error if the stock is empty. */
    public void drawTo(WastePile waste) {
        // If the stock is empty, there is nothing to draw, so stop with an error.
        if (isEmpty()) {
            throw new IllegalStateException("Cannot draw from an empty stock");
        }
        // Take the top card off the stock and give it to the waste, which turns it face up.
        waste.receiveDrawn(takeTop());
    }

    /**
     * When the stock is empty, moves every waste card back to the stock face down, so the cards can be drawn again in
     * the same order.
     */
    public void recycle(WastePile waste) {
        // Recycling is only allowed when the stock is empty and the waste still has cards.
        if (!isEmpty() || waste.isEmpty()) {
            throw new IllegalStateException("Recycle needs an empty stock and a nonempty waste");
        }
        // Move the waste cards one by one, from the top, back onto the stock.
        while (!waste.isEmpty()) {
            Card card = waste.takeTop();
            // Turn the card face down again, because it is back in the stock.
            card.conceal();
            addToTop(card);
        }
    }
}
