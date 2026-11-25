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
 * The whole board of one Klondike game: the stock, the waste, the seven tableau columns, the four foundations, and the
 * draw mode (1 or 3). It can deal a new board from a deck, make a full copy of itself, and check that no card was lost.
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
        return deal(deck, DRAW_ONE);
    }

    /**
     * Deals a new board from the deck: 1 card to column 1, 2 cards to column 2, and so on up to 7 cards, with only the
     * top card of each column face up. The remaining 24 cards go to the stock, and the four foundations start empty.
     */
    public static GameState deal(Deck deck, int drawCount) {
        List<TableauPile> tableau = new ArrayList<>();
        for (int columnIndex = 0; columnIndex < TABLEAU_COLUMNS; columnIndex++) {
            List<Card> columnCards = new ArrayList<>();
            for (int cardIndex = 0; cardIndex <= columnIndex; cardIndex++) {
                columnCards.add(deck.draw());
            }
            tableau.add(new TableauPile(columnCards));
        }
        List<Card> stockCards = new ArrayList<>();
        while (deck.size() > 0) {
            stockCards.add(deck.draw());
        }
        Map<Suit, FoundationPile> foundations = new EnumMap<>(Suit.class);
        for (Suit suit : Suit.values()) {
            foundations.put(suit, new FoundationPile(suit));
        }
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
            case DRAW -> stock;
            case RECYCLE, WASTE_TO_TABLEAU, WASTE_TO_FOUNDATION -> waste;
            case TABLEAU_TO_TABLEAU, TABLEAU_TO_FOUNDATION -> tableau.get(move.source());
            case FOUNDATION_TO_TABLEAU -> foundation(Suit.values()[move.source()]);
        };
    }

    /**
     * Returns the pile a move puts cards on, based on its type: the waste for a draw, the stock for a recycle, a
     * column, or a foundation.
     */
    public CardPile destinationPile(Move move) {
        return switch (move.type()) {
            case DRAW -> waste;
            case RECYCLE -> stock;
            case TABLEAU_TO_TABLEAU, WASTE_TO_TABLEAU, FOUNDATION_TO_TABLEAU -> tableau.get(move.destination());
            case TABLEAU_TO_FOUNDATION, WASTE_TO_FOUNDATION -> foundation(Suit.values()[move.destination()]);
        };
    }

    /** Adds up the cards on all four foundations and returns the total, from 0 to 52. */
    public int foundationCount() {
        return foundations.values().stream().mapToInt(CardPile::size).sum();
    }

    /** Returns true when all 52 cards are on the foundations and the stock, the waste, and every column are empty. */
    public boolean won() {
        return foundationCount() == Deck.CARD_COUNT && stock.isEmpty() && waste.isEmpty()
                && tableau.stream().allMatch(CardPile::isEmpty);
    }

    /**
     * Makes a full copy of the board, copying every pile and card. Moves played on the copy do not change the original,
     * so the solver can play without touching the real game.
     */
    public GameState copy() {
        List<TableauPile> tableauCopy = new ArrayList<>();
        for (TableauPile pile : tableau) {
            tableauCopy.add(pile.copy());
        }
        Map<Suit, FoundationPile> foundationsCopy = new EnumMap<>(Suit.class);
        for (Suit suit : Suit.values()) {
            foundationsCopy.put(suit, foundation(suit).copy());
        }
        return new GameState(drawCount, stock.copy(), waste.copy(), tableauCopy, foundationsCopy);
    }

    /**
     * Collects the cards from every pile and checks that there are exactly 52 and none appears twice. Throws an error
     * if a card was lost or duplicated.
     */
    public void verify() {
        List<Card> allCards = new ArrayList<>(stock.cards());
        allCards.addAll(waste.cards());
        for (TableauPile pile : tableau) {
            allCards.addAll(pile.cards());
        }
        for (FoundationPile pile : foundations.values()) {
            allCards.addAll(pile.cards());
        }
        if (allCards.size() != Deck.CARD_COUNT || new HashSet<>(allCards).size() != Deck.CARD_COUNT) {
            throw new IllegalStateException("Card conservation violated");
        }
    }
}
