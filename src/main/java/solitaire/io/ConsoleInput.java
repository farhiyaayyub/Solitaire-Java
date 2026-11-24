package solitaire.io;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;

import solitaire.game.GameState;
import solitaire.model.Deck;
import solitaire.move.Move;
import solitaire.shuffle.ShuffleConfiguration;
import solitaire.shuffle.ShuffleType;

/**
 * Asks the player every question and keeps asking until the answer is valid, so other classes only ever receive
 * valid answers. Every question accepts q to cancel, and the end of input (for example Ctrl+Z) counts as q.
 *
 * <p>Encapsulation: the scanner and helpers are private; other classes only call the public question methods.
 * Composition: it uses a CardFileLoader to load decks. Both the loader and the game limit are passed in, so their
 * settings come from solitaire.properties.
 *
 * @author Farhiya Ayyub
 */
public final class ConsoleInput {
    private static final String CANCEL = "q";
    // The two shuffle options appear in both shuffle menus (standard deck and file deck).
    private static final String RANDOM_OPTION = "[1] Random           - mix the cards randomly";
    private static final String PERFECT_OPTION = "[2] Perfect shuffle  - split the deck in half and interleave (Faro)";

    private final Scanner scanner;
    private final CardFileLoader cardFileLoader;
    // Upper limit for a simulation from the menu, so a typo such as 10000 cannot start a very long run.
    private final int maxGameCount;
    // Unseeded, so each menu game gets a different seed.
    private final Random seedGenerator = new Random();

    /**
     * Reads all answers from the supplied scanner, loads card files with the supplied loader, and accepts at most
     * maxGameCount games for a simulation.
     *
     * <p>Called by: {@code SolitaireApplication.getController()}.
     *
     * @param scanner the source of the player's answers
     * @param cardFileLoader loads the card file the player names
     * @param maxGameCount the highest number of games the player may enter (simulation.maxGameCount)
     * @throws NullPointerException if scanner or cardFileLoader is {@code null}
     * @throws IllegalArgumentException if maxGameCount is less than 1
     */
    public ConsoleInput(Scanner scanner, CardFileLoader cardFileLoader, int maxGameCount) {
        this.scanner = Objects.requireNonNull(scanner);
        this.cardFileLoader = Objects.requireNonNull(cardFileLoader);
        if (maxGameCount < 1) {
            throw new IllegalArgumentException("The maximum game count must be at least 1");
        }
        this.maxGameCount = maxGameCount;
    }

    /**
     * Asks where the deck comes from: "1" for a standard deck, "2" for a card file, or q.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@code menu()} - shows the options and reads the choice</li>
     * </ul>
     *
     * <p>Called by: {@code GameController.playFromMenu()}.
     *
     * @return "1" for a standard deck, "2" for a card file, or "q"
     */
    public String deckSource() {
        return menu("Choose a deck", "12",
                "[1] Standard deck  - a normal 52-card deck",
                "[2] Card file      - use your own 52 cards from a file (input.txt)");
    }

    /**
     * Asks for a card file name until a valid deck loads, and returns that deck, or {@code null} if the player enters
     * q. After a file or card error, the message is shown and the question is asked again.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@code readExact()} - reads the file name, keeping its case</li>
     *   <li>{@link CardFileLoader#load} - reads and checks the file</li>
     *   <li>{@code showError()} - prints the file problem in red</li>
     * </ul>
     *
     * <p>Called by: {@code GameController.playFromMenu()}.
     *
     * @return the loaded deck, or {@code null} if the player enters q
     */
    public Deck loadDeck() {
        while (true) {
            String fileName = readExact("\nType the card file name (for example input.txt), or q to exit:\n> ");
            if (fileName.equalsIgnoreCase(CANCEL)) {
                return null;
            }
            try {
                Deck deck = cardFileLoader.load(Path.of(fileName));
                System.out.println("  OK - loaded 52 unique cards from " + fileName + ".");
                return deck;
            } catch (IOException | InvalidPathException exception) {
                showError("File error: " + exception.getMessage());
            } catch (IllegalArgumentException exception) {
                showError("Invalid card file: " + exception.getMessage());
            }
            showError("Please check the file name and try again.");
        }
    }

    /**
     * Asks for the shuffle and returns the settings with a new random seed, or {@code null} if the player enters q. A
     * standard deck is randomized before a Faro shuffle, otherwise every Faro game would be the same deal. A file deck
     * keeps its own order before the Faro shuffle, and only a file deck offers "No shuffle".
     *
     * <p>Calls:
     * <ul>
     *   <li>{@code menu()} - shows the options and reads the choice</li>
     *   <li>{@link ShuffleType#isFaro} - randomizes first only for Faro</li>
     * </ul>
     *
     * <p>Called by: {@code GameController.playFromMenu()}.
     *
     * @param deckFromFile {@code true} if the deck was loaded from a card file
     * @return the chosen settings, or {@code null} if the player enters q
     */
    public ShuffleConfiguration shuffleConfiguration(boolean deckFromFile) {
        String selected = deckFromFile
                ? menu("Choose how to shuffle", "123", RANDOM_OPTION, PERFECT_OPTION,
                        "[3] No shuffle       - keep the exact card order from the file")
                : menu("Choose how to shuffle", "12", RANDOM_OPTION, PERFECT_OPTION);
        ShuffleType type;
        switch (selected) {
            case "1" -> type = ShuffleType.RANDOM;
            case "3" -> type = ShuffleType.NONE;
            case "2" -> {
                String faro = menu("Which perfect shuffle?", "12",
                        "[1] In-Faro   - the top card moves to 2nd place",
                        "[2] Out-Faro  - the top and bottom cards stay in place");
                if (faro.equals(CANCEL)) {
                    return null;
                }
                type = faro.equals("1") ? ShuffleType.IN : ShuffleType.OUT;
            }
            default -> {
                return null;
            }
        }
        return new ShuffleConfiguration(type, type.isFaro() && !deckFromFile, seedGenerator.nextLong());
    }

    /**
     * Returns the selected draw count (1 or 3), or 0 if the player cancels.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@code menu()} - shows the options and reads the choice</li>
     * </ul>
     *
     * <p>Called by: {@code GameController.playFromMenu()}.
     *
     * @return 1, 3, or 0 for q
     */
    public int drawCount() {
        String selected = menu("How many cards to turn from the stock each time?", "13",
                "[1] Draw 1  - turn 1 card at a time",
                "[3] Draw 3  - turn 3 cards at a time");
        return selected.equals(CANCEL) ? 0 : Integer.parseInt(selected);
    }

    /**
     * Returns "1" to watch one game move by move, "2" to test the win rate over many games, or "q".
     *
     * <p>Calls:
     * <ul>
     *   <li>{@code menu()} - shows the options and reads the choice</li>
     * </ul>
     *
     * <p>Called by: {@code GameController.playFromMenu()}.
     *
     * @return "1", "2", or "q"
     */
    public String playMode() {
        return menu("What do you want to do?", "12",
                "[1] Watch one game  - see every move on the board",
                "[2] Test win rate   - play many games quickly and show only the results");
    }

    /**
     * Asks until the player enters a whole number from 1 to the maximum game count (simulation.maxGameCount, 100 by
     * default) and returns it, or returns 0 for q.
     *
     * <p>Key idea: {@code Integer.parseInt} throws NumberFormatException for text that is not a number, which is
     * caught; a number outside 1 to the maximum is rejected by a range check. Both print the same red error and ask
     * again.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@code read()} - reads the number typed</li>
     *   <li>{@code showError()} - prints the problem in red</li>
     * </ul>
     *
     * <p>Called by: {@code GameController.playFromMenu()}.
     *
     * @return the number of games, 1 to the maximum game count, or 0 if the player enters q
     */
    public int gameCount() {
        while (true) {
            String value = read("\nHow many games should be played? (1 to " + maxGameCount + "), or q to exit:\n> ");
            if (value.equals(CANCEL)) {
                return 0;
            }
            try {
                int count = Integer.parseInt(value);
                if (count >= 1 && count <= maxGameCount) {
                    return count;
                }
            } catch (NumberFormatException ignored) {
                // Falls through to the retry message below.
            }
            showError("Enter a whole number from 1 to " + maxGameCount + ".");
        }
    }

    /**
     * Returns {@code true} if the player answers y to watching game 1 of the simulation move by move.
     *
     * <p>Called by: {@code GameController.playFromMenu()}.
     *
     * @return {@code true} if the player answers y to watching game 1 of the simulation move by move
     */
    public boolean wantsToWatchSimulationGame() {
        return choice("\nWatch game 1 move by move? Type y (yes) or n (no):\n> ", "yn").equals("y");
    }

    /**
     * Returns {@code true} if the player answers y to playing again; n or q returns {@code false}.
     *
     * <p>Called by: {@code GameController.shuffleAndPlay()}.
     *
     * @return {@code true} if the player answers y to playing again
     */
    public boolean playAgain() {
        return choice("\nPlay again? Type y (yes) or n (no):\n> ", "yn").equals("y");
    }

    /**
     * Prints the legal moves numbered from 1 and returns the chosen move.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link ConsoleRenderer#section} - prints the LEGAL MOVES title</li>
     *   <li>{@link ConsoleRenderer#describeMove} - prints each move as a sentence</li>
     *   <li>{@code read()} - reads the chosen number</li>
     *   <li>{@code showError()} - prints the problem in red</li>
     * </ul>
     *
     * <p>Called by: {@code GameController.manual()}.
     *
     * @param moves the legal moves to choose from
     * @param board the current board, used to name the cards in each move
     * @param renderer prints the move list
     * @return {@code null} if there are no legal moves or the player enters q; asks again for a number that is not
     *     listed
     */
    public Move selectMove(List<Move> moves, GameState board, ConsoleRenderer renderer) {
        renderer.section("LEGAL MOVES");
        if (moves.isEmpty()) {
            System.out.println("  No legal moves available. Game ended.");
            return null;
        }
        for (int moveIndex = 0; moveIndex < moves.size(); moveIndex++) {
            System.out.printf("  [%2d] %s%n", moveIndex + 1, renderer.describeMove(moves.get(moveIndex), board));
        }
        while (true) {
            String input = read("\nChoose a move number, or q to exit:\n> ");
            if (input.equals(CANCEL)) {
                return null;
            }
            try {
                int selectedIndex = Integer.parseInt(input) - 1;
                if (selectedIndex >= 0 && selectedIndex < moves.size()) {
                    return moves.get(selectedIndex);
                }
                showError("Invalid selection. Enter a listed move number.");
            } catch (NumberFormatException invalidNumber) {
                showError("Enter a whole-number move choice, or q to exit.");
            }
        }
    }

    /**
     * Shows a titled menu with one option per line plus [q] Exit, and returns the player's valid choice.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@code choice()} - repeats until the answer is valid</li>
     * </ul>
     *
     * <p>Called by: {@link #deckSource}, {@link #shuffleConfiguration}, {@link #drawCount}, {@link #playMode}.
     *
     * @param title the title text
     * @param allowedChoices the option numbers that are valid answers, such as "12"
     * @param options the menu lines to show, such as "[1] Random"
     * @return the chosen option number, or "q"
     */
    private String menu(String title, String allowedChoices, String... options) {
        StringBuilder prompt = new StringBuilder("\n").append(title).append('\n');
        for (String option : options) {
            prompt.append("  ").append(option).append('\n');
        }
        prompt.append("  [q] Exit\nType your choice and press Enter:\n> ");
        return choice(prompt.toString(), allowedChoices);
    }

    /**
     * Asks until the answer is q or exactly one of the characters in allowedChoices, and returns it.
     *
     * <p>Key idea: a {@code while (true)} loop asks again until the answer is valid. An answer is valid when it is q or
     * exactly one character found in allowedChoices; anything else prints a red error through showError().
     *
     * <p>Calls:
     * <ul>
     *   <li>{@code read()} - reads one answer</li>
     *   <li>{@code showError()} - prints the problem in red</li>
     * </ul>
     *
     * <p>Called by: {@link #wantsToWatchSimulationGame}, {@link #playAgain}, {@code menu()}.
     *
     * @param prompt the question to print
     * @param allowedChoices the single characters that are valid answers, such as "12"
     * @return the valid answer, or "q"
     */
    private String choice(String prompt, String allowedChoices) {
        while (true) {
            String input = read(prompt);
            if (input.equals(CANCEL) || (input.length() == 1 && allowedChoices.contains(input))) {
                return input;
            }
            showError("\"" + input + "\" is not an option. Type one of: "
                    + String.join(", ", allowedChoices.split("")) + ", or q.");
        }
    }

    /**
     * Prints an input error indented and in red, so it stands out from the menus and prompts. Every message about a
     * wrong answer goes through here, so all errors look the same.
     *
     * <p>Called by: {@link #loadDeck}, {@link #gameCount}, {@link #selectMove}, {@code choice()}.
     *
     * @param message the error text
     */
    private static void showError(String message) {
        System.out.println("  " + ConsoleColor.RED.paint(message));
    }

    /**
     * Prints the prompt and returns the answer trimmed and in lower case, so "Q" and "q" are the same.
     *
     * <p>Called by: {@link #gameCount}, {@link #selectMove}, {@code choice()}.
     *
     * @param prompt the question to print
     * @return q if input has ended
     */
    private String read(String prompt) {
        return readExact(prompt).toLowerCase(Locale.ROOT);
    }

    /**
     * Prints the prompt and returns the answer trimmed, keeping its case because file names may need it.
     *
     * <p>Called by: {@link #loadDeck}, {@code read()}.
     *
     * @param prompt the question to print
     * @return q if input has ended
     */
    private String readExact(String prompt) {
        System.out.print(prompt);
        return scanner.hasNextLine() ? scanner.nextLine().trim() : CANCEL;
    }
}
