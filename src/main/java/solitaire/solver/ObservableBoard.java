package solitaire.solver;

import java.util.Arrays;
import java.util.List;

import solitaire.enums.Suit;
import solitaire.game.GameState;
import solitaire.model.Card;

/**
 * What a player can actually see: an immutable view of the board with every face-down card masked. The solver
 * decides using only this view, so it never uses hidden information.
 *
 * <p>Type: record (an immutable data class).
 * OOP: ABSTRACTION (hides face-down cards from the solver), ENCAPSULATION (lists are copied and cannot change).
 *
 * @param drawCount   cards turned per draw
 * @param stockSize   number of face-down stock cards
 * @param waste       waste cards, bottom first (the playable card is last)
 * @param tableau     tableau columns, each bottom first
 * @param foundations number of cards in each foundation, in {@link Suit} order
 * @author Farhiya Ayyub
 */
public record ObservableBoard(int drawCount, int stockSize, List<VisibleCard> waste,
                              List<List<VisibleCard>> tableau, List<Integer> foundations) {

    /**
     * Type: record inside ObservableBoard. A card as seen by the player: rank and suit when face up, or rank 0 and
     * no suit when face down.
     *
     * @param rank the rank value, or 0 when hidden
     * @param suit the suit, or null when hidden
     */
    public record VisibleCard(int rank, Suit suit) {
        /** Method: Reports whether this card is face down. */
        public boolean isHidden() {
            return rank == 0;
        }

        /**
         * Method: Reports whether this visible card belongs to a red suit.
         * Calls: Suit.isRed()
         */
        public boolean isRed() {
            return suit != null && suit.isRed();
        }
    }

    /**
     * Constructor (compact): Copies each collection so the caller cannot change the observation.
     * Calls: List.copyOf()
     */
    public ObservableBoard {
        waste = List.copyOf(waste);
        tableau = tableau.stream().map(List::copyOf).toList();
        foundations = List.copyOf(foundations);
    }

    /**
     * Method (static): Builds the player's view of a board, masking every face-down card.
     * Calls: GameState.drawCount(), GameState.stock(), GameState.waste(), GameState.tableau(),
     * GameState.foundation(), CardPile.cards(), CardPile.size(), observe()
     */
    public static ObservableBoard from(GameState state) {
        // Object: ObservableBoard - built from the board's visible information only.
        return new ObservableBoard(state.drawCount(), state.stock().size(),
                state.waste().cards().stream().map(ObservableBoard::observe).toList(),
                state.tableau().stream().map(pile -> pile.cards().stream().map(ObservableBoard::observe).toList())
                        .toList(),
                Arrays.stream(Suit.values()).map(suit -> state.foundation(suit).size()).toList());
    }

    /**
     * Method: Returns what the player sees of one card.
     * Calls: Card.isFaceUp(), Card.rank(), Rank.value(), Card.suit()
     */
    private static VisibleCard observe(Card card) {
        return card.isFaceUp() ? new VisibleCard(card.rank().value(), card.suit()) : new VisibleCard(0, null);
    }
}
