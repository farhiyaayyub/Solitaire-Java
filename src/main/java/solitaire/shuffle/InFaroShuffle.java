package solitaire.shuffle;

/**
 * In-Faro shuffle: the first card comes from the second half, so the original top card moves to position 2.
 *
 * <p>Inheritance: it reuses FaroShuffle.shuffle() and only overrides startsWithSecondHalf() and name().
 *
 * @author Farhiya Ayyub
 */
public final class InFaroShuffle extends FaroShuffle {
    /**
     * Returns {@code true}, so interleaving starts with the second half.
     *
     * <p>Called by: {@link FaroShuffle#shuffle} (through FaroShuffle).
     *
     * @return {@code true}, so interleaving starts with the second half
     */
    @Override
    protected boolean startsWithSecondHalf() {
        return true;
    }

    /**
     * Returns "In Faro".
     *
     * <p>Called by: {@code ConsoleRenderer.shuffleConfiguration()} (through ShuffleStrategy).
     *
     * @return "In Faro"
     */
    @Override
    public String name() {
        return "In Faro";
    }
}
