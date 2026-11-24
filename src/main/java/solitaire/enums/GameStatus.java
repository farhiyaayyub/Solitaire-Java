package solitaire.enums;

/**
 * The possible states of a game.
 *
 * <p>Kind: enum.
 * <p>Purpose: it tells whether a game is still going and, if not, how it ended. A game starts RUNNING and ends in
 * exactly one of the other three.
 * <p>Data: none; only the four constants. An enum is used so no other status value is possible, unlike a String or
 * int.
 *
 * @author Farhiya Ayyub
 */
public enum GameStatus {
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
