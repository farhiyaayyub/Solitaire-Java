package solitaire.shuffle;

/**
 * The shuffle choices offered to the player, each backed by its {@link ShuffleStrategy}.
 *
 * <p>Type: enum; each constant holds one shuffle object.
 * OOP: ENCAPSULATION (private strategy field), POLYMORPHISM (strategy() returns any kind of ShuffleStrategy).
 *
 * @author Farhiya Ayyub
 */
public enum ShuffleType {
    // Objects: each constant creates its shuffle object once (RandomShuffle, InFaroShuffle, ...).
    RANDOM(new RandomShuffle()),
    IN(new InFaroShuffle()),
    OUT(new OutFaroShuffle()),
    NONE(new NoShuffle());

    // Field: the shuffle object for this choice.
    private final ShuffleStrategy strategy;

    /** Constructor: Stores the shuffle object for this choice. */
    ShuffleType(ShuffleStrategy strategy) {
        this.strategy = strategy;
    }

    /** Method: Returns the strategy that performs this shuffle. */
    public ShuffleStrategy strategy() {
        return strategy;
    }

    /** Method: Reports whether this is one of the two perfect (Faro) shuffles. */
    public boolean isFaro() {
        return this == IN || this == OUT;
    }
}
