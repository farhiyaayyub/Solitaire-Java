package solitaire.io;

import java.util.List;
import java.util.Locale;

import solitaire.enums.GameStatus;
import solitaire.enums.MoveType;
import solitaire.enums.Suit;
import solitaire.game.GameState;
import solitaire.game.SolitaireGame;
import solitaire.model.Card;
import solitaire.move.Move;
import solitaire.pile.CardPile;
import solitaire.shuffle.ShuffleConfiguration;

/**
 * Prints everything the player sees: the board, the moves, the results, and the simulation reports.
 *
 * @author Farhiya Ayyub
 */
public final class ConsoleRenderer {
    private static final String EMPTY_SLOT = "[     ]";

    /** Prints a title after a blank line. */
    public void heading(String title) {
        System.out.println("\n" + title);
    }

    /** Prints a section title after a blank line, indented and in cyan. */
    public void section(String title) {
        System.out.println("\n  " + ConsoleColor.CYAN.paint(title));
    }

    /**
     * Prints the whole board: the draw mode, the stock and the waste with their sizes, the top card of each foundation,
     * the seven columns, a legend, and the move count and status.
     */
    public void render(SolitaireGame game) {
        GameState board = game.state();
        section("STOCK & WASTE");
        System.out.println("  Draw " + board.drawCount());
        System.out.printf("  Stock: %s  (%d cards)    Waste: %s  (%d cards)%n",
                board.stock().isEmpty() ? EMPTY_SLOT : "[XX]", board.stock().size(),
                topOrEmpty(board.waste(), 0), board.waste().size());
        section("FOUNDATIONS (Ace to King)");
        System.out.print("  ");
        for (Suit suit : Suit.values()) {
            System.out.printf("%-17s", suit.symbol() + " " + suit.name());
        }
        System.out.print("\n  ");
        for (Suit suit : Suit.values()) {
            System.out.print(topOrEmpty(board.foundation(suit), 17));
        }
        System.out.println();
        renderTableau(board);
        System.out.println("\n  [XX] Face down    " + EMPTY_SLOT + " Empty pile");
        System.out.println("\n  Moves: " + game.moves() + " | " + statusText(game.status()));
    }

    /** Turns a status into words for the screen, such as "Won - all 52 cards completed". */
    public String statusText(GameStatus status) {
        return switch (status) {
            case RUNNING -> "In progress";
            case WON -> "Won - all 52 cards completed";
            case STUCK -> "Not solved - no new move to try";
            case LIMIT_REACHED -> "Not solved - play limit reached";
        };
    }

    /**
     * Turns a move into a sentence for the player, such as "Move [H-5] from T2 to T4, onto [S-6].". It reads the board
     * before the move is played, so it can name the card being moved and where it lands.
     */
    public String describeMove(Move move, GameState board) {
        if (move.type() == MoveType.DRAW) {
            return "Draw " + cardCount(move.count()) + " from the stock to the waste.";
        }
        if (move.type() == MoveType.RECYCLE) {
            return "Return the waste cards to the stock.";
        }
        CardPile from = board.sourcePile(move);
        CardPile to = board.destinationPile(move);
        Card moving = from.get(from.size() - move.count());
        String cards = move.count() == 1 ? moving.toString()
                : moving + " and the " + cardCount(move.count() - 1) + " on it";
        String landing = move.type().endsOnTableau() ? (to.isEmpty() ? " (empty)" : ", onto " + to.top()) : "";
        return "Move " + cards + " from " + from.name(move.source()) + " to " + to.name(move.destination())
                + landing + ".";
    }

    /** Prints the LEGAL MOVES title and every move numbered from 1, or a message if there are no moves. */
    public void showMoves(List<Move> moves, GameState board) {
        section("LEGAL MOVES");
        if (moves.isEmpty()) {
            System.out.println("  No legal moves available. Game ended.");
            return;
        }
        for (int moveIndex = 0; moveIndex < moves.size(); moveIndex++) {
            System.out.printf("  [%2d] %s%n", moveIndex + 1, describeMove(moves.get(moveIndex), board));
        }
    }

    /** Prints the name of the chosen shuffle, such as "Shuffle: Random". */
    public void shuffleConfiguration(ShuffleConfiguration configuration) {
        System.out.println("\nShuffle: " + configuration.type().strategy().name());
    }

    /** Prints "Playing..." with the draw mode, such as "Playing... (Draw 1)". */
    public void playing(int drawCount) {
        System.out.println("Playing... (Draw " + drawCount + ")");
    }

    /** Prints the next move as a sentence in yellow. */
    public void nextMove(Move move, GameState board) {
        System.out.println("\nNext move: " + ConsoleColor.YELLOW.paint(describeMove(move, board)));
    }

    /** Prints the result line of a game, such as "Result: Auto-play won the game". */
    public void resultDetail(String detail) {
        System.out.println("  Result: " + detail);
    }

    /** Prints the heading of the simulation report. */
    public void simulationStarted() {
        heading("SOLITAIRE SIMULATION REPORT");
    }

    /** Prints one line for a simulated game with its number, its result, and its move count. */
    public void simulationGame(int gameNumber, GameStatus status, int moves) {
        System.out.printf(Locale.ROOT, "Game %d | %s | Moves: %d%n", gameNumber, statusText(status), moves);
    }

    /**
     * Prints the simulation totals: games won and not solved, how many reached the limit or got stuck, the win rate as
     * a percentage, and the average moves per game.
     */
    public void simulationSummary(int count, int won, int limitedGames, int stuckGames, long moves) {
        section("RESULTS");
        System.out.printf(Locale.ROOT,
                "  Games: %d | Won: %d | Not solved: %d%n"
                        + "  Limit reached: %d | No new moves: %d | Win rate: %.2f%%%n"
                        + "  Average moves: %.2f%n",
                count, won, count - won, limitedGames, stuckGames,
                100.0 * won / count, (double) moves / count);
    }

    /** Converts the elapsed time from nanoseconds to seconds and prints it. */
    public void elapsed(long elapsedNanos) {
        System.out.printf(Locale.ROOT, "  Elapsed: %.1f seconds%n", elapsedNanos / 1_000_000_000.0);
    }

    /** Prints "Thank you for playing!". */
    public void showClosingMessage() {
        System.out.println("Thank you for playing!");
    }

    /**
     * Prints the seven columns side by side, one row per card, from the bottom card down to the top card. An empty
     * column shows an empty slot.
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
                System.out.print(row < column.size() ? showCard(column.get(row), 9)
                        : pad(row == 0 ? EMPTY_SLOT : "", 9));
            }
            System.out.println();
        }
    }

    /** Returns the top card's text, or an empty slot if the pile has no cards, padded to the given width. */
    private static String topOrEmpty(CardPile pile, int width) {
        return pile.isEmpty() ? pad(EMPTY_SLOT, width) : showCard(pile.top(), width);
    }

    /** Returns the card's text padded to the given width, in red if it is a face-up Heart or Diamond. */
    private static String showCard(Card card, int width) {
        String text = card.toString();
        String shown = card.isFaceUp() && card.isRed() ? ConsoleColor.RED.paint(text) : text;
        return shown + " ".repeat(Math.max(0, width - text.length()));
    }

    /** Adds spaces after the text until it reaches the given width, so the columns line up. */
    private static String pad(String text, int width) {
        return text + " ".repeat(Math.max(0, width - text.length()));
    }

    /** Returns "1 card" for 1, or "N cards" for any other number. */
    private static String cardCount(int count) {
        return count + (count == 1 ? " card" : " cards");
    }
}
