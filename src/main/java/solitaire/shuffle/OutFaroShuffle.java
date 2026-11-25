package solitaire.shuffle;

/**
 * The Out-Faro shuffle, which starts the interleaving with the first half of the deck.
 *
 * @author Farhiya Ayyub
 */
public final class OutFaroShuffle extends FaroShuffle {
    /** Returns false, so the first card comes from the first half of the deck. */
    @Override
    protected boolean startsWithSecondHalf() {
        return false;
    }

    /** Returns "Out Faro", the name shown to the player. */
    @Override
    public String name() {
        return "Out Faro";
    }
}
