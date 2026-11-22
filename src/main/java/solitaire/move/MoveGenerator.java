package solitaire.move;

import java.util.ArrayList;
import java.util.List;

import solitaire.enums.MoveType;
import solitaire.enums.Suit;
import solitaire.game.GameState;

/**
 * Lists every legal move on a board in a fixed, repeatable order.
 *
 * <p>Type: final class.
 * OOP: ABSTRACTION (callers just call generate), uses a MoveValidator object to check each candidate move.
 *
 * @author Farhiya Ayyub
 */
public final class MoveGenerator {
    // Field and object: one MoveValidator, reused for every check.
    private final MoveValidator validator = new MoveValidator();

    /**
     * Method: Returns the legal moves, ordered by move type, then source, destination, and card count.
     * Calls: sourceCount(), destinationCount(), addLegalMoves()
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
     * Method: Adds each legal card count for one move type, source, and destination.
     * Calls: GameState.tableau(), GameState.drawCount(), GameState.stock(), MoveValidator.isLegal()
     */
    private void addLegalMoves(GameState state, MoveType type, int sourceIndex, int destinationIndex,
                               List<Move> legalMoves) {
        int maximumCount = type == MoveType.TABLEAU_TO_TABLEAU ? state.tableau().get(sourceIndex).size() : 1;
        for (int sequenceLength = 1; sequenceLength <= maximumCount; sequenceLength++) {
            int count = type == MoveType.DRAW ? Math.min(state.drawCount(), state.stock().size()) : sequenceLength;
            // Object: Move - a candidate move; it is kept only if it is legal.
            Move candidateMove = new Move(type, sourceIndex, destinationIndex, count);
            if (validator.isLegal(state, candidateMove)) {
                legalMoves.add(candidateMove);
            }
        }
    }

    /** Method: Returns how many source piles a move type can use. */
    private static int sourceCount(MoveType type) {
        return switch (type) {
            case TABLEAU_TO_TABLEAU, TABLEAU_TO_FOUNDATION -> GameState.TABLEAU_COLUMNS;
            case FOUNDATION_TO_TABLEAU -> Suit.FOUNDATION_COUNT;
            default -> 1;
        };
    }

    /** Method: Returns how many destination piles a move type can use. */
    private static int destinationCount(MoveType type) {
        return switch (type) {
            case TABLEAU_TO_TABLEAU, WASTE_TO_TABLEAU, FOUNDATION_TO_TABLEAU -> GameState.TABLEAU_COLUMNS;
            case TABLEAU_TO_FOUNDATION, WASTE_TO_FOUNDATION -> Suit.FOUNDATION_COUNT;
            default -> 1;
        };
    }
}
