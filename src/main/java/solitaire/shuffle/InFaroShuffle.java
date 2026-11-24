package solitaire.shuffle;

/**
 * In-Faro shuffle.
 *
 * <p>Kind: final class, extends FaroShuffle.
 * <p>Purpose: it interleaves starting with the second half, so the original top card moves to position 2.
 * <p>Data: none; it only answers startsWithSecondHalf() and name().
 * <p>OOP: Inheritance - it reuses FaroShuffle.shuffle(). Polymorphism - it overrides startsWithSecondHalf() and
 * name().
 *
 * @author Farhiya Ayyub
 */
public final class InFaroShuffle extends FaroShuffle {
    /** {@return {@code true}} Called by: FaroShuffle.shuffle(). */
    @Override
    protected boolean startsWithSecondHalf() {
        return true;
    }

    /** {@return "In Faro"} Called by: ConsoleRenderer.shuffleConfiguration() (through ShuffleStrategy). */
    @Override
    public String name() {
        return "In Faro";
    }
}
