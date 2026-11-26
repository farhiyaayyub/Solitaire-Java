package solitaire.shuffle;

import java.util.ArrayList;
import java.util.List;

import solitaire.model.Card;

/**
 * A perfect (Faro) shuffle. It cuts the deck in half and interleaves the two halves one card at a time.
 *
 * @author Farhiya Ayyub
 */
public abstract class FaroShuffle implements ShuffleStrategy {
    /** Returns true if the first card comes from the second half of the deck. Each subclass gives its own answer. */
    protected abstract boolean startsWithSecondHalf();

    /**
     * Splits the cards into two equal halves (26 and 26), then takes one card from each half in turn, starting with the
     * half chosen by startsWithSecondHalf(). The same input always gives the same result. Throws an error for an odd
     * number of cards.
     */
    @Override
    public List<Card> shuffle(List<Card> cards) {
        if (cards.size() % 2 != 0) {
            throw new IllegalArgumentException("A Faro shuffle requires an even number of cards");
        }

        int half = cards.size() / 2;
        List<Card> result = new ArrayList<>(cards.size());
        boolean secondFirst = startsWithSecondHalf();
        for (int index = 0; index < half; index++) {
            result.add(cards.get((secondFirst ? half : 0) + index));
            result.add(cards.get((secondFirst ? 0 : half) + index));
        }
        return List.copyOf(result);
    }
}
