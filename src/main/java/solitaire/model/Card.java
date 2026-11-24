package solitaire.model;

import solitaire.enums.Rank;
import solitaire.enums.Suit;

import java.util.Objects;

/**
 * One playing card.
 *
 * <p>Kind: final class.
 * <p>Purpose: it represents a single card on the board; every pile, the deck, and the solver work with Card objects.
 * <p>Data: a Suit and a Rank as final enum fields (fixed for the card's life, and an enum allows only valid values),
 * plus a boolean faceUp, the only thing that changes during play.
 * <p>OOP: Encapsulation - the fields are private, and faceUp changes only through reveal() and conceal().
 * Polymorphism - it overrides toString(), equals(), and hashCode() from Object.
 *
 * @author Farhiya Ayyub
 */
public final class Card {
    private final Suit suit;
    private final Rank rank;
    private boolean faceUp;

    /**
     * Creates a face-down card.
     *
     * <p>Called by: CardFileLoader.parseCard(), Deck(), SolvableDealProvider.ascendingDrawOrder(), copy().
     *
     * @param suit the card's suit
     * @param rank the card's rank
     * @throws NullPointerException if suit or rank is {@code null}
     */
    public Card(Suit suit, Rank rank) {
        this.suit = Objects.requireNonNull(suit);
        this.rank = Objects.requireNonNull(rank);
    }

    /** {@return the suit} Called by: FoundationPile.accepts(), ObservableBoard.observe(). */
    public Suit suit() {
        return suit;
    }

    /**
     * {@return the rank}
     *
     * <p>Called by: FoundationPile.accepts(), TableauPile.accepts(), TableauPile.fitsBelow(), ObservableBoard.observe().
     */
    public Rank rank() {
        return rank;
    }

    /**
     * {@return {@code true} if the card is face up (visible to the player)}
     *
     * <p>Called by: ConsoleRenderer.showCard(), FoundationPile.accepts(), TableauPile.accepts(),
     * TableauPile.isValidSequence(), TableauPile.isOrderedSequence(), ObservableBoard.observe().
     */
    public boolean isFaceUp() {
        return faceUp;
    }

    /** {@return {@code true} for Hearts and Diamonds} Called by: ConsoleRenderer.showCard(), TableauPile.fitsBelow(). */
    public boolean isRed() {
        return suit.isRed();
    }

    /**
     * Turns this card face up; does nothing if it is already face up.
     *
     * <p>Called by: TableauPile.revealTop(), WastePile.receiveDrawn().
     */
    public void reveal() {
        faceUp = true;
    }

    /**
     * Turns this card face down; does nothing if it is already face down.
     *
     * <p>Called by: StockPile(List), StockPile.recycle(), TableauPile(List).
     */
    public void conceal() {
        faceUp = false;
    }

    /**
     * {@return a unique number from 0 to 51: 13 x suit position + (rank value - 1)} So the Ace of Hearts is 0 and the
     * King of Spades is 51.
     *
     * <p>Called by: hashCode().
     */
    public int id() {
        return suit.ordinal() * Rank.values().length + (rank.value() - 1);
    }

    /**
     * {@return the code used in card files, such as H-A or S-10} Face-down cards return their real code too.
     *
     * <p>Called by: toString(), Deck(List).
     */
    public String code() {
        return suit.code() + "-" + rank.symbol();
    }

    /**
     * {@return a new Card with the same suit, rank, and face-up flag} Turning the copy over does not affect this card,
     * which is why board copies and read-only views use it.
     *
     * <p>Called by: Deck(List), Deck.drawOrder(), CardPile.get(), CardPile.cards(), CardPile.top(),
     * CardPile.copyCardsInto(), TableauPile.sequenceStart().
     */
    public Card copy() {
        Card cardCopy = new Card(suit, rank);
        cardCopy.faceUp = faceUp;
        return cardCopy;
    }

    /**
     * {@return the board text: the code in brackets, such as [H-A], when face up, or [XX] when face down} So printing a
     * card can never reveal a hidden card.
     *
     * <p>Called by: ConsoleRenderer.describeMove(), ConsoleRenderer.showCard().
     */
    @Override
    public String toString() {
        return faceUp ? "[" + code() + "]" : "[XX]";
    }

    /**
     * {@return {@code true} if the other object is a Card with the same suit and rank} The face-up flag is ignored, so
     * a card equals its own copy whether it is face up or face down.
     *
     * <p>Called by: HashSet, in Deck(List), Deck.verify(), CardFileLoader.load(), and GameState.verify().
     *
     * @param other the object to compare with
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof Card card && suit == card.suit && rank == card.rank;
    }

    /**
     * {@return id(), so equal cards have equal hashes, as equals() requires} This lets HashSet find duplicate cards.
     *
     * <p>Called by: HashSet, together with equals().
     */
    @Override
    public int hashCode() {
        return id();
    }
}
