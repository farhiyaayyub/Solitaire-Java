package solitaire.enums;

/**
 * The possible states of a game: still running, won, stuck (no new move to try), or stopped because the move or time
 * limit was reached.
 *
 * @author Farhiya Ayyub
 */
public enum GameStatus {
    RUNNING,
    /** All 52 cards are on the foundations. */
    WON,
    /** The solver has tried every legal move in the current visible position. */
    STUCK,
    /** The solver reached its move or time limit before winning or getting stuck. */
    LIMIT_REACHED
}
