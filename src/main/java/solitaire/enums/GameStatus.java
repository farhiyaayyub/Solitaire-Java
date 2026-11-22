package solitaire.enums;

/**
 * The possible states of a game.
 *
 * <p>Type: enum; a fixed set of constants, safer than plain strings or numbers for the game state.
 *
 * @author Farhiya Ayyub
 */
public enum GameStatus {
    /** Constant: The game is still being played. */
    RUNNING,
    /** Constant: All 52 cards reached the foundations. */
    WON,
    /** Constant: The solver has no untried move in the current position (not proof it is unsolvable). */
    STUCK,
    /** Constant: The decision, move, or time limit ended play. */
    LIMIT_REACHED
}
