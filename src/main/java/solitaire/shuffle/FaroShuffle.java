package solitaire.shuffle;

import java.util.ArrayList;
import java.util.List;

import solitaire.model.Card;

/**
 * A perfect Faro shuffle: the deck is cut into two equal halves, and the halves are interleaved one card at a time.
 * The In and Out versions differ only in which half gives the first card, so that is the only method they write.
 *
 * <p>Inheritance: the interleaving code is shared by both versions. Polymorphism: each subclass overrides
 * startsWithSecondHalf().
 *
 * @author Farhiya Ayyub
 */
public abstract class FaroShuffle implements ShuffleStrategy {
    /**
     * Returns {@code true} if the first card comes from the second half (cards 27 to 52).
     *
     * <p>Abstract: implemented by {@link InFaroShuffle} ({@code true}) and {@link OutFaroShuffle} ({@code false}).
     *
     * <p>Called by: {@link #shuffle}.
     *
     * @return {@code true} if the second half goes first
     */
    protected abstract boolean startsWithSecondHalf();

    /**
     * Returns the perfectly interleaved deck. The seed is ignored because a perfect shuffle is not random.
     *
     * <p>Algorithm: perfect interleave, O(n).
     * <ol>
     *   <li>Split the cards into two equal halves (26 and 26).</li>
     *   <li>For i = 0 to 25: add card i of the half that goes first, then card i of the other half.</li>
     *   <li>In-Faro takes from the second half first; Out-Faro from the first half.</li>
     * </ol>
     *
     * <p>Implements {@link ShuffleStrategy#shuffle}; InFaroShuffle and OutFaroShuffle inherit it.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link #startsWithSecondHalf} - In or Out decides the first half</li>
     * </ul>
     *
     * <p>Called by: {@link ShuffleConfiguration#prepareDeck} (through ShuffleStrategy).
     *
     * @param cards the cards in draw order; the first element is the first card to be drawn
     * @param seed not used; a perfect shuffle is not random
     * @return the perfectly interleaved deck
     * @throws IllegalArgumentException for an odd number of cards, since the halves would not be equal
     */
    @Override
    public List<Card> shuffle(List<Card> cards, long seed) {
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
