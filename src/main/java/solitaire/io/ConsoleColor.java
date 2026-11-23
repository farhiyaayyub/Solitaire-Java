package solitaire.io;

/**
 * Text colors for the console, using ANSI escape codes. Used only for display, never by game logic.
 *
 * <p>Encapsulation: the escape codes are private; callers only use paint().
 *
 * @author Farhiya Ayyub
 */
public enum ConsoleColor {
    /** Used for section titles, such as TABLEAU and RESULTS. */
    CYAN("\u001B[36m"),
    /** Used for the "Next move" sentence. */
    YELLOW("\u001B[33m"),
    /** Used for face-up Hearts and Diamonds, and for error messages. */
    RED("\u001B[31m");

    // ANSI code that returns the terminal to its normal color.
    private static final String RESET = "\u001B[0m";
    private final String escapeCode;

    /**
     * Runs once for each constant above and stores its ANSI code.
     *
     * @param escapeCode the ANSI code that switches to this color
     */
    ConsoleColor(String escapeCode) {
        this.escapeCode = escapeCode;
    }

    /**
     * Returns the text between this color's code and the reset code, so only that text is colored.
     *
     * <p>Called by: {@code ConsoleInput.showError()}, {@link ConsoleRenderer#section},
     * {@link ConsoleRenderer#nextMove}, {@code ConsoleRenderer.showCard()}, {@code SolitaireApplication.run()}.
     *
     * @param text the text to color
     * @return the text between this color's code and the reset code, so only that text is colored
     */
    public String paint(String text) {
        return escapeCode + text + RESET;
    }
}
