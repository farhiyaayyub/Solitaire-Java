package solitaire.solver;

import java.util.List;
import java.util.Objects;

import solitaire.enums.GameStatus;
import solitaire.game.GameState;
import solitaire.move.Move;

/**
 * The interface for an automatic player, so GameController can use any solver in the same way.
 *
 * @author Farhiya Ayyub
 */
public interface SolitaireSolver {

    /** Plays a full game on a copy of the board and returns how it ended, together with the moves played. */
    Result solve(GameState initialBoard);

    /** The limits for one game: the most moves and the most milliseconds the solver may use. */
    record Limits(int maxMoves, long maxMillis) {
        /** Rejects limits below 1, which would stop a game before it starts. */
        public Limits {
            // A limit below 1 would stop the game before the first move.
            if (maxMoves < 1 || maxMillis < 1) {
                throw new IllegalArgumentException("Solver limits must be positive");
            }
        }
    }

    /** The outcome of one solved game: the final status, the moves played, and a short message. */
    record Result(GameStatus status, List<Move> moves, String detail) {
        /** Rejects a missing status or message, and stores an unmodifiable copy of the move list. */
        public Result {
            // The status and the message must be given; the move list is copied so it cannot be changed later.
            Objects.requireNonNull(status, "status");
            Objects.requireNonNull(detail, "detail");
            moves = List.copyOf(moves);
        }
    }
}
