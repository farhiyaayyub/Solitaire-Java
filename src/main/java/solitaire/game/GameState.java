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
 * The working board: stock, waste, seven tableau columns, and four foundations. It is mutable so the engine and
 * solver can play on it; {@link SolitaireGame} protects its own board by handing out copies only.
 *
 * <p>Type: final class; the board is made of pile objects (composition: a GameState "has" piles).
 * OOP: ENCAPSULATION (private constructor; boards are created only through deal() and copy()),
 * POLYMORPHISM (sourcePile and destinationPile return any kind of pile as a CardPile).
 *
 * @author Farhiya Ayyub
 */
public final class GameState {
    /** Constant: Number of tableau columns. */
    public static final int TABLEAU_COLUMNS = 7;
    /** Constant: Draw mode that turns one card per draw. */
    public static final int DRAW_ONE = 1;
    /** Constant: Draw mode that turns three cards per draw. */
    public static final int DRAW_THREE = 3;

    // Fields: the draw mode and every pile on the board.
    private final int drawCount;
    private final StockPile stock;
    private final WastePile waste;
    private final List<TableauPile> tableau;
    private final Map<Suit, FoundationPile> foundations;

    /** Constructor (private): Builds a board from already-created piles; only deal() and copy() use it. */
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

    /**
     * Method (static): Deals a Draw 1 board without shuffling.
     * Calls: deal(Deck, int)
     */
    public static GameState deal(Deck deck) {
        return deal(deck, DRAW_ONE);
    }

    /**
     * Method (static): Deals the deck without shuffling: columns of 1 to 7 cards left to right with only each top
     * card face up, then the remaining 24 cards into the stock. Waste and foundations start empty.
     * Calls: Deck.draw(), Deck.size(), verify()
     */
    public static GameState deal(Deck deck, int drawCount) {
        List<TableauPile> tableau = new ArrayList<>();
        for (int columnIndex = 0; columnIndex < TABLEAU_COLUMNS; columnIndex++) {
            List<Card> columnCards = new ArrayList<>();
            for (int cardIndex = 0; cardIndex <= columnIndex; cardIndex++) {
                columnCards.add(deck.draw());
            }
            // Object: TableauPile - one per column.
            tableau.add(new TableauPile(columnCards));
        }
        List<Card> stockCards = new ArrayList<>();
        while (deck.size() > 0) {
            stockCards.add(deck.draw());
        }
        Map<Suit, FoundationPile> foundations = new EnumMap<>(Suit.class);
        for (Suit suit : Suit.values()) {
            // Object: FoundationPile - one per suit.
            foundations.put(suit, new FoundationPile(suit));
        }
        // Objects: StockPile, WastePile, and the GameState itself.
        GameState state = new GameState(drawCount, new StockPile(stockCards), new WastePile(), tableau, foundations);
        state.verify();
        return state;
    }

    /** Method: Returns the number of cards turned per draw (1 or 3). */
    public int drawCount() {
        return drawCount;
    }

    /** Method: Returns this board's stock pile. */
    public StockPile stock() {
        return stock;
    }

    /** Method: Returns this board's waste pile. */
    public WastePile waste() {
        return waste;
    }

    /**
     * Method: Returns an unmodifiable list of this board's tableau columns.
     * Calls: Collections.unmodifiableList()
     */
    public List<TableauPile> tableau() {
        return Collections.unmodifiableList(tableau);
    }

    /** Method: Returns this board's foundation for the requested suit. */
    public FoundationPile foundation(Suit suit) {
        return foundations.get(suit);
    }

    /**
     * Method: Returns the pile a move takes cards from, as a general CardPile.
     * Calls: foundation()
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
     * Method: Returns the pile a move puts cards on, as a general CardPile.
     * Calls: foundation()
     */
    public CardPile destinationPile(Move move) {
        return switch (move.type()) {
            case DRAW -> waste;
            case RECYCLE -> stock;
            case TABLEAU_TO_TABLEAU, WASTE_TO_TABLEAU, FOUNDATION_TO_TABLEAU -> tableau.get(move.destination());
            case TABLEAU_TO_FOUNDATION, WASTE_TO_FOUNDATION -> foundation(Suit.values()[move.destination()]);
        };
    }

    /**
     * Method: Returns the total number of cards in the foundations.
     * Calls: CardPile.size()
     */
    public int foundationCount() {
        return foundations.values().stream().mapToInt(CardPile::size).sum();
    }

    /**
     * Method: Reports a win only when all 52 cards are in the foundations and every other pile is empty.
     * Calls: foundationCount(), CardPile.isEmpty()
     */
    public boolean won() {
        return foundationCount() == Deck.CARD_COUNT && stock.isEmpty() && waste.isEmpty()
                && tableau.stream().allMatch(CardPile::isEmpty);
    }

    /**
     * Method: Returns a detached copy of the whole board, including card visibility.
     * Calls: TableauPile.copy(), FoundationPile.copy(), StockPile.copy(), WastePile.copy()
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
        // Object: GameState - a new board built from copies of every pile.
        return new GameState(drawCount, stock.copy(), waste.copy(), tableauCopy, foundationsCopy);
    }

    /**
     * Method: Throws IllegalStateException unless the board holds exactly 52 distinct cards.
     * Calls: CardPile.cards()
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
