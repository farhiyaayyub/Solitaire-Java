package solitaire;

/**
 * Starts the Solitaire application.
 *
 * @author Farhiya Ayyub
 */
public final class Main {
    /** Prevents creating an instance of this entry-point class. */
    private Main() {
    }

    /** Starts the application and exits with its reported process status. */
    public static void main(String[] args) {
        System.exit(SolitaireApplication.run(args));
    }
}
