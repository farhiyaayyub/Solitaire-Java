package solitaire.move;

import java.util.ArrayList;
import java.util.List;

import solitaire.enums.MoveType;
import solitaire.enums.Suit;
import solitaire.game.GameState;

/**
 * Lists every legal move on a board. It builds every possible candidate move and keeps the ones MoveValidator
 * accepts, so it never needs rules of its own. The order is always the same for the same board, which keeps games
 * repeatable.
 *
 * <p>Abstraction: callers only call generate(); a MoveValidator checks each candidate.
 *
 * @author Farhiya Ayyub
 */
public final class MoveGenerator {
    private final MoveValidator validator = new MoveValidator();

    /**
     * Returns all legal moves on the board, always in the same order. The board is not changed.
     *
     * <p>Algorithm: generate and test (try every possible move, keep the legal ones).
     * <ol>
     *   <li>Go through every move type (in MoveType order), every source pile, and every destination pile.</li>
     *   <li>For each combination, build the candidate move; between columns, build one for every card count.</li>
     *   <li>Keep the candidate only if MoveValidator says it is legal.</li>
     * </ol>
     *
     * <p>Calls:
     * <ul>
     *   <li>{@code sourceCount()} - how many sources to try</li>
     *   <li>{@code destinationCount()} - how many destinations to try</li>
     *   <li>{@code addLegalMoves()} - keeps the legal moves for each pair</li>
     * </ul>
     *
     * <p>Called by: {@code GameController.manual()}, {@code SolvableDealProvider.create()},
     * {@code VisibleSolver.solve()}.
     *
     * @param state the board to read; it is not changed
     * @return all legal moves on the board, always in the same order
     */
    public List<Move> generate(GameState state) {
        List<Move> legalMoves = new ArrayList<>();
        for (MoveType type : MoveType.values()) {
            for (int sourceIndex = 0; sourceIndex < sourceCount(type); sourceIndex++) {
                for (int destinationIndex = 0; destinationIndex < destinationCount(type); destinationIndex++) {
                    addLegalMoves(state, type, sourceIndex, destinationIndex, legalMoves);
                }
            }
        }
        return legalMoves;
    }

    /**
     * Adds to legalMoves every legal move for one type, source, and destination. Between columns it tries every count
     * from 1 to the column size; a draw uses the draw mode, or fewer cards if the stock has fewer; every other type
     * moves exactly 1 card.
     *
     * <p>Key idea: only column-to-column moves try more than one card ({@code maximumCount} is the column size); every
     * other type uses a count of 1. That is why a move of several cards to a foundation is never generated.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link GameState#tableau()} - gets the source column</li>
     *   <li>{@code CardPile.size()} - tries every card count up to the column size</li>
     *   <li>{@link GameState#drawCount()} - gets the draw mode for a draw</li>
     *   <li>{@link GameState#stock()} - draws fewer when the stock is short</li>
     *   <li>{@link MoveValidator#isLegal} - keeps only legal moves</li>
     * </ul>
     *
     * <p>Called by: {@link #generate}.
     *
     * @param state the board to read
     * @param type the move type to try
     * @param sourceIndex the source column or foundation index
     * @param destinationIndex the destination column or foundation index
     * @param legalMoves the list that legal moves are added to
     */
    private void addLegalMoves(GameState state, MoveType type, int sourceIndex, int destinationIndex,
                               List<Move> legalMoves) {
        int maximumCount = type == MoveType.TABLEAU_TO_TABLEAU ? state.tableau().get(sourceIndex).size() : 1;
        for (int sequenceLength = 1; sequenceLength <= maximumCount; sequenceLength++) {
            int count = type == MoveType.DRAW ? Math.min(state.drawCount(), state.stock().size()) : sequenceLength;
            Move candidateMove = new Move(type, sourceIndex, destinationIndex, count);
            if (validator.isLegal(state, candidateMove)) {
                legalMoves.add(candidateMove);
            }
        }
    }

    /**
     * Returns the number of source indexes to try: 7 columns, 4 foundations, or 1 for the stock or waste.
     *
     * <p>Called by: {@link #generate}.
     *
     * @param type the move type
     * @return 7, 4, or 1
     */
    private static int sourceCount(MoveType type) {
        return switch (type) {
            case TABLEAU_TO_TABLEAU, TABLEAU_TO_FOUNDATION -> GameState.TABLEAU_COLUMNS;
            case FOUNDATION_TO_TABLEAU -> Suit.FOUNDATION_COUNT;
            default -> 1;
        };
    }

    /**
     * Returns the number of destination indexes to try: 7 columns, 4 foundations, or 1 otherwise.
     *
     * <p>Called by: {@link #generate}.
     *
     * @param type the move type
     * @return 7, 4, or 1
     */
    private static int destinationCount(MoveType type) {
        return switch (type) {
            case TABLEAU_TO_TABLEAU, WASTE_TO_TABLEAU, FOUNDATION_TO_TABLEAU -> GameState.TABLEAU_COLUMNS;
            case TABLEAU_TO_FOUNDATION, WASTE_TO_FOUNDATION -> Suit.FOUNDATION_COUNT;
            default -> 1;
        };
    }
}
