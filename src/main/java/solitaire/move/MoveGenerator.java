package solitaire.move;

import java.util.ArrayList;
import java.util.List;

import solitaire.enums.MoveType;
import solitaire.enums.Suit;
import solitaire.game.GameState;

/**
 * Finds every legal move on a board. It builds every possible move and keeps only the ones MoveValidator accepts, so
 * the solver and the player always choose from legal moves.
 *
 * @author Farhiya Ayyub
 */
public final class MoveGenerator {
    private final MoveValidator validator = new MoveValidator();

    /**
     * Goes through every move type, every source, and every destination, and returns a list of all the legal moves on
     * the board.
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
     * Builds the possible moves for one type, source, and destination and adds the legal ones to the list. A
     * column-to-column move is tried with every run length, from 1 card up to the whole column; a draw uses the draw
     * mode, or fewer cards if the stock has fewer left.
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

    /** Returns how many sources to try for a move type: 7 columns, 4 foundations, or 1 for the stock or the waste. */
    private static int sourceCount(MoveType type) {
        return switch (type) {
            case TABLEAU_TO_TABLEAU, TABLEAU_TO_FOUNDATION -> GameState.TABLEAU_COLUMNS;
            case FOUNDATION_TO_TABLEAU -> Suit.FOUNDATION_COUNT;
            default -> 1;
        };
    }

    /** Returns how many destinations to try for a move type: 7 columns, 4 foundations, or 1 otherwise. */
    private static int destinationCount(MoveType type) {
        return switch (type) {
            case TABLEAU_TO_TABLEAU, WASTE_TO_TABLEAU, FOUNDATION_TO_TABLEAU -> GameState.TABLEAU_COLUMNS;
            case TABLEAU_TO_FOUNDATION, WASTE_TO_FOUNDATION -> Suit.FOUNDATION_COUNT;
            default -> 1;
        };
    }
}
