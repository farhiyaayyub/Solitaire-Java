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
 * The automatic player. It scores every legal move using only the face-up cards and plays the best one it has not tried
 * yet.
 *
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
    private static final int ALWAYS_SAFE_RANK = 2;
    private static final long NANOS_PER_MILLISECOND = 1_000_000;
    private final SolitaireSolver.Limits limits;
    private final MoveGenerator generator = new MoveGenerator();

    /** Stores the move and time limits used for every game. */
    public VisibleSolver(SolitaireSolver.Limits limits) {
        this.limits = Objects.requireNonNull(limits);
    }

    /**
     * Plays a game on its own copy of the board. Each turn it checks for a win, picks the best untried move for the
     * current visible position, remembers it as tried, and plays it. It returns WON, STUCK when every move has been
     * tried, or LIMIT_REACHED when the move or time limit runs out.
     */
    @Override
    public Result solve(GameState initialBoard) {
        SolitaireGame game = new SolitaireGame(initialBoard);
        List<Move> playedMoves = new ArrayList<>();
        Map<ObservableBoard, Set<Move>> attemptedMoves = new HashMap<>();
        long startNanos = System.nanoTime();
        while (playedMoves.size() < limits.maxMoves() && elapsedMillis(startNanos) < limits.maxMillis()) {
            GameState currentBoard = game.state();
            if (currentBoard.won()) {
                return new Result(GameStatus.WON, playedMoves, "Auto-play won the game");
            }
            ObservableBoard board = ObservableBoard.from(currentBoard);
            Set<Move> attemptedAtPosition = attemptedMoves.computeIfAbsent(board, ignored -> new HashSet<>());
            Move move = choose(board, generator.generate(currentBoard), attemptedAtPosition);
            if (move == null) {
                return new Result(GameStatus.STUCK, playedMoves, "Stuck - every possible move has been tried");
            }
            attemptedAtPosition.add(move);
            game.play(move);
            playedMoves.add(move);
        }
        if (game.state().won()) {
            return new Result(GameStatus.WON, playedMoves, "Auto-play won the game");
        }
        return new Result(GameStatus.LIMIT_REACHED, playedMoves, "Stopped - ran out of moves or time");
    }

    /**
     * Scores every legal move that has not been tried yet in this position and returns the one with the highest score,
     * or null if every move has been tried.
     */
    private Move choose(ObservableBoard board, List<Move> legalMoves, Set<Move> attemptedMoves) {
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
     * Gives a move a score, where a higher score is preferred. Draw, recycle, foundation-to-tableau, and
     * waste-to-tableau moves have a fixed score, while column and foundation moves are scored from the board.
     */
    private double score(ObservableBoard board, Move move) {
        return switch (move.type()) {
            case DRAW -> DRAW_SCORE;
            case RECYCLE -> RECYCLE_SCORE;
            case FOUNDATION_TO_TABLEAU -> FOUNDATION_RETURN_SCORE;
            case WASTE_TO_TABLEAU -> WASTE_TRANSFER_SCORE;
            case WASTE_TO_FOUNDATION -> foundationScore(board, board.waste().getLast());
            case TABLEAU_TO_TABLEAU, TABLEAU_TO_FOUNDATION -> tableauMoveScore(board, move);
        };
    }

    /**
     * Scores a move that starts in a column. It adds a large bonus if the move uncovers a face-down card, gives a
     * negative score for moving a whole column onto another empty column, and otherwise prefers moves that empty a
     * column or move more cards.
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
     * Returns 500 if moving the card to its foundation is safe, or 12 if not. A card is safe when it is an Ace or a 2,
     * or when the other foundations are high enough that no tableau card will ever need it.
     */
    private double foundationScore(ObservableBoard board, ObservableBoard.VisibleCard card) {
        if (card.rank() <= ALWAYS_SAFE_RANK) {
            return SAFE_FOUNDATION_SCORE;
        }
        for (Suit suit : Suit.values()) {
            int requiredRank = suit.isRed() == card.isRed() ? card.rank() - 2 : card.rank() - 1;
            if (suit != card.suit() && board.foundations().get(suit.ordinal()) < requiredRank) {
                return FOUNDATION_SCORE;
            }
        }
        return SAFE_FOUNDATION_SCORE;
    }

    /** Returns how many milliseconds have passed since the start time. */
    private static long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / NANOS_PER_MILLISECOND;
    }
}
