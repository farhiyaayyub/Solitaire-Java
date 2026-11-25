package solitaire.pile;

import java.util.List;

import solitaire.enums.Rank;
import solitaire.model.Card;

/**
 * One of the seven tableau columns. It accepts a card one rank lower and the opposite color, and can move a run of
 * cards at once.
 *
 * @author Farhiya Ayyub
 */
public final class TableauPile extends CardPile {

    /** Creates an empty column. */
    public TableauPile() {
    }

    /** Creates a column from the dealt cards: every card is placed face down, then the top card is turned face up. */
    public TableauPile(List<Card> dealtCards) {
        // Put every dealt card in the column face down.
        for (Card card : dealtCards) {
            card.conceal();
            addToTop(card);
        }
        // Only the top card of a new column is face up.
        revealTop();
    }

    /**
     * Accepts a face-up King on an empty column. On a column with cards, it accepts a face-up card that is one rank
     * lower and the opposite color of the top card, such as a red 9 on a black 10.
     */
    @Override
    public boolean accepts(Card card) {
        // A missing card or a face-down card can never be placed, so return false.
        if (card == null || !card.isFaceUp()) {
            return false;
        }
        // An empty column accepts only a King.
        if (isEmpty()) {
            return card.rank() == Rank.KING;
        }
        // Otherwise the top card must be face up, and the new card must be one rank lower and the opposite color.
        Card topCard = cardAt(size() - 1);
        return topCard.isFaceUp() && fitsBelow(topCard, card);
    }

    /** Returns a new column with copies of these cards, keeping which ones are face up. */
    @Override
    public TableauPile copy() {
        // Make an empty column and fill it with copies of this column's cards.
        return copyCardsInto(new TableauPile());
    }

    /** Returns the column's name for move sentences, "T1" to "T7". */
    @Override
    public String name(int position) {
        // Positions start at 0, so add 1: position 0 is "T1".
        return "T" + (position + 1);
    }

    /** After cards are removed, turns the new top card face up, as the Klondike rules require. */
    @Override
    protected void onTopRemoved() {
        // CardPile calls this after cards leave the column, so the card below is uncovered.
        revealTop();
    }

    /**
     * Checks if the top count cards can move together: all of them must be face up, and each card must be one rank
     * lower and the opposite color of the card under it.
     */
    public boolean isValidSequence(int count) {
        // The count must be at least 1 and not more than the cards in the column; otherwise return false.
        if (count < 1 || count > size()) {
            return false;
        }
        // Check each card of the run, from the lowest moving card up to the top card.
        for (int cardIndex = size() - count; cardIndex < size(); cardIndex++) {
            Card card = cardAt(cardIndex);
            // A face-down card cannot be moved, so return false.
            if (!card.isFaceUp()) {
                return false;
            }
            // The next card up must fit on this one (one rank lower, opposite color); if not, return false.
            if (cardIndex + 1 < size() && !fitsBelow(card, cardAt(cardIndex + 1))) {
                return false;
            }
        }
        // Every card passed, so the run can move together.
        return true;
    }

    /**
     * Returns a copy of the lowest card of the top count cards. This is the card the destination column must accept.
     */
    public Card sequenceStart(int count) {
        // A count of 0 or more than the column has is not a run, so stop with an error.
        if (count < 1 || count > size()) {
            throw new IllegalArgumentException("No sequence of " + count + " cards");
        }
        // The lowest moving card is count places below the top.
        return cardAt(size() - count).copy();
    }

    /**
     * Removes the top count cards after checking that they form a valid run, and returns them bottom card first. The
     * new top card is then turned face up.
     */
    public List<Card> removeSequence(int count) {
        // If the cards are not a valid run, stop with an error and change nothing.
        if (!isValidSequence(count)) {
            throw new IllegalArgumentException("Invalid sequence of " + count + " cards");
        }
        // Remove the run; CardPile then calls onTopRemoved(), which turns the new top card face up.
        return removeTopCards(count);
    }

    /**
     * Puts a run of cards on top of the column after checking that the column accepts the first card and that the run
     * itself is in order.
     */
    public void addSequence(List<Card> sequence) {
        // Stop with an error if the run is empty, its first card does not fit here, or the run is out of order.
        if (sequence.isEmpty() || !accepts(sequence.getFirst()) || !isOrderedSequence(sequence)) {
            throw new IllegalArgumentException("Invalid tableau placement");
        }
        // Add the cards one by one, bottom card first.
        sequence.forEach(this::addToTop);
    }

    /** Returns true if the lower card can sit directly on the upper card: one rank lower and the opposite color. */
    private static boolean fitsBelow(Card upper, Card lower) {
        // Example: a red 9 fits below a black 10 (10 = 9 + 1, and red is not black).
        return upper.rank().value() == lower.rank().value() + 1 && upper.isRed() != lower.isRed();
    }

    /**
     * Checks a list of cards that is not in the column yet: all must be face up, and each must fit below the card
     * before it.
     */
    private static boolean isOrderedSequence(List<Card> sequence) {
        // Check each card in the list, from the first (bottom) card to the last.
        for (int cardIndex = 0; cardIndex < sequence.size(); cardIndex++) {
            Card card = sequence.get(cardIndex);
            // A face-down card cannot be in a moving run, so return false.
            if (!card.isFaceUp()) {
                return false;
            }
            // The next card must fit on this one; if not, return false.
            if (cardIndex + 1 < sequence.size() && !fitsBelow(card, sequence.get(cardIndex + 1))) {
                return false;
            }
        }
        // Every card passed, so the run is in order.
        return true;
    }

    /** Turns the top card face up, if the column is not empty. */
    private void revealTop() {
        // An empty column has no top card, so only turn it over when there is one.
        if (!isEmpty()) {
            cardAt(size() - 1).reveal();
        }
    }
}
