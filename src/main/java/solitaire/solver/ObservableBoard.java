package solitaire.solver;

import solitaire.enums.Suit;
import solitaire.game.GameState;
import solitaire.model.Card;
import java.util.List;

/**
 * Immutable decision input containing no hidden card identities.
 * @author Farhiya Ayyub
 */
public record ObservableBoard(int drawCount, int stockSize, List<VisibleCard> waste,
                              List<List<VisibleCard>> tableau, List<Integer> foundations) {
/**
 * Stores the rank and suit information visible to the solver.
 *
 * @author Farhiya Ayyub
 */
    /** Shows a card's rank and suit, or masks both when the card is hidden. */
    public record VisibleCard(int rank, Suit suit) {
        /** Reports whether this observation masks the card identity. */
        public boolean hidden() { return rank == 0; }
        /** Reports whether this visible card belongs to a red suit. */
        public boolean red() { return suit != null && suit.isRed(); }
    }

    /** Copies each collection so the observation cannot be changed by its caller. */
    public ObservableBoard {
        waste = List.copyOf(waste);
        tableau = tableau.stream().map(List::copyOf).toList();
        foundations = List.copyOf(foundations);
    }

    /** Hides a face-down card's identity in the solver's observation. */
    private static VisibleCard visible(Card card) {
        return card.isFaceUp() ? new VisibleCard(card.rank().getValue(), card.suit()) : new VisibleCard(0, null);
    }

    /** Builds an immutable observation with hidden identities removed. */
    public static ObservableBoard from(GameState state) {
        return new ObservableBoard(state.drawCount(), state.stock().size(),
                state.waste().cards().stream().map(ObservableBoard::visible).toList(),
                state.tableau().stream().map(pile -> pile.cards().stream().map(ObservableBoard::visible).toList()).toList(),
                java.util.Arrays.stream(Suit.values()).map(suit -> state.foundation(suit).size()).toList());
    }
}
