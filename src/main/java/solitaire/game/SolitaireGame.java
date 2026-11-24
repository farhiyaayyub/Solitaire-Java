package solitaire.game;

import java.util.List;
import java.util.Objects;

import solitaire.enums.GameStatus;
import solitaire.model.Card;
import solitaire.move.Move;
import solitaire.move.MoveValidator;
import solitaire.pile.TableauPile;

/**
 * The game engine.
 *
 * <p>Kind: final class, owns one GameState and uses MoveValidator.
 * <p>Purpose: it applies only legal moves to its board, counts the moves, and tracks the game status.
 * <p>Data: its private GameState, a move counter, and a GameStatus enum value. One static MoveValidator is shared by
 * all games, because it keeps no state of its own.
 * <p>OOP: Encapsulation - the board is private, other classes only get copies, and it changes only through play(),
 * which checks every move first. Polymorphism - applyLegalMove() handles four move types with one line, because every
 * pile is a CardPile.
 *
 * @author Farhiya Ayyub
 */
public final class SolitaireGame {
    private static final MoveValidator VALIDATOR = new MoveValidator();

    private final GameState state;
    private int moves;
    private GameStatus status = GameStatus.RUNNING;

    /**
     * Checks the supplied board and keeps a private copy, so later changes to the original do not affect this game.
     * The status starts as RUNNING, or WON if the board is already complete.
     *
     * <p>Called by: GameController.guaranteed(), GameController.manual(), GameController.playShuffled(),
     * GameController.simulate(), VisibleSolver.solve().
     *
     * @param state the starting board
     * @throws IllegalStateException if the board does not hold 52 unique cards
     */
    public SolitaireGame(GameState state) {
        state.verify();
        this.state = state.copy();
        if (this.state.won()) {
            status = GameStatus.WON;
        }
    }

    /**
     * {@return a copy of the board} Changing the copy does not change this game.
     *
     * <p>Called by: GameController.manual(), GameController.solveAndReplay(), GameController.replay(),
     * ConsoleRenderer.render(), VisibleSolver.solve().
     */
    public GameState state() {
        return state.copy();
    }

    /**
     * {@return how many legal moves play() has applied} Rejected moves are not counted.
     *
     * <p>Called by: GameController.simulate(), ConsoleRenderer.render().
     */
    public int moves() {
        return moves;
    }

    /**
     * {@return the current status: RUNNING, WON, STUCK, or LIMIT_REACHED}
     *
     * <p>Called by: GameController.simulate(), ConsoleRenderer.render().
     */
    public GameStatus status() {
        return status;
    }

    /**
     * Records how the game ended. A complete board is always recorded as WON, whatever status is given.
     *
     * <p>Called by: GameController.guaranteed(), GameController.solveAndReplay().
     *
     * @param finalStatus how the game ended
     * @throws NullPointerException for a {@code null} status
     * @throws IllegalArgumentException if WON is given for an unfinished board
     */
    public void finish(GameStatus finalStatus) {
        Objects.requireNonNull(finalStatus, "finalStatus");
        if (finalStatus == GameStatus.WON && !state.won()) {
            throw new IllegalArgumentException("Cannot declare an unfinished board won");
        }
        status = state.won() ? GameStatus.WON : finalStatus;
    }

    /**
     * Plays one move if it is legal; an illegal move changes nothing.
     *
     * <p>Algorithm: validate, apply, check.
     * <ol>
     *   <li>If MoveValidator rejects the move, return {@code false}.</li>
     *   <li>Move the cards, then check that 52 unique cards remain.</li>
     *   <li>Add 1 to the move count; set the status to WON if the board is complete.</li>
     * </ol>
     *
     * <p>Key idea: place() also checks the destination's accepts() while the card moves, so an illegal card can never
     * land on a pile.
     *
     * <p>Called by: GameController.manual(), GameController.replay(), VisibleSolver.solve().
     *
     * @param move the move to play
     * @return {@code true} if it was played, {@code false} if it is illegal
     * @throws IllegalStateException if a card was lost or duplicated
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
     * Checks a move and applies it to a board that no game owns; no move count or status is kept.
     *
     * <p>Called by: SolvableDealProvider.create().
     *
     * @param board the board to change
     * @param move the move to play
     * @throws IllegalArgumentException if the move is illegal (nothing is changed)
     */
    public static void apply(GameState board, Move move) {
        if (!VALIDATOR.isLegal(board, move)) {
            throw new IllegalArgumentException("Illegal move: " + move);
        }
        applyLegalMove(board, move);
    }

    /**
     * Moves the cards for a move that has already passed MoveValidator, without checking it again.
     *
     * <p>Called by: play(), apply().
     *
     * @param board the board to change
     * @param move a move that has already been validated
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
            // Java runs the accepts() and onTopRemoved() of each actual pile (for example, a tableau column turns
            // its new top card face up).
            case TABLEAU_TO_FOUNDATION, WASTE_TO_TABLEAU, WASTE_TO_FOUNDATION, FOUNDATION_TO_TABLEAU ->
                    board.destinationPile(move).place(board.sourcePile(move).takeTop());
        }
    }
}
