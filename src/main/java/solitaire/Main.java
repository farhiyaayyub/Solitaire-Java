package solitaire;

/**
 * Entry point of the program. It passes the command-line arguments to {@link SolitaireApplication} and ends the
 * process with the exit code that the application returns.
 *
 * @author Farhiya Ayyub
 */
public final class Main {
    /** Prevents objects of this class; it only provides main(). */
    private Main() {
    }

    /**
     * Runs the application, then exits with 0 (success), 1 (file or game error), or 2 (invalid input).
     *
     * <p>Called by: the Java runtime, when the program starts.
     *
     * @param args the command-line arguments; the first one is the command
     */
    public static void main(String[] args) {
        System.exit(SolitaireApplication.run(args));
    }
}
