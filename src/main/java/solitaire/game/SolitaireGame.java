package solitaire.game;

import java.util.List;
import java.util.Objects;

import solitaire.enums.GameStatus;
import solitaire.model.Card;
import solitaire.move.Move;
import solitaire.move.MoveValidator;
import solitaire.pile.TableauPile;

/**
 * The game engine. It owns one board, applies only legal moves to it, and counts moves and tracks the status.
 * Other classes only ever receive copies of the board, so play() is the only way the real board changes.
 *
 * <p>Encapsulation: the board is private and changes only through play(), which checks every move first. Polymorphism:
 * applyLegalMove() handles four move types with one line, because every pile is a CardPile.
 *
 * @author Farhiya Ayyub
 */
public final class SolitaireGame {
    // One validator is shared by all games because it keeps no state of its own.
    private static final MoveValidator VALIDATOR = new MoveValidator();

    private final GameState state;
    private int moves;
    private GameStatus status = GameStatus.RUNNING;

    /**
     * Checks the supplied board, then keeps its own copy, so later changes to the supplied board do not affect this
     * game. The status starts as RUNNING, or WON if the board is already complete.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link GameState#verify} - rejects a board without 52 unique cards</li>
     *   <li>{@link GameState#copy} - keeps a private copy of the board</li>
     *   <li>{@link GameState#won} - sets WON if the board is already complete</li>
     * </ul>
     *
     * @param state the starting board; the game keeps its own copy
     */
    public SolitaireGame(GameState state) {
        state.verify();
        this.state = state.copy();
        if (this.state.won()) {
            status = GameStatus.WON;
        }
    }

    /**
     * Returns a copy of the board. Changing the copy does not change this game.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link GameState#copy} - gives out a copy, not the real board</li>
     * </ul>
     *
     * <p>Called by: {@link GameController#manual}, {@code GameController.solveAndReplay()},
     * {@code GameController.replay()}, {@code ConsoleRenderer.render()}, {@code VisibleSolver.solve()}.
     *
     * @return a copy of the board
     */
    public GameState state() {
        return state.copy();
    }

    /**
     * Returns how many legal moves play() has applied; illegal moves are not counted.
     *
     * <p>Called by: {@code GameController.simulate()}, {@code ConsoleRenderer.render()}.
     *
     * @return how many legal moves play() has applied
     */
    public int moves() {
        return moves;
    }

    /**
     * Returns RUNNING, WON, STUCK, or LIMIT_REACHED.
     *
     * <p>Called by: {@code GameController.simulate()}, {@code ConsoleRenderer.render()}.
     *
     * @return RUNNING, WON, STUCK, or LIMIT_REACHED
     */
    public GameStatus status() {
        return status;
    }

    /**
     * Records how the game ended. If the board is complete the status is always WON.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link GameState#won} - checks that a WON claim is true</li>
     * </ul>
     *
     * <p>Called by: {@link GameController#guaranteed}, {@code GameController.solveAndReplay()}.
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
     * Plays one move.
     *
     * <p>Algorithm: validate, apply, check.
     * <ol>
     *   <li>If {@link MoveValidator#isLegal} rejects the move, return {@code false}.</li>
     *   <li>Move the cards, then check that 52 unique cards remain.</li>
     *   <li>Add 1 to the move count; set the status to WON if the board is complete.</li>
     * </ol>
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link MoveValidator#isLegal} - rejects an illegal move</li>
     *   <li>{@code applyLegalMove()} - moves the cards</li>
     *   <li>{@link GameState#verify} - confirms no card was lost or duplicated</li>
     *   <li>{@link GameState#won} - sets WON when the board is complete</li>
     * </ul>
     *
     * <p>Called by: {@link GameController#manual}, {@code GameController.replay()}, {@code VisibleSolver.solve()}.
     *
     * @param move the move to play
     * @return {@code true} if it was played, {@code false} (changing nothing) if it is illegal
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
     * Applies a move to a board that is not owned by a game; used by SolvableDealProvider to check its solution.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link MoveValidator#isLegal} - rejects an illegal move</li>
     *   <li>{@code applyLegalMove()} - moves the cards</li>
     * </ul>
     *
     * <p>Called by: {@code SolvableDealProvider.create()}.
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
     * Moves the cards for a move that has already passed MoveValidator. It does not check the move again.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link GameState#stock()} - gets the stock for draw and recycle</li>
     *   <li>{@link GameState#waste()} - gets the waste for draw and recycle</li>
     *   <li>{@code StockPile.drawTo()} - draws one card to the waste</li>
     *   <li>{@code StockPile.recycle()} - turns the waste back into the stock</li>
     *   <li>{@link GameState#tableau()} - gets the two columns of a column move</li>
     *   <li>{@link TableauPile#removeSequence} - lifts cards off the source column</li>
     *   <li>{@link TableauPile#addSequence} - puts them on the destination column</li>
     *   <li>{@link GameState#destinationPile} - finds the pile the card goes to</li>
     *   <li>{@link GameState#sourcePile} - finds the pile the card leaves</li>
     *   <li>{@code CardPile.takeTop()} - removes the top card</li>
     *   <li>{@code CardPile.place()} - adds it to the destination</li>
     * </ul>
     *
     * <p>Called by: {@link #play}, {@link #apply}.
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
            // One line handles four move types: the piles are typed as CardPile, and Java runs the accepts() and
            // onTopRemoved() of each actual pile (for example, a tableau column turns its new top card face up).
            case TABLEAU_TO_FOUNDATION, WASTE_TO_TABLEAU, WASTE_TO_FOUNDATION, FOUNDATION_TO_TABLEAU ->
                    board.destinationPile(move).place(board.sourcePile(move).takeTop());
        }
    }
}
