package solitaire.game;

import java.util.List;
import java.util.Objects;

import solitaire.enums.GameStatus;
import solitaire.model.Card;
import solitaire.move.Move;
import solitaire.move.MoveValidator;
import solitaire.pile.TableauPile;

/**
 * The game engine. It keeps the board, plays a move only if MoveValidator says it is legal, counts the moves, and
 * checks for a win.
 *
 * @author Farhiya Ayyub
 */
public final class SolitaireGame {
    private static final MoveValidator VALIDATOR = new MoveValidator();

    private final GameState state;
    private int moves;
    private GameStatus status = GameStatus.RUNNING;

    /**
     * Starts a game from the given board. It checks that the board has 52 unique cards, keeps a private copy so outside
     * changes cannot affect the game, and sets the status to WON if the board is already complete.
     */
    public SolitaireGame(GameState state) {
        // Stop with an error if the board does not have 52 unique cards.
        state.verify();
        // Keep a private copy, so changing the original board later does not change this game.
        this.state = state.copy();
        // A board that is already complete counts as won.
        if (this.state.won()) {
            status = GameStatus.WON;
        }
    }

    /** Returns a copy of the board, so other classes can read it but cannot change the game. */
    public GameState state() {
        return state.copy();
    }

    /** Returns how many moves have been played. Rejected moves are not counted. */
    public int moves() {
        return moves;
    }

    /** Returns the current status: RUNNING, WON, STUCK, or LIMIT_REACHED. */
    public GameStatus status() {
        return status;
    }

    /**
     * Saves how the game ended. A complete board is always saved as WON, and declaring WON on an unfinished board
     * throws an error.
     */
    public void finish(GameStatus finalStatus) {
        // The status must be given.
        Objects.requireNonNull(finalStatus, "finalStatus");
        // A game cannot be called WON if the board is not complete.
        if (finalStatus == GameStatus.WON && !state.won()) {
            throw new IllegalArgumentException("Cannot declare an unfinished board won");
        }
        // A complete board is always WON; otherwise save the status that was given.
        status = state.won() ? GameStatus.WON : finalStatus;
    }

    /**
     * Plays one move. It first asks MoveValidator if the move is legal; if not, it returns false and nothing changes.
     * If it is legal, it moves the cards, checks that 52 cards remain, adds 1 to the move count, and sets the status to
     * WON when the board is complete.
     */
    public boolean play(Move move) {
        // Step 1: if the move breaks a rule, return false and change nothing.
        if (!VALIDATOR.isLegal(state, move)) {
            return false;
        }
        // Step 2: move the cards.
        applyLegalMove(state, move);
        // Step 3: make sure no card was lost or duplicated.
        state.verify();
        // Step 4: count the move, and mark the game WON if the board is now complete.
        moves++;
        if (state.won()) {
            status = GameStatus.WON;
        }
        return true;
    }

    /**
     * Checks a move and plays it on a board that is not part of a game, so no move count or status is kept. Throws an
     * error if the move is illegal. Used to build the guaranteed winnable deal.
     */
    public static void apply(GameState board, Move move) {
        // If the move breaks a rule, stop with an error and change nothing.
        if (!VALIDATOR.isLegal(board, move)) {
            throw new IllegalArgumentException("Illegal move: " + move);
        }
        // The move is legal, so move the cards.
        applyLegalMove(board, move);
    }

    /**
     * Moves the cards for a move that is already known to be legal. A draw turns cards from the stock to the waste, a
     * recycle turns the waste back into the stock, a column move lifts a run of cards onto another column, and every
     * other move takes the top card of the source pile and places it on the destination pile.
     */
    private static void applyLegalMove(GameState board, Move move) {
        switch (move.type()) {
            // A draw: move 1 or 3 cards, one at a time, from the stock to the waste.
            case DRAW -> {
                for (int drawIndex = 0; drawIndex < move.count(); drawIndex++) {
                    board.stock().drawTo(board.waste());
                }
            }
            // A recycle: put all the waste cards back on the stock.
            case RECYCLE -> board.stock().recycle(board.waste());
            // A column move: lift the run of cards off the source column and put it on the destination column.
            case TABLEAU_TO_TABLEAU -> {
                TableauPile sourceColumn = board.tableau().get(move.source());
                List<Card> movedCards = sourceColumn.removeSequence(move.count());
                board.tableau().get(move.destination()).addSequence(movedCards);
            }
            // Every other move carries one card: take the top card of the source and place it on the destination.
            // Java runs the accepts() and onTopRemoved() of each actual pile (for example, a tableau column turns
            // its new top card face up).
            case TABLEAU_TO_FOUNDATION, WASTE_TO_TABLEAU, WASTE_TO_FOUNDATION, FOUNDATION_TO_TABLEAU ->
                    board.destinationPile(move).place(board.sourcePile(move).takeTop());
        }
    }
}
