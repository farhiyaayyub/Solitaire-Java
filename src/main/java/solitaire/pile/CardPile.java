package solitaire.pile;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import solitaire.model.Card;

/**
 * The parent class of every pile on the board. It holds the cards and the shared actions, while each subclass sets its
 * own rule in accepts().
 *
 * @author Farhiya Ayyub
 */
public abstract class CardPile extends AbstractList<Card> {
    private final List<Card> cards = new ArrayList<>();

    /**
     * Returns a copy of the card at a position, counting from 0 at the bottom, so the real card cannot be changed from
     * outside.
     */
    @Override
    public Card get(int index) {
        // Give back a copy, so whoever reads it cannot turn the real card over.
        return cards.get(index).copy();
    }

    /** Returns the number of cards in the pile. */
    @Override
    public int size() {
        return cards.size();
    }

    /** Returns copies of all the cards in the pile, bottom card first. */
    public List<Card> cards() {
        // Copy every card into a new list that cannot be changed.
        return cards.stream().map(Card::copy).toList();
    }

    /** Returns a copy of the top card without removing it. */
    public Card top() {
        // The top card is the last one in the list.
        return cardAt(size() - 1).copy();
    }

    /** Returns true if the pile has no cards. */
    @Override
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /** Returns true if this pile's rule allows the card to be placed on top. Each kind of pile has its own rule. */
    public abstract boolean accepts(Card card);

    /** Returns a new pile of the same kind holding copies of these cards. */
    public abstract CardPile copy();

    /** Returns the pile's name used in move sentences, such as "T3", "the waste", or "the clubs foundation". */
    public abstract String name(int position);

    /**
     * Puts the card on top of the pile if accepts() allows it; otherwise it throws an error, so an illegal card can
     * never land on a pile.
     */
    public void place(Card card) {
        // Ask this pile's own rule first; if it says no, stop with an error.
        if (!accepts(card)) {
            throw new IllegalArgumentException("Pile does not accept this card");
        }
        // The rule allows it, so put the card on top.
        addToTop(card);
    }

    /**
     * Removes the top card and returns it, then calls onTopRemoved() so a subclass can react; for example, a tableau
     * column turns its new top card face up.
     */
    public Card takeTop() {
        // Remove the last card in the list, which is the top card.
        Card removed = cards.remove(size() - 1);
        // Let the subclass react to the change.
        onTopRemoved();
        return removed;
    }

    /** Removes the top count cards and returns them bottom card first, then calls onTopRemoved(). */
    protected List<Card> removeTopCards(int count) {
        // A negative count or more cards than the pile has is not possible, so stop with an error.
        if (count < 0 || count > size()) {
            throw new IllegalArgumentException("Invalid number of cards: " + count);
        }
        // Copy the top count cards into a new list, keeping their order.
        List<Card> removed = new ArrayList<>(cards.subList(size() - count, size()));
        // Clearing the sub-list view removes those cards from the pile itself.
        cards.subList(size() - count, size()).clear();
        // Let the subclass react to the change.
        onTopRemoved();
        return removed;
    }

    /** Returns the real card, not a copy, at a position. Only subclasses use it. */
    protected Card cardAt(int index) {
        return cards.get(index);
    }

    /** Puts the card on top without checking accepts(). Used for dealing, drawing, and copying. */
    protected void addToTop(Card card) {
        // Adding to the end of the list puts the card on top.
        cards.add(card);
    }

    /**
     * Runs after cards are removed from the top. It does nothing here; TableauPile overrides it to turn its new top
     * card face up.
     */
    protected void onTopRemoved() {
    }

    /**
     * Adds a copy of every card, bottom to top, to the given empty pile and returns that pile. Each subclass uses it in
     * its copy() method.
     */
    protected <T extends CardPile> T copyCardsInto(T target) {
        // Copy each card, from bottom to top, so the new pile has the same order.
        for (Card card : cards) {
            target.addToTop(card.copy());
        }
        return target;
    }
}
