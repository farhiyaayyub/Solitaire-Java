package solitaire.shuffle;

import java.util.List;

import solitaire.model.Card;

/** A strategy that returns a shuffled copy of a top-first card order. */
public interface ShuffleStrategy {
    /** Returns the shuffled cards without modifying {@code cards}. */
    List<Card> shuffle(List<Card> cards, long seed);

    /** Returns the display name of this shuffle. */
    String name();
}
