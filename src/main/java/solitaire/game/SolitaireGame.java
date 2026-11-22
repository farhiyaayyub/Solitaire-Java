package solitaire.game;

import java.util.List;
import java.util.Objects;

import solitaire.enums.GameStatus;
import solitaire.model.Card;
import solitaire.move.Move;
import solitaire.move.MoveValidator;
import solitaire.pile.TableauPile;

/**
 * Owns one live board, accepts only legal moves, and tracks the move count and status. Callers only ever receive
 * copies of the board, so they cannot change it directly.
 *
 * <p>Type: final class; the game engine.
 * OOP: ENCAPSULATION (the board is private; it changes only through play(), which validates first),
 * POLYMORPHISM (applyLegalMove handles four move types with one line using CardPile).
 *
 * @author Farhiya Ayyub
 */
public final class SolitaireGame {
    // Constant and object: one shared MoveValidator.
    private static final MoveValidator VALIDATOR = new MoveValidator();

    // Fields: the owned board, the number of accepted moves, and the current status.
    private final GameState state;
    private int moves;
    private GameStatus status = GameStatus.RUNNING;

    /**
     * Constructor: Creates a game from a detached copy of the supplied board.
     * Calls: GameState.verify(), GameState.copy(), GameState.won()
     */
    public SolitaireGame(GameState state) {
        state.verify();
        this.state = state.copy();
        if (this.state.won()) {
            status = GameStatus.WON;
        }
    }

    /**
     * Method: Returns a detached board copy so callers cannot modify the owned board.
     * Calls: GameState.copy()
     */
    public GameState state() {
        return state.copy();
    }

    /** Method: Returns the number of accepted moves. */
    public int moves() {
        return moves;
    }

    /** Method: Returns the current game status. */
    public GameStatus status() {
        return status;
    }

    /**
     * Method: Records the final result; throws IllegalArgumentException if WON is claimed for an unfinished board.
     * Calls: Objects.requireNonNull(), GameState.won()
     */
    public void finish(GameStatus finalStatus) {
        Objects.requireNonNull(finalStatus, "finalStatus");
        if (finalStatus == GameStatus.WON && !state.won()) {
            throw new IllegalArgumentException("Cannot declare an unfinished board won");
        }
        status = state.won() ? GameStatus.WON : finalStatus;
    }

    /**
     * Method: Validates and applies one move; returns false, without changing anything, for an illegal move.
     * Calls: MoveValidator.isLegal(), applyLegalMove(), GameState.verify(), GameState.won()
     */
    public boolean play(Move move) {
        if (!VALIDATOR.isLegal(state, move)) {
            return false;
        }
        applyLegalMove(state, move);
        state.verify();
        moves++;
        if (state.won()) {
            status = GameStatus.WON;
        }
        return true;
    }

    /**
     * Method (static): Applies a move to a working board; throws IllegalArgumentException for an illegal move.
     * Calls: MoveValidator.isLegal(), applyLegalMove()
     */
    public static void apply(GameState board, Move move) {
        if (!VALIDATOR.isLegal(board, move)) {
            throw new IllegalArgumentException("Illegal move: " + move);
        }
        applyLegalMove(board, move);
    }

    /**
     * Method: Changes the board for a move that has already been validated.
     * Calls: StockPile.drawTo(), StockPile.recycle(), TableauPile.removeSequence(), TableauPile.addSequence(),
     * GameState.sourcePile(), GameState.destinationPile(), CardPile.takeTop(), CardPile.place()
     */
    private static void applyLegalMove(GameState board, Move move) {
        switch (move.type()) {
            case DRAW -> {
                for (int drawIndex = 0; drawIndex < move.count(); drawIndex++) {
                    board.stock().drawTo(board.waste());
                }
            }
            case RECYCLE -> board.stock().recycle(board.waste());
            case TABLEAU_TO_TABLEAU -> {
                TableauPile sourceColumn = board.tableau().get(move.source());
                List<Card> movedCards = sourceColumn.removeSequence(move.count());
                board.tableau().get(move.destination()).addSequence(movedCards);
            }
            // One line for four move types: takeTop() and place() work on any pile, and each pile's own
            // accepts() and onTopRemoved() run automatically.
            case TABLEAU_TO_FOUNDATION, WASTE_TO_TABLEAU, WASTE_TO_FOUNDATION, FOUNDATION_TO_TABLEAU ->
                    board.destinationPile(move).place(board.sourcePile(move).takeTop());
        }
    }
}
