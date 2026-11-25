package solitaire.shuffle;

/**
 * The In-Faro shuffle, which starts the interleaving with the second half of the deck.
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
