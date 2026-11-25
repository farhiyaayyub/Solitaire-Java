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
 * Creates a deal that is sure to be winnable, for the guaranteed mode, together with its winning moves.
 *
 * @author Farhiya Ayyub
 */
public final class SolvableDealProvider {
    // Verification stops with an error after this many moves, so a mistake in the deal can never loop forever.
    private static final int MAX_VERIFICATION_MOVES = 200;

    /** A winnable deal: the starting board together with the moves that win it. */
    public record VerifiedDeal(GameState state, List<Move> solution) {
        /** Stores copies of the board and the moves, so later changes to the originals do not change the deal. */
        public VerifiedDeal {
            state = state.copy();
            solution = List.copyOf(solution);
        }

        /** Returns a copy of the stored board, so callers cannot change it. */
        @Override
        public GameState state() {
            return state.copy();
        }
    }

    /**
     * Deals a board from a specially ordered deck, then wins it by moving a card to a foundation whenever possible and
     * drawing from the stock otherwise. It saves every move and returns the board with those winning moves. Throws an
     * error if no move is possible or it takes more than 200 moves.
     */
    public VerifiedDeal create() {
        // Deal the specially ordered deck, and play on a copy so the starting board is kept.
        GameState initialBoard = GameState.deal(new Deck(ascendingDrawOrder()));
        GameState workingBoard = initialBoard.copy();
        List<Move> solution = new ArrayList<>();
        MoveGenerator generator = new MoveGenerator();
        // Keep playing until the board is won.
        while (!workingBoard.won()) {
            // Too many moves means something is wrong with the deal, so stop with an error.
            if (solution.size() >= MAX_VERIFICATION_MOVES) {
                throw new IllegalStateException("Deal verification failed");
            }
            // First choice: any legal move that puts a card on a foundation.
            Move move = generator.generate(workingBoard).stream()
                    .filter(candidate -> candidate.type() == MoveType.TABLEAU_TO_FOUNDATION
                            || candidate.type() == MoveType.WASTE_TO_FOUNDATION)
                    .findFirst()
                    .orElse(null);
            // Second choice: if no foundation move exists, draw a card from the stock.
            if (move == null && !workingBoard.stock().isEmpty()) {
                move = new Move(MoveType.DRAW, 0, 0, 1);
            }
            // No move at all means the deal cannot be won this way, so stop with an error.
            if (move == null) {
                throw new IllegalStateException("Deal verification failed");
            }
            // Play the move, check that no card was lost, and save it as part of the solution.
            SolitaireGame.apply(workingBoard, move);
            workingBoard.verify();
            solution.add(move);
        }
        // Return the starting board together with the winning moves.
        return new VerifiedDeal(initialBoard, solution);
    }

    /**
     * Builds the card order for the winnable deal: the cards sorted from Ace up to King, with each column's cards
     * reversed so the lowest card ends up face up on top, and the rest going to the stock.
     */
    private static List<Card> ascendingDrawOrder() {
        // Step 1: list all 52 cards from low to high: the four Aces, then the four 2s, and so on up to the Kings.
        List<Card> cardsByAscendingRank = new ArrayList<>();
        for (Rank rank : Rank.values()) {
            for (Suit suit : Suit.values()) {
                cardsByAscendingRank.add(new Card(suit, rank));
            }
        }
        // Step 2: take the cards for each column (1 card, then 2, ... up to 7) and reverse them,
        // so the lowest card of each column is dealt last and ends up face up on top.
        List<Card> drawOrder = new ArrayList<>();
        int offset = 0;
        for (int columnSize = 1; columnSize <= GameState.TABLEAU_COLUMNS; columnSize++) {
            List<Card> column = new ArrayList<>(cardsByAscendingRank.subList(offset, offset + columnSize));
            Collections.reverse(column);
            drawOrder.addAll(column);
            offset += columnSize;
        }
        // Step 3: the remaining cards go to the stock, still in low-to-high order.
        drawOrder.addAll(cardsByAscendingRank.subList(offset, Deck.CARD_COUNT));
        return drawOrder;
    }
}
