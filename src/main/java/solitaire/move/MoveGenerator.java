package solitaire.move;

import java.util.ArrayList;
import java.util.List;

import solitaire.enums.MoveType;
import solitaire.enums.Suit;
import solitaire.game.GameState;

/**
 * Lister of all legal moves.
 *
 * <p>Kind: final class, uses MoveValidator.
 * <p>Purpose: it lists every legal move on a board, always in the same order for the same board, which keeps games
 * repeatable. It has no rules of its own: MoveValidator checks every candidate.
 * <p>Data: one MoveValidator; generate() returns the moves in a new ArrayList, which keeps the generation order.
 * <p>OOP: Abstraction - callers only call generate() and never see how candidates are built.
 *
 * @author Farhiya Ayyub
 */
public final class MoveGenerator {
    private final MoveValidator validator = new MoveValidator();

    /**
     * {@return all legal moves on the board, in MoveType order}
     *
     * <p>Algorithm: generate and test.
     * <ol>
     *   <li>Go through every move type, every source pile, and every destination pile.</li>
     *   <li>Build a candidate move for each combination; between columns, one for every card count.</li>
     *   <li>Keep the candidate only if MoveValidator says it is legal.</li>
     * </ol>
     *
     * <p>Called by: GameController.manual(), SolvableDealProvider.create(), VisibleSolver.solve().
     *
     * @param state the board to read; it is not changed
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
     * Adds to legalMoves every legal move for one type, source, and destination. A draw uses the draw mode, or fewer
     * cards if the stock has fewer.
     *
     * <p>Key idea: only column-to-column moves try several counts, from 1 to the column size; every other type is
     * tried once. That is why a move of several cards to a foundation is never generated.
     *
     * <p>Called by: generate().
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
     * {@return the number of source indexes to try: 7 columns, 4 foundations, or 1 for the stock or waste}
     *
     * <p>Called by: generate().
     *
     * @param type the move type
     */
    private static int sourceCount(MoveType type) {
        return switch (type) {
            case TABLEAU_TO_TABLEAU, TABLEAU_TO_FOUNDATION -> GameState.TABLEAU_COLUMNS;
            case FOUNDATION_TO_TABLEAU -> Suit.FOUNDATION_COUNT;
            default -> 1;
        };
    }

    /**
     * {@return the number of destination indexes to try: 7 columns, 4 foundations, or 1 otherwise}
     *
     * <p>Called by: generate().
     *
     * @param type the move type
     */
    private static int destinationCount(MoveType type) {
        return switch (type) {
            case TABLEAU_TO_TABLEAU, WASTE_TO_TABLEAU, FOUNDATION_TO_TABLEAU -> GameState.TABLEAU_COLUMNS;
            case TABLEAU_TO_FOUNDATION, WASTE_TO_FOUNDATION -> Suit.FOUNDATION_COUNT;
            default -> 1;
        };
    }
}
