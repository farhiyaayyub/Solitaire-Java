package solitaire.io;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;

import solitaire.model.Deck;
import solitaire.move.Move;
import solitaire.shuffle.ShuffleConfiguration;
import solitaire.shuffle.ShuffleType;

/**
 * Reads and validates everything the player types. Every prompt accepts q to cancel, and end of input counts as q.
 *
 * <p>Type: final class; all keyboard input goes through this class.
 * OOP: ENCAPSULATION (private scanner and helpers; other classes only call the public question methods),
 * COMPOSITION (uses a CardFileLoader object to load decks).
 *
 * @author Farhiya Ayyub
 */
public final class ConsoleInput {
    // Constants: the cancel key and menu text shared by two menus.
    private static final String CANCEL = "q";
    private static final String RANDOM_OPTION = "[1] Random           - mix the cards randomly";
    private static final String PERFECT_OPTION = "[2] Perfect shuffle  - split the deck in half and interleave (Faro)";

    // Fields and objects: the Scanner for input, a CardFileLoader for card files, and a Random for new seeds.
    private final Scanner scanner;
    private final CardFileLoader cardFileLoader = new CardFileLoader();
    private final Random seedGenerator = new Random();

    /**
     * Constructor: Uses the supplied scanner for all console prompts.
     * Calls: Objects.requireNonNull()
     */
    public ConsoleInput(Scanner scanner) {
        this.scanner = Objects.requireNonNull(scanner);
    }

    /**
     * Method: Asks where the deck comes from: "1" for a standard deck, "2" for a card file, or q.
     * Calls: menu()
     */
    public String deckSource() {
        return menu("Choose a deck", "12",
                "[1] Standard deck  - a normal 52-card deck",
                "[2] Card file      - use your own 52 cards from a file (for example input.txt)");
    }

    /**
     * Method: Asks for a card file until it loads; returns null if the player cancels. Errors are shown and retried.
     * Calls: readExact(), CardFileLoader.load()
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
                System.out.println("  File error: " + exception.getMessage());
            } catch (IllegalArgumentException exception) {
                System.out.println("  Invalid card file: " + exception.getMessage());
            }
            System.out.println("  Please check the file name and try again.");
        }
    }

    /**
     * Method: Asks for the shuffle; returns null if the player cancels. A standard deck is randomized before a Faro
     * shuffle, otherwise every Faro game would be the same deal. A file deck keeps its own order and can also be
     * left unshuffled.
     * Calls: menu(), ShuffleType.isFaro(), Random.nextLong()
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
        // Object: ShuffleConfiguration - the player's choices with a new random seed.
        return new ShuffleConfiguration(type, type.isFaro() && !deckFromFile, seedGenerator.nextLong());
    }

    /**
     * Method: Returns the selected draw count (1 or 3), or 0 if the player cancels.
     * Calls: menu()
     */
    public int drawCount() {
        String selected = menu("How many cards to turn from the stock each time?", "13",
                "[1] Draw 1  - turn 1 card at a time",
                "[3] Draw 3  - turn 3 cards at a time");
        return selected.equals(CANCEL) ? 0 : Integer.parseInt(selected);
    }

    /**
     * Method: Asks whether to watch one game ("1") or test the win rate over many games ("2"), or q.
     * Calls: menu()
     */
    public String playMode() {
        return menu("What do you want to do?", "12",
                "[1] Watch one game  - see every move on the board",
                "[2] Test win rate   - play many games quickly and show only the results");
    }

    /**
     * Method: Reads a positive number of games, or returns 0 if the player cancels.
     * Calls: read()
     */
    public int gameCount() {
        while (true) {
            String value = read("\nHow many games should be played? (for example 100), or q to exit:\n> ");
            if (value.equals(CANCEL)) {
                return 0;
            }
            try {
                int count = Integer.parseInt(value);
                if (count > 0) {
                    return count;
                }
            } catch (NumberFormatException ignored) {
                // Falls through to the retry message below.
            }
            System.out.println("  Enter a positive whole number.");
        }
    }

    /**
     * Method: Asks whether to watch the first game of the simulation move by move.
     * Calls: choice()
     */
    public boolean wantsToWatchSimulationGame() {
        return choice("\nWatch game 1 move by move? Type y (yes) or n (no):\n> ", "yn").equals("y");
    }

    /**
     * Method: Returns true when the player chooses to start another game.
     * Calls: choice()
     */
    public boolean playAgain() {
        return choice("\nPlay again? Type y (yes) or n (no):\n> ", "yn").equals("y");
    }

    /**
     * Method: Lists the legal moves and reads a choice; returns null if the player cancels or no moves exist.
     * Calls: ConsoleRenderer.section(), ConsoleRenderer.describeMove(), read()
     */
    public Move selectMove(List<Move> moves, ConsoleRenderer renderer) {
        renderer.section("LEGAL MOVES");
        if (moves.isEmpty()) {
            System.out.println("  No legal moves available. Game ended.");
            return null;
        }
        for (int moveIndex = 0; moveIndex < moves.size(); moveIndex++) {
            System.out.printf("  [%2d] %s%n", moveIndex + 1, renderer.describeMove(moves.get(moveIndex)));
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
                System.out.println("  Invalid selection. Enter a listed move number.");
            } catch (NumberFormatException invalidNumber) {
                System.out.println("  Enter a whole-number move choice, or q to exit.");
            }
        }
    }

    /**
     * Method: Shows a titled menu with one option per line plus [q] Exit, and returns the player's valid choice.
     * Calls: choice()
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
     * Method: Repeats a prompt until the player enters one of the allowed single-character choices or q.
     * Calls: read()
     */
    private String choice(String prompt, String allowedChoices) {
        while (true) {
            String input = read(prompt);
            if (input.equals(CANCEL) || (input.length() == 1 && allowedChoices.contains(input))) {
                return input;
            }
            System.out.println("  \"" + input + "\" is not an option. Type one of: "
                    + String.join(", ", allowedChoices.split("")) + ", or q.");
        }
    }

    /**
     * Method: Displays a prompt and returns trimmed, lower-case input, or q at end of input.
     * Calls: readExact()
     */
    private String read(String prompt) {
        return readExact(prompt).toLowerCase(Locale.ROOT);
    }

    /**
     * Method: Displays a prompt and returns trimmed input with its case kept (for file names), or q at end of input.
     * Calls: Scanner.hasNextLine(), Scanner.nextLine()
     */
    private String readExact(String prompt) {
        System.out.print(prompt);
        return scanner.hasNextLine() ? scanner.nextLine().trim() : CANCEL;
    }
}
