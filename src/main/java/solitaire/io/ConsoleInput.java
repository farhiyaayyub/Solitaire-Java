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
 * Asks the player questions in the console and reads the answers: the deck source, the card file, the shuffle, the draw
 * mode, the play mode, the number of games, and the moves in manual play. It repeats a question until the answer is
 * valid, and q always exits.
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
     * Stores the keyboard scanner, the card file loader, and the highest allowed game count, which must be at least 1.
     */
    public ConsoleInput(Scanner scanner, CardFileLoader cardFileLoader, int maxGameCount) {
        this.scanner = Objects.requireNonNull(scanner);
        this.cardFileLoader = Objects.requireNonNull(cardFileLoader);
        if (maxGameCount < 1) {
            throw new IllegalArgumentException("The maximum game count must be at least 1");
        }
        this.maxGameCount = maxGameCount;
    }

    /** Asks whether to use a standard deck or a card file, and returns "1", "2", or "q". */
    public String deckSource() {
        return menu("Choose a deck", "12",
                "[1] Standard deck  - a normal 52-card deck",
                "[2] Card file      - use your own 52 cards from a file (input.txt)");
    }

    /**
     * Asks for a card file name and tries to load it. If the file is missing or invalid, it shows the error and asks
     * again. Returns the deck, or null if the player types q.
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
     * Asks how to shuffle: random, perfect (Faro), or, for a file deck, no shuffle. For a perfect shuffle it also asks
     * In-Faro or Out-Faro. Returns the chosen settings, or null if the player types q.
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

    /** Asks whether to draw 1 or 3 cards at a time, and returns 1, 3, or 0 if the player types q. */
    public int drawCount() {
        String selected = menu("How many cards to turn from the stock each time?", "13",
                "[1] Draw 1  - turn 1 card at a time",
                "[3] Draw 3  - turn 3 cards at a time");
        return selected.equals(CANCEL) ? 0 : Integer.parseInt(selected);
    }

    /** Asks whether to watch one game or to test the win rate over many games, and returns "1", "2", or "q". */
    public String playMode() {
        return menu("What do you want to do?", "12",
                "[1] Watch one game  - see every move on the board",
                "[2] Test win rate   - play many games quickly and show only the results");
    }

    /**
     * Asks how many games to simulate and repeats until the answer is a whole number from 1 to the maximum. Returns 0
     * if the player types q.
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

    /** Asks whether to watch game 1 of the simulation move by move, and returns true if the player types y. */
    public boolean wantsToWatchSimulationGame() {
        return choice("\nWatch game 1 move by move? Type y (yes) or n (no):\n> ", "yn").equals("y");
    }

    /** Asks whether to play again, and returns true if the player types y. */
    public boolean playAgain() {
        return choice("\nPlay again? Type y (yes) or n (no):\n> ", "yn").equals("y");
    }

    /**
     * Asks the player to pick one of the listed moves by its number and returns that move. It asks again on a wrong
     * number, and returns null if the player types q or there are no moves.
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

    /** Builds a menu from the title and the options, adds [q] Exit, and returns the player's valid choice. */
    private String menu(String title, String allowedChoices, String... options) {
        StringBuilder prompt = new StringBuilder("\n").append(title).append('\n');
        for (String option : options) {
            prompt.append("  ").append(option).append('\n');
        }
        prompt.append("  [q] Exit\nType your choice and press Enter:\n> ");
        return choice(prompt.toString(), allowedChoices);
    }

    /** Shows the prompt until the answer is q or one of the allowed characters, then returns it. */
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

    /** Prints an error message indented and in red. */
    private static void showError(String message) {
        System.out.println("  " + ConsoleColor.RED.paint(message));
    }

    /** Shows the prompt and reads one answer in lower case, so Q and q are treated the same. */
    private String read(String prompt) {
        return readExact(prompt).toLowerCase(Locale.ROOT);
    }

    /**
     * Shows the prompt and reads one answer with its case kept, because file names may need it. Returns q if there is
     * no more input.
     */
    private String readExact(String prompt) {
        System.out.print(prompt);
        return scanner.hasNextLine() ? scanner.nextLine().trim() : CANCEL;
    }
}
