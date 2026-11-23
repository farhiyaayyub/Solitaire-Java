package solitaire.model;

import solitaire.enums.Rank;
import solitaire.enums.Suit;

import java.util.Objects;

/**
 * One playing card. Its suit and rank are fixed when it is created; only its face-up flag changes during play.
 *
 * <p>Encapsulation: the fields are private, and the face-up flag changes only through reveal() and conceal().
 * Polymorphism: it overrides toString(), equals(), and hashCode() from Object.
 *
 * @author Farhiya Ayyub
 */
public final class Card {
    // faceUp starts false, so every new card is face down.
    private final Suit suit;
    private final Rank rank;
    private boolean faceUp;

    /**
     * Creates a face-down card.
     *
     * @param suit the card's suit
     * @param rank the card's rank
     * @throws NullPointerException if suit or rank is {@code null}
     */
    public Card(Suit suit, Rank rank) {
        this.suit = Objects.requireNonNull(suit);
        this.rank = Objects.requireNonNull(rank);
    }

    /**
     * Returns the suit, which never changes.
     *
     * <p>Called by: {@code FoundationPile.accepts()}, {@code ObservableBoard.observe()}.
     *
     * @return the suit, which never changes
     */
    public Suit suit() {
        return suit;
    }

    /**
     * Returns the rank, which never changes.
     *
     * <p>Called by: {@code FoundationPile.accepts()}, {@code TableauPile.accepts()}, {@code TableauPile.fitsBelow()},
     * {@code ObservableBoard.observe()}.
     *
     * @return the rank, which never changes
     */
    public Rank rank() {
        return rank;
    }

    /**
     * Returns {@code true} if the card is face up (visible to the player).
     *
     * <p>Called by: {@code ConsoleRenderer.showCard()}, {@code FoundationPile.accepts()},
     * {@code TableauPile.accepts()}, {@code TableauPile.isValidSequence()}, {@code TableauPile.isOrderedSequence()},
     * {@code ObservableBoard.observe()}.
     *
     * @return {@code true} if the card is face up (visible to the player)
     */
    public boolean isFaceUp() {
        return faceUp;
    }

    /**
     * Returns {@code true} for Hearts and Diamonds; the color comes from the suit.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link Suit#isRed} - the color belongs to the suit</li>
     * </ul>
     *
     * <p>Called by: {@code ConsoleRenderer.showCard()}, {@code TableauPile.fitsBelow()}.
     *
     * @return {@code true} for Hearts and Diamonds
     */
    public boolean isRed() {
        return suit.isRed();
    }

    /**
     * Turns this card face up; has no effect if it is already face up.
     *
     * <p>Called by: {@code TableauPile.revealTop()}, {@code WastePile.receiveDrawn()}.
     */
    public void reveal() {
        faceUp = true;
    }

    /**
     * Turns this card face down; has no effect if it is already face down.
     *
     * <p>Called by: {@code StockPile.StockPile()}, {@code StockPile.recycle()}, {@code TableauPile.TableauPile()}.
     */
    public void conceal() {
        faceUp = false;
    }

    /**
     * Returns a number from 0 to 51 that identifies the card: 13 x suit position + (rank value - 1). For example, the
     * Ace of Hearts is 0 and the King of Spades is 51.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link Rank#value()} - gives the rank position 1 to 13</li>
     * </ul>
     *
     * <p>Called by: {@link #hashCode}.
     *
     * @return a number from 0 to 51 that identifies the card: 13 x suit position + (rank value - 1)
     */
    public int id() {
        return suit.ordinal() * Rank.values().length + (rank.value() - 1);
    }

    /**
     * Returns the code used in card files, such as H-A or S-10. Face-down cards return their real code too.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link Suit#code()} - gives the suit letter</li>
     *   <li>{@link Rank#symbol()} - gives the rank symbol</li>
     * </ul>
     *
     * <p>Called by: {@link #toString}, {@link Deck#Deck}.
     *
     * @return the code used in card files, such as H-A or S-10
     */
    public String code() {
        return suit.code() + "-" + rank.symbol();
    }

    /**
     * Returns a new Card with the same suit, rank, and face-up flag. Turning the copy over does not affect this card,
     * which is why board copies use it.
     *
     * <p>Called by: {@link Deck#Deck}, {@link Deck#drawOrder}, {@code CardPile.cards()},
     * {@code CardPile.copyCardsInto()}, {@code TableauPile.sequenceStart()}.
     *
     * @return a new Card with the same suit, rank, and face-up flag
     */
    public Card copy() {
        Card cardCopy = new Card(suit, rank);
        cardCopy.faceUp = faceUp;
        return cardCopy;
    }

    /**
     * Returns the board text: the code in brackets, such as [H-A], when face up, or [XX] when face down, so printing a
     * card can never reveal a hidden card.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link #code} - gives the text inside the brackets</li>
     * </ul>
     *
     * <p>Called by: {@code ConsoleRenderer.describeMove()}, {@code ConsoleRenderer.showCard()}.
     *
     * @return the board text: the code in brackets, such as [H-A], when face up, or [XX] when face down, so printing a
     *     card can never reveal a hidden card
     */
    @Override
    public String toString() {
        return faceUp ? "[" + code() + "]" : "[XX]";
    }

    /**
     * Returns {@code true} if the other object is a Card with the same suit and rank. The face-up flag is ignored, so a
     * card equals its own copy whether it is face up or face down.
     *
     * <p>Called by: Java's HashSet and HashMap, for example when Deck and CardFileLoader check for duplicate cards.
     *
     * @param other the object to compare with
     * @return {@code true} if the other object is a Card with the same suit and rank
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof Card card && suit == card.suit && rank == card.rank;
    }

    /**
     * Returns id(), so equal cards have equal hashes. This lets HashSet find duplicate cards.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link #id} - gives a unique number per card</li>
     * </ul>
     *
     * <p>Called by: Java's HashSet and HashMap, together with equals().
     *
     * @return id(), so equal cards have equal hashes
     */
    @Override
    public int hashCode() {
        return id();
    }
}
