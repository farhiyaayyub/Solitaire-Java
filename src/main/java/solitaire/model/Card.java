package solitaire.model;

import solitaire.enums.Rank;
import solitaire.enums.Suit;

import java.util.Objects;

/**
 * A card with a fixed suit and rank and a face-up flag that can change during play.
 *
 * <p>Type: final class; every pile and deck holds Card objects.
 * OOP: ENCAPSULATION (private fields; faceUp changes only through reveal() and conceal()),
 * POLYMORPHISM (overrides toString, equals, and hashCode from Object).
 *
 * @author Farhiya Ayyub
 */
public final class Card {
    // Fields: suit and rank never change; faceUp changes when the card is turned over.
    private final Suit suit;
    private final Rank rank;
    private boolean faceUp;

    /**
     * Constructor: Creates a card with the supplied suit and rank, face down.
     * Calls: Objects.requireNonNull()
     */
    public Card(Suit suit, Rank rank) {
        this.suit = Objects.requireNonNull(suit);
        this.rank = Objects.requireNonNull(rank);
    }

    /** Method: Returns this card's suit. */
    public Suit suit() {
        return suit;
    }

    /** Method: Returns this card's rank. */
    public Rank rank() {
        return rank;
    }

    /** Method: Reports whether the card is face up. */
    public boolean isFaceUp() {
        return faceUp;
    }

    /**
     * Method: Reports whether the card is red.
     * Calls: Suit.isRed()
     */
    public boolean isRed() {
        return suit.isRed();
    }

    /** Method: Turns this card face up. */
    public void reveal() {
        faceUp = true;
    }

    /** Method: Turns this card face down. */
    public void conceal() {
        faceUp = false;
    }

    /**
     * Method: Returns the stable zero-based identity (0 to 51) derived from suit and rank.
     * Calls: Rank.value()
     */
    public int id() {
        return suit.ordinal() * Rank.values().length + (rank.value() - 1);
    }

    /**
     * Method: Returns the suit-rank code used in card files, such as H-A, whether or not the card is face up.
     * Calls: Suit.code(), Rank.symbol()
     */
    public String code() {
        return suit.code() + "-" + rank.symbol();
    }

    /** Method: Returns a detached copy, including card visibility. */
    public Card copy() {
        // Object: Card - a new card with the same suit, rank, and visibility.
        Card cardCopy = new Card(suit, rank);
        cardCopy.faceUp = faceUp;
        return cardCopy;
    }

    /**
     * Method (override): Board display: the code in brackets when face up, or [XX] when face down.
     * Calls: code()
     */
    @Override
    public String toString() {
        return faceUp ? "[" + code() + "]" : "[XX]";
    }

    /** Method (override): Compares card identity by suit and rank; visibility does not change identity. */
    @Override
    public boolean equals(Object other) {
        return other instanceof Card card && suit == card.suit && rank == card.rank;
    }

    /**
     * Method (override): Returns a stable identity hash even when the card is revealed or concealed.
     * Calls: id()
     */
    @Override
    public int hashCode() {
        return id();
    }
}
