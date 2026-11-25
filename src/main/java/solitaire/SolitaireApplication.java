package solitaire;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

import solitaire.config.GameConfig;
import solitaire.game.GameController;
import solitaire.io.CardFileLoader;
import solitaire.io.ConsoleColor;
import solitaire.io.ConsoleInput;
import solitaire.io.ConsoleRenderer;
import solitaire.shuffle.ShuffleConfiguration;
import solitaire.shuffle.ShuffleType;
import solitaire.solver.SolitaireSolver;
import solitaire.solver.VisibleSolver;

/**
 * Sets up and starts the program. It loads the settings, checks the command typed by the player, builds the
 * GameController, and runs the command.
 *
 * @author Farhiya Ayyub
 */
public final class SolitaireApplication {
    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_FAILURE = 1;
    private static final int EXIT_INVALID_INPUT = 2;
    // Position of --compact/--verbose for the guaranteed and random commands.
    private static final int OUTPUT_OPTION_INDEX = 1;

    /** Private constructor so no object can be created; all methods are static. */
    private SolitaireApplication() {
    }

    /**
     * Loads solitaire.properties, checks the arguments, builds the controller, and runs the command (menu, random,
     * simulate, compare, manual, guaranteed, validate, or help). With no arguments it opens the menu. Any error is
     * caught and printed in red, and the method returns 0, 1, or 2 as the exit code.
     */
    public static int run(String[] args) {
        // Step 1: load the settings first, so a broken settings file is reported before anything else.
        GameConfig config;
        try {
            config = GameConfig.load(GameConfig.DEFAULT_FILE);
        } catch (IllegalArgumentException exception) {
            // A wrong value in solitaire.properties: the player can fix it.
            System.err.println(ConsoleColor.RED.paint("Configuration error: " + exception.getMessage()));
            return EXIT_INVALID_INPUT;
        } catch (IOException | IllegalStateException exception) {
            // A settings file that cannot be read, or default.properties missing from the program.
            System.err.println(ConsoleColor.RED.paint("Configuration error: " + exception.getMessage()));
            return EXIT_FAILURE;
        }
        // try-with-resources closes the keyboard Scanner when the command finishes or fails.
        try (Scanner input = new Scanner(System.in)) {
            // Step 2: read the command; with no arguments, open the menu ("shuffle").
            String command = args.length == 0 ? "shuffle" : args[0];
            // Step 3: check the arguments, then build the controller with its solver, input, and renderer.
            validateArguments(command, args);
            GameController controller = getController(args, input, config);
            // Step 4: run the code for the command the player typed. Only the matching case runs.
            switch (command) {
                // validate: only checks the card file and prints VALID; no game is played.
                case "validate" -> {
                    new CardFileLoader(config.cardFilesFolder()).load(Path.of(args[1]));
                    System.out.println("VALID: " + args[1] + " - 52 unique cards, valid suits and ranks.");
                }
                // shuffle or menu: opens the interactive menu.
                case "shuffle", "menu" -> controller.shuffleAndPlay();
                // guaranteed: plays the deal that is known to be winnable.
                case "guaranteed" -> controller.guaranteed(hasCompactOption(args));
                // random: the solver plays one game on a randomly shuffled deck.
                case "random" -> controller.automatic(new ShuffleConfiguration(ShuffleType.RANDOM, false),
                        hasCompactOption(args));
                // simulate: plays many games; the count is the second argument, or the default from the settings.
                case "simulate" -> controller.simulate(intArgument(args, 1, config.defaultGameCount()));
                // compare: plays the same decks with the Random, In-Faro, and Out-Faro shuffles.
                case "compare" -> controller.compare(intArgument(args, 1, config.defaultGameCount()));
                // manual: the player chooses every move.
                case "manual" -> controller.manual();
                // help: prints the list of commands.
                case "help", "--help" -> printUsage();
                // Any other word is not a command, so an error is thrown.
                default -> throw new IllegalArgumentException("Unknown command: " + command);
            }
            return EXIT_SUCCESS;
        } catch (IOException exception) {
            // The card file could not be read.
            System.err.println(ConsoleColor.RED.paint("File error: " + exception.getMessage()));
            return EXIT_FAILURE;
        } catch (IllegalArgumentException exception) {
            // Wrong input: an unknown command, a bad number, or an invalid card file.
            System.err.println(ConsoleColor.RED.paint("Invalid input: " + exception.getMessage()));
            // The usage text is not relevant to a card-file problem, so validate skips it.
            if (args.length == 0 || !args[0].equals("validate")) {
                printUsage();
            }
            return EXIT_INVALID_INPUT;
        } catch (IllegalStateException exception) {
            // Something went wrong inside the game, such as a lost card.
            System.err.println(ConsoleColor.RED.paint("Game error: " + exception.getMessage()));
            return EXIT_FAILURE;
        }
    }

    /**
     * Creates the GameController with its three parts: a VisibleSolver with the move and time limits, a ConsoleInput
     * for the keyboard, and a ConsoleRenderer for the screen. The limits come from the settings unless the command
     * line gives them.
     */
    private static GameController getController(String[] args, Scanner input, GameConfig config) {
        // The move limit is argument 3 and the time limit is argument 4; if missing, use the settings.
        SolitaireSolver.Limits limits = new SolitaireSolver.Limits(
                intArgument(args, 2, config.maxMoves()),
                numberArgument(args, 3, config.maxMillis(), Long.MAX_VALUE));
        // The keyboard reader also needs the card file loader and the highest allowed game count.
        ConsoleInput consoleInput = new ConsoleInput(input, new CardFileLoader(config.cardFilesFolder()),
                config.maxGameCount());
        // Give the controller everything it needs (dependency injection).
        return new GameController(new VisibleSolver(limits), consoleInput, new ConsoleRenderer());
    }

    /**
     * Checks the command before anything runs: the command must exist, it must not have too many arguments, validate
     * needs exactly one file, and the output option may only be --compact or --verbose. Otherwise it throws an error.
     */
    private static void validateArguments(String command, String[] args) {
        // validate must be followed by exactly one file name.
        if (command.equals("validate") && args.length != 2) {
            throw new IllegalArgumentException("Usage: validate <card-file>");
        }
        // Find how many arguments this command may have, counting the command itself.
        int maximumArguments = switch (command) {
            // These take no extra argument.
            case "shuffle", "menu", "manual", "help", "--help" -> 1;
            // These take one extra argument: the output option or the card file.
            case "guaranteed", "validate" -> 2;
            // These take up to three: an option or game count, the move limit, and the time limit.
            case "random", "simulate", "compare" -> 4;
            // Any other word is not a command.
            default -> throw new IllegalArgumentException("Unknown command: " + command);
        };
        if (args.length > maximumArguments) {
            throw new IllegalArgumentException("Too many arguments for " + command);
        }
        // Find where the --compact/--verbose option is for this command; -1 means the command has no such option.
        int outputOptionIndex = switch (command) {
            // Only these two commands accept --compact or --verbose, as the second argument.
            case "guaranteed", "random" -> OUTPUT_OPTION_INDEX;
            // Every other command has no output option.
            default -> -1;
        };
        // If the option was typed, it must be exactly --compact or --verbose.
        if (outputOptionIndex >= 0 && args.length > outputOptionIndex
                && !args[outputOptionIndex].equals("--compact") && !args[outputOptionIndex].equals("--verbose")) {
            throw new IllegalArgumentException("Output option must be --compact or --verbose");
        }
    }

    /** Returns true when the second argument is --compact, meaning only the final board should be printed. */
    private static boolean hasCompactOption(String[] args) {
        return args.length > OUTPUT_OPTION_INDEX && args[OUTPUT_OPTION_INDEX].equals("--compact");
    }

    /**
     * Reads the argument at the given position as an int, or returns the default value if the argument was not typed.
     */
    private static int intArgument(String[] args, int index, int defaultValue) {
        // Reuse numberArgument() with the largest int as the limit, then convert the result to int.
        return (int) numberArgument(args, index, defaultValue, Integer.MAX_VALUE);
    }

    /**
     * Reads the argument at the given position as a whole number, or returns the default value if it is missing. If it
     * is not a number or is too large, it throws an error that names the command and the wrong value.
     */
    private static long numberArgument(String[] args, int index, long defaultValue, long maximum) {
        // The argument was not typed, so use the default.
        if (args.length <= index) {
            return defaultValue;
        }
        // Convert the text to a number; Java's own error message is replaced with a clearer one.
        String where = args[0] + " argument " + index;
        long value;
        try {
            value = Long.parseLong(args[index]);
        } catch (NumberFormatException notANumber) {
            throw new IllegalArgumentException(where + " must be a whole number, but is '" + args[index] + "'");
        }
        // Reject a number that is larger than allowed.
        if (value > maximum) {
            throw new IllegalArgumentException(where + " is too large: " + args[index]);
        }
        return value;
    }

    /** Prints the list of commands and their optional arguments. */
    private static void printUsage() {
        System.out.println("""
                Commands:
                  shuffle | menu        Interactive setup (standard deck or card file).
                  validate <card-file>  Check a deck file without starting a game.
                  guaranteed [--compact|--verbose]
                  random [--compact|--verbose [moves [ms]]]
                  simulate [count [moves [ms]]]
                  compare [count [moves [ms]]]
                  manual
                  help""");
    }
}
