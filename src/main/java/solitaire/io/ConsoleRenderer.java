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
 * Console display of the game.
 *
 * <p>Kind: final class, uses SolitaireGame, GameState, CardPile, and ConsoleColor.
 * <p>Purpose: prints everything the player sees: the board, moves, results, and simulation reports. It only reads
 * games and boards and never changes them, so the display can change without affecting the rules.
 * <p>Data: none; it only keeps the empty-slot text. renderTableau() turns the columns into a List of Lists, because
 * it needs index access to print row by row across the seven columns.
 * <p>OOP: Abstraction - other classes call render(game) without knowing the layout. Polymorphism - describeMove() and
 * topOrEmpty() work with any kind of CardPile.
 *
 * @author Farhiya Ayyub
 */
public final class ConsoleRenderer {
    private static final String EMPTY_SLOT = "[     ]";

    /**
     * Prints the title after a blank line, without color.
     *
     * <p>Called by: GameController.shuffleAndPlay(), GameController.playFromMenu(), simulationStarted().
     *
     * @param title the title text
     */
    public void heading(String title) {
        System.out.println("\n" + title);
    }

    /**
     * Prints the title after a blank line, indented and in cyan.
     *
     * <p>Called by: render(), showMoves(), simulationSummary(), renderTableau().
     *
     * @param title the title text
     */
    public void section(String title) {
        System.out.println("\n  " + ConsoleColor.CYAN.paint(title));
    }

    /**
     * Prints the whole board from a copy of the game's state: the draw mode, the stock (always shown as [XX]) and the
     * waste top card with their sizes, the top card of each foundation, the seven columns, a legend, and the move
     * count and status.
     *
     * <p>Called by: GameController.playShuffled(), GameController.guaranteed(), GameController.manual(),
     * GameController.replay().
     *
     * @param game the game to print
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

    /**
     * {@return the status in words, such as "Won - all 52 cards completed"}
     *
     * <p>Called by: render(), simulationGame().
     *
     * @param status the status to describe
     */
    public String statusText(GameStatus status) {
        return switch (status) {
            case RUNNING -> "In progress";
            case WON -> "Won - all 52 cards completed";
            case STUCK -> "Not solved - no new move to try";
            case LIMIT_REACHED -> "Not solved - play limit reached";
        };
    }

    /**
     * {@return the move as a sentence that names the card being moved, read from the board before the move is played}
     * Examples: "Move [C-A] from T1 to the clubs foundation.", "Move [H-5] and the 2 cards on it from T3 to T6, onto
     * [S-6]." A move to an empty column ends with "(empty)". Draw and recycle name no card, because the stock cards are
     * still face down.
     *
     * <p>Called by: showMoves(), nextMove().
     *
     * @param move the move to describe
     * @param board the board before the move is played
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
        // For a run of cards this is the lowest one, the card that must fit on the destination.
        Card moving = from.get(from.size() - move.count());
        String cards = move.count() == 1 ? moving.toString()
                : moving + " and the " + cardCount(move.count() - 1) + " on it";
        String landing = move.type().endsOnTableau() ? (to.isEmpty() ? " (empty)" : ", onto " + to.top()) : "";
        // name() is the method of the actual pile (polymorphism), so no check of the pile's class is needed.
        return "Move " + cards + " from " + from.name(move.source()) + " to " + to.name(move.destination())
                + landing + ".";
    }

    /**
     * Prints the LEGAL MOVES title and the moves numbered from 1, or a message if there are none.
     *
     * <p>Called by: GameController.manual().
     *
     * @param moves the legal moves, in the order the player will number them
     * @param board the current board, used to name the cards in each move
     */
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

    /**
     * Prints "Shuffle: " followed by the strategy's name, such as Random or In Faro.
     *
     * <p>Called by: GameController.playShuffled(), GameController.simulate().
     *
     * @param configuration the settings whose shuffle name is printed
     */
    public void shuffleConfiguration(ShuffleConfiguration configuration) {
        System.out.println("\nShuffle: " + configuration.type().strategy().name());
    }

    /**
     * Prints "Playing... (Draw 1)" or "Playing... (Draw 3)".
     *
     * <p>Called by: GameController.playShuffled(), GameController.simulate().
     *
     * @param drawCount cards turned per draw: 1 or 3
     */
    public void playing(int drawCount) {
        System.out.println("Playing... (Draw " + drawCount + ")");
    }

    /**
     * Prints "Next move: " and the move sentence in yellow. The board must be the one before the move is played, so the
     * sentence names the card that is about to move.
     *
     * <p>Called by: GameController.replay().
     *
     * @param move the move about to be played
     * @param board the board before the move is played
     */
    public void nextMove(Move move, GameState board) {
        System.out.println("\nNext move: " + ConsoleColor.YELLOW.paint(describeMove(move, board)));
    }

    /**
     * Prints "Result: " followed by the detail, such as "Auto-play won the game".
     *
     * <p>Called by: GameController.playShuffled(), GameController.guaranteed().
     *
     * @param detail the text after "Result:"
     */
    public void resultDetail(String detail) {
        System.out.println("  Result: " + detail);
    }

    /** Prints the heading "SOLITAIRE SIMULATION REPORT". Called by: GameController.simulate(). */
    public void simulationStarted() {
        heading("SOLITAIRE SIMULATION REPORT");
    }

    /**
     * Prints one line for a simulated game, such as "Game 3 | Won - all 52 cards completed | Moves: 112".
     *
     * <p>Called by: GameController.simulate().
     *
     * @param gameNumber the game number, starting at 1
     * @param status how the game ended
     * @param moves how many moves were played
     */
    public void simulationGame(int gameNumber, GameStatus status, int moves) {
        System.out.printf(Locale.ROOT, "Game %d | %s | Moves: %d%n", gameNumber, statusText(status), moves);
    }

    /**
     * Prints the games won and not solved (split into limit reached and no new moves), the win rate as a percentage,
     * and the average moves per game, each with 2 decimal places.
     *
     * <p>Formula: win rate = 100.0 * won / count, and average moves = moves / count. The 100.0 (a double) makes the
     * division keep its decimals; 100 * won / count would be integer division.
     *
     * <p>Called by: GameController.simulate().
     *
     * @param count how many games were played
     * @param won games won
     * @param limitedGames games that reached a limit
     * @param stuckGames games that got stuck
     * @param moves moves played in all games together
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

    /**
     * Prints the elapsed time, converted from nanoseconds to seconds with 1 decimal place.
     *
     * <p>Called by: GameController.simulate().
     *
     * @param elapsedNanos the time taken, in nanoseconds
     */
    public void elapsed(long elapsedNanos) {
        System.out.printf(Locale.ROOT, "  Elapsed: %.1f seconds%n", elapsedNanos / 1_000_000_000.0);
    }

    /** Prints "Thank you for playing!" when the player leaves the menu. Called by: GameController.shuffleAndPlay(). */
    public void showClosingMessage() {
        System.out.println("Thank you for playing!");
    }

    /**
     * Prints the seven columns side by side, one card per row, from the bottom card down to the top card.
     *
     * <p>Key idea: the number of rows is the height of the tallest column; shorter columns get blank cells, and an
     * empty column shows an empty slot in the first row so it stays visible.
     *
     * <p>Called by: render().
     *
     * @param board the board whose columns are printed
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

    /**
     * {@return the top card's text, or the empty slot if the pile is empty, followed by spaces up to width} Works for
     * the waste and for every foundation.
     *
     * <p>Called by: render().
     *
     * @param pile the pile to show
     * @param width the number of characters to fill
     */
    private static String topOrEmpty(CardPile pile, int width) {
        return pile.isEmpty() ? pad(EMPTY_SLOT, width) : showCard(pile.top(), width);
    }

    /**
     * {@return the card's text followed by spaces up to width} Face-up Hearts and Diamonds are red; Clubs, Spades, and
     * face-down cards keep the terminal's normal color, so black suits stay readable on a dark background. The spaces
     * are counted from the plain text, because the invisible color codes would otherwise break the column alignment.
     *
     * <p>Called by: renderTableau(), topOrEmpty().
     *
     * @param card the card to show
     * @param width the number of characters to fill
     */
    private static String showCard(Card card, int width) {
        String text = card.toString();
        String shown = card.isFaceUp() && card.isRed() ? ConsoleColor.RED.paint(text) : text;
        return shown + " ".repeat(Math.max(0, width - text.length()));
    }

    /**
     * {@return the text followed by spaces up to width} Text that is already longer is unchanged.
     *
     * <p>Called by: renderTableau(), topOrEmpty().
     *
     * @param text the text to pad
     * @param width the number of characters to fill
     */
    private static String pad(String text, int width) {
        return text + " ".repeat(Math.max(0, width - text.length()));
    }

    /**
     * {@return "1 card" for 1 and "N cards" for any other number} Called by: describeMove().
     *
     * @param count the number of cards
     */
    private static String cardCount(int count) {
        return count + (count == 1 ? " card" : " cards");
    }
}
