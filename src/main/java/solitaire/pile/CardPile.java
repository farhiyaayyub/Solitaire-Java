package solitaire.pile;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import solitaire.model.Card;

/**
 * Parent class of the four kinds of piles. It holds the code every pile shares (the card list, taking and placing
 * cards, copying) and leaves two decisions to each subclass: which cards it accepts, and how it copies itself.
 * The top card is the last element of the list.
 *
 * <p>It extends AbstractList&lt;Card&gt;, so a pile can be read like a read-only List. Inheritance: the four piles
 * share this code. Abstraction: accepts() and copy() are abstract, so each pile must define them. Encapsulation: the
 * card list is private; subclasses change it only through protected methods.
 *
 * @author Farhiya Ayyub
 */
public abstract class CardPile extends AbstractList<Card> {
    private final List<Card> cards = new ArrayList<>();

    /**
     * Returns the card at a position, counted from 0 at the bottom. Required by AbstractList; the returned card is the
     * real card, not a copy.
     *
     * <p>Called by: {@code ConsoleRenderer.describeMove()}.
     *
     * @param index the position, 0 for the bottom card
     * @return the card at a position, counted from 0 at the bottom
     */
    @Override
    public Card get(int index) {
        return cards.get(index);
    }

    /**
     * Returns the number of cards in the pile. Required by AbstractList.
     *
     * <p>Called by: {@code GameState.foundationCount()}, {@code ConsoleRenderer.render()},
     * {@code ConsoleRenderer.describeMove()}, {@code MoveGenerator.addLegalMoves()},
     * {@code MoveValidator.isLegalDraw()}, {@link #top}, {@link #takeTop}, {@link #removeTopCards},
     * {@link FoundationPile#accepts}, {@link TableauPile#accepts}, {@link TableauPile#isValidSequence},
     * {@link TableauPile#sequenceStart}, {@code TableauPile.revealTop()}, {@code ObservableBoard.from()}.
     *
     * @return the number of cards in the pile
     */
    @Override
    public int size() {
        return cards.size();
    }

    /**
     * Returns copies of all cards from bottom to top in a list that cannot be modified, so callers cannot change the
     * pile through it.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link Card#copy} - protects the real cards</li>
     * </ul>
     *
     * <p>Called by: {@code GameState.verify()}, {@code ConsoleRenderer.renderTableau()},
     * {@code ObservableBoard.from()}.
     *
     * @return copies of all cards from bottom to top in a list that cannot be modified, so callers cannot change the
     *     pile through it
     */
    public List<Card> cards() {
        return cards.stream().map(Card::copy).toList();
    }

    /**
     * Returns the top card without removing it.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link #size} - the top card is at size() - 1</li>
     *   <li>{@link #cardAt} - reads the card</li>
     * </ul>
     *
     * <p>Called by: {@code ConsoleRenderer.describeMove()}, {@code ConsoleRenderer.topOrEmpty()},
     * {@code MoveValidator.isLegalSingleCardMove()}.
     *
     * @return the top card without removing it
     * @throws IndexOutOfBoundsException if the pile is empty
     */
    public Card top() {
        return cardAt(size() - 1);
    }

    /**
     * Returns {@code true} if the pile has no cards.
     *
     * <p>Called by: {@code GameState.won()}, {@code ConsoleRenderer.render()}, {@code ConsoleRenderer.describeMove()},
     * {@code ConsoleRenderer.topOrEmpty()}, {@code MoveValidator.isLegal()}, {@code MoveValidator.isLegalDraw()},
     * {@code MoveValidator.isLegalSingleCardMove()}, {@link StockPile#recycle}, {@link TableauPile#accepts},
     * {@code TableauPile.revealTop()}, {@code SolvableDealProvider.create()}.
     *
     * @return {@code true} if the pile has no cards
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * Returns {@code true} if this pile's rule allows the card to be placed on top. Each subclass defines its own rule,
     * and Java runs the rule of the actual pile object.
     *
     * <p>Called by: {@code MoveValidator.isLegalSingleCardMove()}, {@link #place}.
     *
     * @param card the card to check
     * @return {@code true} if this pile's rule allows the card to be placed on top
     */
    public abstract boolean accepts(Card card);

    /**
     * Returns a new pile of the same kind holding copies of these cards.
     *
     * <p>Called by: no one directly; {@code GameState.copy()} calls the copy() of each concrete pile.
     *
     * @return a new pile of the same kind holding copies of these cards
     */
    public abstract CardPile copy();

    /**
     * Adds the card on top if this pile's accepts() rule allows it.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link #accepts} - enforces this pile's rule</li>
     *   <li>{@link #addToTop} - adds the card</li>
     * </ul>
     *
     * <p>Called by: {@code SolitaireGame.applyLegalMove()}.
     *
     * @param card the card to add
     * @throws IllegalArgumentException if the pile does not accept the card (nothing is changed)
     */
    public void place(Card card) {
        if (!accepts(card)) {
            throw new IllegalArgumentException("Pile does not accept this card");
        }
        addToTop(card);
    }

    /**
     * Removes and returns the top card, then runs onTopRemoved().
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link #size} - finds the top position</li>
     *   <li>{@link #onTopRemoved} - lets the subclass react, e.g. reveal</li>
     * </ul>
     *
     * <p>Called by: {@code SolitaireGame.applyLegalMove()}, {@link StockPile#drawTo}, {@link StockPile#recycle}.
     *
     * @return the card that was removed
     * @throws IndexOutOfBoundsException if the pile is empty
     */
    public Card takeTop() {
        Card removed = cards.remove(size() - 1);
        onTopRemoved();
        return removed;
    }

    /**
     * Removes the top count cards and returns them in bottom-to-top order, then runs onTopRemoved().
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link #size} - checks the count and finds the top</li>
     *   <li>{@link #onTopRemoved} - lets the subclass react, e.g. reveal</li>
     * </ul>
     *
     * <p>Called by: {@link TableauPile#removeSequence}.
     *
     * @param count how many cards to remove
     * @return the removed cards, bottom card first
     * @throws IllegalArgumentException if count is negative or larger than the pile
     */
    protected List<Card> removeTopCards(int count) {
        if (count < 0 || count > size()) {
            throw new IllegalArgumentException("Invalid number of cards: " + count);
        }
        List<Card> removed = new ArrayList<>(cards.subList(size() - count, size()));
        // Clearing the sub-list view removes those cards from the pile itself.
        cards.subList(size() - count, size()).clear();
        onTopRemoved();
        return removed;
    }

    /**
     * Returns the card at a position counted from 0 at the bottom; for use by subclasses.
     *
     * <p>Called by: {@link #top}, {@link TableauPile#accepts}, {@link TableauPile#isValidSequence},
     * {@link TableauPile#sequenceStart}, {@code TableauPile.revealTop()}.
     *
     * @param index the position, 0 for the bottom card
     * @return the card at a position counted from 0 at the bottom
     */
    protected Card cardAt(int index) {
        return cards.get(index);
    }

    /**
     * Puts the card on top without checking accepts(); used while dealing, drawing, and copying.
     *
     * <p>Called by: {@link #place}, {@link #copyCardsInto}, {@link StockPile#StockPile}, {@link StockPile#recycle},
     * {@link TableauPile#TableauPile}, {@link TableauPile#addSequence}, {@code WastePile.receiveDrawn()}.
     *
     * @param card the card to add
     */
    protected void addToTop(Card card) {
        cards.add(card);
    }

    /**
     * Runs after cards are removed from the top. It does nothing here; TableauPile overrides it to turn the newly
     * exposed card face up.
     *
     * <p>Called by: {@link #takeTop}, {@link #removeTopCards}.
     */
    protected void onTopRemoved() {
    }

    /**
     * Adds a copy of every card, bottom to top, to the supplied empty pile and returns that pile. The generic type T
     * lets each subclass's copy() return its own type without a cast.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link Card#copy} - copies each card</li>
     *   <li>{@link #addToTop} - adds it without the accepts() check</li>
     * </ul>
     *
     * <p>Called by: {@link FoundationPile#copy}, {@link StockPile#copy}, {@link TableauPile#copy},
     * {@link WastePile#copy}.
     *
     * @param <T> the kind of pile, so copy() can return its own type
     * @param target an empty pile of the right kind
     * @return the same target pile, now holding the copies
     */
    protected <T extends CardPile> T copyCardsInto(T target) {
        for (Card card : cards) {
            target.addToTop(card.copy());
        }
        return target;
    }
}
