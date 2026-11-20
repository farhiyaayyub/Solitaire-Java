package solitaire.io;


import solitaire.move.Move;
import solitaire.shuffle.ShuffleConfiguration;
import solitaire.enums.ShuffleType;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Reads and validates console input.
 *
 * @author Farhiya Ayyub
 */
public final class ConsoleInput {
    private final Scanner scanner;

    /** Uses the supplied scanner for all console prompts. */
    public ConsoleInput(Scanner scanner) {
        this.scanner = java.util.Objects.requireNonNull(scanner);
    }

    /** Displays a prompt and returns normalized input, or q at end of input. */
    private String read(String prompt) {
        System.out.print(prompt);
        return scanner.hasNextLine() ? scanner.nextLine().trim().toLowerCase(Locale.ROOT) : "q";
    }

    /** Repeats a prompt until the user enters an allowed choice or q. */
    private String choice(String prompt, String allowedChoices) {
        while (true) {
            String input = read(prompt);
            if (input.equals("q") || (input.length() == 1 && allowedChoices.contains(input))) return input;
            System.out.println("  Invalid selection. Choose one of the listed options.");
        }
    }

    /** Reads or displays the shuffle selection; input returns null on cancellation. */
    public ShuffleConfiguration shuffleConfiguration() {
        String selected = choice("\nChoose shuffle: [1] Random  [2] Perfect Shuffle  [q] Exit\n> ", "12");
        if (selected.equals("q")) return null;
        ShuffleType type = ShuffleType.RANDOM;
        if (selected.equals("2")) {
            String faro = choice("\nPerfect shuffle: [1] In-Faro  [2] Out-Faro  [q] Exit\n> ", "12");
            if (faro.equals("q")) return null;
            type = faro.equals("1") ? ShuffleType.IN : ShuffleType.OUT;
        }
        return new ShuffleConfiguration(type, 1, type != ShuffleType.RANDOM, new java.util.Random().nextLong());
    }

    /** Returns the selected draw count; console input uses zero to signal cancellation. */
    public int drawCount() {
        String selected = choice("\nCards per draw: [1] Draw 1  [3] Draw 3  [q] Exit\n> ", "13");
        return selected.equals("q") ? 0 : Integer.parseInt(selected);
    }

    /** Returns true when the player chooses to start another game. */
    public boolean playAgain() {
        return choice("\nPlay again? [y] Yes  [n] No\n> ", "yn").equals("y");
    }

    /** Reads whether to play one game or run a simulation. */
    public String playMode() {
        return choice("\nWhat do you want to do? [1] Watch ONE game  [2] Test the win rate (many games)  [q] Exit\n> ", "12");
    }

    /** Reads a positive simulation count, or returns zero when canceled. */
    public int gameCount() {
        while (true) {
            String value = read("\nHow many games? (e.g. 1000)  [q] Exit\n> ");
            if (value.equals("q")) return 0;
            try {
                int count = Integer.parseInt(value);
                if (count > 0) return count;
            } catch (NumberFormatException ignored) {
                // Report invalid input and let the player retry.
            }
            System.out.println("  Enter a positive whole number.");
        }
    }

    /** Asks whether the player wants to watch the first deal from the simulation. */
    public boolean wantsToWatchSimulationGame() {
        String watch = choice("\nWould you like to see how a game was played? [y] Yes  [n] No\n> ", "yn");
        return watch.equals("y");
    }

    /** Reads a legal move selection; returns null on cancellation or when no moves exist. */
    public Move selectMove(List<Move> moves, ConsoleRenderer renderer) {
        ConsoleRenderer.section("LEGAL MOVES");
        if (moves.isEmpty()) {
            System.out.println("  No legal moves available. Game ended.");
            return null;
        }
        for (int moveIndex = 0; moveIndex < moves.size(); moveIndex++)
            System.out.printf("  [%2d] %s%n", moveIndex + 1, renderer.describeMove(moves.get(moveIndex)));
        while (true) {
            String input = read("\nChoose a move number, or q to exit:\n> ");
            if (input.equals("q")) return null;
            try {
                int selectedIndex = Integer.parseInt(input) - 1;
                if (selectedIndex >= 0 && selectedIndex < moves.size()) return moves.get(selectedIndex);
            } catch (NumberFormatException invalidNumber) {
                System.out.println("  Enter a whole-number move choice, or q to return.");
                continue;
            }
            System.out.println("  Invalid selection. Enter a listed move number.");
        }
    }
}
