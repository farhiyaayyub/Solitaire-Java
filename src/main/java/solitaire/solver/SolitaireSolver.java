package solitaire.solver;

import java.util.List;
import java.util.Objects;

import solitaire.enums.GameStatus;
import solitaire.game.GameState;
import solitaire.move.Move;

/**
 * Contract for an automatic player (Strategy pattern).
 *
 * <p>Kind: interface, implemented by VisibleSolver; contains the records Limits and Result.
 * <p>Purpose: it says what every solver must do: take a starting board and return the moves it chose and how the
 * game ended. GameController depends only on this interface, so another solver can replace VisibleSolver.
 * <p>Data: none; it only defines solve() and the two records that carry its input limits and output.
 * <p>OOP: Abstraction - it says what a solver does, not how. Polymorphism - any implementation can be passed to
 * GameController.
 *
 * @author Farhiya Ayyub
 */
public interface SolitaireSolver {

    /**
     * Plays a game from the board and returns the result. The board passed in must not be changed, and the returned
     * moves must be legal when replayed from it in order.
     *
     * <p>Called by: GameController.solveAndReplay().
     *
     * @param initialBoard the starting board; it is not changed
     * @return how the game ended, with the moves played
     */
    Result solve(GameState initialBoard);

    /**
     * Work limits for one game.
     *
     * <p>Kind: record, nested in SolitaireSolver.
     * <p>Purpose: it guarantees every game ends: a game stops when either limit is reached.
     * <p>Data: record fields maxMoves (int) and maxMillis (long, because milliseconds can exceed an int).
     *
     * @param maxMoves maximum number of moves played
     * @param maxMillis maximum running time in milliseconds
     */
    record Limits(int maxMoves, long maxMillis) {
        /**
         * Rejects limits that would stop a game before it starts.
         *
         * @throws IllegalArgumentException if any limit is less than 1
         */
        public Limits {
            if (maxMoves < 1 || maxMillis < 1) {
                throw new IllegalArgumentException("Solver limits must be positive");
            }
        }
    }

    /**
     * Outcome of one solved game.
     *
     * <p>Kind: record, nested in SolitaireSolver.
     * <p>Purpose: it carries how the game ended and the moves that led there, back to GameController.
 * <p>Data: record fields; the moves are kept with List.copyOf, so the result cannot be changed afterwards.
     *
     * @param status WON, STUCK, or LIMIT_REACHED
     * @param moves the moves played, in order
     * @param detail the message printed after "Result:", such as "Auto-play won the game"
     */
    record Result(GameStatus status, List<Move> moves, String detail) {
        /**
         * Rejects missing values and copies the move list.
         *
         * @throws NullPointerException for a null status, detail, or move list
         */
        public Result {
            Objects.requireNonNull(status, "status");
            Objects.requireNonNull(detail, "detail");
            moves = List.copyOf(moves);
        }
    }
}
