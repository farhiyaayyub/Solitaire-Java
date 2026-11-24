package solitaire.io;

/**
 * Console text colors.
 *
 * <p>Kind: enum.
 * <p>Purpose: colors text with ANSI escape codes. Used only for display, never by game logic.
 * <p>Data: each constant holds its ANSI escape code as a String, plus one shared reset code.
 * <p>OOP: Encapsulation - the escape codes are private; callers only use paint().
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

    /**
     * Stores the ANSI code of one constant.
     *
     * @param escapeCode the ANSI code that switches to this color
     */
    ConsoleColor(String escapeCode) {
        this.escapeCode = escapeCode;
    }

    /**
     * {@return the text between this color's code and the reset code}
     *
     * <p>Called by: ConsoleInput.showError(), ConsoleRenderer.section(), ConsoleRenderer.nextMove(),
     * ConsoleRenderer.showCard(), SolitaireApplication.run().
     *
     * @param text the text to color
     */
    public String paint(String text) {
        return escapeCode + text + RESET;
    }
}
