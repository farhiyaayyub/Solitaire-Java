package solitaire.shuffle;

/** Perfect Faro shuffle that starts by taking a card from the bottom half. */
public final class InFaroShuffle extends FaroShuffle {
    @Override
    protected boolean startsWithSecondHalf() {
        return true;
    }

    @Override
    public String name() {
        return "In Faro";
    }
}
