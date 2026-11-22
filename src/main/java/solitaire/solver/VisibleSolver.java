package solitaire.solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import solitaire.enums.GameStatus;
import solitaire.enums.MoveType;
import solitaire.enums.Suit;
import solitaire.game.GameState;
import solitaire.game.SolitaireGame;
import solitaire.move.Move;
import solitaire.move.MoveGenerator;

/**
 * Plays automatically using only visible cards and fixed scoring priorities. It commits every move (no undo) and
 * never tries the same move twice in the same visible position. A STUCK result does not prove a deal unsolvable.
 *
 * <p>Type: final class that implements SolitaireSolver.
 * OOP: POLYMORPHISM (its own version of solve()), ENCAPSULATION (private scores and helpers).
 *
 * @author Farhiya Ayyub
 */
public final class VisibleSolver implements SolitaireSolver {
    // Constants: the score of each kind of move; a higher score means the move is chosen first.
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
    private static final int ALWAYS_SAFE_RANK = 2;
    private static final long NANOS_PER_MILLISECOND = 1_000_000;

    // Fields: the solver limits, and a MoveGenerator object reused for every decision.
    private final SolitaireSolver.Limits limits;
    private final MoveGenerator generator = new MoveGenerator();

    /**
     * Constructor: Creates a solver with the supplied decision, move, and time limits.
     * Calls: Objects.requireNonNull()
     */
    public VisibleSolver(SolitaireSolver.Limits limits) {
        this.limits = Objects.requireNonNull(limits);
    }

    /**
     * Method (override): Plays from a copy of the starting board until it wins, gets stuck, or reaches a limit.
     * Calls: SolitaireGame.state(), GameState.won(), ObservableBoard.from(), MoveGenerator.generate(), choose(),
     * SolitaireGame.play(), elapsedMillis()
     */
    @Override
    public Result solve(GameState initialBoard) {
        // Object: SolitaireGame - the solver plays on its own game, so the real game is not touched.
        SolitaireGame game = new SolitaireGame(initialBoard);
        List<Move> playedMoves = new ArrayList<>();
        Map<ObservableBoard, Set<Move>> attemptedMoves = new HashMap<>();
        long startNanos = System.nanoTime();
        int decisionCount = 0;
        while (decisionCount < limits.maxDecisions() && playedMoves.size() < limits.maxMoves()
                && elapsedMillis(startNanos) < limits.maxMillis()) {
            GameState currentBoard = game.state();
            if (currentBoard.won()) {
                return new Result(GameStatus.WON, playedMoves, decisionCount, "Visible-information play won");
            }
            // Object: ObservableBoard - the player's view of the board, with face-down cards hidden.
            ObservableBoard board = ObservableBoard.from(currentBoard);
            Set<Move> attemptedAtPosition = attemptedMoves.computeIfAbsent(board, ignored -> new HashSet<>());
            Move move = choose(board, generator.generate(currentBoard), attemptedAtPosition);
            decisionCount++;
            if (move == null) {
                return new Result(GameStatus.STUCK, playedMoves, decisionCount,
                        "No untried move in the visible position");
            }
            attemptedAtPosition.add(move);
            if (!game.play(move)) {
                throw new IllegalStateException("Visible policy selected an illegal move");
            }
            playedMoves.add(move);
        }
        if (game.state().won()) {
            return new Result(GameStatus.WON, playedMoves, decisionCount, "Visible-information play won");
        }
        return new Result(GameStatus.LIMIT_REACHED, playedMoves, decisionCount,
                "Decision, move, or time limit reached");
    }

    /**
     * Method: Selects the highest-scoring untried legal move, or null when none remain; ties keep list order.
     * Calls: score()
     */
    public Move choose(ObservableBoard board, List<Move> legalMoves, Set<Move> attemptedMoves) {
        Move bestMove = null;
        double bestScore = -Double.MAX_VALUE;
        for (Move move : legalMoves) {
            if (attemptedMoves.contains(move)) {
                continue;
            }
            double score = score(board, move);
            if (score > bestScore) {
                bestMove = move;
                bestScore = score;
            }
        }
        return bestMove;
    }

    /**
     * Method: Scores a legal move using only information visible on the board.
     * Calls: foundationScore(), tableauMoveScore()
     */
    private double score(ObservableBoard board, Move move) {
        return switch (move.type()) {
            case DRAW -> DRAW_SCORE;
            case RECYCLE -> RECYCLE_SCORE;
            case FOUNDATION_TO_TABLEAU -> FOUNDATION_RETURN_SCORE;
            case WASTE_TO_TABLEAU -> WASTE_TRANSFER_SCORE;
            case WASTE_TO_FOUNDATION -> foundationScore(board, board.waste().get(board.waste().size() - 1));
            case TABLEAU_TO_TABLEAU, TABLEAU_TO_FOUNDATION -> tableauMoveScore(board, move);
        };
    }

    /**
     * Method: Scores a move that starts in the tableau, rewarding moves that uncover a hidden card.
     * Calls: VisibleCard.isHidden(), foundationScore()
     */
    private double tableauMoveScore(ObservableBoard board, Move move) {
        List<ObservableBoard.VisibleCard> sourceCards = board.tableau().get(move.source());
        int firstMovedCardIndex = sourceCards.size() - move.count();
        boolean revealsHiddenCard = firstMovedCardIndex > 0 && sourceCards.get(firstMovedCardIndex - 1).isHidden();
        double revealScore = revealsHiddenCard ? REVEAL_BONUS + firstMovedCardIndex : 0;
        if (move.type() == MoveType.TABLEAU_TO_FOUNDATION) {
            return revealScore + foundationScore(board, sourceCards.get(firstMovedCardIndex));
        }
        if (firstMovedCardIndex == 0 && board.tableau().get(move.destination()).isEmpty()) {
            return EMPTY_COLUMN_RELOCATION_SCORE;
        }
        int transferScore = firstMovedCardIndex == 0 ? EMPTY_SOURCE_SCORE : TABLEAU_TRANSFER_SCORE;
        return revealScore + transferScore + move.count() * SEQUENCE_LENGTH_BONUS;
    }

    /**
     * Method: Scores a move to a foundation. It is "safe" when the card can no longer be needed in the tableau:
     * other-suit foundations of the opposite color are at least one rank lower, and of the same color at least
     * two lower.
     * Calls: Suit.isRed(), VisibleCard.isRed()
     */
    private double foundationScore(ObservableBoard board, ObservableBoard.VisibleCard card) {
        boolean safe = true;
        if (card.rank() > ALWAYS_SAFE_RANK) {
            for (Suit suit : Suit.values()) {
                int requiredRank = suit.isRed() == card.isRed() ? card.rank() - 2 : card.rank() - 1;
                if (suit != card.suit() && board.foundations().get(suit.ordinal()) < requiredRank) {
                    safe = false;
                }
            }
        }
        return safe ? SAFE_FOUNDATION_SCORE : FOUNDATION_SCORE;
    }

    /**
     * Method: Returns milliseconds elapsed since the supplied start time.
     * Calls: System.nanoTime()
     */
    private static long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / NANOS_PER_MILLISECOND;
    }
}
