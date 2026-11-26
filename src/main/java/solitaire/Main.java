package solitaire;

/**
 * The starting point of the program. It calls SolitaireApplication.run() and closes the program with its exit code.
 *
 * @author Farhiya Ayyub
 */
public final class Main {
    /** Private constructor so no Main object can be created; the class only has the static main method. */
    private Main() {
    }

    /**
     * Runs SolitaireApplication with the command-line arguments, then closes the program with its exit code: 0 for
     * success, 1 for a file or game error, or 2 for invalid input.
     */
    public static void main(String[] args) {
        System.exit(SolitaireApplication.run(args));
    }
}
