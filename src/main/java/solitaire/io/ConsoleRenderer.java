package solitaire.io;

import solitaire.game.GameState;
import solitaire.enums.GameStatus;
import solitaire.game.SolitaireGame;
import solitaire.enums.Suit;
import solitaire.move.Move;

/**
 * Displays the game board and play reports.
 *
 * @author Farhiya Ayyub
 */
public final class ConsoleRenderer {

    /** Prints a compact opening title. */
    public static void heading(String title) {
        System.out.println("\n" + title);
    }

    /** Prints a section title with minimal foreground color. */
    public static void section(String title) {
        System.out.println("\n  " + solitaire.enums.ConsoleColor.CYAN.paint(title));
    }
    /** Prints a board snapshot, draw mode, and game summary. */
    public void render(SolitaireGame game) {
        GameState board = game.state();
        ConsoleRenderer.section("STOCK & WASTE");
        System.out.println("  Draw " + board.drawCount());
        System.out.printf("  Stock: %s  (%d cards)    Waste: %s  (%d cards)%n",
                board.stock().size() == 0 ? "[     ]" : "[XX]", board.stock().size(),
                board.waste().top() == null ? "[     ]" : board.waste().top(), board.waste().size());
        ConsoleRenderer.section("FOUNDATIONS (Ace to King)");
        System.out.print("  ");
        for (Suit suit : Suit.values()) System.out.printf("%-17s", suit.symbol() + " " + suit.name());
        System.out.print("\n  ");
        for (Suit suit : Suit.values())
            System.out.printf("%-17s", board.foundation(suit).top() == null ? "[     ]" : board.foundation(suit).top());
        System.out.println();
        ConsoleRenderer.section("TABLEAU");
        System.out.print("  ");
        for (int column = 1; column <= GameState.TABLEAU_COLUMNS; column++) System.out.printf("%-9s", "T" + column);
        System.out.println();
        int rows = board.tableau().stream().mapToInt(pile -> pile.size()).max().orElse(0);
        for (int row = 0; row < Math.max(1, rows); row++) {
            System.out.print("  ");
            for (var pile : board.tableau())
                System.out.printf("%-9s", row < pile.size() ? pile.cards().get(row) : row == 0 ? "[     ]" : "");
            System.out.println();
        }
        System.out.println("\n  [XX] Face down    [     ] Empty pile");
        System.out.println("\n  Moves: " + game.moves() + " | " + statusText(game.status()));
    }

    /** Returns a readable message distinguishing victory, policy exhaustion, and execution limits. */
    public String statusText(GameStatus status) {
        return switch (status) {
            case RUNNING -> "In progress";
            case WON -> "Won - all 52 cards completed";
            case STUCK -> "Not solved - no new move to try";
            case SEARCH_LIMIT_REACHED -> "Not solved - play limit reached";
        };
    }

    /** Describes a move using one-based tableau labels and the actual card count. */
    public String describeMove(Move move) {
        int source = move.source() + 1;
        int destination = move.destination() + 1;
        return switch (move.type()) {
            case DRAW -> "Draw " + move.count() + (move.count() == 1 ? " card" : " cards") + " from the stock to the waste.";
            case RECYCLE -> "Return the waste cards to the stock.";
            case TABLEAU_TO_TABLEAU -> "Move " + move.count() + (move.count() == 1 ? " card" : " cards")
                    + " from T" + source + " to T" + destination + ".";
            case TABLEAU_TO_FOUNDATION -> "Move the top card from T" + source + " to the "
                    + Suit.values()[move.destination()].name().toLowerCase(java.util.Locale.ROOT) + " foundation.";
            case WASTE_TO_TABLEAU -> "Move the waste card to T" + destination + ".";
            case WASTE_TO_FOUNDATION -> "Move the waste card to the "
                    + Suit.values()[move.destination()].name().toLowerCase(java.util.Locale.ROOT) + " foundation.";
            case FOUNDATION_TO_TABLEAU -> "Move the top card from the "
                    + Suit.values()[move.source()].name().toLowerCase(java.util.Locale.ROOT)
                    + " foundation to T" + destination + ".";
        };
    }

    /** Reads or displays the shuffle selection; input returns null on cancellation. */
    public void shuffleConfiguration(solitaire.shuffle.ShuffleConfiguration configuration) {
        System.out.println("\nShuffle: " + switch (configuration.type()) {
            case RANDOM -> "Random Shuffle";
            case IN -> "In-Faro Shuffle";
            case OUT -> "Out-Faro Shuffle";
        });
    }

    /** Prints the solver's explanation of its result. */
    public void solverDetail(String detail) {
        System.out.println("  Result: " + detail);
    }
    /** Prints the action about to be replayed. */
    public void nextMove(Move move) {
        System.out.println("\nNext move: " + describeMove(move));
    }

    /** Prints the simulation report title. */
    public void simulationStarted() {
        heading("SOLITAIRE SIMULATION REPORT");
    }

    /** Prints the outcome and move count for one simulated game. */
    public void simulationGame(int gameNumber, GameStatus status, int moves) {
        System.out.printf(java.util.Locale.ROOT, "Game %d | %s | Moves: %d%n",
                gameNumber, statusText(status), moves);
    }

    /** Prints aggregate counts and averages across all simulated games. */
    public void simulationSummary(int count, int won, int limitedGames, int stuckGames, long moves, long totalDecisions) {
        section("RESULTS");
        System.out.printf(java.util.Locale.ROOT,
                "  Games: %d | Won: %d | Not solved: %d%n"
                + "  Limit reached: %d | No new moves: %d | Win rate: %.2f%%%n"
                + "  Average moves: %.2f | Average decisions: %.2f%n",
                count, won, count - won, limitedGames, stuckGames,
                100.0 * won / count, (double) moves / count, (double) totalDecisions / count);
    }
}
