package solitaire.pile;

import solitaire.model.Card;
import solitaire.enums.Rank;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores an ordered tableau column.
 *
 * @author Farhiya Ayyub
 */
public final class TableauPile {
    private final ArrayList<Card> cards = new ArrayList<>();

    /** Returns the number of cards in this pile or deck. */
    public int size() {
        return cards.size();
    }

    /** Returns a detached top-card snapshot, or null when empty. */
    public Card top() {
        return cards.isEmpty() ? null : cards.get(cards.size() - 1).copy();
    }

    /** Returns an unmodifiable list of detached card snapshots. */
    public List<Card> cards() {
        return cards.stream().map(Card::copy).toList();
    }

    /** Deals cards into the initial layout without shuffling. */
    public void deal(Card card) {
        cards.add(card);
    }

    /** Reveals the top tableau card if this column is nonempty. */
    public void revealTop() {
        if (!cards.isEmpty()) cards.get(cards.size() - 1).reveal();
    }

    /** Reports whether the supplied face-up card satisfies this pile's placement rule. */
    public boolean accepts(Card card) {
        if (card == null || !card.isFaceUp()) return false;
        return top() == null ? card.rank() == Rank.KING : top().isFaceUp() && top().rank().getValue() == card.rank().getValue() + 1 && top().isRed() != card.isRed();
    }

    /** Checks that the requested top suffix is face-up, descending, and alternating in color. */
    public boolean validSequence(int count) {
        if (count < 1 || count > size()) return false;
        for (int cardIndex = size() - count; cardIndex < size(); cardIndex++) {
            Card card = cards.get(cardIndex);
            if (!card.isFaceUp()) return false;
            if (cardIndex + 1 < size()) {
                Card nextCard = cards.get(cardIndex + 1);
                if (card.rank().getValue() != nextCard.rank().getValue() + 1 || card.isRed() == nextCard.isRed()) return false;
            }
        }
        return true;
    }

    /** Removes a valid top suffix and reveals the newly exposed card. */
    public List<Card> removeSequence(int count) {
        if (!validSequence(count)) throw new IllegalArgumentException("Invalid sequence");
        List<Card> removedCards = new ArrayList<>(cards.subList(size() - count, size()));
        cards.subList(size() - count, size()).clear();
        revealTop();
        return removedCards;
    }

    /** Appends a valid descending sequence when its first card fits this column. */
    public void addSequence(List<Card> sequence) {
        TableauPile sequenceCheck = new TableauPile();
        sequence.forEach(sequenceCheck::deal);
        if (!sequenceCheck.validSequence(sequence.size()) || !accepts(sequence.get(0)))
            throw new IllegalArgumentException("Invalid placement");
        cards.addAll(sequence);
    }

    /** Returns a detached copy, including card visibility. */
    public TableauPile copy() {
        TableauPile pileCopy = new TableauPile();
        for (Card card : cards) pileCopy.deal(card.copy());
        return pileCopy;
    }
}
