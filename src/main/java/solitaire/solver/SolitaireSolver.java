package solitaire.solver;

import java.util.List;
import java.util.Objects;

import solitaire.enums.GameStatus;
import solitaire.game.GameState;
import solitaire.move.Move;

/**
 * What every automatic player must provide: given a starting board, return the moves it chose and how the game
 * ended. GameController depends only on this interface, so a different solver can replace VisibleSolver without
 * changing the controller.
 *
 * <p>Abstraction: it says what a solver does, not how. Polymorphism: any implementation can be passed to
 * GameController.
 *
 * @author Farhiya Ayyub
 */
public interface SolitaireSolver {

    /**
     * Plays a game starting from the board and returns the result. The board passed in must not be changed; the
     * returned moves must be legal when replayed from that board in order.
     *
     * <p>Implemented by {@link VisibleSolver}.
     *
     * <p>Called by: {@code GameController.solveAndReplay()}.
     *
     * @param initialBoard the starting board; it is not changed
     * @return how the game ended, with the moves played
     */
    Result solve(GameState initialBoard);

    /**
     * The most work a solver may do for one game, so every game ends: a game stops when either limit is reached.
     *
     * @param maxMoves maximum number of moves played
     * @param maxMillis maximum running time in milliseconds
     */
    record Limits(int maxMoves, long maxMillis) {
        /**
         * Checks the limits when the record is created.
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
     * How one game ended and the moves that led there.
     *
     * @param status WON, STUCK, or LIMIT_REACHED
     * @param moves the moves played, in order
     * @param detail the message printed after "Result:", such as "Auto-play won the game"
     */
    record Result(GameStatus status, List<Move> moves, String detail) {
        /**
         * Stores an unmodifiable copy of the moves.
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
