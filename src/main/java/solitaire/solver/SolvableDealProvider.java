package solitaire.solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import solitaire.enums.MoveType;
import solitaire.enums.Rank;
import solitaire.enums.Suit;
import solitaire.game.GameState;
import solitaire.game.SolitaireGame;
import solitaire.model.Card;
import solitaire.model.Deck;
import solitaire.move.Move;
import solitaire.move.MoveGenerator;

/**
 * Builds a Draw 1 demonstration deal that is known to be winnable, together with winning moves that were checked by
 * actually playing them.
 *
 * <p>Used by the "guaranteed" command. Encapsulation: the VerifiedDeal record protects the board and the moves with
 * copies.
 *
 * @author Farhiya Ayyub
 */
public final class SolvableDealProvider {
    // Verification stops with an error after this many moves, so a mistake in the deal can never loop forever.
    private static final int MAX_VERIFICATION_MOVES = 200;

    /**
     * A starting board and a winning move list that was checked by actually playing it.
     *
     * @param state the starting board
     * @param solution the winning moves, in order
     */
    public record VerifiedDeal(GameState state, List<Move> solution) {
        /**
         * Stores a copy of the board and an unmodifiable copy of the moves, so changing the originals later does not
         * change the stored deal.
         */
        public VerifiedDeal {
            state = state.copy();
            solution = List.copyOf(solution);
        }

        /**
         * Returns a copy of the stored board, so callers cannot change it. Replaces the getter that Java generates for
         * the record.
         *
         * <p>Called by: {@code GameController.guaranteed()}.
         *
         * @return a copy of the stored board
         */
        @Override
        public GameState state() {
            return state.copy();
        }
    }

    /**
     * Returns a Draw 1 deal together with moves proven to win it.
     *
     * <p>Algorithm: build, then verify by playing.
     * <ol>
     *   <li>Deal the cards in ascending rank order, so low cards are on top.</li>
     *   <li>On a copy, until it is won: play the first legal move to a foundation; if there is none, draw 1 card.</li>
     *   <li>Check 52 unique cards after every move and record each move.</li>
     *   <li>Return the original board and the recorded moves.</li>
     * </ol>
     *
     * <p>Calls:
     * <ul>
     *   <li>{@code ascendingDrawOrder()} - orders the cards low to high</li>
     *   <li>{@link GameState#deal} - deals them onto a board</li>
     *   <li>{@link GameState#copy} - tests the moves on a copy</li>
     *   <li>{@link GameState#won} - stops when the test game is won</li>
     *   <li>{@link MoveGenerator#generate} - finds a move to a foundation</li>
     *   <li>{@link GameState#stock()} - checks whether a draw is still possible</li>
     *   <li>{@code CardPile.isEmpty()} - stops with an error if no move is left</li>
     *   <li>{@link SolitaireGame#apply} - checks and plays each move</li>
     *   <li>{@link GameState#verify} - confirms 52 unique cards after each move</li>
     * </ul>
     *
     * <p>Called by: {@code GameController.guaranteed()}.
     *
     * @return a Draw 1 deal together with moves proven to win it
     * @throws IllegalStateException if the test game gets stuck or needs more than 200 moves
     */
    public VerifiedDeal create() {
        GameState initialBoard = GameState.deal(new Deck(ascendingDrawOrder()));
        GameState workingBoard = initialBoard.copy();
        List<Move> solution = new ArrayList<>();
        MoveGenerator generator = new MoveGenerator();
        while (!workingBoard.won()) {
            if (solution.size() >= MAX_VERIFICATION_MOVES) {
                throw new IllegalStateException("Deal verification failed");
            }
            Move move = generator.generate(workingBoard).stream()
                    .filter(candidate -> candidate.type() == MoveType.TABLEAU_TO_FOUNDATION
                            || candidate.type() == MoveType.WASTE_TO_FOUNDATION)
                    .findFirst()
                    .orElse(null);
            if (move == null && !workingBoard.stock().isEmpty()) {
                move = new Move(MoveType.DRAW, 0, 0, 1);
            }
            if (move == null) {
                throw new IllegalStateException("Deal verification failed");
            }
            SolitaireGame.apply(workingBoard, move);
            workingBoard.verify();
            solution.add(move);
        }
        return new VerifiedDeal(initialBoard, solution);
    }

    /**
     * Returns a draw order that deals the low cards to the columns with the lowest card of each column on top, and
     * leaves the higher ranks in the stock in ascending order, so cards can go straight to the foundations.
     *
     * <p>Called by: {@link #create}.
     *
     * @return the 52 cards in the order to deal them
     */
    private static List<Card> ascendingDrawOrder() {
        List<Card> cardsByAscendingRank = new ArrayList<>();
        for (Rank rank : Rank.values()) {
            for (Suit suit : Suit.values()) {
                cardsByAscendingRank.add(new Card(suit, rank));
            }
        }
        List<Card> drawOrder = new ArrayList<>();
        int offset = 0;
        for (int columnSize = 1; columnSize <= GameState.TABLEAU_COLUMNS; columnSize++) {
            List<Card> column = new ArrayList<>(cardsByAscendingRank.subList(offset, offset + columnSize));
            Collections.reverse(column);
            drawOrder.addAll(column);
            offset += columnSize;
        }
        drawOrder.addAll(cardsByAscendingRank.subList(offset, Deck.CARD_COUNT));
        return drawOrder;
    }
}
