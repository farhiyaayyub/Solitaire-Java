package solitaire.pile;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import solitaire.model.Card;

/**
 * A pile of cards on the board (Template Method pattern for takeTop() and the onTopRemoved() hook).
 *
 * <p>Kind: abstract class, extends AbstractList&lt;Card&gt; (so a pile reads like a read-only List); extended by
 * StockPile, WastePile, TableauPile, and FoundationPile.
 * <p>Purpose: it holds the code every pile shares: reading, taking, placing, and copying cards. Each subclass adds
 * only its own rules.
 * <p>Data: the cards in a private ArrayList, top card at the end: adding or removing the top is O(1), and the tableau
 * also needs cards from the middle (get(index), subList), which a Deque cannot give.
 * <p>OOP: Inheritance - the four piles reuse this code. Abstraction - accepts(), copy(), and name() are abstract, so
 * each pile gives its own rule. Polymorphism - place() and the display code call the accepts() and name() of the
 * actual pile. Encapsulation - subclasses change the list only through protected methods; others only get copies.
 *
 * @author Farhiya Ayyub
 */
public abstract class CardPile extends AbstractList<Card> {
    private final List<Card> cards = new ArrayList<>();

    /**
     * {@return a copy of the card at a position, counted from 0 at the bottom} It is a copy, so callers cannot turn a
     * card on the board face up or down.
     *
     * <p>Called by: ConsoleRenderer.describeMove().
     *
     * @param index the position, 0 for the bottom card
     */
    @Override
    public Card get(int index) {
        return cards.get(index).copy();
    }

    /**
     * {@return the number of cards in the pile}
     *
     * <p>Called by: GameState.foundationCount(), ConsoleRenderer.render(), ConsoleRenderer.describeMove(),
     * MoveGenerator.addLegalMoves(), MoveValidator.isLegalDraw(), ObservableBoard.from(), and the pile classes.
     */
    @Override
    public int size() {
        return cards.size();
    }

    /**
     * {@return unmodifiable copies of the cards, bottom first} Callers cannot change the pile through it.
     *
     * <p>Called by: GameState.verify(), ConsoleRenderer.renderTableau(), ObservableBoard.from().
     */
    public List<Card> cards() {
        return cards.stream().map(Card::copy).toList();
    }

    /**
     * {@return a copy of the top card, without removing it}
     *
     * <p>Called by: ConsoleRenderer.describeMove(), ConsoleRenderer.topOrEmpty(), MoveValidator.isLegalSingleCardMove().
     *
     * @throws IndexOutOfBoundsException if the pile is empty
     */
    public Card top() {
        return cardAt(size() - 1).copy();
    }

    /**
     * {@return {@code true} if the pile has no cards}
     *
     * <p>Called by: GameState.won(), ConsoleRenderer.render(), ConsoleRenderer.describeMove(),
     * ConsoleRenderer.topOrEmpty(), MoveValidator.isLegal(), MoveValidator.isLegalDraw(),
     * MoveValidator.isLegalSingleCardMove(), SolvableDealProvider.create(), and the pile classes.
     */
    @Override
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * {@return {@code true} if this pile's rule allows the card to be placed on top}
     *
     * <p>Called by: MoveValidator.isLegalSingleCardMove(), place().
     *
     * @param card the card to check
     */
    public abstract boolean accepts(Card card);

    /**
     * {@return a new pile of the same kind holding copies of these cards} Each subclass returns its own type, so a
     * copied board keeps its real pile types.
     *
     * <p>Called by: GameState.copy() (on each concrete pile).
     */
    public abstract CardPile copy();

    /**
     * {@return the pile's name in a move sentence, such as "T3", "the waste", or "the clubs foundation"} The display
     * code never has to check which kind of pile it has.
     *
     * <p>Called by: ConsoleRenderer.describeMove().
     *
     * @param position the column index from the move (0 for T1); piles that are not columns ignore it
     */
    public abstract String name(int position);

    /**
     * Adds the card on top if this pile's accepts() rule allows it.
     *
     * <p>Called by: SolitaireGame.applyLegalMove().
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
     * Removes and returns the top card, then runs the onTopRemoved() hook so a subclass can react.
     *
     * <p>Called by: SolitaireGame.applyLegalMove(), StockPile.drawTo(), StockPile.recycle().
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
     * Removes the top count cards and returns them bottom card first, then runs the onTopRemoved() hook.
     *
     * <p>Called by: TableauPile.removeSequence().
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
     * {@return the real card at a position, counted from 0 at the bottom} Only subclasses get the real card; other
     * classes get copies from get() and top().
     *
     * <p>Called by: top(), TableauPile.accepts(), TableauPile.isValidSequence(), TableauPile.sequenceStart(),
     * TableauPile.revealTop().
     *
     * @param index the position, 0 for the bottom card
     */
    protected Card cardAt(int index) {
        return cards.get(index);
    }

    /**
     * Puts the card on top without checking accepts(), for dealing, drawing, and copying.
     *
     * <p>Called by: place(), copyCardsInto(), StockPile(List), StockPile.recycle(), TableauPile(List),
     * TableauPile.addSequence(), WastePile.receiveDrawn().
     *
     * @param card the card to add
     */
    protected void addToTop(Card card) {
        cards.add(card);
    }

    /**
     * Hook that runs after cards are removed from the top; does nothing unless a subclass overrides it (TableauPile
     * does, to reveal its new top card).
     *
     * <p>Called by: takeTop(), removeTopCards().
     */
    protected void onTopRemoved() {
    }

    /**
     * Adds a copy of every card, bottom to top, to the supplied empty pile and returns that pile. The generic type T
     * lets each subclass's copy() return its own type without a cast.
     *
     * <p>Called by: the copy() of StockPile, WastePile, TableauPile, and FoundationPile.
     *
     * @param <T> the kind of pile
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
