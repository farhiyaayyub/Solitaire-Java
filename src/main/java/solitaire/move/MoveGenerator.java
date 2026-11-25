package solitaire.move;

import java.util.ArrayList;
import java.util.List;

import solitaire.enums.MoveType;
import solitaire.enums.Suit;
import solitaire.game.GameState;

/**
 * Finds every legal move on a board by trying every possible move and keeping only the legal ones.
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
        // Try every kind of move...
        for (MoveType type : MoveType.values()) {
            // ...from every possible source...
            for (int sourceIndex = 0; sourceIndex < sourceCount(type); sourceIndex++) {
                // ...to every possible destination, and keep the legal ones.
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
        // Only a column-to-column move can carry several cards; every other move tries just 1.
        int maximumCount = type == MoveType.TABLEAU_TO_TABLEAU ? state.tableau().get(sourceIndex).size() : 1;
        for (int sequenceLength = 1; sequenceLength <= maximumCount; sequenceLength++) {
            // A draw turns 1 or 3 cards, or fewer if the stock is almost empty.
            int count = type == MoveType.DRAW ? Math.min(state.drawCount(), state.stock().size()) : sequenceLength;
            Move candidateMove = new Move(type, sourceIndex, destinationIndex, count);
            // Keep the move only if MoveValidator says it is legal.
            if (validator.isLegal(state, candidateMove)) {
                legalMoves.add(candidateMove);
            }
        }
    }

    /** Returns how many sources to try for a move type: 7 columns, 4 foundations, or 1 for the stock or the waste. */
    private static int sourceCount(MoveType type) {
        return switch (type) {
            // Moves that start in a column: try all 7 columns.
            case TABLEAU_TO_TABLEAU, TABLEAU_TO_FOUNDATION -> GameState.TABLEAU_COLUMNS;
            // Moves that start on a foundation: try all 4 foundations.
            case FOUNDATION_TO_TABLEAU -> Suit.FOUNDATION_COUNT;
            // The stock and the waste are single piles, so there is only 1 source.
            default -> 1;
        };
    }

    /** Returns how many destinations to try for a move type: 7 columns, 4 foundations, or 1 otherwise. */
    private static int destinationCount(MoveType type) {
        return switch (type) {
            // Moves that end in a column: try all 7 columns.
            case TABLEAU_TO_TABLEAU, WASTE_TO_TABLEAU, FOUNDATION_TO_TABLEAU -> GameState.TABLEAU_COLUMNS;
            // Moves that end on a foundation: try all 4 foundations.
            case TABLEAU_TO_FOUNDATION, WASTE_TO_FOUNDATION -> Suit.FOUNDATION_COUNT;
            // A draw and a recycle always go to the same single pile.
            default -> 1;
        };
    }
}
