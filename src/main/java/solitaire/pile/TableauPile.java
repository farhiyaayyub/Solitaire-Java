package solitaire.pile;

import java.util.List;

import solitaire.enums.Rank;
import solitaire.model.Card;

/**
 * One of the seven tableau columns. Cards are built down in alternating colors, and the newly exposed card is
 * turned face up whenever cards leave the column.
 *
 * <p>Type: final class, subclass of CardPile.
 * OOP: INHERITANCE (extends CardPile), POLYMORPHISM (overrides accepts, copy, and onTopRemoved).
 *
 * @author Farhiya Ayyub
 */
public final class TableauPile extends CardPile {

    /** Constructor: Creates an empty column. */
    public TableauPile() {
    }

    /**
     * Constructor: Creates a dealt column: every card face down except the top card.
     * Calls: Card.conceal(), addToTop() (from CardPile), revealTop()
     */
    public TableauPile(List<Card> dealtCards) {
        for (Card card : dealtCards) {
            card.conceal();
            addToTop(card);
        }
        revealTop();
    }

    /**
     * Method (override): Accepts a King on an empty column, or a card one rank lower and opposite in color.
     * Calls: Card.isFaceUp(), isEmpty(), cardAt(), fitsBelow()
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

    /**
     * Method (override): Returns a detached copy, including card visibility.
     * Calls: copyCardsInto()
     */
    @Override
    public TableauPile copy() {
        // Object: TableauPile - a new empty column that receives the copied cards.
        return copyCardsInto(new TableauPile());
    }

    /**
     * Method (override): Reveals the newly exposed card after cards are removed from this column.
     * Calls: revealTop()
     */
    @Override
    protected void onTopRemoved() {
        revealTop();
    }

    /**
     * Method: Checks that the top {@code count} cards are face up, descending, and alternating in color.
     * Calls: size(), cardAt(), Card.isFaceUp(), fitsBelow()
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
     * Method: Returns a detached copy of the first (deepest) card of the top {@code count} cards.
     * Calls: cardAt(), Card.copy()
     */
    public Card sequenceStart(int count) {
        if (count < 1 || count > size()) {
            throw new IllegalArgumentException("No sequence of " + count + " cards");
        }
        return cardAt(size() - count).copy();
    }

    /**
     * Method: Removes a valid top sequence; the newly exposed card is revealed automatically.
     * Calls: isValidSequence(), removeTopCards()
     */
    public List<Card> removeSequence(int count) {
        if (!isValidSequence(count)) {
            throw new IllegalArgumentException("Invalid sequence of " + count + " cards");
        }
        return removeTopCards(count);
    }

    /**
     * Method: Appends a valid descending sequence whose first card fits this column.
     * Calls: accepts(), isOrderedSequence(), addToTop()
     */
    public void addSequence(List<Card> sequence) {
        if (sequence.isEmpty() || !accepts(sequence.get(0)) || !isOrderedSequence(sequence)) {
            throw new IllegalArgumentException("Invalid tableau placement");
        }
        sequence.forEach(this::addToTop);
    }

    /**
     * Method: Reports whether {@code lower} can sit directly on {@code upper}: one rank lower and opposite color.
     * Calls: Card.rank(), Rank.value(), Card.isRed()
     */
    private static boolean fitsBelow(Card upper, Card lower) {
        return upper.rank().value() == lower.rank().value() + 1 && upper.isRed() != lower.isRed();
    }

    /**
     * Method: Reports whether a moving sequence is face up and built down in alternating colors.
     * Calls: Card.isFaceUp(), fitsBelow()
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

    /**
     * Method: Turns the top card face up if the column is not empty.
     * Calls: isEmpty(), cardAt(), Card.reveal()
     */
    private void revealTop() {
        if (!isEmpty()) {
            cardAt(size() - 1).reveal();
        }
    }
}
