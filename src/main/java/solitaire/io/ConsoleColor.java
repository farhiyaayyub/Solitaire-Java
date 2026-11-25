package solitaire.io;

/**
 * The text colors used in the console: cyan, yellow, and red.
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
        // Start the color, write the text, then reset so the next text is not colored.
        return escapeCode + text + RESET;
    }
}
