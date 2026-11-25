package solitaire.io;

/**
 * Colors for console text using ANSI codes: cyan for section titles, yellow for the next move, and red for red cards
 * and error messages.
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

    private static final String RESET = "\u001B[0m";
    private final String escapeCode;

    /** Stores the ANSI code of one color. */
    ConsoleColor(String escapeCode) {
        this.escapeCode = escapeCode;
    }

    /** Wraps the text in this color's code and the reset code, so only that text is colored. */
    public String paint(String text) {
        return escapeCode + text + RESET;
    }
}
