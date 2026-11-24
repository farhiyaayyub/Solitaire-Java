package solitaire.shuffle;

/**
 * The shuffle choices offered to the player.
 *
 * <p>Kind: enum.
 * <p>Purpose: it connects a menu choice to the object that does that shuffle, through type.strategy().
 * <p>Data: each constant stores one ShuffleStrategy object.
 * <p>OOP: Polymorphism - strategy() returns the shuffle as the ShuffleStrategy interface, whatever its actual class.
 *
 * @author Farhiya Ayyub
 */
public enum ShuffleType {
    // Each strategy object is created once, when the enum is loaded, and shared by every game.
    RANDOM(new RandomShuffle()),
    IN(new InFaroShuffle()),
    OUT(new OutFaroShuffle()),
    /** Keeps the order; offered only for a deck loaded from a file. */
    NONE(new NoShuffle());

    private final ShuffleStrategy strategy;

    /**
     * Runs once for each constant above.
     *
     * @param strategy the object that does this shuffle
     */
    ShuffleType(ShuffleStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * {@return the shuffle object for this choice}
     *
     * <p>Called by: ConsoleRenderer.shuffleConfiguration(), ShuffleConfiguration.prepareDeck(Deck).
     */
    public ShuffleStrategy strategy() {
        return strategy;
    }

    /**
     * {@return {@code true} for IN and OUT, the two perfect (Faro) shuffles; {@code false} for RANDOM and NONE}
     *
     * <p>Called by: ShuffleConfiguration(ShuffleType, boolean), ConsoleInput.shuffleConfiguration().
     */
    public boolean isFaro() {
        return this == IN || this == OUT;
    }
}
