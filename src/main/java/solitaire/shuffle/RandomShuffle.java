package solitaire.shuffle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import solitaire.model.Card;

/**
 * Random shuffle.
 *
 * <p>Kind: final class, implements ShuffleStrategy.
 * <p>Purpose: it gives a new random order on every call, so every game is a different deal.
 * <p>Data: none; it only rearranges the list it is given.
 * <p>OOP: Polymorphism - it is one of the ShuffleStrategy implementations, used through the interface.
 *
 * @author Farhiya Ayyub
 */
public final class RandomShuffle implements ShuffleStrategy {
    /**
     * {@return the cards in a new random order}
     *
     * <p>Algorithm: Fisher-Yates shuffle (inside Collections.shuffle), O(n).
     * <ol>
     *   <li>Copy the list into an ArrayList.</li>
     *   <li>From the last position down to the second: swap the card with a randomly chosen card at or before it.</li>
     *   <li>Return the result as an unmodifiable list.</li>
     * </ol>
     *
     * <p>Called by: ShuffleConfiguration.prepareDeck(Deck) (through ShuffleStrategy).
     *
     * @param cards the cards in draw order; the first element is the first card to be drawn
     */
    @Override
    public List<Card> shuffle(List<Card> cards) {
        List<Card> result = new ArrayList<>(cards);
        Collections.shuffle(result);
        return List.copyOf(result);
    }

    /** {@return "Random"} Called by: ConsoleRenderer.shuffleConfiguration() (through ShuffleStrategy). */
    @Override
    public String name() {
        return "Random";
    }
}
