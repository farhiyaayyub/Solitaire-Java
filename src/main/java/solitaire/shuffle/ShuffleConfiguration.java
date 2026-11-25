package solitaire.shuffle;

import java.util.List;
import java.util.Objects;

import solitaire.model.Card;
import solitaire.model.Deck;

/**
 * The player's shuffle settings. It turns a deck into a shuffled deck that is ready to deal.
 *
 * @author Farhiya Ayyub
 */
public record ShuffleConfiguration(ShuffleType type, boolean randomizedInitial) {

    /**
     * Checks the settings when they are created: the shuffle type is required, and only a Faro shuffle may start from a
     * randomized deck.
     */
    public ShuffleConfiguration {
        // A shuffle type must be chosen.
        Objects.requireNonNull(type, "type");
        // Randomizing first only makes sense before a Faro shuffle; otherwise stop with an error.
        if (randomizedInitial && !type.isFaro()) {
            throw new IllegalArgumentException("Only Faro shuffles start from a randomized deck");
        }
    }

    /** Creates a new standard deck and shuffles it with these settings. */
    public Deck prepareDeck() {
        // Start from a new standard deck.
        return prepareDeck(new Deck());
    }

    /**
     * Takes the card order of the given deck, randomizes it first if the settings say so, then applies the chosen
     * shuffle and returns the result as a new deck. The given deck is not changed.
     */
    public Deck prepareDeck(Deck initialDeck) {
        // Step 1: get a copy of the deck's card order, so the given deck is not changed.
        List<Card> drawOrder = initialDeck.drawOrder();
        // Step 2: if the settings say so, mix the cards randomly first.
        if (randomizedInitial) {
            drawOrder = ShuffleType.RANDOM.strategy().shuffle(drawOrder);
        }
        // Step 3: apply the chosen shuffle and build a new deck from the result.
        // type.strategy() may be any ShuffleStrategy; Java runs the shuffle() of the actual strategy object.
        Deck deck = new Deck(type.strategy().shuffle(drawOrder));
        // Step 4: make sure the new deck still has 52 different cards.
        deck.verify();
        return deck;
    }
}
