package solitaire.shuffle;

import java.util.List;

import solitaire.model.Card;

/**
 * One way of shuffling a card order (Strategy pattern).
 *
 * <p>Kind: interface, implemented by RandomShuffle, NoShuffle, and FaroShuffle (abstract; extended by InFaroShuffle
 * and OutFaroShuffle).
 * <p>Purpose: it lets ShuffleConfiguration shuffle a deck without knowing which shuffle was chosen, so a new shuffle
 * can be added as a new class without changing the code that uses it.
 * <p>Data: none; an interface only declares methods.
 * <p>OOP: Abstraction - the interface says what a shuffle does, not how. Polymorphism - ShuffleConfiguration runs the
 * shuffle() of the actual object.
 *
 * @author Farhiya Ayyub
 */
public interface ShuffleStrategy {
    /**
     * {@return a new, unmodifiable list with the cards in shuffled order, next card to draw first} The input list is
     * not changed.
     *
     * <p>Called by: ShuffleConfiguration.prepareDeck(Deck).
     *
     * @param cards the cards in draw order; the first element is the first card to be drawn
     */
    List<Card> shuffle(List<Card> cards);

    /**
     * {@return the name shown to the player, such as "Random" or "In Faro"}
     *
     * <p>Called by: ConsoleRenderer.shuffleConfiguration().
     */
    String name();
}
