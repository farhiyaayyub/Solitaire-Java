package solitaire.solver;

import solitaire.game.GameState;
import solitaire.enums.GameStatus;
import solitaire.move.Move;

import java.util.List;

/**
 * Contract for bounded automatic play and its result values.
 * @author Farhiya Ayyub
 */
public interface SolitaireSolver {
    /** Solves from a copy of the supplied starting board. */
    Result solve(GameState initial);

    /** Limits the amount of work a solver may perform. */
    record Limits(int states, int moves, long milliseconds) {
        /** Rejects limits that would allow no work to begin. */
        public Limits {
            if (states < 1 || moves < 1 || milliseconds < 1)
                throw new IllegalArgumentException("Solver limits must be positive");
        }
    }

    /** Holds the outcome, moves played, and decision count from a solved. */
    record Result(GameStatus status, List<Move> moves, int explored, String detail) {
        /** Validates the result and stores an immutable move list. */
        public Result {
            java.util.Objects.requireNonNull(status, "status");
            java.util.Objects.requireNonNull(detail, "detail");
            moves = List.copyOf(moves);
            if (explored < 0) throw new IllegalArgumentException("Explored states cannot be negative");
        }
    }
}
