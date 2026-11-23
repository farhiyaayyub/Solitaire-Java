package solitaire.shuffle;

/** Perfect Faro shuffle that starts by taking a card from the top half. */
public final class OutFaroShuffle extends FaroShuffle {
    @Override
    protected boolean startsWithSecondHalf() {
        return false;
    }

    @Override
    public String name() {
        return "Out Faro";
    }
}
