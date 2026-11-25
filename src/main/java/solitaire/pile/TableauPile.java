package solitaire.pile;

import java.util.List;

import solitaire.enums.Rank;
import solitaire.model.Card;

/**
 * One of the seven tableau columns. It holds face-down cards with face-up cards on top, built down from King in
 * alternating colors, and it is the only pile where a run of several cards can move at once.
 *
 * @author Farhiya Ayyub
 */
public final class TableauPile extends CardPile {

    /** Creates an empty column. */
    public TableauPile() {
    }

    /** Creates a column from the dealt cards: every card is placed face down, then the top card is turned face up. */
    public TableauPile(List<Card> dealtCards) {
        for (Card card : dealtCards) {
            card.conceal();
            addToTop(card);
        }
        revealTop();
    }

    /**
     * Accepts a face-up King on an empty column. On a column with cards, it accepts a face-up card that is one rank
     * lower and the opposite color of the top card, such as a red 9 on a black 10.
     */
    @Override
    public boolean accepts(Card card) {
        if (card == null || !card.isFaceUp()) {
            return false;
        }
        if (isEmpty()) {
            return card.rank() == Rank.KING;
        }
        Card topCard = cardAt(size() - 1);
        return topCard.isFaceUp() && fitsBelow(topCard, card);
    }

    /** Returns a new column with copies of these cards, keeping which ones are face up. */
    @Override
    public TableauPile copy() {
        return copyCardsInto(new TableauPile());
    }

    /** Returns the column's name for move sentences, "T1" to "T7". */
    @Override
    public String name(int position) {
        return "T" + (position + 1);
    }

    /** After cards are removed, turns the new top card face up, as the Klondike rules require. */
    @Override
    protected void onTopRemoved() {
        revealTop();
    }

    /**
     * Checks if the top count cards can move together: all of them must be face up, and each card must be one rank
     * lower and the opposite color of the card under it.
     */
    public boolean isValidSequence(int count) {
        if (count < 1 || count > size()) {
            return false;
        }
        for (int cardIndex = size() - count; cardIndex < size(); cardIndex++) {
            Card card = cardAt(cardIndex);
            if (!card.isFaceUp()) {
                return false;
            }
            if (cardIndex + 1 < size() && !fitsBelow(card, cardAt(cardIndex + 1))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a copy of the lowest card of the top count cards. This is the card the destination column must accept.
     */
    public Card sequenceStart(int count) {
        if (count < 1 || count > size()) {
            throw new IllegalArgumentException("No sequence of " + count + " cards");
        }
        return cardAt(size() - count).copy();
    }

    /**
     * Removes the top count cards after checking that they form a valid run, and returns them bottom card first. The
     * new top card is then turned face up.
     */
    public List<Card> removeSequence(int count) {
        if (!isValidSequence(count)) {
            throw new IllegalArgumentException("Invalid sequence of " + count + " cards");
        }
        return removeTopCards(count);
    }

    /**
     * Puts a run of cards on top of the column after checking that the column accepts the first card and that the run
     * itself is in order.
     */
    public void addSequence(List<Card> sequence) {
        if (sequence.isEmpty() || !accepts(sequence.getFirst()) || !isOrderedSequence(sequence)) {
            throw new IllegalArgumentException("Invalid tableau placement");
        }
        sequence.forEach(this::addToTop);
    }

    /** Returns true if the lower card can sit directly on the upper card: one rank lower and the opposite color. */
    private static boolean fitsBelow(Card upper, Card lower) {
        return upper.rank().value() == lower.rank().value() + 1 && upper.isRed() != lower.isRed();
    }

    /**
     * Checks a list of cards that is not in the column yet: all must be face up, and each must fit below the card
     * before it.
     */
    private static boolean isOrderedSequence(List<Card> sequence) {
        for (int cardIndex = 0; cardIndex < sequence.size(); cardIndex++) {
            Card card = sequence.get(cardIndex);
            if (!card.isFaceUp()) {
                return false;
            }
            if (cardIndex + 1 < sequence.size() && !fitsBelow(card, sequence.get(cardIndex + 1))) {
                return false;
            }
        }
        return true;
    }

    /** Turns the top card face up, if the column is not empty. */
    private void revealTop() {
        if (!isEmpty()) {
            cardAt(size() - 1).reveal();
        }
    }
}
