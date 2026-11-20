package solitaire.enums;

/**
 * Foreground colors used by console presentation only.
 * @author Farhiya Ayyub
 */
public enum ConsoleColor {
    CYAN("\u001B[36m");

    private static final String RESET = "\u001B[0m";
    private final String escapeCode;

    ConsoleColor(String escapeCode) {
        this.escapeCode = escapeCode;
    }

    /** Wraps text in this foreground color and restores the terminal style afterwards. */
    public String paint(String text) {
        return escapeCode + text + RESET;
    }
}
