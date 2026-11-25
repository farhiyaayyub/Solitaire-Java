package solitaire.model;

import solitaire.enums.Rank;
import solitaire.enums.Suit;

import java.util.Objects;

/**
 * One playing card with a suit, a rank, and a side that is face up or face down. Two cards are equal when they have the
 * same suit and rank.
 *
 * @author Farhiya Ayyub
 */
public final class Card {
    private final Suit suit;
    private final Rank rank;
    private boolean faceUp;

    /** Creates a face-down card of the given suit and rank. */
    public Card(Suit suit, Rank rank) {
        this.suit = Objects.requireNonNull(suit);
        this.rank = Objects.requireNonNull(rank);
    }

    /** Returns the card's suit. */
    public Suit suit() {
        return suit;
    }

    /** Returns the card's rank. */
    public Rank rank() {
        return rank;
    }

    /** Returns true if the card is face up, so the player can see it. */
    public boolean isFaceUp() {
        return faceUp;
    }

    /** Returns true if the card is a Heart or a Diamond. */
    public boolean isRed() {
        return suit.isRed();
    }

    /** Turns the card face up. */
    public void reveal() {
        faceUp = true;
    }

    /** Turns the card face down. */
    public void conceal() {
        faceUp = false;
    }

    /**
     * Returns a unique number from 0 to 51 for this suit and rank, calculated as 13 x suit position + (rank value - 1).
     */
    public int id() {
        return suit.ordinal() * Rank.values().length + (rank.value() - 1);
    }

    /** Returns the card's code as used in card files, such as H-A or S-10. */
    public String code() {
        return suit.code() + "-" + rank.symbol();
    }

    /** Returns a new card with the same suit, rank, and face-up side. */
    public Card copy() {
        Card cardCopy = new Card(suit, rank);
        cardCopy.faceUp = faceUp;
        return cardCopy;
    }

    /**
     * Returns how the card is shown on the board: its code in brackets, such as [H-A], when face up, or [XX] when face
     * down.
     */
    @Override
    public String toString() {
        return faceUp ? "[" + code() + "]" : "[XX]";
    }

    /**
     * Returns true if the other object is a card with the same suit and rank; whether it is face up does not matter.
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof Card card && suit == card.suit && rank == card.rank;
    }

    /** Returns id() as the hash code, so equal cards always have the same hash in a HashSet or HashMap. */
    @Override
    public int hashCode() {
        return id();
    }
}
