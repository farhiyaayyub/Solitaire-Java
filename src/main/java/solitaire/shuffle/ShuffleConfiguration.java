package solitaire.shuffle;

import java.util.List;
import java.util.Objects;

import solitaire.model.Card;
import solitaire.model.Deck;

/**
 * The player's shuffle settings: which shuffle to use and whether to randomize the deck first. It turns a deck into a
 * shuffled deck that is ready to deal.
 *
 * @author Farhiya Ayyub
 */
public record ShuffleConfiguration(ShuffleType type, boolean randomizedInitial) {

    /**
     * Checks the settings when they are created: the shuffle type is required, and only a Faro shuffle may start from a
     * randomized deck.
     */
    public ShuffleConfiguration {
        Objects.requireNonNull(type, "type");
        if (randomizedInitial && !type.isFaro()) {
            throw new IllegalArgumentException("Only Faro shuffles start from a randomized deck");
        }
    }

    /** Creates a new standard deck and shuffles it with these settings. */
    public Deck prepareDeck() {
        return prepareDeck(new Deck());
    }

    /**
     * Takes the card order of the given deck, randomizes it first if the settings say so, then applies the chosen
     * shuffle and returns the result as a new deck. The given deck is not changed.
     */
    public Deck prepareDeck(Deck initialDeck) {
        List<Card> drawOrder = initialDeck.drawOrder();
        if (randomizedInitial) {
            drawOrder = ShuffleType.RANDOM.strategy().shuffle(drawOrder);
        }
        // type.strategy() may be any ShuffleStrategy; Java runs the shuffle() of the actual strategy object.
        Deck deck = new Deck(type.strategy().shuffle(drawOrder));
        deck.verify();
        return deck;
    }
}
