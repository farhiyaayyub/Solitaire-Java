package solitaire.solver;

import java.util.Arrays;
import java.util.List;

import solitaire.enums.Suit;
import solitaire.game.GameState;
import solitaire.model.Card;

/**
 * The board as a player sees it, with face-down cards hidden. The solver uses it so it never looks at hidden cards.
 *
 * @author Farhiya Ayyub
 */
public record ObservableBoard(int drawCount, int stockSize, List<VisibleCard> waste,
                              List<List<VisibleCard>> tableau, List<Integer> foundations) {

    /** One card as the player sees it: its rank and suit if it is face up, or rank 0 and no suit if it is face down. */
    public record VisibleCard(int rank, Suit suit) {
        /** Returns true if the card is face down. */
        public boolean isHidden() {
            return rank == 0;
        }

        /** Returns true if the card is a face-up Heart or Diamond. */
        public boolean isRed() {
            return suit != null && suit.isRed();
        }
    }

    /** Stores unmodifiable copies of the lists, so the view cannot be changed after it is made. */
    public ObservableBoard {
        waste = List.copyOf(waste);
        tableau = tableau.stream().map(List::copyOf).toList();
        foundations = List.copyOf(foundations);
    }

    /**
     * Builds the player's view of a board: the draw mode, the stock size, the waste and column cards with face-down
     * cards hidden, and how many cards each foundation holds.
     */
    public static ObservableBoard from(GameState state) {
        return new ObservableBoard(state.drawCount(), state.stock().size(),
                state.waste().cards().stream().map(ObservableBoard::observe).toList(),
                state.tableau().stream().map(pile -> pile.cards().stream().map(ObservableBoard::observe).toList())
                        .toList(),
                Arrays.stream(Suit.values()).map(suit -> state.foundation(suit).size()).toList());
    }

    /**
     * Turns one card into what the player sees: its rank and suit if it is face up, or the hidden marker if it is face
     * down.
     */
    private static VisibleCard observe(Card card) {
        return card.isFaceUp() ? new VisibleCard(card.rank().value(), card.suit()) : new VisibleCard(0, null);
    }
}
