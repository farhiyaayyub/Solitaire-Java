package solitaire.move;

import solitaire.enums.Suit;
import solitaire.game.GameState;
import solitaire.pile.CardPile;
import solitaire.pile.TableauPile;

/**
 * Checks if a move follows the Klondike rules, without changing the board.
 *
 * @author Farhiya Ayyub
 */
public final class MoveValidator {

    /**
     * Returns true only if the move is allowed on the board. A missing move or a count below 1 is rejected; otherwise
     * it checks the rule for the move's type: draw, recycle, column-to-column, or a one-card move.
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
     * A draw is legal when the stock is not empty and the number of cards equals the draw mode (1 or 3), or fewer if
     * the stock has fewer cards left.
     */
    private boolean isLegalDraw(GameState board, Move move) {
        return !board.stock().isEmpty() && move.count() == Math.min(board.drawCount(), board.stock().size());
    }

    /**
     * A column-to-column move is legal when both columns exist and are different, the moving cards form a valid run,
     * and the destination column accepts the lowest card of the run.
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
     * A one-card move is legal when exactly 1 card moves, the indexes are valid, the source pile is not empty, and the
     * destination pile accepts the top card of the source.
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
     * Checks that the indexes the move uses are in range: 0 to 6 for columns and 0 to 3 for foundations. Waste moves
     * have no source index, so only their destination is checked.
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

    /** Returns true if the index is one of the seven columns, 0 to 6. */
    private boolean isValidColumnIndex(int index) {
        return index >= 0 && index < GameState.TABLEAU_COLUMNS;
    }

    /** Returns true if the index is one of the four foundations, 0 to 3. */
    private boolean isValidFoundationIndex(int index) {
        return index >= 0 && index < Suit.FOUNDATION_COUNT;
    }
}
