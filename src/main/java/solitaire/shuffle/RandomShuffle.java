package solitaire.shuffle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import solitaire.model.Card;

/**
 * A shuffle that mixes the cards into a random order.
 *
 * @author Farhiya Ayyub
 */
public final class RandomShuffle implements ShuffleStrategy {
    /** Copies the cards, mixes them with Collections.shuffle(), and returns them in the new random order. */
    @Override
    public List<Card> shuffle(List<Card> cards) {
        // Copy the list, so the original order is not changed.
        List<Card> result = new ArrayList<>(cards);
        // Mix the copy into a random order.
        Collections.shuffle(result);
        // Return the mixed cards as a list that cannot be changed.
        return List.copyOf(result);
    }

    /** Returns "Random", the name shown to the player. */
    @Override
    public String name() {
        return "Random";
    }
}
