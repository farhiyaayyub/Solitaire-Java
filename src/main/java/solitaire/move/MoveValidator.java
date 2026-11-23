package solitaire.move;

import solitaire.enums.Suit;
import solitaire.game.GameState;
import solitaire.pile.CardPile;
import solitaire.pile.TableauPile;

/**
 * Checks whether a move follows Klondike rules without changing the board.
 *
 * <p>Type: final class; all move rules are in one place.
 * OOP: ABSTRACTION (callers only ask isLegal), POLYMORPHISM (uses CardPile.accepts for any kind of pile).
 *
 * @author Farhiya Ayyub
 */
public final class MoveValidator {

    /**
     * Method: Returns true only if the move is legal on the supplied board.
     * Calls: isLegalDraw(), isLegalTableauTransfer(), isLegalSingleCardMove(), StockPile.isEmpty(),
     * WastePile.isEmpty()
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
     * Method: A draw turns the configured number of cards, or all that remain when fewer are left.
     * Calls: GameState.stock(), StockPile.isEmpty(), StockPile.size(), GameState.drawCount()
     */
    private boolean isLegalDraw(GameState board, Move move) {
        return !board.stock().isEmpty() && move.count() == Math.min(board.drawCount(), board.stock().size());
    }

    /**
     * Method: Moves a face-up, ordered sequence onto a different column that accepts its first card.
     * Calls: isValidColumnIndex(), GameState.tableau(), TableauPile.isValidSequence(), TableauPile.sequenceStart(),
     * TableauPile.accepts()
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
     * Method: Moves one top card; the destination pile's own rule decides.
     * Calls: hasValidIndexes(), GameState.sourcePile(), GameState.destinationPile(), CardPile.top(),
     * CardPile.accepts()
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
     * Method: Checks the indexes that this single-card move type actually uses.
     * Calls: isValidColumnIndex(), isValidFoundationIndex()
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

    /** Method: Checks whether an index points to one of the seven tableau columns. */
    private boolean isValidColumnIndex(int index) {
        return index >= 0 && index < GameState.TABLEAU_COLUMNS;
    }

    /** Method: Checks whether an index points to one of the four foundations. */
    private boolean isValidFoundationIndex(int index) {
        return index >= 0 && index < Suit.FOUNDATION_COUNT;
    }
}
