package solitaire.shuffle;

import java.util.List;

import solitaire.model.Card;

/**
 * The common interface for every way of shuffling: random, In-Faro, Out-Faro, and none. The rest of the program only
 * calls shuffle() and name(), so a new shuffle can be added without changing other code.
 *
 * @author Farhiya Ayyub
 */
public interface ShuffleStrategy {
    /** Returns a new list with the cards in shuffled order, next card to draw first. */
    List<Card> shuffle(List<Card> cards);

    /** Returns the shuffle's name shown to the player, such as "Random" or "In Faro". */
    String name();
}
