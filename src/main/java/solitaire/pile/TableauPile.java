package solitaire.pile;

import java.util.List;

import solitaire.enums.Rank;
import solitaire.model.Card;

/**
 * One of the seven tableau columns.
 *
 * <p>Kind: final class, extends CardPile.
 * <p>Purpose: it holds a column of face-down cards with face-up cards on top, built down from King to Ace in
 * alternating colors, and it is the only pile where several cards can move at once.
 * <p>Data: none of its own; the cards are kept in CardPile's list, whose index access lets it check and move a run
 * of cards from the middle.
 * <p>OOP: Inheritance - it reuses CardPile's card handling. Polymorphism - it overrides accepts(), copy(), name(),
 * and the onTopRemoved() hook, so the new top card is turned face up automatically.
 *
 * @author Farhiya Ayyub
 */
public final class TableauPile extends CardPile {

    /** Creates an empty column. Called by: copy(). */
    public TableauPile() {
    }

    /**
     * Creates a dealt column, first card at the bottom: every card is turned face down, then the top card face up.
     *
     * <p>Called by: GameState.deal().
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
     * Accepts a face-up King on an empty column, or a face-up card one rank lower and the opposite color of a face-up
     * top card. Returns {@code false} for {@code null} or a face-down card.
     *
     * <p>Called by: MoveValidator.isLegalTableauTransfer(), addSequence(), and MoveValidator.isLegalSingleCardMove()
     * and CardPile.place() (through CardPile).
     *
     * @param card the card to check
     * @return {@code true} if the card may be placed on this column
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
     * {@return a new column holding copies of these cards, with the same face-up flags}
     *
     * <p>Called by: GameState.copy().
     */
    @Override
    public TableauPile copy() {
        return copyCardsInto(new TableauPile());
    }

    /**
     * {@return the column's name, "T1" to "T7"}
     *
     * <p>Called by: ConsoleRenderer.describeMove() (through CardPile).
     *
     * @param position the column index, 0 for T1
     */
    @Override
    public String name(int position) {
        return "T" + (position + 1);
    }

    /**
     * Turns the new top card face up after cards are removed, as Klondike requires.
     *
     * <p>Called by: CardPile.takeTop() and CardPile.removeTopCards() (through CardPile).
     */
    @Override
    protected void onTopRemoved() {
        revealTop();
    }

    /**
     * {@return {@code true} if the top {@code count} cards can move together} Returns {@code false} if count is less
     * than 1 or larger than the column.
     *
     * <p>Algorithm: linear scan, O(count). From the lowest of the top count cards upward, every card must be face up,
     * and each next card must fit below it (one rank lower, opposite color).
     *
     * <p>Called by: MoveValidator.isLegalTableauTransfer(), removeSequence().
     *
     * @param count how many top cards would move
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
     * {@return a copy of the lowest card of the top {@code count} cards} This is the card the destination column must
     * accept.
     *
     * <p>Called by: MoveValidator.isLegalTableauTransfer().
     *
     * @param count how many top cards would move
     * @throws IllegalArgumentException if count is less than 1 or larger than the column
     */
    public Card sequenceStart(int count) {
        if (count < 1 || count > size()) {
            throw new IllegalArgumentException("No sequence of " + count + " cards");
        }
        return cardAt(size() - count).copy();
    }

    /**
     * Removes the top {@code count} cards and returns them bottom card first; the new top card is then turned face up
     * by onTopRemoved().
     *
     * <p>Called by: SolitaireGame.applyLegalMove().
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
     * Puts a run of cards on top of this column, after checking that its first card is accepted and the run itself is
     * in order.
     *
     * <p>Called by: SolitaireGame.applyLegalMove().
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
     * {@return {@code true} if {@code lower} can sit directly on {@code upper}: one rank lower and the opposite color}
     *
     * <p>Called by: accepts(), isValidSequence(), isOrderedSequence().
     *
     * @param upper the card already in the column
     * @param lower the card that would be placed on top
     */
    private static boolean fitsBelow(Card upper, Card lower) {
        return upper.rank().value() == lower.rank().value() + 1 && upper.isRed() != lower.isRed();
    }

    /**
     * {@return {@code true} if every card in the list is face up and each fits below the one before it} Same check as
     * isValidSequence(), but for cards that are not in a column yet.
     *
     * <p>Called by: addSequence().
     *
     * @param sequence the cards to check, bottom card first
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

    /** Turns the top card face up; does nothing on an empty column. Called by: TableauPile(List), onTopRemoved(). */
    private void revealTop() {
        if (!isEmpty()) {
            cardAt(size() - 1).reveal();
        }
    }
}
