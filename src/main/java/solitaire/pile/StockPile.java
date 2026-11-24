package solitaire.pile;

import java.util.List;

import solitaire.model.Card;

/**
 * The stock: the face-down cards that have not been drawn yet.
 *
 * <p>Kind: final class, extends CardPile.
 * <p>Purpose: it feeds the waste with draw moves and is refilled from the waste with a recycle move.
 * <p>Data: none of its own; the cards are kept in CardPile's list.
 * <p>OOP: Inheritance - it reuses CardPile's card handling. Polymorphism - it overrides accepts(), copy(), and
 * name().
 *
 * @author Farhiya Ayyub
 */
public final class StockPile extends CardPile {

    /** Creates an empty stock. Called by: copy(). */
    public StockPile() {
    }

    /**
     * Turns every card face down and stacks them so the first card in the list ends on top and is drawn first.
     *
     * <p>Called by: GameState.deal().
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
     * {@return {@code false}, always} Players never place cards on the stock; only recycle() refills it.
     *
     * <p>Called by: MoveValidator.isLegalSingleCardMove() and CardPile.place() (through CardPile).
     *
     * @param card the card to check
     */
    @Override
    public boolean accepts(Card card) {
        return false;
    }

    /** {@return a new stock holding copies of these cards in the same order} Called by: GameState.copy(). */
    @Override
    public StockPile copy() {
        return copyCardsInto(new StockPile());
    }

    /**
     * {@return "the stock"}
     *
     * <p>Called by: ConsoleRenderer.describeMove() (through CardPile).
     *
     * @param position not used; there is only one stock
     */
    @Override
    public String name(int position) {
        return "the stock";
    }

    /**
     * Moves one card from the stock to the waste, face up. A Draw 3 move calls this three times.
     *
     * <p>Called by: SolitaireGame.applyLegalMove().
     *
     * @param waste the waste that receives the card
     * @throws IllegalStateException if the stock is empty
     */
    public void drawTo(WastePile waste) {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot draw from an empty stock");
        }
        waste.receiveDrawn(takeTop());
    }

    /**
     * Moves every waste card back to the stock, face down.
     *
     * <p>Key idea: taking the waste from the top reverses its order, so the stock is drawn in the same order as the
     * first time.
     *
     * <p>Called by: SolitaireGame.applyLegalMove().
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
