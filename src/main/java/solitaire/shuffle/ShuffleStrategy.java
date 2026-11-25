package solitaire.shuffle;

import java.util.List;

import solitaire.model.Card;

/**
 * The interface for every way of shuffling, so the program can shuffle without knowing which shuffle is used.
 *
 * @author Farhiya Ayyub
 */
public interface ShuffleStrategy {
    /** Returns a new list with the cards in shuffled order, next card to draw first. */
    List<Card> shuffle(List<Card> cards);

    /** Returns the shuffle's name shown to the player, such as "Random" or "In Faro". */
    String name();
}
