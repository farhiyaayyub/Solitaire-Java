package solitaire.shuffle;

/**
 * The shuffle choices the player can pick: Random, In-Faro, Out-Faro, or None.
 *
 * @author Farhiya Ayyub
 */
public enum ShuffleType {
    // Each strategy object is created once, when the enum is loaded, and shared by every game.
    RANDOM(new RandomShuffle()),
    IN(new InFaroShuffle()),
    OUT(new OutFaroShuffle()),
    /** Keeps the card order exactly as given. */
    NONE(new NoShuffle());

    private final ShuffleStrategy strategy;

    /** Stores the shuffle object of one choice. */
    ShuffleType(ShuffleStrategy strategy) {
        this.strategy = strategy;
    }

    /** Returns the shuffle object for this choice. */
    public ShuffleStrategy strategy() {
        return strategy;
    }

    /** Returns true for IN and OUT, the two perfect (Faro) shuffles, and false for RANDOM and NONE. */
    public boolean isFaro() {
        // Only IN and OUT are Faro shuffles.
        return this == IN || this == OUT;
    }
}
