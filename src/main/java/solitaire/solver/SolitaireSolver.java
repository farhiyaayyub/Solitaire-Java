package solitaire.solver;

import java.util.List;
import java.util.Objects;

import solitaire.enums.GameStatus;
import solitaire.game.GameState;
import solitaire.move.Move;

/**
 * Contract for automatic play: given a starting board, produce a bounded, replayable list of moves.
 *
 * <p>Type: interface, implemented by VisibleSolver. GameController only knows this interface.
 * OOP: ABSTRACTION (says what a solver does, not how), POLYMORPHISM (any implementation can be plugged in).
 *
 * @author Farhiya Ayyub
 */
public interface SolitaireSolver {

    /** Abstract method: Plays from a copy of the supplied starting board; the board passed in is not changed. */
    Result solve(GameState initialBoard);

    /**
     * Type: record inside the interface. Limits the work a solver may do for one game.
     *
     * @param maxDecisions maximum number of move decisions
     * @param maxMoves     maximum number of moves played
     * @param maxMillis    maximum running time in milliseconds
     */
    record Limits(int maxDecisions, int maxMoves, long maxMillis) {
        /** Constructor (compact): Rejects limits that would not allow any work. */
        public Limits {
            if (maxDecisions < 1 || maxMoves < 1 || maxMillis < 1) {
                throw new IllegalArgumentException("Solver limits must be positive");
            }
        }
    }

    /**
     * Type: record inside the interface. The outcome of one solve.
     *
     * @param status    WON, STUCK, or LIMIT_REACHED
     * @param moves     the moves played, in order
     * @param decisions the number of decisions made
     * @param detail    a short explanation of the outcome
     */
    record Result(GameStatus status, List<Move> moves, int decisions, String detail) {
        /**
         * Constructor (compact): Validates the result and stores an unmodifiable copy of the moves.
         * Calls: Objects.requireNonNull(), List.copyOf()
         */
        public Result {
            Objects.requireNonNull(status, "status");
            Objects.requireNonNull(detail, "detail");
            moves = List.copyOf(moves);
            if (decisions < 0) {
                throw new IllegalArgumentException("Decision count cannot be negative");
            }
        }
    }
}
