package solitaire.shuffle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import solitaire.model.Card;

/**
 * Random shuffle controlled by a seed: the same seed and the same starting order always give the same result, so
 * any deal can be repeated.
 *
 * <p>Polymorphism: it is one of the ShuffleStrategy implementations.
 *
 * @author Farhiya Ayyub
 */
public final class RandomShuffle implements ShuffleStrategy {
    /**
     * Returns the cards in a random order that depends only on the seed.
     *
     * <p>Algorithm: Fisher-Yates shuffle (inside Collections.shuffle), O(n).
     * <ol>
     *   <li>Copy the list and create a Random from the seed.</li>
     *   <li>From the last position down to the second: swap the card with a randomly chosen card at or before it.</li>
     *   <li>Return the result as an unmodifiable list.</li>
     * </ol>
     *
     * <p>Called by: {@link ShuffleConfiguration#prepareDeck} (through ShuffleStrategy).
     *
     * @param cards the cards in draw order; the first element is the first card to be drawn
     * @param seed the seed that decides the random order
     * @return the cards in a random order that depends only on the seed
     */
    @Override
    public List<Card> shuffle(List<Card> cards, long seed) {
        List<Card> result = new ArrayList<>(cards);
        Collections.shuffle(result, new Random(seed));
        return List.copyOf(result);
    }

    /**
     * Returns "Random".
     *
     * <p>Called by: {@code ConsoleRenderer.shuffleConfiguration()} (through ShuffleStrategy).
     *
     * @return "Random"
     */
    @Override
    public String name() {
        return "Random";
    }
}
