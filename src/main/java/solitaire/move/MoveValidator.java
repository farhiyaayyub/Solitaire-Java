package solitaire.move;

import solitaire.enums.Suit;
import solitaire.game.GameState;
import solitaire.pile.CardPile;
import solitaire.pile.TableauPile;

/**
 * Rule checker for moves.
 *
 * <p>Kind: final class, used by MoveGenerator and SolitaireGame.
 * <p>Purpose: it decides whether a move follows the Klondike rules on a given board. It only reads the board, so the
 * generator, the engine, and (through the generator) the solver all share the same rules safely.
 * <p>Data: none; it only reads the board it is given.
 * <p>OOP: Encapsulation - all move rules live in this one class. Polymorphism - it calls accepts() on a CardPile, and
 * Java runs the rule of the actual pile.
 *
 * @author Farhiya Ayyub
 */
public final class MoveValidator {

    /**
     * {@return {@code true} only if the move is legal on the board} A {@code null} move, a {@code null} type, or a
     * count below 1 is illegal. A recycle is legal only with count 1, an empty stock, and a waste that is not empty.
     *
     * <p>Called by: SolitaireGame.play(), SolitaireGame.apply(), MoveGenerator.addLegalMoves().
     *
     * @param board the board to read
     * @param move the move to check
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
     * {@return {@code true} if the stock is not empty and count equals the draw mode, or the number of stock cards
     * left when fewer remain}
     *
     * <p>Called by: isLegal().
     *
     * @param board the board to read
     * @param move the draw move to check
     */
    private boolean isLegalDraw(GameState board, Move move) {
        return !board.stock().isEmpty() && move.count() == Math.min(board.drawCount(), board.stock().size());
    }

    /**
     * {@return {@code true} if the column move is legal} Both column indexes must be valid and different, the top
     * count cards of the source must form a valid sequence, and the destination must accept the lowest of them.
     *
     * <p>Called by: isLegal().
     *
     * @param board the board to read
     * @param move the column-to-column move to check
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
     * {@return {@code true} if the one-card move is legal} Count must be 1, the indexes valid, the source not empty,
     * and the destination must accept the source's top card.
     *
     * <p>Key idea: rejecting {@code count != 1} keeps foundation and waste moves to one card at a time. The
     * destination is typed as CardPile, but Java runs the accepts() of the real pile, so one line checks four move
     * types.
     *
     * <p>Called by: isLegal().
     *
     * @param board the board to read
     * @param move the one-card move to check
     */
    private boolean isLegalSingleCardMove(GameState board, Move move) {
        if (move.count() != 1 || !hasValidIndexes(move)) {
            return false;
        }
        CardPile source = board.sourcePile(move);
        CardPile destination = board.destinationPile(move);
        return !source.isEmpty() && destination.accepts(source.top());
    }

    /**
     * {@return {@code true} if the indexes this move type uses are in range} Waste moves have no source index, so only
     * their destination is checked.
     *
     * <p>Called by: isLegalSingleCardMove().
     *
     * @param move the move whose indexes are checked
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
     * {@return {@code true} if the index is one of the seven columns, 0 to 6}
     * Called by: isLegalTableauTransfer(), hasValidIndexes().
     *
     * @param index the index to check
     */
    private boolean isValidColumnIndex(int index) {
        return index >= 0 && index < GameState.TABLEAU_COLUMNS;
    }

    /**
     * {@return {@code true} if the index is one of the four foundations, 0 to 3} Called by: hasValidIndexes().
     *
     * @param index the index to check
     */
    private boolean isValidFoundationIndex(int index) {
        return index >= 0 && index < Suit.FOUNDATION_COUNT;
    }
}
