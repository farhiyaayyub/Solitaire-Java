package solitaire.solver;

import solitaire.enums.GameStatus;

import solitaire.enums.MoveType;

import solitaire.enums.Suit;
import solitaire.game.GameState;
import solitaire.game.SolitaireGame;
import solitaire.move.Move;
import solitaire.move.MoveGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Uses visible information and deterministic heuristic priorities; does not prove unsolvability.
 * @author Farhiya Ayyub
 */
public final class VisibleSolver implements SolitaireSolver {
    private static final int DRAW_SCORE = 5;
    private static final int RECYCLE_SCORE = 1;
    private static final int FOUNDATION_RETURN_SCORE = -100;
    private static final int REVEAL_BONUS = 300;
    private static final int SAFE_FOUNDATION_SCORE = 500;
    private static final int FOUNDATION_SCORE = 12;
    private static final int WASTE_TRANSFER_SCORE = 80;
    private static final int EMPTY_COLUMN_RELOCATION_SCORE = -200;
    private static final int EMPTY_SOURCE_SCORE = 30;
    private static final int TABLEAU_TRANSFER_SCORE = 20;
    private static final double SEQUENCE_LENGTH_BONUS = 0.1;
    private static final long NANOS_PER_MILLISECOND = 1_000_000;
    private final SolitaireSolver.Limits limits;

    /** Creates a solver with the supplied decision, move, and time limits. */
    public VisibleSolver(SolitaireSolver.Limits limits) {
        this.limits = Objects.requireNonNull(limits);
    }

    /** Selects the highest-scoring untried legal move, or null when none remain. */
    public Move choose(ObservableBoard board, List<Move> legalMoves, Set<Move> attemptedMoves) {
        Move bestMove = null;
        double bestScore = -Double.MAX_VALUE;
        for (Move move : legalMoves) {
            if (attemptedMoves.contains(move)) continue;
            double score = score(board, move);
            if (score > bestScore) {
                bestMove = move;
                bestScore = score;
            }
        }
        return bestMove;
    }

    /** Scores a legal move using only information visible on the board. */
    private double score(ObservableBoard board, Move move) {
        if (move.type() == MoveType.DRAW) return DRAW_SCORE;
        if (move.type() == MoveType.RECYCLE) return RECYCLE_SCORE;
        if (move.type() == MoveType.FOUNDATION_TO_TABLEAU) return FOUNDATION_RETURN_SCORE;
        boolean fromTableau = move.type() == MoveType.TABLEAU_TO_FOUNDATION || move.type() == MoveType.TABLEAU_TO_TABLEAU;
        List<ObservableBoard.VisibleCard> sourceCards = fromTableau ? board.tableau().get(move.source()) : board.waste();
        int firstMovedCardIndex = fromTableau ? sourceCards.size() - move.count() : 0;
        ObservableBoard.VisibleCard card = sourceCards.get(firstMovedCardIndex);
        boolean revealsHiddenCard = fromTableau && firstMovedCardIndex > 0 && sourceCards.get(firstMovedCardIndex - 1).hidden();
        double score = revealsHiddenCard ? REVEAL_BONUS + firstMovedCardIndex : 0;
        if (move.type() == MoveType.TABLEAU_TO_FOUNDATION || move.type() == MoveType.WASTE_TO_FOUNDATION) {
            boolean safeForFoundation = card.rank() <= 2;
            if (!safeForFoundation) {
                safeForFoundation = true;
                for (Suit suit : Suit.values()) {
                    int requiredFoundationRank = suit.isRed() == card.red() ? card.rank() - 2 : card.rank() - 1;
                    if (suit != card.suit() && board.foundations().get(suit.ordinal()) < requiredFoundationRank) safeForFoundation = false;
                }
            }
            return score + (safeForFoundation ? SAFE_FOUNDATION_SCORE : FOUNDATION_SCORE);
        }
        if (move.type() == MoveType.WASTE_TO_TABLEAU) return WASTE_TRANSFER_SCORE;
        if (firstMovedCardIndex == 0 && board.tableau().get(move.destination()).isEmpty()) return EMPTY_COLUMN_RELOCATION_SCORE;
        return score + (firstMovedCardIndex == 0 ? EMPTY_SOURCE_SCORE : TABLEAU_TRANSFER_SCORE) + move.count() * SEQUENCE_LENGTH_BONUS;
    }

    /** Plays from a copied starting board and returns a bounded result and replayable move path. */
    @Override
    public Result solve(GameState initialBoard) {
        SolitaireGame game = new SolitaireGame(initialBoard);
        List<Move> playedMoves = new ArrayList<>();
        Map<ObservableBoard, Set<Move>> attemptedMoves = new HashMap<>();
        MoveGenerator generator = new MoveGenerator();
        long startNanos = System.nanoTime();
        int decisionCount = 0;
        while (true) {
            GameState currentBoard = game.state();
            if (currentBoard.won()) return new Result(GameStatus.WON, playedMoves, decisionCount, "Visible-information play won");
            if (decisionCount >= limits.states() || playedMoves.size() >= limits.moves()
                    || (System.nanoTime() - startNanos) / NANOS_PER_MILLISECOND >= limits.milliseconds())
                return new Result(GameStatus.SEARCH_LIMIT_REACHED, playedMoves, decisionCount, "Decision, move, or time limit reached");
            ObservableBoard board = ObservableBoard.from(currentBoard);
            Set<Move> attemptedAtPosition = attemptedMoves.computeIfAbsent(board, ignored -> new HashSet<>());
            Move move = choose(board, generator.generate(currentBoard), attemptedAtPosition);
            decisionCount++;
            if (move == null) return new Result(GameStatus.STUCK, playedMoves, decisionCount, "No untried action in the observable position");
            attemptedAtPosition.add(move);
            if (!game.play(move)) throw new IllegalStateException("Visible policy selected an illegal move");
            playedMoves.add(move);
        }
    }
}
