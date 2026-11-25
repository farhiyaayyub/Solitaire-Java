package solitaire.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import solitaire.enums.Suit;
import solitaire.model.Card;
import solitaire.model.Deck;
import solitaire.move.Move;
import solitaire.pile.CardPile;
import solitaire.pile.FoundationPile;
import solitaire.pile.StockPile;
import solitaire.pile.TableauPile;
import solitaire.pile.WastePile;

/**
 * The whole board of one game: the stock, the waste, the seven tableau columns, and the four foundations. It deals a
 * new board, copies it, checks for a win, and checks that no card is lost.
 *
 * @author Farhiya Ayyub
 */
public final class GameState {
    /** Klondike has 7 tableau columns. */
    public static final int TABLEAU_COLUMNS = 7;
    /** Draw mode where each draw turns 1 card. */
    public static final int DRAW_ONE = 1;
    /** Draw mode where each draw turns 3 cards. */
    public static final int DRAW_THREE = 3;

    private final int drawCount;
    private final StockPile stock;
    private final WastePile waste;
    private final List<TableauPile> tableau;
    private final Map<Suit, FoundationPile> foundations;

    /** Stores the piles of a board after checking that the draw mode is 1 or 3. */
    private GameState(int drawCount, StockPile stock, WastePile waste, List<TableauPile> tableau,
                      Map<Suit, FoundationPile> foundations) {
        // Only Draw 1 and Draw 3 exist; any other number stops with an error.
        if (drawCount != DRAW_ONE && drawCount != DRAW_THREE) {
            throw new IllegalArgumentException("Draw must be 1 or 3");
        }
        this.drawCount = drawCount;
        this.stock = stock;
        this.waste = waste;
        this.tableau = tableau;
        this.foundations = foundations;
    }

    /** Deals a new Draw 1 board from the deck. */
    public static GameState deal(Deck deck) {
        // Same as the deal below, with the draw mode set to 1.
        return deal(deck, DRAW_ONE);
    }

    /**
     * Deals a new board from the deck: 1 card to column 1, 2 cards to column 2, and so on up to 7 cards, with only the
     * top card of each column face up. The remaining 24 cards go to the stock, and the four foundations start empty.
     */
    public static GameState deal(Deck deck, int drawCount) {
        // Step 1: fill the 7 columns; column 1 gets 1 card, column 2 gets 2 cards, ..., column 7 gets 7 cards.
        List<TableauPile> tableau = new ArrayList<>();
        for (int columnIndex = 0; columnIndex < TABLEAU_COLUMNS; columnIndex++) {
            List<Card> columnCards = new ArrayList<>();
            for (int cardIndex = 0; cardIndex <= columnIndex; cardIndex++) {
                columnCards.add(deck.draw());
            }
            // TableauPile turns the cards face down and only the top card face up.
            tableau.add(new TableauPile(columnCards));
        }
        // Step 2: every card still in the deck (24 cards) goes to the stock.
        List<Card> stockCards = new ArrayList<>();
        while (deck.size() > 0) {
            stockCards.add(deck.draw());
        }
        // Step 3: make one empty foundation for each suit.
        Map<Suit, FoundationPile> foundations = new EnumMap<>(Suit.class);
        for (Suit suit : Suit.values()) {
            foundations.put(suit, new FoundationPile(suit));
        }
        // Step 4: build the board with an empty waste, then check that all 52 cards are on it.
        GameState state = new GameState(drawCount, new StockPile(stockCards), new WastePile(), tableau, foundations);
        state.verify();
        return state;
    }

    /** Returns how many cards one draw turns over: 1 or 3. */
    public int drawCount() {
        return drawCount;
    }

    /** Returns the stock pile itself, not a copy, so changing it changes this board. */
    public StockPile stock() {
        return stock;
    }

    /** Returns the waste pile itself, not a copy, so changing it changes this board. */
    public WastePile waste() {
        return waste;
    }

    /**
     * Returns the seven tableau columns, index 0 (T1) to 6 (T7). The list cannot be resized, but the columns in it are
     * the real piles.
     */
    public List<TableauPile> tableau() {
        // Wrap the list so no column can be added or removed from outside.
        return Collections.unmodifiableList(tableau);
    }

    /** Returns the foundation pile of the given suit. */
    public FoundationPile foundation(Suit suit) {
        return foundations.get(suit);
    }

    /**
     * Returns the pile a move takes cards from, based on its type: the stock for a draw, the waste for a waste move or
     * a recycle, a column for a tableau move, or a foundation.
     */
    public CardPile sourcePile(Move move) {
        return switch (move.type()) {
            // A draw takes cards from the stock.
            case DRAW -> stock;
            // A recycle and every waste move take cards from the waste.
            case RECYCLE, WASTE_TO_TABLEAU, WASTE_TO_FOUNDATION -> waste;
            // A tableau move takes cards from column number move.source().
            case TABLEAU_TO_TABLEAU, TABLEAU_TO_FOUNDATION -> tableau.get(move.source());
            // A foundation move takes the card from a foundation; the number 0 to 3 is turned into its suit.
            case FOUNDATION_TO_TABLEAU -> foundation(Suit.values()[move.source()]);
        };
    }

    /**
     * Returns the pile a move puts cards on, based on its type: the waste for a draw, the stock for a recycle, a
     * column, or a foundation.
     */
    public CardPile destinationPile(Move move) {
        return switch (move.type()) {
            // A draw puts cards on the waste.
            case DRAW -> waste;
            // A recycle puts the waste cards back on the stock.
            case RECYCLE -> stock;
            // A move that ends on the tableau goes to column number move.destination().
            case TABLEAU_TO_TABLEAU, WASTE_TO_TABLEAU, FOUNDATION_TO_TABLEAU -> tableau.get(move.destination());
            // A move to a foundation goes to the foundation of suit number move.destination().
            case TABLEAU_TO_FOUNDATION, WASTE_TO_FOUNDATION -> foundation(Suit.values()[move.destination()]);
        };
    }

    /** Adds up the cards on all four foundations and returns the total, from 0 to 52. */
    public int foundationCount() {
        // Take the size of each foundation and add them together.
        return foundations.values().stream().mapToInt(CardPile::size).sum();
    }

    /** Returns true when all 52 cards are on the foundations and the stock, the waste, and every column are empty. */
    public boolean won() {
        // All 52 cards must be on the foundations, and every other pile must be empty.
        return foundationCount() == Deck.CARD_COUNT && stock.isEmpty() && waste.isEmpty()
                && tableau.stream().allMatch(CardPile::isEmpty);
    }

    /**
     * Makes a full copy of the board, copying every pile and card. Moves played on the copy do not change the original,
     * so the solver can play without touching the real game.
     */
    public GameState copy() {
        // Copy each of the 7 columns.
        List<TableauPile> tableauCopy = new ArrayList<>();
        for (TableauPile pile : tableau) {
            tableauCopy.add(pile.copy());
        }
        // Copy each of the 4 foundations.
        Map<Suit, FoundationPile> foundationsCopy = new EnumMap<>(Suit.class);
        for (Suit suit : Suit.values()) {
            foundationsCopy.put(suit, foundation(suit).copy());
        }
        // Build a new board from the copied piles, with copies of the stock and the waste.
        return new GameState(drawCount, stock.copy(), waste.copy(), tableauCopy, foundationsCopy);
    }

    /**
     * Collects the cards from every pile and checks that there are exactly 52 and none appears twice. Throws an error
     * if a card was lost or duplicated.
     */
    public void verify() {
        // Put the cards of every pile into one list.
        List<Card> allCards = new ArrayList<>(stock.cards());
        allCards.addAll(waste.cards());
        for (TableauPile pile : tableau) {
            allCards.addAll(pile.cards());
        }
        for (FoundationPile pile : foundations.values()) {
            allCards.addAll(pile.cards());
        }
        // There must be 52 cards, and a set of them must also have 52 (so none is repeated).
        if (allCards.size() != Deck.CARD_COUNT || new HashSet<>(allCards).size() != Deck.CARD_COUNT) {
            throw new IllegalStateException("Card conservation violated");
        }
    }
}
