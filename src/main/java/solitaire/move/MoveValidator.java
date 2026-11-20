package solitaire.move;

import solitaire.enums.MoveType;

import solitaire.game.GameState;
import solitaire.enums.Suit;

/**
 * Checks whether a move follows Solitaire rules.
 *
 * @author Farhiya Ayyub
 */
public final class MoveValidator {
    /** Checks whether an index points to one of the seven tableau columns. */
    private boolean isValidColumnIndex(int index) {
        return index >= 0 && index < GameState.TABLEAU_COLUMNS;
    }

    /** Checks whether an index points to one of the four foundations. */
    private boolean isValidSuitIndex(int index) {
        return index >= 0 && index < Suit.FOUNDATION_COUNT;
    }

    /** Checks indexes, draw counts, and placement rules without changing the board. */
    public boolean isLegal(GameState board, Move move) {
        if (move == null || move.type() == null || move.count() < 1) return false;
        int sourceIndex = move.source(), destinationIndex = move.destination();
        if (move.type() != MoveType.TABLEAU_TO_TABLEAU && move.type() != MoveType.DRAW && move.count() != 1) return false;
        return switch (move.type()) {
            case DRAW -> board.stock().size() > 0 && move.count() == Math.min(board.drawCount(), board.stock().size());
            case RECYCLE -> board.stock().size() == 0 && board.waste().size() > 0;
            case TABLEAU_TO_TABLEAU ->
                isValidColumnIndex(sourceIndex) && isValidColumnIndex(destinationIndex) && sourceIndex != destinationIndex && board.tableau().get(sourceIndex).validSequence(move.count()) && board.tableau().get(destinationIndex).accepts(board.tableau().get(sourceIndex).cards().get(board.tableau().get(sourceIndex).size() - move.count()));
            case TABLEAU_TO_FOUNDATION ->
                isValidColumnIndex(sourceIndex) && isValidSuitIndex(destinationIndex) && board.foundation(Suit.values()[destinationIndex]).accepts(board.tableau().get(sourceIndex).top());
            case WASTE_TO_TABLEAU ->
                isValidColumnIndex(destinationIndex) && board.waste().top() != null && board.tableau().get(destinationIndex).accepts(board.waste().top());
            case WASTE_TO_FOUNDATION -> isValidSuitIndex(destinationIndex) && board.foundation(Suit.values()[destinationIndex]).accepts(board.waste().top());
            case FOUNDATION_TO_TABLEAU ->
                isValidSuitIndex(sourceIndex) && isValidColumnIndex(destinationIndex) && board.foundation(Suit.values()[sourceIndex]).top() != null && board.tableau().get(destinationIndex).accepts(board.foundation(Suit.values()[sourceIndex]).top());
        };
    }
}
