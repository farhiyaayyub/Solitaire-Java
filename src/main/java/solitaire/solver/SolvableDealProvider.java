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
 * Builds a demonstration deal that is known to be winnable, together with its checked winning moves.
 *
 * <p>Type: final class; used by the "guaranteed" command.
 * OOP: ENCAPSULATION (the VerifiedDeal record protects the board and solution with copies).
 *
 * @author Farhiya Ayyub
 */
public final class SolvableDealProvider {
    // Constant: safety limit so verification can never loop forever.
    private static final int MAX_VERIFICATION_MOVES = 200;

    /**
     * Type: record inside SolvableDealProvider. A starting board and a winning move list that was checked by
     * actually playing it.
     *
     * @param state    the starting board
     * @param solution the winning moves, in order
     */
    public record VerifiedDeal(GameState state, List<Move> solution) {
        /**
         * Constructor (compact): Copies the board and move list to protect the stored solution.
         * Calls: GameState.copy(), List.copyOf()
         */
        public VerifiedDeal {
            state = state.copy();
            solution = List.copyOf(solution);
        }

        /**
         * Method (override): Returns a detached board copy so callers cannot modify the stored board.
         * Calls: GameState.copy()
         */
        @Override
        public GameState state() {
            return state.copy();
        }
    }

    /**
     * Method: Deals every card in ascending rank order (Aces exposed first) and verifies the deal by playing it:
     * always move a card to a foundation when possible, otherwise draw.
     * Calls: ascendingDrawOrder(), GameState.deal(), GameState.copy(), GameState.won(), MoveGenerator.generate(),
     * SolitaireGame.apply(), GameState.verify()
     */
    public VerifiedDeal create() {
        // Object: Deck - built from the ascending card order.
        GameState initialBoard = GameState.deal(new Deck(ascendingDrawOrder()));
        GameState workingBoard = initialBoard.copy();
        List<Move> solution = new ArrayList<>();
        // Object: MoveGenerator - lists the legal moves on the working board.
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
                // Object: Move - a draw of one card.
                move = new Move(MoveType.DRAW, 0, 0, 1);
            }
            if (move == null) {
                throw new IllegalStateException("Deal verification failed");
            }
            SolitaireGame.apply(workingBoard, move);
            workingBoard.verify();
            solution.add(move);
        }
        // Object: VerifiedDeal - the starting board with its checked winning moves.
        return new VerifiedDeal(initialBoard, solution);
    }

    /**
     * Method: Orders the cards so each column holds ascending ranks and the stock continues upward.
     * Calls: Collections.reverse()
     */
    private static List<Card> ascendingDrawOrder() {
        List<Card> cardsByAscendingRank = new ArrayList<>();
        for (Rank rank : Rank.values()) {
            for (Suit suit : Suit.values()) {
                // Object: Card - one for each rank and suit.
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
