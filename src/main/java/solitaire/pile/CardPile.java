package solitaire.pile;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import solitaire.model.Card;

/** Base class for a pile of cards, with the top card at the end of the list. */
public abstract class CardPile extends AbstractList<Card> {
    private final List<Card> cards = new ArrayList<>();

    @Override
    public Card get(int index) {
        return cards.get(index);
    }

    @Override
    public int size() {
        return cards.size();
    }

    /** Returns a detached snapshot of all cards, from bottom to top. */
    public List<Card> cards() {
        return cards.stream().map(Card::copy).toList();
    }

    /** Returns the top card, or throws if the pile is empty. */
    public Card top() {
        return cardAt(size() - 1);
    }

    /** Returns whether this pile contains no cards. */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /** Returns whether this pile can receive the supplied card. */
    public abstract boolean accepts(Card card);

    /** Returns a detached copy of this pile. */
    public abstract CardPile copy();

    /** Adds a card to the top if this pile accepts it. */
    public void place(Card card) {
        if (!accepts(card)) {
            throw new IllegalArgumentException("Pile does not accept this card");
        }
        addToTop(card);
    }

    /** Removes and returns the top card. */
    public Card takeTop() {
        Card removed = cards.remove(size() - 1);
        onTopRemoved();
        return removed;
    }

    /** Removes the top {@code count} cards and returns them bottom-to-top. */
    protected List<Card> removeTopCards(int count) {
        if (count < 0 || count > size()) {
            throw new IllegalArgumentException("Invalid number of cards: " + count);
        }
        List<Card> removed = new ArrayList<>(cards.subList(size() - count, size()));
        cards.subList(size() - count, size()).clear();
        onTopRemoved();
        return removed;
    }

    /** Returns the card at a zero-based position from the bottom. */
    protected Card cardAt(int index) {
        return cards.get(index);
    }

    /** Adds a card to the top without applying the pile's placement rule. */
    protected void addToTop(Card card) {
        cards.add(card);
    }

    /** Hook called after the top card is removed. */
    protected void onTopRemoved() {
    }

    /** Copies every card into the supplied empty pile. */
    protected <T extends CardPile> T copyCardsInto(T target) {
        for (Card card : cards) {
            target.addToTop(card.copy());
        }
        return target;
    }
}
