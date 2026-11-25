package solitaire.shuffle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import solitaire.model.Card;

/**
 * A random shuffle that mixes the cards into a new random order every time.
 *
 * @author Farhiya Ayyub
 */
public final class RandomShuffle implements ShuffleStrategy {
    /** Copies the cards, mixes them with Collections.shuffle(), and returns them in the new random order. */
    @Override
    public List<Card> shuffle(List<Card> cards) {
        List<Card> result = new ArrayList<>(cards);
        Collections.shuffle(result);
        return List.copyOf(result);
    }

    /** Returns "Random", the name shown to the player. */
    @Override
    public String name() {
        return "Random";
    }
}
