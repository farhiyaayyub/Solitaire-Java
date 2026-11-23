package solitaire.shuffle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import solitaire.model.Card;

/** Seeded pseudorandom shuffle. */
public final class RandomShuffle implements ShuffleStrategy {
    @Override
    public List<Card> shuffle(List<Card> cards, long seed) {
        List<Card> result = new ArrayList<>(cards);
        Collections.shuffle(result, new Random(seed));
        return List.copyOf(result);
    }

    @Override
    public String name() {
        return "Random";
    }
}
