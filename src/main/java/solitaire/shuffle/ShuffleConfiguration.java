package solitaire.shuffle;

import solitaire.enums.ShuffleType;

import solitaire.model.Deck;

import java.util.Objects;

/**
 * Stores and applies deck shuffle settings.
 *
 * @author Farhiya Ayyub
 */
public record ShuffleConfiguration(ShuffleType type, int repetitions,
                                   boolean randomizedInitial, long seed) {
    public static final int MAX_OPERATIONS = 1000;

    /** Rejects missing shuffle types and invalid repetition settings. */
    public ShuffleConfiguration {
        Objects.requireNonNull(type);
        if (repetitions < 1 || repetitions > MAX_OPERATIONS)
            throw new IllegalArgumentException("Shuffle count must be 1 to " + MAX_OPERATIONS);
        if (type == ShuffleType.RANDOM && (repetitions != 1 || randomizedInitial))
            throw new IllegalArgumentException("Random mode applies one seeded shuffle to the ordered deck");
    }

    /** Creates and verifies a shuffled deck using this configuration. */
    public Deck prepareDeck() {
        return prepareDeck(new Deck());
    }

    /** Shuffles a copy of the supplied full deck, preserving its file order as the starting order. */
    public Deck prepareDeck(Deck initialDeck) {
        Deck deck = new Deck(initialDeck.drawOrder());
        if (randomizedInitial) deck.shuffle(seed);
        for (int shuffleIndex = 0; shuffleIndex < repetitions; shuffleIndex++) {
            deck.shuffle(type, seed);
        }
        deck.verify();
        return deck;
    }

    /** Returns a readable description of the shuffle configuration. */
    public String description() {
        String operation = switch (type) {
            case IN -> "In-Faro x " + repetitions;
            case OUT -> "Out-Faro x " + repetitions;

            case RANDOM -> "Random shuffle";
        };
        return operation + " | Initial: " + (randomizedInitial ? "randomized" : "ordered")
                + ((randomizedInitial || type == ShuffleType.RANDOM) ? " | Seed: " + seed : "");
    }
}
