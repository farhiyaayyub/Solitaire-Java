package solitaire.shuffle;

import java.util.List;

import solitaire.model.Card;

/**
 * A shuffle that changes nothing: it keeps the card order exactly as given. Used to play a card file in its file order.
 *
 * @author Farhiya Ayyub
 */
public final class NoShuffle implements ShuffleStrategy {
    /** Returns a copy of the cards in the same order. */
    @Override
    public List<Card> shuffle(List<Card> cards) {
        return List.copyOf(cards);
    }

    /** Returns "None", the name shown to the player. */
    @Override
    public String name() {
        return "None";
    }
}
