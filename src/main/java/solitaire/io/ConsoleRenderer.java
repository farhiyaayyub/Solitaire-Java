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
import solitaire.pile.FoundationPile;
import solitaire.pile.TableauPile;
import solitaire.pile.WastePile;
import solitaire.shuffle.ShuffleConfiguration;

/**
 * Prints everything the player sees: the board, moves, results, and simulation reports. It only reads games and
 * boards and never changes them, so the display can be changed without affecting the rules.
 *
 * <p>Abstraction: other classes call render(game) without knowing the layout. Polymorphism: topOrEmpty() works with any
 * kind of CardPile.
 *
 * @author Farhiya Ayyub
 */
public final class ConsoleRenderer {
    // Printed in place of the top card of an empty pile.
    private static final String EMPTY_SLOT = "[     ]";

    /**
     * Prints the title after a blank line, without color.
     *
     * <p>Called by: {@code GameController.shuffleAndPlay()}, {@code GameController.playFromMenu()},
     * {@link #simulationStarted}.
     *
     * @param title the title text
     */
    public void heading(String title) {
        System.out.println("\n" + title);
    }

    /**
     * Prints the title after a blank line, indented and in cyan.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link ConsoleColor#paint} - colors the title cyan</li>
     * </ul>
     *
     * <p>Called by: {@link ConsoleInput#selectMove}, {@link #render}, {@link #simulationSummary},
     * {@code renderTableau()}.
     *
     * @param title the title text
     */
    public void section(String title) {
        System.out.println("\n  " + ConsoleColor.CYAN.paint(title));
    }

    /**
     * Prints the whole board from a copy of the game's state: the draw mode, the stock (always as [XX]) and waste top
     * card with their sizes, the top card of each foundation, the seven columns, a legend, and finally the move count
     * and status line.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link SolitaireGame#state()} - gets a copy of the board to print</li>
     *   <li>{@link #section} - prints each section title</li>
     *   <li>{@link GameState#drawCount()} - prints Draw 1 or Draw 3</li>
     *   <li>{@link GameState#stock()} - gets the stock</li>
     *   <li>{@link CardPile#isEmpty} - shows an empty slot for an empty stock</li>
     *   <li>{@link CardPile#size} - prints how many cards each pile has</li>
     *   <li>{@link GameState#waste()} - gets the waste</li>
     *   <li>{@code topOrEmpty()} - shows the top waste and foundation cards</li>
     *   <li>{@link Suit#symbol()} - prints the suit symbol above each foundation</li>
     *   <li>{@link GameState#foundation} - gets each foundation</li>
     *   <li>{@code renderTableau()} - prints the seven columns</li>
     *   <li>{@link SolitaireGame#moves()} - gets the move count</li>
     *   <li>{@link SolitaireGame#status()} - gets the game status</li>
     *   <li>{@link #statusText} - turns the status into words</li>
     * </ul>
     *
     * <p>Called by: {@code GameController.automatic()}, {@code GameController.guaranteed()},
     * {@code GameController.manual()}, {@code GameController.replay()}.
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
     * Returns the status line text, such as "Won - all 52 cards completed".
     *
     * <p>Called by: {@link #render}, {@link #simulationGame}.
     *
     * @param status the status to describe
     * @return the status line text, such as "Won - all 52 cards completed"
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
     * Returns the move as a sentence that names the card being moved, read from the board before the move is played.
     * Examples: "Move [C-A] from T1 to the clubs foundation.", "Move [H-5] and the 2 cards on it from T3 to T6, onto
     * [S-6]." A move to an empty column ends with "(empty)". Draw and recycle name no card, because the stock cards are
     * still face down.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@code cardCount()} - writes "1 card" or "N cards"</li>
     *   <li>{@link GameState#sourcePile} - finds the pile the cards leave</li>
     *   <li>{@link GameState#destinationPile} - finds the pile they go to</li>
     *   <li>{@link CardPile#size} - finds the position of the lowest moving card</li>
     *   <li>{@link CardPile#get} - gets the lowest card being moved</li>
     *   <li>{@link Card#toString} - writes the card as [H-A]</li>
     *   <li>{@link CardPile#isEmpty} - writes "(empty)" for an empty column</li>
     *   <li>{@link CardPile#top} - names the card the move lands on</li>
     *   <li>{@code pileName()} - writes T1 to T7, "the waste", or the foundation</li>
     * </ul>
     *
     * <p>Called by: {@link ConsoleInput#selectMove}, {@link #nextMove}.
     *
     * @param move the move to describe
     * @param board the board before the move is played
     * @return the move as a sentence that names the card being moved, read from the board before the move is played
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
        String landing = to instanceof TableauPile ? (to.isEmpty() ? " (empty)" : ", onto " + to.top()) : "";
        return "Move " + cards + " from " + pileName(from, move.source()) + " to "
                + pileName(to, move.destination()) + landing + ".";
    }

    /**
     * Returns how a pile is named in a move sentence: "T1" to "T7" for a column (index + 1), "the waste", or "the clubs
     * foundation".
     *
     * <p>Calls:
     * <ul>
     *   <li>{@code suitName()} - writes the foundation's suit</li>
     * </ul>
     *
     * <p>Called by: {@link #describeMove}.
     *
     * @param pile the pile to name
     * @param index the column or foundation index from the move
     * @return how a pile is named in a move sentence: "T1" to "T7" for a column
     */
    private static String pileName(CardPile pile, int index) {
        if (pile instanceof FoundationPile) {
            return "the " + suitName(index) + " foundation";
        }
        return pile instanceof WastePile ? "the waste" : "T" + (index + 1);
    }

    /**
     * Prints "Shuffle: " followed by the strategy's name, such as Random or In Faro.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@code ShuffleType.strategy()} - gets the chosen shuffle object</li>
     *   <li>{@code ShuffleStrategy.name()} - gets its display name</li>
     * </ul>
     *
     * <p>Called by: {@code GameController.automatic()}, {@code GameController.simulate()}.
     *
     * @param configuration the settings whose shuffle name is printed
     */
    public void shuffleConfiguration(ShuffleConfiguration configuration) {
        System.out.println("\nShuffle: " + configuration.type().strategy().name());
    }

    /**
     * Prints "Playing... (Draw 1)" or "Playing... (Draw 3)".
     *
     * <p>Called by: {@code GameController.automatic()}, {@code GameController.simulate()}.
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
     * <p>Calls:
     * <ul>
     *   <li>{@link #describeMove} - turns the move into a sentence</li>
     *   <li>{@link ConsoleColor#paint} - makes the sentence yellow</li>
     * </ul>
     *
     * <p>Called by: {@code GameController.replay()}.
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
     * <p>Called by: {@code GameController.automatic()}, {@code GameController.guaranteed()}.
     *
     * @param detail the text after "Result:"
     */
    public void resultDetail(String detail) {
        System.out.println("  Result: " + detail);
    }

    /**
     * Prints the heading "SOLITAIRE SIMULATION REPORT".
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link #heading} - prints the report title</li>
     * </ul>
     *
     * <p>Called by: {@code GameController.simulate()}.
     */
    public void simulationStarted() {
        heading("SOLITAIRE SIMULATION REPORT");
    }

    /**
     * Prints one line for a simulated game, such as "Game 3 | Won - all 52 cards completed | Moves: 112".
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link #statusText} - turns the status into words</li>
     * </ul>
     *
     * <p>Called by: {@code GameController.simulate()}.
     *
     * @param gameNumber the game number, starting at 1
     * @param status how the game ended
     * @param moves how many moves were played
     */
    public void simulationGame(int gameNumber, GameStatus status, int moves) {
        System.out.printf(Locale.ROOT, "Game %d | %s | Moves: %d%n", gameNumber, statusText(status), moves);
    }

    /**
     * Prints the number of games won and not solved (split into limit reached and no new moves), the win rate as a
     * percentage, and the average moves and decisions per game, each with 2 decimal places.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link #section} - prints the RESULTS title</li>
     * </ul>
     *
     * <p>Called by: {@code GameController.simulate()}.
     *
     * @param count how many games were played
     * @param won games won
     * @param limitedGames games that reached a limit
     * @param stuckGames games that got stuck
     * @param moves moves played in all games together
     * @param totalDecisions decisions made in all games together
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

    /**
     * Prints the elapsed time, converted from nanoseconds to seconds with 1 decimal place.
     *
     * <p>Called by: {@code GameController.simulate()}.
     *
     * @param elapsedNanos the time taken, in nanoseconds
     */
    public void elapsed(long elapsedNanos) {
        System.out.printf(Locale.ROOT, "  Elapsed: %.1f seconds%n", elapsedNanos / 1_000_000_000.0);
    }

    /**
     * Prints "Thank you for playing!" when the player leaves the menu.
     *
     * <p>Called by: {@code GameController.shuffleAndPlay()}.
     */
    public void showClosingMessage() {
        System.out.println("Thank you for playing!");
    }

    /**
     * Prints the seven columns side by side, one card per row, from the bottom card down to the top card. Shorter
     * columns leave blank cells; an empty column shows an empty slot in the first row.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link #section} - prints the TABLEAU title</li>
     *   <li>{@link GameState#tableau()} - gets the seven columns</li>
     *   <li>{@link CardPile#cards()} - gets the cards of each column</li>
     *   <li>{@code showCard()} - shows each card, red for a red suit</li>
     *   <li>{@code pad()} - fills blank and empty cells</li>
     * </ul>
     *
     * <p>Called by: {@link #render}.
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
     * Returns the top card's text, or the empty slot if the pile is empty, followed by spaces up to width. It accepts
     * any CardPile, so the same method is used for the waste and for every foundation.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link CardPile#isEmpty} - shows an empty slot for an empty pile</li>
     *   <li>{@code pad()} - spaces after the empty slot</li>
     *   <li>{@link CardPile#top} - gets the top card</li>
     *   <li>{@code showCard()} - shows the top card, red for a red suit</li>
     * </ul>
     *
     * <p>Called by: {@link #render}.
     *
     * @param pile the pile to show
     * @param width the number of characters to fill
     * @return the top card's text, or the empty slot if the pile is empty, followed by spaces up to width
     */
    private static String topOrEmpty(CardPile pile, int width) {
        return pile.isEmpty() ? pad(EMPTY_SLOT, width) : showCard(pile.top(), width);
    }

    /**
     * Returns the card's text followed by spaces up to width. Face-up Hearts and Diamonds are red; Clubs, Spades, and
     * face-down cards keep the terminal's normal color, so black suits stay readable on a dark background. The spaces
     * are counted from the plain text, because the invisible color codes would otherwise break the column alignment.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link Card#toString} - gets [H-A] or [XX]</li>
     *   <li>{@link Card#isFaceUp} - a hidden card is not colored</li>
     *   <li>{@link Card#isRed} - picks red for Hearts and Diamonds</li>
     *   <li>{@link ConsoleColor#paint} - applies the red color</li>
     * </ul>
     *
     * <p>Called by: {@code renderTableau()}, {@code topOrEmpty()}.
     *
     * @param card the card to show
     * @param width the number of characters to fill
     * @return the card's text followed by spaces up to width
     */
    private static String showCard(Card card, int width) {
        String text = card.toString();
        String shown = card.isFaceUp() && card.isRed() ? ConsoleColor.RED.paint(text) : text;
        return shown + " ".repeat(Math.max(0, width - text.length()));
    }

    /**
     * Returns the text followed by spaces up to width; text that is already longer is unchanged.
     *
     * <p>Called by: {@code renderTableau()}, {@code topOrEmpty()}.
     *
     * @param text the text to pad
     * @param width the number of characters to fill
     * @return the text followed by spaces up to width
     */
    private static String pad(String text, int width) {
        return text + " ".repeat(Math.max(0, width - text.length()));
    }

    /**
     * Returns "1 card" for 1 and "N cards" for any other number.
     *
     * <p>Called by: {@link #describeMove}.
     *
     * @param count the number of cards
     * @return "1 card" for 1 and "N cards" for any other number
     */
    private static String cardCount(int count) {
        return count + (count == 1 ? " card" : " cards");
    }

    /**
     * Returns the suit name in lower case for a foundation index, for example 0 gives "hearts".
     *
     * <p>Called by: {@code pileName()}.
     *
     * @param foundationIndex the foundation index, 0 (Hearts) to 3 (Spades)
     * @return the suit name in lower case for a foundation index, for example 0 gives "hearts"
     */
    private static String suitName(int foundationIndex) {
        return Suit.values()[foundationIndex].name().toLowerCase(Locale.ROOT);
    }
}
