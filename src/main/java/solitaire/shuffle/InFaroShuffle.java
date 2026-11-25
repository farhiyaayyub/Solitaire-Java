package solitaire.shuffle;

/**
 * In-Faro shuffle: the interleaving starts with the second half, so the original top card moves to second place.
 *
 * @author Farhiya Ayyub
 */
public final class InFaroShuffle extends FaroShuffle {
    /** Returns true, so the first card comes from the second half of the deck. */
    @Override
    protected boolean startsWithSecondHalf() {
        return true;
    }

    /** Returns "In Faro", the name shown to the player. */
    @Override
    public String name() {
        return "In Faro";
    }
}
