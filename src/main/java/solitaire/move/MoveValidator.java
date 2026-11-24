package solitaire.move;

import solitaire.enums.Suit;
import solitaire.game.GameState;
import solitaire.pile.CardPile;
import solitaire.pile.TableauPile;

/**
 * Decides whether a move follows the Klondike rules on a given board. It only reads the board and never changes
 * it, so MoveGenerator, SolitaireGame, and the solver can all use the same rules safely.
 *
 * <p>All move rules are in this one class. Polymorphism: it calls accepts() on a CardPile, and Java runs the rule of
 * the actual pile.
 *
 * @author Farhiya Ayyub
 */
public final class MoveValidator {

    /**
     * Returns {@code true} only if the move is legal on the board. Returns {@code false} for a {@code null} move, a
     * {@code null} type, or a count below 1. A recycle is legal only with count 1, an empty stock, and a waste that is
     * not empty.
     *
     * <p>Key idea: one {@code switch} on the move type sends each move to the check for its kind. All move rules live
     * in this class, and it only reads the board, so the generator, the engine, and the solver all use exactly the same
     * rules.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@code isLegalDraw()} - checks a draw</li>
     *   <li>{@link GameState#stock()} - gets the stock for a recycle</li>
     *   <li>{@link CardPile#isEmpty} - recycle needs an empty stock and cards in the waste</li>
     *   <li>{@link GameState#waste()} - gets the waste for a recycle</li>
     *   <li>{@code isLegalTableauTransfer()} - checks a column-to-column move</li>
     *   <li>{@code isLegalSingleCardMove()} - checks a one-card move</li>
     * </ul>
     *
     * <p>Called by: {@code SolitaireGame.play()}, {@code SolitaireGame.apply()}, {@code MoveGenerator.addLegalMoves()}.
     *
     * @param board the board to read
     * @param move the move to check
     * @return {@code true} only if the move is legal on the board
     */
    public boolean isLegal(GameState board, Move move) {
        if (move == null || move.type() == null || move.count() < 1) {
            return false;
        }
        return switch (move.type()) {
            case DRAW -> isLegalDraw(board, move);
            case RECYCLE -> move.count() == 1 && board.stock().isEmpty() && !board.waste().isEmpty();
            case TABLEAU_TO_TABLEAU -> isLegalTableauTransfer(board, move);
            case TABLEAU_TO_FOUNDATION, WASTE_TO_TABLEAU, WASTE_TO_FOUNDATION, FOUNDATION_TO_TABLEAU ->
                    isLegalSingleCardMove(board, move);
        };
    }

    /**
     * Returns {@code true} if the stock is not empty and count equals the draw mode (1 or 3), or the number of stock
     * cards left when fewer remain.
     *
     * <p>Called by: {@link #isLegal}.
     *
     * @param board the board to read
     * @param move the draw move to check
     * @return {@code true} if the draw is legal
     */
    private boolean isLegalDraw(GameState board, Move move) {
        return !board.stock().isEmpty() && move.count() == Math.min(board.drawCount(), board.stock().size());
    }

    /**
     * Returns {@code true} if both column indexes are valid and different, the top count cards of the source form a
     * valid sequence, and the destination column accepts the lowest card of that sequence.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@code isValidColumnIndex()} - rejects a column outside 0 to 6</li>
     *   <li>{@link GameState#tableau()} - gets both columns</li>
     *   <li>{@link TableauPile#isValidSequence} - checks the cards can move together</li>
     *   <li>{@link TableauPile#sequenceStart} - gets the card that must fit</li>
     *   <li>{@link TableauPile#accepts} - applies the column rule</li>
     * </ul>
     *
     * <p>Called by: {@link #isLegal}.
     *
     * @param board the board to read
     * @param move the column-to-column move to check
     * @return {@code true} if the column move is legal
     */
    private boolean isLegalTableauTransfer(GameState board, Move move) {
        if (!isValidColumnIndex(move.source()) || !isValidColumnIndex(move.destination())
                || move.source() == move.destination()) {
            return false;
        }
        TableauPile sourceColumn = board.tableau().get(move.source());
        TableauPile destinationColumn = board.tableau().get(move.destination());
        return sourceColumn.isValidSequence(move.count())
                && destinationColumn.accepts(sourceColumn.sequenceStart(move.count()));
    }

    /**
     * Returns {@code true} if count is 1, the indexes are valid, the source pile is not empty, and the destination pile
     * accepts the source's top card. The destination's own accepts() rule makes the decision.
     *
     * <p>Key idea: {@code count != 1} is rejected, so foundation and waste moves are always one card at a time. The
     * decision uses polymorphism: {@code destination} is typed as CardPile, but Java runs the accepts() of the real
     * pile (tableau, foundation, or waste), so one line checks four move types.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@code hasValidIndexes()} - rejects indexes out of range</li>
     *   <li>{@link GameState#sourcePile} - finds the pile the card leaves</li>
     *   <li>{@link GameState#destinationPile} - finds the pile the card goes to</li>
     *   <li>{@link CardPile#isEmpty} - there must be a card to move</li>
     *   <li>{@link CardPile#top} - gets the card to move</li>
     *   <li>{@link CardPile#accepts} - applies the destination pile's own rule</li>
     * </ul>
     *
     * <p>Called by: {@link #isLegal}.
     *
     * @param board the board to read
     * @param move the one-card move to check
     * @return {@code true} if the one-card move is legal
     */
    private boolean isLegalSingleCardMove(GameState board, Move move) {
        if (move.count() != 1 || !hasValidIndexes(move)) {
            return false;
        }
        // The variables are CardPile, but the real objects are TableauPile, FoundationPile, or WastePile.
        // Java runs the accepts() of the real object.
        CardPile source = board.sourcePile(move);
        CardPile destination = board.destinationPile(move);
        return !source.isEmpty() && destination.accepts(source.top());
    }

    /**
     * Returns {@code true} if the indexes this move type uses are in range. Waste moves have no source index, so only
     * their destination is checked.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@code isValidColumnIndex()} - checks a column index</li>
     *   <li>{@code isValidFoundationIndex()} - checks a foundation index</li>
     * </ul>
     *
     * <p>Called by: {@code isLegalSingleCardMove()}.
     *
     * @param move the move whose indexes are checked
     * @return {@code true} if the indexes this move type uses are in range
     */
    private boolean hasValidIndexes(Move move) {
        boolean validSource = switch (move.type()) {
            case TABLEAU_TO_FOUNDATION -> isValidColumnIndex(move.source());
            case FOUNDATION_TO_TABLEAU -> isValidFoundationIndex(move.source());
            default -> true;
        };
        boolean validDestination = switch (move.type()) {
            case WASTE_TO_TABLEAU, FOUNDATION_TO_TABLEAU -> isValidColumnIndex(move.destination());
            case TABLEAU_TO_FOUNDATION, WASTE_TO_FOUNDATION -> isValidFoundationIndex(move.destination());
            default -> true;
        };
        return validSource && validDestination;
    }

    /**
     * Returns {@code true} if the index is 0 to 6, one of the seven tableau columns.
     *
     * <p>Called by: {@code isLegalTableauTransfer()}, {@code hasValidIndexes()}.
     *
     * @param index the index to check
     * @return {@code true} if the index is 0 to 6, one of the seven tableau columns
     */
    private boolean isValidColumnIndex(int index) {
        return index >= 0 && index < GameState.TABLEAU_COLUMNS;
    }

    /**
     * Returns {@code true} if the index is 0 to 3, one of the four foundations.
     *
     * <p>Called by: {@code hasValidIndexes()}.
     *
     * @param index the index to check
     * @return {@code true} if the index is 0 to 3, one of the four foundations
     */
    private boolean isValidFoundationIndex(int index) {
        return index >= 0 && index < Suit.FOUNDATION_COUNT;
    }
}
