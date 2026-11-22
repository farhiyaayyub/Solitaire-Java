package solitaire.io;

import java.util.List;
import java.util.Locale;

import solitaire.enums.GameStatus;
import solitaire.enums.Suit;
import solitaire.game.GameState;
import solitaire.game.SolitaireGame;
import solitaire.model.Card;
import solitaire.move.Move;
import solitaire.pile.CardPile;
import solitaire.shuffle.ShuffleConfiguration;

/**
 * Prints the board, moves, and reports to the console. It only displays; it never changes a game.
 *
 * <p>Type: final class; all screen output goes through this class (display is kept separate from game logic).
 * OOP: ABSTRACTION (other classes just call render(game) without knowing the layout),
 * POLYMORPHISM (topOrEmpty works with any CardPile).
 *
 * @author Farhiya Ayyub
 */
public final class ConsoleRenderer {
    // Constant: how an empty pile is shown.
    private static final String EMPTY_SLOT = "[     ]";

    /** Method: Prints a plain title. */
    public void heading(String title) {
        System.out.println("\n" + title);
    }

    /**
     * Method: Prints a colored section title.
     * Calls: ConsoleColor.paint()
     */
    public void section(String title) {
        System.out.println("\n  " + ConsoleColor.CYAN.paint(title));
    }

    /**
     * Method: Prints the stock, waste, foundations, tableau, move count, and status.
     * Calls: SolitaireGame.state(), section(), topOrEmpty(), renderTableau(), SolitaireGame.moves(),
     * SolitaireGame.status(), statusText()
     */
    public void render(SolitaireGame game) {
        GameState board = game.state();
        section("STOCK & WASTE");
        System.out.println("  Draw " + board.drawCount());
        System.out.printf("  Stock: %s  (%d cards)    Waste: %s  (%d cards)%n",
                board.stock().isEmpty() ? EMPTY_SLOT : "[XX]", board.stock().size(),
                topOrEmpty(board.waste()), board.waste().size());
        section("FOUNDATIONS (Ace to King)");
        System.out.print("  ");
        for (Suit suit : Suit.values()) {
            System.out.printf("%-17s", suit.symbol() + " " + suit.name());
        }
        System.out.print("\n  ");
        for (Suit suit : Suit.values()) {
            System.out.printf("%-17s", topOrEmpty(board.foundation(suit)));
        }
        System.out.println();
        renderTableau(board);
        System.out.println("\n  [XX] Face down    " + EMPTY_SLOT + " Empty pile");
        System.out.println("\n  Moves: " + game.moves() + " | " + statusText(game.status()));
    }

    /** Method: Returns a readable message for a game status. */
    public String statusText(GameStatus status) {
        return switch (status) {
            case RUNNING -> "In progress";
            case WON -> "Won - all 52 cards completed";
            case STUCK -> "Not solved - no new move to try";
            case LIMIT_REACHED -> "Not solved - play limit reached";
        };
    }

    /**
     * Method: Describes a move in plain words, using one-based column labels T1 to T7.
     * Calls: cardCount(), suitName()
     */
    public String describeMove(Move move) {
        int source = move.source() + 1;
        int destination = move.destination() + 1;
        return switch (move.type()) {
            case DRAW -> "Draw " + cardCount(move.count()) + " from the stock to the waste.";
            case RECYCLE -> "Return the waste cards to the stock.";
            case TABLEAU_TO_TABLEAU -> "Move " + cardCount(move.count()) + " from T" + source + " to T" + destination
                    + ".";
            case TABLEAU_TO_FOUNDATION -> "Move the top card from T" + source + " to the "
                    + suitName(move.destination()) + " foundation.";
            case WASTE_TO_TABLEAU -> "Move the waste card to T" + destination + ".";
            case WASTE_TO_FOUNDATION -> "Move the waste card to the " + suitName(move.destination()) + " foundation.";
            case FOUNDATION_TO_TABLEAU -> "Move the top card from the " + suitName(move.source())
                    + " foundation to T" + destination + ".";
        };
    }

    /**
     * Method: Prints the selected shuffle.
     * Calls: ShuffleType.strategy(), ShuffleStrategy.name() (the chosen shuffle's own version)
     */
    public void shuffleConfiguration(ShuffleConfiguration configuration) {
        System.out.println("\nShuffle: " + configuration.type().strategy().name());
    }

    /** Method: Prints the draw mode that is about to be played. */
    public void playing(int drawCount) {
        System.out.println("Playing... (Draw " + drawCount + ")");
    }

    /**
     * Method: Prints the move about to be replayed.
     * Calls: describeMove()
     */
    public void nextMove(Move move) {
        System.out.println("\nNext move: " + describeMove(move));
    }

    /** Method: Prints a short explanation of how the game ended. */
    public void resultDetail(String detail) {
        System.out.println("  Result: " + detail);
    }

    /**
     * Method: Prints the simulation report title.
     * Calls: heading()
     */
    public void simulationStarted() {
        heading("SOLITAIRE SIMULATION REPORT");
    }

    /**
     * Method: Prints the outcome and move count for one simulated game.
     * Calls: statusText()
     */
    public void simulationGame(int gameNumber, GameStatus status, int moves) {
        System.out.printf(Locale.ROOT, "Game %d | %s | Moves: %d%n", gameNumber, statusText(status), moves);
    }

    /**
     * Method: Prints totals and averages across all simulated games.
     * Calls: section()
     */
    public void simulationSummary(int count, int won, int limitedGames, int stuckGames, long moves,
                                  long totalDecisions) {
        section("RESULTS");
        System.out.printf(Locale.ROOT,
                "  Games: %d | Won: %d | Not solved: %d%n"
                        + "  Limit reached: %d | No new moves: %d | Win rate: %.2f%%%n"
                        + "  Average moves: %.2f | Average decisions: %.2f%n",
                count, won, count - won, limitedGames, stuckGames,
                100.0 * won / count, (double) moves / count, (double) totalDecisions / count);
    }

    /** Method: Prints how long a simulation took. */
    public void elapsed(long elapsedNanos) {
        System.out.printf(Locale.ROOT, "  Elapsed: %.1f seconds%n", elapsedNanos / 1_000_000_000.0);
    }

    /** Method: Prints the closing message. */
    public void goodbye() {
        System.out.println("Thank you for playing!");
    }

    /**
     * Method: Prints the seven tableau columns side by side.
     * Calls: section(), GameState.tableau(), CardPile.cards()
     */
    private void renderTableau(GameState board) {
        section("TABLEAU");
        System.out.print("  ");
        for (int column = 1; column <= GameState.TABLEAU_COLUMNS; column++) {
            System.out.printf("%-9s", "T" + column);
        }
        System.out.println();
        List<List<Card>> columns = board.tableau().stream().map(CardPile::cards).toList();
        int rows = columns.stream().mapToInt(List::size).max().orElse(0);
        for (int row = 0; row < Math.max(1, rows); row++) {
            System.out.print("  ");
            for (List<Card> column : columns) {
                Object cell = row < column.size() ? column.get(row) : row == 0 ? EMPTY_SLOT : "";
                System.out.printf("%-9s", cell);
            }
            System.out.println();
        }
    }

    /**
     * Method: Returns the top card's text for any kind of pile, or an empty slot.
     * Calls: CardPile.isEmpty(), CardPile.top(), Card.toString()
     */
    private static String topOrEmpty(CardPile pile) {
        return pile.isEmpty() ? EMPTY_SLOT : pile.top().toString();
    }

    /** Method: Returns "1 card" or "N cards". */
    private static String cardCount(int count) {
        return count + (count == 1 ? " card" : " cards");
    }

    /** Method: Returns the lower-case suit name for a foundation index. */
    private static String suitName(int foundationIndex) {
        return Suit.values()[foundationIndex].name().toLowerCase(Locale.ROOT);
    }
}
