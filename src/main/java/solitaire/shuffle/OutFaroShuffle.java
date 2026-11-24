package solitaire.shuffle;

/**
 * Out-Faro shuffle: the first card comes from the first half, so the original top and bottom cards stay in place.
 *
 * <p>Inheritance: it reuses FaroShuffle.shuffle() and only overrides startsWithSecondHalf() and name().
 *
 * @author Farhiya Ayyub
 */
public final class OutFaroShuffle extends FaroShuffle {
    /**
     * Returns {@code false}, so interleaving starts with the first half.
     *
     * <p>Implements {@link FaroShuffle#startsWithSecondHalf}.
     *
     * <p>Called by: {@link FaroShuffle#shuffle} (through FaroShuffle).
     *
     * @return {@code false}
     */
    @Override
    protected boolean startsWithSecondHalf() {
        return false;
    }

    /**
     * Returns "Out Faro".
     *
     * <p>Implements {@link ShuffleStrategy#name}.
     *
     * <p>Called by: {@code ConsoleRenderer.shuffleConfiguration()} (through ShuffleStrategy).
     *
     * @return "Out Faro"
     */
    @Override
    public String name() {
        return "Out Faro";
    }
}
