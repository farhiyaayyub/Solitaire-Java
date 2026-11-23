package solitaire.shuffle;

import java.util.ArrayList;
import java.util.List;

import solitaire.model.Card;

/** Shared implementation for perfect in and out Faro shuffles. */
public abstract class FaroShuffle implements ShuffleStrategy {
    protected abstract boolean startsWithSecondHalf();

    @Override
    public List<Card> shuffle(List<Card> cards, long seed) {
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
