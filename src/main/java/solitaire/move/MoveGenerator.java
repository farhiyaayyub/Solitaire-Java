package solitaire.move;

import solitaire.enums.MoveType;

import solitaire.game.GameState;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates legal move options for a board.
 *
 * @author Farhiya Ayyub
 */
public final class MoveGenerator {
    /** Returns legal move proposals in deterministic order using exposed cards and pile sizes. */
    public List<Move> generate(GameState state) {
        List<Move> legalMoves = new ArrayList<>();
        MoveValidator validator = new MoveValidator();
        for (MoveType type : MoveType.values()) {
            int sourceCount = switch (type) {
                case TABLEAU_TO_TABLEAU, TABLEAU_TO_FOUNDATION -> GameState.TABLEAU_COLUMNS;
                case FOUNDATION_TO_TABLEAU -> solitaire.enums.Suit.FOUNDATION_COUNT;
                default -> 1;
            };
            int destinationCount = switch (type) {
                case TABLEAU_TO_TABLEAU, WASTE_TO_TABLEAU, FOUNDATION_TO_TABLEAU -> GameState.TABLEAU_COLUMNS;
                case TABLEAU_TO_FOUNDATION, WASTE_TO_FOUNDATION -> solitaire.enums.Suit.FOUNDATION_COUNT;
                default -> 1;
            };
            for (int sourceIndex = 0; sourceIndex < sourceCount; sourceIndex++)
                for (int destinationIndex = 0; destinationIndex < destinationCount; destinationIndex++) {
                    int maximumSequenceLength = type == MoveType.TABLEAU_TO_TABLEAU ? state.tableau().get(sourceIndex).size() : 1;
                    for (int sequenceLength = 1; sequenceLength <= maximumSequenceLength; sequenceLength++) {
                        int count = type == MoveType.DRAW ? Math.min(state.drawCount(), state.stock().size()) : sequenceLength;
                        Move candidateMove = new Move(type, sourceIndex, destinationIndex, count);
                        if (validator.isLegal(state, candidateMove)) legalMoves.add(candidateMove);
                    }
                }
        }
        return legalMoves;
    }
}
