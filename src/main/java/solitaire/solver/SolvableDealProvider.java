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
 * Maker of a winnable demonstration deal.
 *
 * <p>Kind: final class, contains the record VerifiedDeal.
 * <p>Purpose: it builds the Draw 1 deal for the "guaranteed" command, together with winning moves that were checked
 * by actually playing them.
 * <p>Data: none; it only builds deals. While verifying, the winning moves are collected in an ArrayList, which keeps
 * them in play order.
 * <p>OOP: Encapsulation - VerifiedDeal protects its board and moves with copies.
 *
 * @author Farhiya Ayyub
 */
public final class SolvableDealProvider {
    // Verification stops with an error after this many moves, so a mistake in the deal can never loop forever.
    private static final int MAX_VERIFICATION_MOVES = 200;

    /**
     * A winnable deal with its proof.
     *
     * <p>Kind: record, nested in SolvableDealProvider.
     * <p>Purpose: it holds a starting board and a winning move list that was checked by playing it.
 * <p>Data: record fields; the board is a GameState copy and the moves a List.copyOf, so outside code cannot change
 * them.
     *
     * @param state the starting board
     * @param solution the winning moves, in order
     */
    public record VerifiedDeal(GameState state, List<Move> solution) {
        /**
         * Copies the board and the moves, so later changes to the originals do not change the stored deal.
         *
         * <p>Called by: SolvableDealProvider.create().
         */
        public VerifiedDeal {
            state = state.copy();
            solution = List.copyOf(solution);
        }

        /** {@return a copy of the stored board, so callers cannot change it} Called by: GameController.guaranteed(). */
        @Override
        public GameState state() {
            return state.copy();
        }
    }

    /**
     * {@return a Draw 1 deal together with moves proven to win it}
     *
     * <p>Algorithm: build, then verify by playing.
     * <ol>
     *   <li>Deal the cards in ascending rank order, so low cards are on top.</li>
     *   <li>On a copy, until it is won: play the first legal move to a foundation; if there is none, draw 1 card.</li>
     *   <li>Check 52 unique cards after every move and record each move.</li>
     *   <li>Return the original board and the recorded moves.</li>
     * </ol>
     *
     * <p>Called by: GameController.guaranteed().
     *
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
     * {@return the 52 cards in the order to deal them} The lowest cards go to the columns, each column reversed so
     * its lowest card ends on top, and the higher ranks follow in the stock in ascending order, so cards can go
     * straight to the foundations.
     *
     * <p>Called by: create().
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
