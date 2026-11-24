package solitaire.shuffle;

import java.util.List;

import solitaire.model.Card;

/**
 * Keeps the card order exactly as given. Used for a deck loaded from a file, where the file already sets the order.
 *
 * <p>Polymorphism: it is one of the ShuffleStrategy implementations.
 *
 * @author Farhiya Ayyub
 */
public final class NoShuffle implements ShuffleStrategy {
    /**
     * Returns an unmodifiable copy of the list in the same order; the seed is ignored.
     *
     * <p>Implements {@link ShuffleStrategy#shuffle}.
     *
     * <p>Called by: {@link ShuffleConfiguration#prepareDeck} (through ShuffleStrategy).
     *
     * @param cards the cards in draw order; the first element is the first card to be drawn
     * @param seed not used
     * @return an unmodifiable copy of the list in the same order
     */
    @Override
    public List<Card> shuffle(List<Card> cards, long seed) {
        return List.copyOf(cards);
    }

    /**
     * Returns "None".
     *
     * <p>Implements {@link ShuffleStrategy#name}.
     *
     * <p>Called by: {@code ConsoleRenderer.shuffleConfiguration()} (through ShuffleStrategy).
     *
     * @return "None"
     */
    @Override
    public String name() {
        return "None";
    }
}
