package solitaire.shuffle;

import java.util.List;
import java.util.Objects;

import solitaire.model.Card;
import solitaire.model.Deck;

/**
 * The player's shuffle settings: which shuffle, whether to randomize first, and the seed. Creating one only checks
 * the settings; {@link #prepareDeck(Deck)} does the shuffling.
 *
 * <p>It is a record, so the settings cannot change after they are created. Polymorphism: it works with any
 * ShuffleStrategy.
 *
 * @param type              the selected shuffle
 * @param randomizedInitial true to apply a seeded random shuffle before the Faro shuffle (Faro shuffles only),
 *                          because a perfect shuffle of a sorted deck always gives the same predictable deal
 * @param seed              seed for every random step; the same settings always produce the same deal
 * @author Farhiya Ayyub
 */
public record ShuffleConfiguration(ShuffleType type, boolean randomizedInitial, long seed) {

    /**
     * Checks the settings when the record is created.
     *
     * @throws NullPointerException if type is null
     * @throws IllegalArgumentException if randomizedInitial is true for a shuffle that is not Faro
     */
    public ShuffleConfiguration {
        Objects.requireNonNull(type, "type");
        if (randomizedInitial && !type.isFaro()) {
            throw new IllegalArgumentException("Only Faro shuffles start from a randomized deck");
        }
    }

    /**
     * Returns a new configuration with seed + offset and the same other settings; this record is not changed. Game n of
     * a simulation uses offset n, so every game is different but repeatable.
     *
     * <p>Called by: {@code GameController.simulate()}.
     *
     * @param offset the number added to the seed
     * @return a new configuration with seed + offset and the same other settings
     */
    public ShuffleConfiguration withSeedOffset(long offset) {
        return new ShuffleConfiguration(type, randomizedInitial, seed + offset);
    }

    /**
     * Returns a new standard deck shuffled with these settings.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link #prepareDeck} - does the work with a new standard deck</li>
     * </ul>
     *
     * <p>Called by: {@code GameController.manual()}, {@code GameController.simulate()}.
     *
     * @return a new standard deck shuffled with these settings
     */
    public Deck prepareDeck() {
        return prepareDeck(new Deck());
    }

    /**
     * Returns a new shuffled deck built from the supplied deck's order; the supplied deck is not changed. Steps: copy
     * the order, apply a random shuffle first if randomizedInitial, apply the chosen strategy, then check that the
     * result still has 52 unique cards.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link Deck#drawOrder} - copies the deck's card order</li>
     *   <li>{@link ShuffleType#strategy()} - gets the chosen shuffle object</li>
     *   <li>{@link ShuffleStrategy#shuffle} - runs that shuffle</li>
     *   <li>{@link Deck#verify} - confirms 52 unique cards remain</li>
     * </ul>
     *
     * <p>Called by: {@code GameController.automatic()}, {@link #prepareDeck}.
     *
     * @param initialDeck the deck to start from; it is not changed
     * @return a new shuffled deck built from the supplied deck's order
     */
    public Deck prepareDeck(Deck initialDeck) {
        List<Card> drawOrder = initialDeck.drawOrder();
        if (randomizedInitial) {
            drawOrder = ShuffleType.RANDOM.strategy().shuffle(drawOrder, seed);
        }
        // type.strategy() may be any ShuffleStrategy; Java runs the shuffle() of the actual strategy object.
        Deck deck = new Deck(type.strategy().shuffle(drawOrder, seed));
        deck.verify();
        return deck;
    }
}
