package solitaire.shuffle;

import java.util.ArrayList;
import java.util.List;

import solitaire.model.Card;

/**
 * A perfect Faro shuffle (Template Method pattern: shuffle() asks subclasses for startsWithSecondHalf()).
 *
 * <p>Kind: abstract class, implements ShuffleStrategy; extended by InFaroShuffle and OutFaroShuffle.
 * <p>Purpose: it cuts the deck into two equal halves and interleaves them one card at a time. The In and Out versions
 * differ only in which half gives the first card.
 * <p>Data: none; it only rearranges the list it is given.
 * <p>OOP: Inheritance - both versions reuse the interleaving code. Abstraction - startsWithSecondHalf() is abstract.
 * Polymorphism - shuffle() runs the startsWithSecondHalf() of the actual subclass.
 *
 * @author Farhiya Ayyub
 */
public abstract class FaroShuffle implements ShuffleStrategy {
    /**
     * {@return {@code true} if the first card comes from the second half (cards 27 to 52)}
     *
     * <p>Called by: shuffle().
     */
    protected abstract boolean startsWithSecondHalf();

    /**
     * {@return the perfectly interleaved cards} It is not random: the same input always gives the same output.
     *
     * <p>Algorithm: perfect interleave, O(n).
     * <ol>
     *   <li>Split the cards into two equal halves (26 and 26).</li>
     *   <li>For i = 0 to 25: add card i of the half that goes first, then card i of the other half.</li>
     *   <li>In-Faro takes from the second half first; Out-Faro from the first half.</li>
     * </ol>
     *
     * <p>Called by: ShuffleConfiguration.prepareDeck(Deck) (through ShuffleStrategy).
     *
     * @param cards the cards in draw order; the first element is the first card to be drawn
     * @throws IllegalArgumentException for an odd number of cards, since the halves would not be equal
     */
    @Override
    public List<Card> shuffle(List<Card> cards) {
        if (cards.size() % 2 != 0) {
            throw new IllegalArgumentException("A Faro shuffle requires an even number of cards");
        }

        int half = cards.size() / 2;
        List<Card> result = new ArrayList<>(cards.size());
        boolean secondFirst = startsWithSecondHalf();
        for (int index = 0; index < half; index++) {
            result.add(cards.get((secondFirst ? half : 0) + index));
            result.add(cards.get((secondFirst ? 0 : half) + index));
        }
        return List.copyOf(result);
    }
}
