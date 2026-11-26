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
     * Builds the card order for the winnable deal: the cards sorted from Ace up to King, with each column's cards
     * reversed so the lowest card ends up face up on top, and the rest going to the stock.
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
