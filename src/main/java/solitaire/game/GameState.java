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
 * The whole board: the stock, the waste, seven tableau columns, four foundations, and the draw mode. Its piles can
 * change so that moves can be applied to it; {@link SolitaireGame} protects its own board by giving out copies only.
 *
 * <p>Composition: a board is made of pile objects. Encapsulation: the constructor is private, so a board can only come
 * from deal() or copy(). Polymorphism: sourcePile() and destinationPile() return any kind of pile as a CardPile.
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

    // The foundations are keyed by suit; EnumMap keeps them in Suit order and allows only Suit keys.
    private final int drawCount;
    private final StockPile stock;
    private final WastePile waste;
    private final List<TableauPile> tableau;
    private final Map<Suit, FoundationPile> foundations;

    /**
     * Stores already-created piles. It is private so that every board comes from deal() or copy() and therefore starts
     * valid.
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
     * Same as deal(deck, 1): deals a Draw 1 board. The deck is used in its current order.
     *
     * <p>Called by: {@link GameController#manual}, {@code SolvableDealProvider.create()}.
     *
     * @param deck the deck to deal from, already in the wanted order
     * @return the new Draw 1 board
     */
    public static GameState deal(Deck deck) {
        return deal(deck, DRAW_ONE);
    }

    /**
     * Creates a new board from the deck in its current order (shuffle first if needed). The deck is emptied.
     *
     * <p>Algorithm: Klondike deal.
     * <ol>
     *   <li>For column 1 to 7: draw that many cards (1, 2, ..., 7; 28 in total); only the top card is face up.</li>
     *   <li>Put the remaining 24 cards in the stock, face down.</li>
     *   <li>Create an empty waste and four empty foundations.</li>
     *   <li>Check that the board holds 52 unique cards.</li>
     * </ol>
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link Deck#draw} - takes the next card from the deck</li>
     *   <li>{@link Deck#size} - checks how many cards are left for the stock</li>
     *   <li>{@link #verify} - confirms the board has 52 unique cards</li>
     * </ul>
     *
     * <p>Called by: {@link GameController#automatic}, {@code GameController.simulate()}, {@link #deal}.
     *
     * @param deck the deck to deal from, already in the wanted order
     * @param drawCount cards turned per draw: 1 or 3
     * @return the new board, ready to play
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
     * Returns the draw mode: 1 or 3 cards per draw.
     *
     * <p>Called by: {@code ConsoleRenderer.render()}, {@code MoveGenerator.addLegalMoves()},
     * {@code MoveValidator.isLegalDraw()}, {@code ObservableBoard.from()}.
     *
     * @return 1 or 3
     */
    public int drawCount() {
        return drawCount;
    }

    /**
     * Returns the real stock pile, not a copy; changing it changes this board.
     *
     * <p>Called by: {@code SolitaireGame.applyLegalMove()}, {@code ConsoleRenderer.render()},
     * {@code MoveGenerator.addLegalMoves()}, {@code MoveValidator.isLegal()}, {@code MoveValidator.isLegalDraw()},
     * {@code ObservableBoard.from()}, {@code SolvableDealProvider.create()}.
     *
     * @return the real stock pile, not a copy
     */
    public StockPile stock() {
        return stock;
    }

    /**
     * Returns the real waste pile, not a copy; changing it changes this board.
     *
     * <p>Called by: {@code SolitaireGame.applyLegalMove()}, {@code ConsoleRenderer.render()},
     * {@code MoveValidator.isLegal()}, {@code ObservableBoard.from()}.
     *
     * @return the real waste pile, not a copy
     */
    public WastePile waste() {
        return waste;
    }

    /**
     * Returns the seven columns, index 0 (T1) to 6 (T7). Columns cannot be added or removed through this list, but each
     * column is the real pile, so its cards can still be moved.
     *
     * <p>Called by: {@code SolitaireGame.applyLegalMove()}, {@code ConsoleRenderer.renderTableau()},
     * {@code MoveGenerator.addLegalMoves()}, {@code MoveValidator.isLegalTableauTransfer()},
     * {@code ObservableBoard.from()}.
     *
     * @return the seven columns, as a list that cannot be resized
     */
    public List<TableauPile> tableau() {
        return Collections.unmodifiableList(tableau);
    }

    /**
     * Returns the real foundation pile for the suit.
     *
     * <p>Called by: {@link #sourcePile}, {@link #destinationPile}, {@link #copy}, {@code ConsoleRenderer.render()},
     * {@code ObservableBoard.from()}.
     *
     * @param suit the suit of the wanted foundation
     * @return the real foundation pile for the suit
     */
    public FoundationPile foundation(Suit suit) {
        return foundations.get(suit);
    }

    /**
     * Returns the pile the move takes cards from, typed as CardPile so callers can treat every pile the same way. A
     * foundation index is converted to its Suit.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link #foundation} - finds the foundation for the suit index</li>
     * </ul>
     *
     * <p>Called by: {@code SolitaireGame.applyLegalMove()}, {@code ConsoleRenderer.describeMove()},
     * {@code MoveValidator.isLegalSingleCardMove()}.
     *
     * @param move the move to use
     * @return the pile the move takes cards from, typed as CardPile so callers can treat every pile the same way
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
     * Returns the pile the move puts cards on, typed as CardPile. A draw goes to the waste and a recycle goes to the
     * stock.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link #foundation} - finds the foundation for the suit index</li>
     * </ul>
     *
     * <p>Called by: {@code SolitaireGame.applyLegalMove()}, {@code ConsoleRenderer.describeMove()},
     * {@code MoveValidator.isLegalSingleCardMove()}.
     *
     * @param move the move to use
     * @return the pile the move puts cards on, typed as CardPile
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
     * Returns the total number of cards on the four foundations, from 0 to 52.
     *
     * <p>Called by: {@link #won}.
     *
     * @return the total number of cards on the four foundations, from 0 to 52
     */
    public int foundationCount() {
        return foundations.values().stream().mapToInt(CardPile::size).sum();
    }

    /**
     * Returns {@code true} only when all 52 cards are on the foundations and the stock, waste, and every column are
     * empty.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link #foundationCount} - checks that all 52 cards are home</li>
     *   <li>{@link CardPile#isEmpty} - checks that every other pile is empty</li>
     * </ul>
     *
     * <p>Called by: {@link GameController#manual}, {@link SolitaireGame#SolitaireGame}, {@link SolitaireGame#finish},
     * {@link SolitaireGame#play}, {@code SolvableDealProvider.create()}, {@code VisibleSolver.solve()}.
     *
     * @return {@code true} if the game is won
     */
    public boolean won() {
        return foundationCount() == Deck.CARD_COUNT && stock.isEmpty() && waste.isEmpty()
                && tableau.stream().allMatch(CardPile::isEmpty);
    }

    /**
     * Returns a new board with copies of every pile and card, including face-up flags. Moves played on the copy do not
     * affect this board.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link TableauPile#copy} - copies each column</li>
     *   <li>{@link #foundation} - gets each foundation to copy</li>
     *   <li>{@link FoundationPile#copy} - copies each foundation</li>
     *   <li>{@link StockPile#copy} - copies the stock</li>
     *   <li>{@link WastePile#copy} - copies the waste</li>
     * </ul>
     *
     * <p>Called by: {@link SolitaireGame#SolitaireGame}, {@link SolitaireGame#state()},
     * {@code SolvableDealProvider.VerifiedDeal.state()}, {@code SolvableDealProvider.create()}.
     *
     * @return a new board with copies of every pile and card, including face-up flags
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
     * Checks that the board still holds exactly 52 different cards. A failure means a move lost or duplicated a card.
     *
     * <p>Algorithm: uniqueness check with a hash set, O(n).
     * <ol>
     *   <li>Collect the cards of the stock, waste, columns, and foundations into one list.</li>
     *   <li>The list must have 52 cards, and a HashSet of it must also have 52 (a duplicate makes it smaller).</li>
     * </ol>
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link CardPile#cards()} - collects the cards of every pile</li>
     * </ul>
     *
     * <p>Called by: {@link #deal}, {@link SolitaireGame#SolitaireGame}, {@link SolitaireGame#play},
     * {@code SolvableDealProvider.create()}.
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
