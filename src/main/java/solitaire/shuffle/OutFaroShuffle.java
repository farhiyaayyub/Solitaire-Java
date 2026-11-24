package solitaire.shuffle;

/**
 * Out-Faro shuffle.
 *
 * <p>Kind: final class, extends FaroShuffle.
 * <p>Purpose: it interleaves starting with the first half, so the original top and bottom cards stay in place.
 * <p>Data: none; it only answers startsWithSecondHalf() and name().
 * <p>OOP: Inheritance - it reuses FaroShuffle.shuffle(). Polymorphism - it overrides startsWithSecondHalf() and
 * name().
 *
 * @author Farhiya Ayyub
 */
public final class OutFaroShuffle extends FaroShuffle {
    /** {@return {@code false}} Called by: FaroShuffle.shuffle(). */
    @Override
    protected boolean startsWithSecondHalf() {
        return false;
    }

    /** {@return "Out Faro"} Called by: ConsoleRenderer.shuffleConfiguration() (through ShuffleStrategy). */
    @Override
    public String name() {
        return "Out Faro";
    }
}
