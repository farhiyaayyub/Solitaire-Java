package solitaire.shuffle;

import java.util.List;
import java.util.Objects;

import solitaire.model.Card;
import solitaire.model.Deck;

/**
 * The player's shuffle settings.
 *
 * <p>Kind: record.
 * <p>Purpose: it stores which shuffle to use and whether to randomize first, and turns a deck into a shuffled deck
 * with those settings.
 * <p>Data: two record fields, a ShuffleType and a boolean; a record is used so the settings cannot change after they
 * are checked.
 * <p>OOP: Encapsulation - the compact constructor rejects invalid settings, so no invalid configuration can exist.
 * Polymorphism - prepareDeck() works with any ShuffleStrategy.
 *
 * @param type              the selected shuffle
 * @param randomizedInitial true to apply a random shuffle before the Faro shuffle (Faro shuffles only), because a
 *                          perfect shuffle of a sorted deck always gives the same predictable deal
 * @author Farhiya Ayyub
 */
public record ShuffleConfiguration(ShuffleType type, boolean randomizedInitial) {

    /**
     * Checks the settings: only a Faro shuffle may start from a randomized deck.
     *
     * <p>Called by: GameController.simulate(), GameController.compare(), GameController.manual(),
     * ConsoleInput.shuffleConfiguration(), SolitaireApplication.run().
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
     * {@return a new sorted deck shuffled with these settings}
     *
     * <p>Called by: GameController.manual(), GameController.shuffledDecks().
     */
    public Deck prepareDeck() {
        return prepareDeck(new Deck());
    }

    /**
     * {@return a new shuffled deck built from the supplied deck's order} Steps: copy the order, apply a random shuffle
     * first if randomizedInitial, apply the chosen strategy, then check that the result still has 52 unique cards.
     *
     * <p>Key idea: the shuffled cards go into a new Deck, whose constructor rejects a duplicate or a count other than
     * 52, and verify() checks again. So a faulty shuffle is caught here, before any card is dealt.
     *
     * <p>Called by: GameController.automatic(), GameController.compare(), prepareDeck().
     *
     * @param initialDeck the deck to start from; it is not changed
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
