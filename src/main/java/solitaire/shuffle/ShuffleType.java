package solitaire.shuffle;

/**
 * The shuffle choices offered to the player. Each constant holds the {@link ShuffleStrategy} object that does the
 * work, so code can go from a menu choice to its shuffle with type.strategy().
 *
 * <p>Polymorphism: strategy() returns the shuffle as the ShuffleStrategy interface, whatever its actual class.
 *
 * @author Farhiya Ayyub
 */
public enum ShuffleType {
    // Each strategy object is created once, when the enum is loaded, and shared by every game.
    /** Random order decided by the seed. */
    RANDOM(new RandomShuffle()),
    /** Perfect In-Faro shuffle. */
    IN(new InFaroShuffle()),
    /** Perfect Out-Faro shuffle. */
    OUT(new OutFaroShuffle()),
    /** Keeps the order; offered only for a deck loaded from a file. */
    NONE(new NoShuffle());

    private final ShuffleStrategy strategy;

    /**
     * Runs once for each constant above and stores its strategy.
     *
     * @param strategy the object that does this shuffle
     */
    ShuffleType(ShuffleStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Returns the shuffle object for this choice, typed as the ShuffleStrategy interface.
     *
     * <p>Called by: {@code ConsoleRenderer.shuffleConfiguration()}, {@link ShuffleConfiguration#prepareDeck}.
     *
     * @return the shuffle object for this choice, typed as the ShuffleStrategy interface
     */
    public ShuffleStrategy strategy() {
        return strategy;
    }

    /**
     * Returns {@code true} for IN and OUT, the two perfect (Faro) shuffles; {@code false} for RANDOM and NONE.
     *
     * <p>Called by: {@code GameController.simulateBatch()}, {@code ConsoleInput.shuffleConfiguration()}.
     *
     * @return {@code true} for IN and OUT, the two perfect (Faro) shuffles
     */
    public boolean isFaro() {
        return this == IN || this == OUT;
    }
}
