package solitaire.solver;

import java.util.Arrays;
import java.util.List;

import solitaire.enums.Suit;
import solitaire.game.GameState;
import solitaire.model.Card;

/**
 * What a player can see of the board: face-up cards are shown, face-down cards are replaced by a hidden marker.
 * The solver scores moves using only this view, so it cannot use hidden cards even by accident. Because it is a
 * record, two views with the same visible cards are equal, so it can be used as a Map key to recognise a position.
 *
 * <p>Abstraction: it hides face-down cards from the solver. It is a record, and its lists are copied, so it cannot
 * change after it is made.
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
     * One card as the player sees it: its rank value (1 to 13) and suit when face up, or rank 0 and a null suit when
     * face down.
     *
     * @param rank the rank value, or 0 when hidden
     * @param suit the suit, or null when hidden
     */
    public record VisibleCard(int rank, Suit suit) {
        /**
         * Returns {@code true} if the card is face down (rank 0).
         *
         * <p>Called by: {@code VisibleSolver.tableauMoveScore()}.
         *
         * @return {@code true} if face down
         */
        public boolean isHidden() {
            return rank == 0;
        }

        /**
         * Returns {@code true} for a face-up Heart or Diamond; returns {@code false} for a hidden card, whose suit is
         * unknown.
         *
         * <p>Called by: {@code VisibleSolver.foundationScore()}.
         *
         * @return {@code true} for a face-up Heart or Diamond
         */
        public boolean isRed() {
            return suit != null && suit.isRed();
        }
    }

    /** Stores unmodifiable copies of the lists, so the view cannot change after it is made. */
    public ObservableBoard {
        waste = List.copyOf(waste);
        tableau = tableau.stream().map(List::copyOf).toList();
        foundations = List.copyOf(foundations);
    }

    /**
     * Builds the view of a board: every waste and tableau card is passed through observe(), and only the size of the
     * stock and of each foundation is kept.
     *
     * <p>Key idea: every face-down card becomes the hidden marker (rank 0, no suit), so the solver cannot use a hidden
     * card even by accident. Because ObservableBoard is a record, two views with the same visible cards are equal,
     * which lets the solver use a view as a HashMap key.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link GameState#drawCount()} - keeps the draw mode</li>
     *   <li>{@link GameState#stock()} - keeps only the stock size</li>
     *   <li>{@code CardPile.size()} - counts each foundation</li>
     *   <li>{@link GameState#waste()} - gets the waste cards</li>
     *   <li>{@code CardPile.cards()} - gets copies of the cards</li>
     *   <li>{@code observe()} - hides each face-down card</li>
     *   <li>{@link GameState#tableau()} - gets the seven columns</li>
     *   <li>{@link GameState#foundation} - gets each foundation</li>
     * </ul>
     *
     * <p>Called by: {@link VisibleSolver#solve}.
     *
     * @param state the full board
     * @return what a player can see of the board
     */
    public static ObservableBoard from(GameState state) {
        return new ObservableBoard(state.drawCount(), state.stock().size(),
                state.waste().cards().stream().map(ObservableBoard::observe).toList(),
                state.tableau().stream().map(pile -> pile.cards().stream().map(ObservableBoard::observe).toList())
                        .toList(),
                Arrays.stream(Suit.values()).map(suit -> state.foundation(suit).size()).toList());
    }

    /**
     * Returns the card's rank value and suit if it is face up, or the hidden marker (0, {@code null}) if not.
     *
     * <p>Called by: {@link #from}.
     *
     * @param card the card to look at
     * @return the visible card, or the hidden marker
     */
    private static VisibleCard observe(Card card) {
        return card.isFaceUp() ? new VisibleCard(card.rank().value(), card.suit()) : new VisibleCard(0, null);
    }
}
