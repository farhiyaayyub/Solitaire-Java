package solitaire.shuffle;

import java.util.List;

import solitaire.model.Card;

/** Leaves the supplied card order unchanged. */
public final class NoShuffle implements ShuffleStrategy {
    @Override
    public List<Card> shuffle(List<Card> cards, long seed) {
        return List.copyOf(cards);
    }

    @Override
    public String name() {
        return "None";
    }
}
