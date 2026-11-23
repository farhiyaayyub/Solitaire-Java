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
 * The automatic player. Before each move it looks only at the visible cards, gives every legal move a score, and
 * plays the highest-scoring move it has not already tried in the same visible position. Moves are never undone.
 * Because it cannot see hidden cards or go back, a STUCK result does not prove that the deal cannot be won.
 *
 * <p>Polymorphism: it implements SolitaireSolver.solve(). Encapsulation: the scores and helper methods are private.
 *
 * @author Farhiya Ayyub
 */
public final class VisibleSolver implements SolitaireSolver {
    // Scores used by score(). The legal move with the highest score is played; moves with negative scores are
    // played only when no other untried move is left.
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
     * Stores the decision, move, and time limits used for every game.
     *
     * @param limits the decision, move, and time limits for every game
     * @throws NullPointerException if limits is {@code null}
     */
    public VisibleSolver(SolitaireSolver.Limits limits) {
        this.limits = Objects.requireNonNull(limits);
    }

    /**
     * Plays one game on its own copy of the board and returns how it ended, with the moves played.
     *
     * <p>Algorithm: greedy search with memory. It always takes the best-looking move and never undoes one.
     * Each turn, while the decision, move, and time limits are not reached:
     * <ol>
     *   <li>If the board is won, return WON.</li>
     *   <li>Hide the face-down cards to get the visible position.</li>
     *   <li>List the legal moves and pick the highest-scoring one not yet tried in this position.</li>
     *   <li>If every move was already tried here, return STUCK.</li>
     *   <li>Remember the move as tried in this position, then play it.</li>
     * </ol>
     * When a limit is reached, return WON if the last move won the game, otherwise LIMIT_REACHED.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@code elapsedMillis()} - checks the time limit</li>
     *   <li>{@link SolitaireGame#state()} - gets a copy of the board</li>
     *   <li>{@link GameState#won} - stops when all 52 cards are home</li>
     *   <li>{@link ObservableBoard#from} - hides face-down cards</li>
     *   <li>{@link MoveGenerator#generate} - lists the legal moves</li>
     *   <li>{@link #choose} - picks the best untried move</li>
     *   <li>{@link SolitaireGame#play} - plays the chosen move</li>
     * </ul>
     *
     * <p>Called by: {@code GameController.solveAndReplay()} (through SolitaireSolver).
     *
     * @param initialBoard the starting board; it is not changed
     * @return WON, STUCK, or LIMIT_REACHED, with the moves played and the decision count
     */
    @Override
    public Result solve(GameState initialBoard) {
        // The solver plays on its own game, so the board passed in is never changed.
        SolitaireGame game = new SolitaireGame(initialBoard);
        List<Move> playedMoves = new ArrayList<>();
        Map<ObservableBoard, Set<Move>> attemptedMoves = new HashMap<>();
        long startNanos = System.nanoTime();
        int decisionCount = 0;
        while (decisionCount < limits.maxDecisions() && playedMoves.size() < limits.maxMoves()
                && elapsedMillis(startNanos) < limits.maxMillis()) {
            GameState currentBoard = game.state();
            if (currentBoard.won()) {
                return new Result(GameStatus.WON, playedMoves, decisionCount, "Auto-play won the game");
            }
            // Moves already tried are remembered per visible position; this stops the solver from repeating a
            // loop such as moving the same card back and forth.
            ObservableBoard board = ObservableBoard.from(currentBoard);
            Set<Move> attemptedAtPosition = attemptedMoves.computeIfAbsent(board, ignored -> new HashSet<>());
            Move move = choose(board, generator.generate(currentBoard), attemptedAtPosition);
            decisionCount++;
            if (move == null) {
                return new Result(GameStatus.STUCK, playedMoves, decisionCount,
                        "Stuck - every possible move has been tried");
            }
            attemptedAtPosition.add(move);
            if (!game.play(move)) {
                throw new IllegalStateException("Visible policy selected an illegal move");
            }
            playedMoves.add(move);
        }
        if (game.state().won()) {
            return new Result(GameStatus.WON, playedMoves, decisionCount, "Auto-play won the game");
        }
        return new Result(GameStatus.LIMIT_REACHED, playedMoves, decisionCount,
                "Stopped - ran out of moves or time");
    }

    /**
     * Returns the best legal move not yet tried, or {@code null} if every legal move has been tried.
     *
     * <p>Algorithm: find the maximum in one pass, O(number of moves).
     * <ol>
     *   <li>Start with no best move and the lowest possible score.</li>
     *   <li>For each legal move: skip it if already tried; otherwise score it.</li>
     *   <li>Keep it if its score is strictly higher than the best so far, so on a tie the earlier move stays.</li>
     *   <li>Return the best move ({@code null} if all were skipped).</li>
     * </ol>
     *
     * <p>Calls:
     * <ul>
     *   <li>{@code score()} - rates each untried move</li>
     * </ul>
     *
     * <p>Called by: {@link #solve}.
     *
     * @param board the visible position
     * @param legalMoves all legal moves in this position
     * @param attemptedMoves the moves already tried in this visible position
     * @return the best legal move not yet tried, or {@code null} if every legal move has been tried
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
     * Returns the score of one legal move, using only the visible board: a fixed score for draw, recycle,
     * foundation-to-tableau, and waste-to-tableau moves, and a calculated score for the other types.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@code foundationScore()} - rates a waste-to-foundation move</li>
     *   <li>{@code tableauMoveScore()} - rates a move from a column</li>
     * </ul>
     *
     * <p>Called by: {@link #choose}.
     *
     * @param board the visible position
     * @param move the legal move to rate
     * @return the move's score; a higher score means the move is preferred
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
     * Returns the score of a move that starts in a column.
     *
     * <p>Steps:
     * <ol>
     *   <li>Find the lowest moving card. If the card under it is face down, add the reveal bonus (300) plus its
     *     position, so columns with more hidden cards are opened first.</li>
     *   <li>To a foundation: return that bonus plus foundationScore().</li>
     *   <li>A whole column onto an empty column: return -200, because it changes nothing useful.</li>
     *   <li>Otherwise: bonus + 30 if the source column becomes empty (else 20) + 0.1 per card moved.</li>
     * </ol>
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link ObservableBoard.VisibleCard#isHidden} - checks if the move uncovers a hidden card</li>
     *   <li>{@code foundationScore()} - rates a column-to-foundation move</li>
     * </ul>
     *
     * <p>Called by: {@code score()}.
     *
     * @param board the visible position
     * @param move a legal move that starts in a column
     * @return the score of a move that starts in a column
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
     * Returns 500 if moving the card to its foundation is safe, otherwise 12. A safe card can never be needed again in
     * the tableau.
     *
     * <p>Steps:
     * <ol>
     *   <li>Aces and 2s are always safe.</li>
     *   <li>For each other suit: the needed rank is card rank - 1 for an opposite color, rank - 2 for the same
     *     color.</li>
     *   <li>If any of those foundations is below its needed rank, the move is not safe.</li>
     * </ol>
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link Suit#isRed} - gets each foundation's color</li>
     *   <li>{@link ObservableBoard.VisibleCard#isRed} - gets the moving card's color</li>
     * </ul>
     *
     * <p>Called by: {@code score()}, {@code tableauMoveScore()}.
     *
     * @param board the visible position
     * @param card the card that would move to its foundation
     * @return 500 if moving the card to its foundation is safe, otherwise 12
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
     * Returns the whole milliseconds passed since startNanos.
     *
     * <p>Called by: {@link #solve}.
     *
     * @param startNanos a value from {@code System.nanoTime()}
     * @return the elapsed time in milliseconds, rounded down
     */
    private static long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / NANOS_PER_MILLISECOND;
    }
}
