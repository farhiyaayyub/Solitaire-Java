package solitaire.io;

/**
 * Foreground colors used by console presentation only.
 *
 * <p>Type: enum; each constant holds the terminal code for one color.
 * OOP: ENCAPSULATION (private escape codes; callers only use paint()).
 *
 * @author Farhiya Ayyub
 */
public enum ConsoleColor {
    // Constant: the cyan color used for section titles.
    CYAN("\u001B[36m");

    // Constant: terminal code that resets the color.
    private static final String RESET = "\u001B[0m";
    // Field: the terminal code for this color.
    private final String escapeCode;

    /** Constructor: Stores the terminal code for this color. */
    ConsoleColor(String escapeCode) {
        this.escapeCode = escapeCode;
    }

    /** Method: Wraps text in this foreground color and restores the terminal style afterwards. */
    public String paint(String text) {
        return escapeCode + text + RESET;
    }
}
