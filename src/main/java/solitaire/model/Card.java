package solitaire.model;

import solitaire.enums.Rank;
import solitaire.enums.Suit;

import java.util.Objects;

/**
 * A card with immutable suit/rank identity and mutable face-up visibility.
 * @author Farhiya Ayyub
 */
public final class Card {
    private final Suit suit;
    private final Rank rank;
    private boolean faceUp;

    /** Creates a card with the supplied suit and rank, face down. */
    public Card(Suit suit, Rank rank) {
        this.suit = Objects.requireNonNull(suit);
        this.rank = Objects.requireNonNull(rank);
    }

    /** Returns this card's suit. */
    public Suit suit() {
        return suit;
    }

    /** Returns this card's rank. */
    public Rank rank() {
        return rank;
    }

    /** Reports whether the card is visible. */
    public boolean isFaceUp() {
        return faceUp;
    }

    /** Reports whether the suit is red. */
    public boolean isRed() {
        return suit.isRed();
    }

    /** Turns this card face up. */
    public void reveal() {
        faceUp = true;
    }

    /** Turns this card face down. */
    public void conceal() {
        faceUp = false;
    }

    /** Returns the stable zero-based identity derived from suit and rank. */
    public int id() {
        return suit.ordinal() * Rank.values().length + (rank.getValue() - 1);
    }

    /** Returns a detached copy, including card visibility. */
    public Card copy() {
        Card cardCopy = new Card(suit, rank);
        cardCopy.faceUp = faceUp;
        return cardCopy;
    }

    /** Returns a readable description for console output. */
    @Override
    public String toString() {
        return faceUp ? "[" + suit.code() + "-" + rank.symbol() + "]" : "[XX]";
    }

    /** Compares card identity by suit and rank; visibility does not change identity. */
    @Override
    public boolean equals(Object other) {
        return other instanceof Card card && suit == card.suit && rank == card.rank;
    }

    /** Returns a stable identity hash even when the card is revealed or concealed. */
    @Override
    public int hashCode() {
        return id();
    }
}
