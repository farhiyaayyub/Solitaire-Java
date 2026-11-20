package solitaire.game;

import java.util.EnumMap;

import solitaire.model.Card;
import solitaire.model.Deck;
import solitaire.enums.Suit;
import solitaire.pile.FoundationPile;
import solitaire.pile.StockPile;
import solitaire.pile.TableauPile;
import solitaire.pile.WastePile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mutable working board; SolitaireGame copies it at ownership boundaries.
 * @author Farhiya Ayyub
 */
public final class GameState {
    public static final int TABLEAU_COLUMNS = 7;
    public static final int DRAW_ONE = 1;
    public static final int DRAW_THREE = 3;
    private final int drawCount;
    private StockPile stock = new StockPile();
    private WastePile waste = new WastePile();
    private final List<TableauPile> tableau = new ArrayList<>();
    private final EnumMap<Suit, FoundationPile> foundations = new EnumMap<>(Suit.class);

    /** Creates an empty Draw 1 board. */
    public GameState() {
        this(DRAW_ONE);
    }

    /** Creates an empty board that draws one or three cards at a time. */
    public GameState(int drawCount) {
        if (drawCount != DRAW_ONE && drawCount != DRAW_THREE) throw new IllegalArgumentException("Draw must be 1 or 3");
        this.drawCount = drawCount;
        for (int columnIndex = 0; columnIndex < TABLEAU_COLUMNS; columnIndex++) tableau.add(new TableauPile());
        for (Suit suit : Suit.values()) foundations.put(suit, new FoundationPile(suit));
    }

    /** Deals cards into the initial layout without shuffling. */
    public static GameState deal(Deck deck) {
        return deal(deck, DRAW_ONE);
    }

    /** Deals cards into the initial layout without shuffling. */
    public static GameState deal(Deck deck, int drawCount) {
        GameState state = new GameState(drawCount);
        for (int columnIndex = 0; columnIndex < TABLEAU_COLUMNS; columnIndex++) {
            for (int cardIndex = 0; cardIndex <= columnIndex; cardIndex++) {
                Card card = deck.draw();
                card.conceal();
                state.tableau.get(columnIndex).deal(card);
            }
            state.tableau.get(columnIndex).revealTop();
        }
        while (deck.size() > 0) state.stock.addBottom(deck.draw());
        state.verify();
        return state;
    }

    /** Returns this working board's mutable stock pile. */
    public StockPile stock() {
        return stock;
    }

    /** Returns the selected draw count; console input uses zero to signal cancellation. */
    public int drawCount() {
        return drawCount;
    }

    /** Returns this working board's mutable waste pile. */
    public WastePile waste() {
        return waste;
    }

    /** Returns an unmodifiable list of mutable tableau piles belonging to this working board. */
    public List<TableauPile> tableau() {
        return Collections.unmodifiableList(tableau);
    }

    /** Returns this working board's mutable foundation for the requested suit. */
    public FoundationPile foundation(Suit suit) {
        return foundations.get(suit);
    }

    /** Returns the total number of cards placed in foundations. */
    public int foundationCount() {
        return foundations.values().stream().mapToInt(FoundationPile::size).sum();
    }

    /** Reports victory only when all cards are in foundations and every other pile is empty. */
    public boolean won() {
        return foundationCount() == Deck.CARD_COUNT && stock.size() == 0 && waste.size() == 0 && tableau.stream().allMatch(pile -> pile.size() == 0);
    }

    /** Returns a detached copy, including card visibility. */
    public GameState copy() {
        GameState stateCopy = new GameState(drawCount);
        stateCopy.stock = stock.copy();
        stateCopy.waste = waste.copy();
        stateCopy.tableau.clear();
        for (TableauPile pile : tableau) stateCopy.tableau.add(pile.copy());
        for (Suit suit : Suit.values()) stateCopy.foundations.put(suit, foundation(suit).copy());
        return stateCopy;
    }

    /** Adds a pile's card identities and visibility to a board key. */
    private void append(StringBuilder keyBuilder, List<Card> cards) {
        for (Card card : cards) {
            keyBuilder.append(card.id()).append(card.isFaceUp() ? '+' : '-').append(',');
        }
        keyBuilder.append(' ');
    }

    /** Encodes draw mode, card order, and visibility for snapshot comparisons. */
    public String key() {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(drawCount).append(':');
        append(keyBuilder, stock.cards());
        append(keyBuilder, waste.cards());
        for (TableauPile pile : tableau) append(keyBuilder, pile.cards());
        for (Suit suit : Suit.values()) append(keyBuilder, foundation(suit).cards());
        return keyBuilder.toString();
    }

    /** Throws IllegalStateException unless exactly 52 distinct card identities are present. */
    public void verify() {
        List<Card> allCards = new ArrayList<>(stock.cards());
        allCards.addAll(waste.cards());
        for (TableauPile pile : tableau) allCards.addAll(pile.cards());
        for (Suit suit : Suit.values()) allCards.addAll(foundation(suit).cards());
        if (allCards.size() != Deck.CARD_COUNT || new java.util.HashSet<>(allCards).size() != Deck.CARD_COUNT)
            throw new IllegalStateException("Card conservation violated");
    }
}
