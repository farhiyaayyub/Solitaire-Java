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
 * The board of one Klondike game.
 *
 * <p>Kind: final class, made of StockPile, WastePile, TableauPile, and FoundationPile objects.
 * <p>Purpose: it holds the stock, the waste, seven tableau columns, four foundations, and the draw mode. Its piles can
 * change so moves can be applied; SolitaireGame protects its own board by handing out copies only.
 * <p>Data: the draw mode, the stock, the waste, seven TableauPiles in a List (columns are numbered 0-6, so index
 * access fits), and the four foundations in an EnumMap keyed by Suit (one pile per suit, fast and in suit order).
 * <p>OOP: Composition - a board is made of pile objects. Encapsulation - the constructor is private, so a board only
 * comes from deal() or copy(). Polymorphism - sourcePile() and destinationPile() return any pile as a CardPile.
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

    /**
     * Stores already-created piles after checking the draw mode.
     *
     * <p>Called by: deal(Deck, int), copy().
     *
     * @param drawCount cards turned per draw: 1 or 3
     * @param stock the stock pile
     * @param waste the waste pile
     * @param tableau the seven tableau columns
     * @param foundations the four foundations, keyed by suit
     * @throws IllegalArgumentException if drawCount is not 1 or 3
     */
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
     * {@return a new Draw 1 board dealt from the deck}
     *
     * <p>Called by: GameController.manual(), SolvableDealProvider.create().
     *
     * @param deck the deck to deal from, already in the wanted order; it is emptied
     */
    public static GameState deal(Deck deck) {
        return deal(deck, DRAW_ONE);
    }

    /**
     * {@return a new board dealt from the deck in its current order} The deck is emptied, so shuffle it first if
     * needed.
     *
     * <p>Algorithm: Klondike deal.
     * <ol>
     *   <li>Column 1 to 7 gets 1, 2, ..., 7 cards (28 in total); only each top card is face up.</li>
     *   <li>The remaining 24 cards go to the stock, face down.</li>
     *   <li>The waste and the four foundations start empty.</li>
     *   <li>verify() confirms the board holds 52 unique cards.</li>
     * </ol>
     *
     * <p>Called by: GameController.playShuffled(), GameController.simulate(), deal(Deck).
     *
     * @param deck the deck to deal from, already in the wanted order
     * @param drawCount cards turned per draw: 1 or 3
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

    /**
     * {@return the draw mode: 1 or 3 cards per draw}
     *
     * <p>Called by: ConsoleRenderer.render(), MoveGenerator.addLegalMoves(), MoveValidator.isLegalDraw(),
     * ObservableBoard.from().
     */
    public int drawCount() {
        return drawCount;
    }

    /**
     * {@return the real stock pile, not a copy} Changing it changes this board.
     *
     * <p>Called by: SolitaireGame.applyLegalMove(), ConsoleRenderer.render(), MoveGenerator.addLegalMoves(),
     * MoveValidator.isLegal(), MoveValidator.isLegalDraw(), ObservableBoard.from(), SolvableDealProvider.create().
     */
    public StockPile stock() {
        return stock;
    }

    /**
     * {@return the real waste pile, not a copy} Changing it changes this board.
     *
     * <p>Called by: SolitaireGame.applyLegalMove(), ConsoleRenderer.render(), MoveValidator.isLegal(),
     * ObservableBoard.from().
     */
    public WastePile waste() {
        return waste;
    }

    /**
     * {@return the seven columns, index 0 (T1) to 6 (T7)} The list cannot be resized, but each column is the real
     * pile, so its cards can still be moved.
     *
     * <p>Called by: SolitaireGame.applyLegalMove(), ConsoleRenderer.renderTableau(), MoveGenerator.addLegalMoves(),
     * MoveValidator.isLegalTableauTransfer(), ObservableBoard.from().
     */
    public List<TableauPile> tableau() {
        return Collections.unmodifiableList(tableau);
    }

    /**
     * {@return the real foundation pile for the suit}
     *
     * <p>Called by: sourcePile(), destinationPile(), copy(), ConsoleRenderer.render(), ObservableBoard.from().
     *
     * @param suit the suit of the wanted foundation
     */
    public FoundationPile foundation(Suit suit) {
        return foundations.get(suit);
    }

    /**
     * {@return the pile the move takes cards from} A foundation index is turned into its Suit.
     *
     * <p>Called by: SolitaireGame.applyLegalMove(), ConsoleRenderer.describeMove(),
     * MoveValidator.isLegalSingleCardMove().
     *
     * @param move the move to use
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
     * {@return the pile the move puts cards on} A draw goes to the waste and a recycle goes back to the stock.
     *
     * <p>Called by: SolitaireGame.applyLegalMove(), ConsoleRenderer.describeMove(),
     * MoveValidator.isLegalSingleCardMove().
     *
     * @param move the move to use
     */
    public CardPile destinationPile(Move move) {
        return switch (move.type()) {
            case DRAW -> waste;
            case RECYCLE -> stock;
            case TABLEAU_TO_TABLEAU, WASTE_TO_TABLEAU, FOUNDATION_TO_TABLEAU -> tableau.get(move.destination());
            case TABLEAU_TO_FOUNDATION, WASTE_TO_FOUNDATION -> foundation(Suit.values()[move.destination()]);
        };
    }

    /** {@return the total number of cards on the four foundations, 0 to 52} Called by: won(). */
    public int foundationCount() {
        return foundations.values().stream().mapToInt(CardPile::size).sum();
    }

    /**
     * {@return {@code true} only when all 52 cards are on the foundations and every other pile is empty}
     *
     * <p>Called by: GameController.manual(), SolitaireGame(GameState), SolitaireGame.finish(), SolitaireGame.play(),
     * SolvableDealProvider.create(), VisibleSolver.solve().
     */
    public boolean won() {
        return foundationCount() == Deck.CARD_COUNT && stock.isEmpty() && waste.isEmpty()
                && tableau.stream().allMatch(CardPile::isEmpty);
    }

    /**
     * {@return a deep copy of the board, including each card's face-up flag} Moves played on the copy do not affect
     * this board.
     *
     * <p>Called by: SolitaireGame(GameState), SolitaireGame.state(), SolvableDealProvider.VerifiedDeal(),
     * SolvableDealProvider.VerifiedDeal.state(), SolvableDealProvider.create().
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
     * Checks that the board still holds exactly 52 different cards; a failure means a move lost or duplicated a card.
     *
     * <p>Key idea: all cards are collected into one list; the list and a HashSet made from it must both have 52
     * entries, because a duplicate makes the set smaller. This takes O(n) time.
     *
     * <p>Called by: deal(Deck, int), SolitaireGame(GameState), SolitaireGame.play(), SolvableDealProvider.create().
     *
     * @throws IllegalStateException unless the board holds exactly 52 cards and no two are equal
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
