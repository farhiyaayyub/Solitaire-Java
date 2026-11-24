package solitaire.io;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;

import solitaire.model.Deck;
import solitaire.move.Move;
import solitaire.shuffle.ShuffleConfiguration;
import solitaire.shuffle.ShuffleType;

/**
 * Keyboard questions for the player.
 *
 * <p>Kind: final class, uses CardFileLoader.
 * <p>Purpose: asks the player every question and keeps asking until the answer is valid, so other classes only ever
 * receive valid answers. Every question accepts q to cancel, and the end of input (for example Ctrl+Z) counts as q.
 * <p>Data: the Scanner it reads from, the CardFileLoader, and the highest game count the menu accepts (so a typo
 * such as 10000 cannot start a very long run).
 * <p>OOP: Encapsulation - the scanner and helpers are private; other classes only call the question methods.
 * Composition - it holds a CardFileLoader to load decks; the loader and the game limit are passed in, so their
 * settings come from solitaire.properties.
 *
 * @author Farhiya Ayyub
 */
public final class ConsoleInput {
    private static final String CANCEL = "q";
    // Shared by both shuffle menus (standard deck and file deck).
    private static final String RANDOM_OPTION = "[1] Random           - mix the cards randomly";
    private static final String PERFECT_OPTION = "[2] Perfect shuffle  - split the deck in half and interleave (Faro)";

    private final Scanner scanner;
    private final CardFileLoader cardFileLoader;
    private final int maxGameCount;

    /**
     * Creates the input reader and checks the game limit.
     *
     * <p>Called by: SolitaireApplication.getController().
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
     * Asks where the deck comes from: a standard deck or a card file.
     *
     * <p>Called by: GameController.playFromMenu().
     *
     * @return "1" for a standard deck, "2" for a card file, or "q"
     */
    public String deckSource() {
        return menu("Choose a deck", "12",
                "[1] Standard deck  - a normal 52-card deck",
                "[2] Card file      - use your own 52 cards from a file (input.txt)");
    }

    /**
     * Asks for a card file name until a valid deck loads. After a file or card error, the reason is shown in red and
     * the question is asked again.
     *
     * <p>Called by: GameController.playFromMenu().
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
     * Asks which shuffle to use. A standard deck is randomized before a Faro shuffle, otherwise every Faro game would
     * be the same deal. A file deck keeps its own order before the Faro shuffle, and only a file deck offers
     * "No shuffle".
     *
     * <p>Called by: GameController.playFromMenu().
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
        return new ShuffleConfiguration(type, type.isFaro() && !deckFromFile);
    }

    /**
     * Asks how many cards to turn from the stock each time.
     *
     * <p>Called by: GameController.playFromMenu().
     *
     * @return 1 or 3, or 0 if the player enters q
     */
    public int drawCount() {
        String selected = menu("How many cards to turn from the stock each time?", "13",
                "[1] Draw 1  - turn 1 card at a time",
                "[3] Draw 3  - turn 3 cards at a time");
        return selected.equals(CANCEL) ? 0 : Integer.parseInt(selected);
    }

    /**
     * Asks whether to watch one game or to test the win rate over many games.
     *
     * <p>Called by: GameController.playFromMenu().
     *
     * @return "1" to watch one game move by move, "2" to test the win rate, or "q"
     */
    public String playMode() {
        return menu("What do you want to do?", "12",
                "[1] Watch one game  - see every move on the board",
                "[2] Test win rate   - play many games quickly and show only the results");
    }

    /**
     * Asks how many games to simulate, until the player enters a whole number from 1 to the maximum game count.
     * Text that is not a number and a number out of range give the same red error.
     *
     * <p>Called by: GameController.playFromMenu().
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
     * {@return {@code true} if the player answers y to watching game 1 of the simulation move by move}
     *
     * <p>Called by: GameController.playFromMenu().
     */
    public boolean wantsToWatchSimulationGame() {
        return choice("\nWatch game 1 move by move? Type y (yes) or n (no):\n> ", "yn").equals("y");
    }

    /**
     * {@return {@code true} if the player answers y to playing again; n or q gives {@code false}}
     *
     * <p>Called by: GameController.shuffleAndPlay().
     */
    public boolean playAgain() {
        return choice("\nPlay again? Type y (yes) or n (no):\n> ", "yn").equals("y");
    }

    /**
     * Asks for the number of a move that ConsoleRenderer.showMoves() has already listed, and returns that move. A
     * number that is not listed is asked again.
     *
     * <p>Called by: GameController.manual().
     *
     * @param moves the listed moves, in the order they were numbered
     * @return the chosen move, or {@code null} if there are no moves or the player enters q
     */
    public Move selectMove(List<Move> moves) {
        if (moves.isEmpty()) {
            return null;
        }
        while (true) {
            String input = read("\nChoose a move number, or q to exit:\n> ");
            if (input.equals(CANCEL)) {
                return null;
            }
            try {
                // The player counts from 1; the list counts from 0.
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
     * <p>Called by: deckSource(), shuffleConfiguration(), drawCount(), playMode().
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
     * <p>Key idea: a {@code while (true)} loop that only returns on a valid answer; any other answer prints a red error
     * and the loop asks again. All menus and yes/no questions share this one loop.
     *
     * <p>Called by: menu(), wantsToWatchSimulationGame(), playAgain().
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
     * Prints an input error indented and in red, so all wrong-answer messages look the same and stand out from the
     * menus.
     *
     * <p>Called by: loadDeck(), gameCount(), selectMove(), choice().
     *
     * @param message the error text
     */
    private static void showError(String message) {
        System.out.println("  " + ConsoleColor.RED.paint(message));
    }

    /**
     * Prints the prompt and reads one answer in lower case, so "Q" and "q" are the same.
     *
     * <p>Called by: gameCount(), selectMove(), choice().
     *
     * @param prompt the question to print
     * @return the answer, trimmed and in lower case, or q if input has ended
     */
    private String read(String prompt) {
        return readExact(prompt).toLowerCase(Locale.ROOT);
    }

    /**
     * Prints the prompt and reads one answer, keeping its case because file names may need it.
     *
     * <p>Called by: loadDeck(), read().
     *
     * @param prompt the question to print
     * @return the answer, trimmed, or q if input has ended
     */
    private String readExact(String prompt) {
        System.out.print(prompt);
        return scanner.hasNextLine() ? scanner.nextLine().trim() : CANCEL;
    }
}
