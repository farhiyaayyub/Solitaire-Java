package solitaire.shuffle;

import java.util.List;

import solitaire.model.Card;

/**
 * One way of shuffling a card order. ShuffleConfiguration calls shuffle() without knowing which strategy it has, so
 * a new shuffle can be added as a new class without changing the code that uses it.
 *
 * <p>This is the Strategy design pattern. Abstraction: the interface says what a shuffle does, not how. Polymorphism:
 * each implementation has its own shuffle().
 *
 * @author Farhiya Ayyub
 */
public interface ShuffleStrategy {
    /**
     * Returns a new, unmodifiable list with the cards in shuffled order; the first element is the first card to be
     * drawn. The input list is not changed. Strategies that are not random ignore the seed.
     *
     * <p>Called by: {@link ShuffleConfiguration#prepareDeck}.
     *
     * @param cards the cards in draw order; the first element is the first card to be drawn
     * @param seed the seed for a random shuffle; other shuffles ignore it
     * @return a new, unmodifiable list with the cards in shuffled order
     */
    List<Card> shuffle(List<Card> cards, long seed);

    /**
     * Returns the name shown to the player, such as "Random" or "In Faro".
     *
     * <p>Called by: {@code ConsoleRenderer.shuffleConfiguration()}.
     *
     * @return the name shown to the player, such as "Random" or "In Faro"
     */
    String name();
}
