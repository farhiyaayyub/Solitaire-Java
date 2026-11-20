package solitaire.solver;

import solitaire.game.GameState;
import solitaire.game.SolitaireGame;
import solitaire.model.Card;
import solitaire.model.Deck;
import solitaire.enums.Rank;
import solitaire.enums.Suit;
import solitaire.move.Move;
import solitaire.move.MoveGenerator;
import solitaire.enums.MoveType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creates a deal with a verified winning solution.
 *
 * @author Farhiya Ayyub
 */
public final class SolvableDealProvider {
    private static final int MAX_VERIFICATION_MOVES = 200;

/**
 * Stores a starting board and its verified solution.
 *
 * @author Farhiya Ayyub
 */
    public record VerifiedDeal(GameState state, List<Move> solution) {
        /** Copies the board and move list to protect the stored solution. */
        public VerifiedDeal {
            state = state.copy();
            solution = List.copyOf(solution);
        }

        /** Returns a detached board snapshot so callers cannot modify the owned board. */
        @Override
        public GameState state() {
            return state.copy();
        }
    }

    /** Constructs a demonstration deal and verifies its winning move sequence. */
    public VerifiedDeal create() {
        List<Card> cardsByAscendingRank = new ArrayList<>();
        for (Rank rank : Rank.values()) for (Suit suit : Suit.values()) cardsByAscendingRank.add(new Card(suit, rank));
        List<Card> drawOrder = new ArrayList<>();
        int offset = 0;
        for (int size = 1; size <= GameState.TABLEAU_COLUMNS; size++) {
            List<Card> column = new ArrayList<>(cardsByAscendingRank.subList(offset, offset + size));
            Collections.reverse(column);
            drawOrder.addAll(column);
            offset += size;
        }
        drawOrder.addAll(cardsByAscendingRank.subList(offset, Deck.CARD_COUNT));
        GameState initialBoard = GameState.deal(new Deck(drawOrder)), workingBoard = initialBoard.copy();
        List<Move> solution = new ArrayList<>();
        MoveGenerator generator = new MoveGenerator();
        while (!workingBoard.won()) {
            Move move = generator.generate(workingBoard).stream().filter(candidateMove -> candidateMove.type() == MoveType.TABLEAU_TO_FOUNDATION || candidateMove.type() == MoveType.WASTE_TO_FOUNDATION).findFirst()
                .orElseGet(() -> workingBoard.stock().size() > 0 ? new Move(MoveType.DRAW, 0, 0, 1) : null);
            if (move == null || solution.size() >= MAX_VERIFICATION_MOVES) throw new IllegalStateException("Deal verification failed");
            SolitaireGame.apply(workingBoard, move);
            workingBoard.verify();
            solution.add(move);
        }
        return new VerifiedDeal(initialBoard, List.copyOf(solution));
    }
}
