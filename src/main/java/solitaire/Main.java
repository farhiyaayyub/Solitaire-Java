package solitaire;

/**
 * Program entry point.
 *
 * <p>Kind: final class, uses SolitaireApplication.
 * <p>Purpose: starts the program and hands the command-line arguments to {@link SolitaireApplication}, which does
 * all the work.
 * <p>Data: none; it only starts SolitaireApplication.
 *
 * @author Farhiya Ayyub
 */
public final class Main {
    /** Prevents objects of this class; it only provides main(). */
    private Main() {
    }

    /**
     * Runs the application, then ends the Java process with its exit code: 0 (success), 1 (file or game error), or 2
     * (invalid input). Started by the JVM.
     *
     * @param args the command-line arguments; the first one is the command
     */
    public static void main(String[] args) {
        System.exit(SolitaireApplication.run(args));
    }
}
