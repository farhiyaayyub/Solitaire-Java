package solitaire.shuffle;

import java.util.List;

import solitaire.model.Card;

/**
 * No shuffle: keeps the card order exactly as given.
 *
 * <p>Kind: final class, implements ShuffleStrategy.
 * <p>Purpose: it is used for a deck loaded from a file, where the file already sets the order. Having it as a
 * strategy means the deck code needs no special case for "no shuffle".
 * <p>Data: none; it only copies the list it is given.
 * <p>OOP: Polymorphism - it is one of the ShuffleStrategy implementations, used through the interface.
 *
 * @author Farhiya Ayyub
 */
public final class NoShuffle implements ShuffleStrategy {
    /**
     * {@return an unmodifiable copy of the list in the same order}
     *
     * <p>Called by: ShuffleConfiguration.prepareDeck(Deck) (through ShuffleStrategy).
     *
     * @param cards the cards in draw order; the first element is the first card to be drawn
     */
    @Override
    public List<Card> shuffle(List<Card> cards) {
        return List.copyOf(cards);
    }

    /** {@return "None"} Called by: ConsoleRenderer.shuffleConfiguration() (through ShuffleStrategy). */
    @Override
    public String name() {
        return "None";
    }
}
