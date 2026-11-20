package solitaire.enums;

import solitaire.model.Card;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Shuffle choices and their card-ordering behavior.
 * @author Farhiya Ayyub
 */
public enum ShuffleType {
    IN,
    OUT,
    RANDOM;

    /** Returns detached cards in shuffled draw order; the input is unchanged. */
    public List<Card> shuffle(List<Card> drawOrder, long seed) {
        List<Card> shuffledCards = new ArrayList<>();
        if (this == RANDOM) {
            for (Card card : drawOrder) shuffledCards.add(card.copy());
            Collections.shuffle(shuffledCards, new Random(seed));
        } else {
            if (drawOrder.size() % 2 != 0) {
                throw new IllegalArgumentException("Faro requires two equal halves");
            }
            int halfSize = drawOrder.size() / 2;
            int firstHalfOffset = this == IN ? halfSize : 0;
            int secondHalfOffset = this == IN ? 0 : halfSize;
            for (int cardIndex = 0; cardIndex < halfSize; cardIndex++) {
                shuffledCards.add(drawOrder.get(firstHalfOffset + cardIndex).copy());
                shuffledCards.add(drawOrder.get(secondHalfOffset + cardIndex).copy());
            }
        }
        return List.copyOf(shuffledCards);
    }
}
