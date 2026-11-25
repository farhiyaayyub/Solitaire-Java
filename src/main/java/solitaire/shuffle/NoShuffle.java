package solitaire.shuffle;

import java.util.List;

import solitaire.model.Card;

/**
 * A shuffle that keeps the cards in the same order.
 *
 * @author Farhiya Ayyub
 */
public final class NoShuffle implements ShuffleStrategy {
    /** Returns a copy of the cards in the same order. */
    @Override
    public List<Card> shuffle(List<Card> cards) {
        // Return a copy with the same order; nothing is mixed.
        return List.copyOf(cards);
    }

    /** Returns "None", the name shown to the player. */
    @Override
    public String name() {
        return "None";
    }
}
