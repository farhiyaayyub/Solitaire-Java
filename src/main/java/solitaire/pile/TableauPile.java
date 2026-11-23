package solitaire.pile;

import java.util.List;

import solitaire.enums.Rank;
import solitaire.model.Card;

/**
 * One of the seven tableau columns. Cards are built down: each card must be one rank lower and the opposite color
 * of the card under it. Whenever cards leave the column, the new top card is turned face up.
 *
 * <p>Inheritance: it extends CardPile. Polymorphism: it overrides accepts(), copy(), and onTopRemoved().
 *
 * @author Farhiya Ayyub
 */
public final class TableauPile extends CardPile {

    /** Creates an empty column; copy() fills it with copied cards. */
    public TableauPile() {
    }

    /**
     * Creates a dealt column in list order (first card at the bottom). Every card is turned face down, then the top
     * card is turned face up.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link Card#conceal} - deals every card face down</li>
     *   <li>{@link CardPile#addToTop} - stacks the dealt cards</li>
     *   <li>{@code revealTop()} - turns the top card face up</li>
     * </ul>
     *
     * @param dealtCards the cards for this column, bottom card first
     */
    public TableauPile(List<Card> dealtCards) {
        for (Card card : dealtCards) {
            card.conceal();
            addToTop(card);
        }
        revealTop();
    }

    /**
     * Returns {@code true} for a face-up King on an empty column, or a face-up card one rank lower and the opposite
     * color of a face-up top card. Returns {@code false} for {@code null} or a face-down card.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link Card#isFaceUp} - only face-up cards may move</li>
     *   <li>{@link CardPile#isEmpty} - an empty column takes only a King</li>
     *   <li>{@link Card#rank()} - an empty column takes only a King</li>
     *   <li>{@link CardPile#size} - finds the current top card</li>
     *   <li>{@link CardPile#cardAt} - gets the current top card</li>
     *   <li>{@code fitsBelow()} - checks one lower and opposite color</li>
     * </ul>
     *
     * <p>Called by: {@code MoveValidator.isLegalTableauTransfer()}, {@link #addSequence},
     * {@code MoveValidator.isLegalSingleCardMove()} (through CardPile), {@link CardPile#place} (through CardPile).
     *
     * @param card the card to check
     * @return {@code true} for a face-up King on an empty column, or a face-up card one rank lower and the opposite
     *     color of a face-up top card
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
     * Returns a new column holding copies of these cards, with the same face-up flags.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link CardPile#copyCardsInto} - fills the new pile with copies</li>
     * </ul>
     *
     * <p>Called by: {@code GameState.copy()}.
     *
     * @return a new column holding copies of these cards, with the same face-up flags
     */
    @Override
    public TableauPile copy() {
        return copyCardsInto(new TableauPile());
    }

    /**
     * Turns the new top card face up after cards are removed, as Klondike requires.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@code revealTop()} - turns the new top card face up</li>
     * </ul>
     *
     * <p>Called by: {@link CardPile#takeTop} (through CardPile), {@link CardPile#removeTopCards} (through CardPile).
     */
    @Override
    protected void onTopRemoved() {
        revealTop();
    }

    /**
     * Returns {@code true} if the top {@code count} cards can move together.
     *
     * <p>Algorithm: linear scan, O(count).
     * <ol>
     *   <li>count must be between 1 and the column size.</li>
     *   <li>For each of the top count cards, from the lowest up: it must be face up, and the next card must be one rank
     *     lower and the opposite color.</li>
     * </ol>
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link CardPile#size} - checks the count fits the column</li>
     *   <li>{@link CardPile#cardAt} - reads each card in the run</li>
     *   <li>{@link Card#isFaceUp} - every card must be face up</li>
     *   <li>{@code fitsBelow()} - checks each pair is in order</li>
     * </ul>
     *
     * <p>Called by: {@code MoveValidator.isLegalTableauTransfer()}, {@link #removeSequence}.
     *
     * @param count how many top cards would move
     * @return {@code true} if the top {@code count} cards can move together
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
     * Returns a copy of the lowest card of the top {@code count} cards; this is the card the destination column must
     * accept.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link CardPile#size} - checks the count and finds the lowest card</li>
     *   <li>{@link CardPile#cardAt} - gets the lowest card of the run</li>
     *   <li>{@link Card#copy} - returns a copy, not the real card</li>
     * </ul>
     *
     * <p>Called by: {@code MoveValidator.isLegalTableauTransfer()}.
     *
     * @param count how many top cards would move
     * @return a copy of the lowest card of the top {@code count} cards
     * @throws IllegalArgumentException if count is less than 1 or larger than the column
     */
    public Card sequenceStart(int count) {
        if (count < 1 || count > size()) {
            throw new IllegalArgumentException("No sequence of " + count + " cards");
        }
        return cardAt(size() - count).copy();
    }

    /**
     * Removes the top {@code count} cards and returns them bottom to top; the new top card is then turned face up by
     * onTopRemoved().
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link #isValidSequence} - rejects an invalid run</li>
     *   <li>{@link CardPile#removeTopCards} - removes the cards and reveals</li>
     * </ul>
     *
     * <p>Called by: {@code SolitaireGame.applyLegalMove()}.
     *
     * @param count how many top cards to remove
     * @return the removed cards, bottom card first
     * @throws IllegalArgumentException if they are not a valid sequence
     */
    public List<Card> removeSequence(int count) {
        if (!isValidSequence(count)) {
            throw new IllegalArgumentException("Invalid sequence of " + count + " cards");
        }
        return removeTopCards(count);
    }

    /**
     * Puts a sequence on top of this column.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link #accepts} - checks the first card fits</li>
     *   <li>{@code isOrderedSequence()} - checks the run itself is in order</li>
     *   <li>{@link CardPile#addToTop} - adds each card</li>
     * </ul>
     *
     * <p>Called by: {@code SolitaireGame.applyLegalMove()}.
     *
     * @param sequence the cards to add, bottom card first
     * @throws IllegalArgumentException if the sequence is empty, its first card is not accepted, or it is not face up
     *     and built down in alternating colors (nothing is changed)
     */
    public void addSequence(List<Card> sequence) {
        if (sequence.isEmpty() || !accepts(sequence.getFirst()) || !isOrderedSequence(sequence)) {
            throw new IllegalArgumentException("Invalid tableau placement");
        }
        sequence.forEach(this::addToTop);
    }

    /**
     * Returns {@code true} if {@code lower} can sit directly on {@code upper}: one rank lower and the opposite color.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link Card#rank()} - gets each rank</li>
     *   <li>{@link Rank#value()} - compares ranks as numbers</li>
     *   <li>{@link Card#isRed} - compares colors</li>
     * </ul>
     *
     * <p>Called by: {@link #accepts}, {@link #isValidSequence}, {@code isOrderedSequence()}.
     *
     * @param upper the card already in the column
     * @param lower the card that would be placed on top
     * @return {@code true} if {@code lower} can sit directly on {@code upper}: one rank lower and the opposite color
     */
    private static boolean fitsBelow(Card upper, Card lower) {
        return upper.rank().value() == lower.rank().value() + 1 && upper.isRed() != lower.isRed();
    }

    /**
     * Returns {@code true} if every card in the sequence is face up and each card fits below the one before it.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link Card#isFaceUp} - every card must be face up</li>
     *   <li>{@code fitsBelow()} - checks each pair is in order</li>
     * </ul>
     *
     * <p>Called by: {@link #addSequence}.
     *
     * @param sequence the cards to check, bottom card first
     * @return {@code true} if every card in the sequence is face up and each card fits below the one before it
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
     * Turns the top card face up; does nothing if the column is empty.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link CardPile#isEmpty} - nothing to reveal in an empty column</li>
     *   <li>{@link CardPile#size} - finds the top card</li>
     *   <li>{@link CardPile#cardAt} - gets the top card</li>
     *   <li>{@link Card#reveal} - turns it face up</li>
     * </ul>
     *
     * <p>Called by: {@link #TableauPile(List)}, {@link #onTopRemoved}.
     */
    private void revealTop() {
        if (!isEmpty()) {
            cardAt(size() - 1).reveal();
        }
    }
}
