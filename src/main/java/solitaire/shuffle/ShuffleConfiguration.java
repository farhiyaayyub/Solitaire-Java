package solitaire.shuffle;

import java.util.List;
import java.util.Objects;

import solitaire.model.Card;
import solitaire.model.Deck;

/**
 * The player's shuffle settings. Creating a configuration only validates it; {@link #prepareDeck(Deck)} does the work.
 *
 * <p>Type: record (an immutable data class; Java creates its fields, constructor, and the getters type(),
 * randomizedInitial(), and seed()).
 * OOP: ENCAPSULATION (fields cannot change after creation), POLYMORPHISM (uses any ShuffleStrategy).
 *
 * @param type              the selected shuffle
 * @param randomizedInitial whether a seeded random shuffle is applied first (Faro shuffles only)
 * @param seed              seed for every random step, so the same settings reproduce the same deal
 * @author Farhiya Ayyub
 */
public record ShuffleConfiguration(ShuffleType type, boolean randomizedInitial, long seed) {

    /**
     * Constructor (compact): Rejects a missing type and a randomized start for shuffles other than Faro.
     * Calls: Objects.requireNonNull(), ShuffleType.isFaro()
     */
    public ShuffleConfiguration {
        Objects.requireNonNull(type, "type");
        if (randomizedInitial && !type.isFaro()) {
            throw new IllegalArgumentException("Only Faro shuffles start from a randomized deck");
        }
    }

    /** Method: Returns the same settings with the seed moved forward, used for consecutive simulation games. */
    public ShuffleConfiguration withSeedOffset(long offset) {
        // Object: ShuffleConfiguration - a new record, because records cannot be changed.
        return new ShuffleConfiguration(type, randomizedInitial, seed + offset);
    }

    /**
     * Method: Shuffles a new standard deck using these settings.
     * Calls: prepareDeck(Deck)
     */
    public Deck prepareDeck() {
        // Object: Deck - a new standard 52-card deck.
        return prepareDeck(new Deck());
    }

    /**
     * Method: Shuffles a copy of the supplied deck; the original deck is not changed.
     * Calls: Deck.drawOrder(), ShuffleType.strategy(), ShuffleStrategy.shuffle(), Deck.verify()
     */
    public Deck prepareDeck(Deck initialDeck) {
        List<Card> drawOrder = initialDeck.drawOrder();
        if (randomizedInitial) {
            drawOrder = ShuffleType.RANDOM.strategy().shuffle(drawOrder, seed);
        }
        // Object: Deck - built from the shuffled cards. shuffle() runs the chosen strategy's own version.
        Deck deck = new Deck(type.strategy().shuffle(drawOrder, seed));
        deck.verify();
        return deck;
    }
}
