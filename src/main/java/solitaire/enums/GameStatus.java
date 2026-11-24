package solitaire.enums;

/**
 * The four possible states of a game. A game starts RUNNING and ends in exactly one of the other three.
 *
 * <p>An enum is used so that only these four values are possible, unlike a String or int status.
 *
 * @author Farhiya Ayyub
 */
public enum GameStatus {
    /** The game has not ended yet. */
    RUNNING,
    /** All 52 cards are on the foundations. */
    WON,
    /**
     * The solver has tried every legal move in the current visible position. This does not prove the deal is
     * unwinnable.
     */
    STUCK,
    /** The solver reached its move or time limit before winning or getting stuck. */
    LIMIT_REACHED
}
