package solitaire.solver;

import java.util.Arrays;
import java.util.List;

import solitaire.enums.Suit;
import solitaire.game.GameState;
import solitaire.model.Card;

/**
 * The board as a player sees it.
 *
 * <p>Kind: record, contains the record VisibleCard; used by VisibleSolver.
 * <p>Purpose: it shows face-up cards and replaces face-down cards with a hidden marker, so the solver plays fairly.
 * Two views with the same visible cards are equal, so a view works as a Map key to recognise a position.
 * <p>Data: record fields: two ints, the waste and the tableau as Lists of VisibleCard, and the foundation sizes as a
 * List of Integer. All lists are List.copyOf copies, so the view is immutable and its hashCode never changes.
 * <p>OOP: Abstraction - it hides face-down cards from the solver. Encapsulation - a view cannot change after it is
 * made.
 *
 * @param drawCount   cards turned per draw (1 or 3)
 * @param stockSize   number of stock cards; their identities stay hidden
 * @param waste       waste cards, bottom first (the playable card is last)
 * @param tableau     tableau columns, each bottom first
 * @param foundations number of cards in each foundation, in {@link Suit} order
 * @author Farhiya Ayyub
 */
public record ObservableBoard(int drawCount, int stockSize, List<VisibleCard> waste,
                              List<List<VisibleCard>> tableau, List<Integer> foundations) {

    /**
     * One card as the player sees it.
     *
     * <p>Kind: record, nested in ObservableBoard.
     * <p>Purpose: it holds the rank value (1 to 13) and suit of a face-up card, or rank 0 and a null suit for a
     * face-down card.
     * <p>Data: record fields rank (int) and suit (Suit); 0 and null mark a hidden card.
     *
     * @param rank the rank value, or 0 when hidden
     * @param suit the suit, or null when hidden
     */
    public record VisibleCard(int rank, Suit suit) {
        /** {@return {@code true} if the card is face down} Called by: VisibleSolver.tableauMoveScore(). */
        public boolean isHidden() {
            return rank == 0;
        }

        /**
         * {@return {@code true} for a face-up Heart or Diamond} A hidden card gives {@code false}, because its suit is
         * unknown.
         *
         * <p>Called by: VisibleSolver.foundationScore().
         */
        public boolean isRed() {
            return suit != null && suit.isRed();
        }
    }

    /** Stores unmodifiable copies of the lists. */
    public ObservableBoard {
        waste = List.copyOf(waste);
        tableau = tableau.stream().map(List::copyOf).toList();
        foundations = List.copyOf(foundations);
    }

    /**
     * {@return the player's view of the board} Only the size of the stock and of each foundation is kept.
     *
     * <p>Key idea: every waste and tableau card goes through observe(), which turns a face-down card into the hidden
     * marker, so the solver cannot use a hidden card even by accident.
     *
     * <p>Called by: VisibleSolver.solve().
     *
     * @param state the full board
     */
    public static ObservableBoard from(GameState state) {
        return new ObservableBoard(state.drawCount(), state.stock().size(),
                state.waste().cards().stream().map(ObservableBoard::observe).toList(),
                state.tableau().stream().map(pile -> pile.cards().stream().map(ObservableBoard::observe).toList())
                        .toList(),
                Arrays.stream(Suit.values()).map(suit -> state.foundation(suit).size()).toList());
    }

    /**
     * {@return the card's rank value and suit if it is face up, or the hidden marker (0, {@code null}) if not}
     *
     * <p>Called by: from().
     *
     * @param card the card to look at
     */
    private static VisibleCard observe(Card card) {
        return card.isFaceUp() ? new VisibleCard(card.rank().value(), card.suit()) : new VisibleCard(0, null);
    }
}
