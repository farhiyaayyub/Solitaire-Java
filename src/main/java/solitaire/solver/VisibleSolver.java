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
 * Greedy automatic player that uses only visible cards (Strategy pattern).
 *
 * <p>Kind: final class, implements SolitaireSolver.
 * <p>Purpose: before each move it scores every legal move from the visible cards and plays the best one not yet
 * tried in the same visible position. It never undoes a move or looks at hidden cards, so a STUCK result does not
 * prove the deal is unwinnable.
 * <p>Data: the Limits record, one MoveGenerator, and score constants. Per game, solve() keeps an ArrayList of played
 * moves and a HashMap&lt;ObservableBoard, Set&lt;Move&gt;&gt; of tried moves: records give equals/hashCode, so a
 * visible position is found again in O(1), and a HashSet answers "already tried?" in O(1).
 * <p>OOP: Polymorphism - GameController calls it through SolitaireSolver.solve(). Encapsulation - the scores and
 * scoring methods are private.
 *
 * @author Farhiya Ayyub
 */
public final class VisibleSolver implements SolitaireSolver {
    // Move preferences: the highest score is played, and negative scores only when nothing else untried is left.
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

    /**
     * Stores the move and time limits used for every game.
     *
     * <p>Called by: SolitaireApplication.getController().
     *
     * @param limits the limits
     * @throws NullPointerException if limits is {@code null}
     */
    public VisibleSolver(SolitaireSolver.Limits limits) {
        this.limits = Objects.requireNonNull(limits);
    }

    /**
     * Plays one game on its own copy of the board.
     *
     * <p>Algorithm: greedy search with memory. While the move and time limits are not reached:
     * <ol>
     *   <li>If the board is won, return WON.</li>
     *   <li>Hide the face-down cards to get the visible position.</li>
     *   <li>Pick the highest-scoring legal move not yet tried in this position; if none is left, return STUCK.</li>
     *   <li>Remember the move as tried here, then play it.</li>
     * </ol>
     * When a limit is reached, return WON if the last move won, otherwise LIMIT_REACHED.
     *
     * <p>Key idea: remembering the tried moves per visible position stops endless loops, such as moving one card back
     * and forth; computeIfAbsent() creates the empty set the first time a position is seen.
     *
     * <p>Called by: GameController.solveAndReplay() (through SolitaireSolver).
     *
     * @param initialBoard the starting board; it is not changed
     * @return WON, STUCK, or LIMIT_REACHED, with the moves played
     */
    @Override
    public Result solve(GameState initialBoard) {
        // The solver plays on its own game, so the board passed in is never changed.
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
            if (!game.play(move)) {
                throw new IllegalStateException("Visible policy selected an illegal move");
            }
            playedMoves.add(move);
        }
        if (game.state().won()) {
            return new Result(GameStatus.WON, playedMoves, "Auto-play won the game");
        }
        return new Result(GameStatus.LIMIT_REACHED, playedMoves, "Stopped - ran out of moves or time");
    }

    /**
     * {@return the best legal move not yet tried, or {@code null} if every legal move has been tried}
     *
     * <p>Key idea: one pass that keeps the maximum, O(number of moves). Only a strictly higher score replaces the
     * best, so on a tie the earlier move wins.
     *
     * <p>Called by: solve().
     *
     * @param board the visible position
     * @param legalMoves all legal moves in this position
     * @param attemptedMoves the moves already tried in this visible position
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
     * {@return the score of one legal move; higher means preferred} Draw, recycle, foundation-to-tableau, and
     * waste-to-tableau moves have a fixed score; the other types are calculated from the visible board.
     *
     * <p>Called by: choose().
     *
     * @param board the visible position
     * @param move the legal move to rate
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
     * {@return the score of a move that starts in a column}
     *
     * <p>Algorithm: scoring a column move.
     * <ol>
     *   <li>If the card under the lowest moving card is face down, add the reveal bonus (300) plus its position, so
     *     columns with more hidden cards are opened first.</li>
     *   <li>To a foundation: that bonus plus foundationScore().</li>
     *   <li>A whole column onto an empty column: -200, because it changes nothing useful.</li>
     *   <li>Otherwise: the bonus + 30 if the source column becomes empty (else 20) + 0.1 per card moved.</li>
     * </ol>
     *
     * <p>Called by: score().
     *
     * @param board the visible position
     * @param move a legal move that starts in a column
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
     * {@return 500 if moving the card to its foundation is safe, otherwise 12}
     *
     * <p>Key idea: safe foundation move. A card is safe when no tableau card could ever need it as a place to go:
     * <ol>
     *   <li>Aces and 2s are always safe.</li>
     *   <li>Otherwise every other foundation must be at least rank - 1 (opposite color) or rank - 2 (same color).</li>
     * </ol>
     *
     * <p>Called by: score(), tableauMoveScore().
     *
     * @param board the visible position
     * @param card the card that would move to its foundation
     */
    private double foundationScore(ObservableBoard board, ObservableBoard.VisibleCard card) {
        if (card.rank() <= ALWAYS_SAFE_RANK) {
            return SAFE_FOUNDATION_SCORE;
        }
        for (Suit suit : Suit.values()) {
            int requiredRank = suit.isRed() == card.isRed() ? card.rank() - 2 : card.rank() - 1;
            // One foundation that is too low is enough to make the move unsafe, so stop checking.
            if (suit != card.suit() && board.foundations().get(suit.ordinal()) < requiredRank) {
                return FOUNDATION_SCORE;
            }
        }
        return SAFE_FOUNDATION_SCORE;
    }

    /**
     * {@return the milliseconds passed since startNanos, rounded down} Called by: solve().
     *
     * @param startNanos a value from {@code System.nanoTime()}
     */
    private static long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / NANOS_PER_MILLISECOND;
    }
}
