package solitaire;

/**
 * Starts the Solitaire application.
 *
 * <p>Type: final class; the entry point. Java starts the program at main().
 *
 * @author Farhiya Ayyub
 */
public final class Main {
    /** Constructor (private): Prevents creating an object of this entry-point class. */
    private Main() {
    }

    /**
     * Method (static): Starts the application and exits with its status code (0, 1, or 2).
     * Calls: SolitaireApplication.run(), System.exit()
     */
    public static void main(String[] args) {
        System.exit(SolitaireApplication.run(args));
    }
}
