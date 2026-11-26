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
        state.verify();
        this.state = state.copy();
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
        Objects.requireNonNull(finalStatus, "finalStatus");
        if (finalStatus == GameStatus.WON && !state.won()) {
            throw new IllegalArgumentException("Cannot declare an unfinished board won");
        }
        status = state.won() ? GameStatus.WON : finalStatus;
    }

    /**
     * Plays one move. It first asks MoveValidator if the move is legal; if not, it throws an error and nothing changes.
     * If it is legal, it moves the cards, checks that 52 cards remain, adds 1 to the move count, and sets the status to
     * WON when the board is complete.
     */
    public void play(Move move) {
        if (!VALIDATOR.isLegal(state, move)) {
            throw new IllegalStateException("Illegal move: " + move);
        }
        applyLegalMove(state, move);
        state.verify();
        moves++;
        if (state.won()) {
            status = GameStatus.WON;
        }
    }

    /**
     * Checks a move and plays it on a board that is not part of a game, so no move count or status is kept. Throws an
     * error if the move is illegal. Used to build the guaranteed winnable deal.
     */
    public static void apply(GameState board, Move move) {
        if (!VALIDATOR.isLegal(board, move)) {
            throw new IllegalArgumentException("Illegal move: " + move);
        }
        applyLegalMove(board, move);
    }

    /**
     * Moves the cards for a move that is already known to be legal. A draw turns cards from the stock to the waste, a
     * recycle turns the waste back into the stock, a column move lifts a run of cards onto another column, and every
     * other move takes the top card of the source pile and places it on the destination pile.
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
            case TABLEAU_TO_FOUNDATION, WASTE_TO_TABLEAU, WASTE_TO_FOUNDATION, FOUNDATION_TO_TABLEAU ->
                    board.destinationPile(move).place(board.sourcePile(move).takeTop());
        }
    }
}
