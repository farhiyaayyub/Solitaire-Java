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
        GameConfig config;
        try {
            config = GameConfig.load(GameConfig.DEFAULT_FILE);
        } catch (IllegalArgumentException exception) {
            System.err.println(ConsoleColor.RED.paint("Configuration error: " + exception.getMessage()));
            return EXIT_INVALID_INPUT;
        } catch (IOException | IllegalStateException exception) {
            System.err.println(ConsoleColor.RED.paint("Configuration error: " + exception.getMessage()));
            return EXIT_FAILURE;
        }
        try (Scanner input = new Scanner(System.in)) {
            String command = args.length == 0 ? "shuffle" : args[0];
            validateArguments(command, args);
            GameController controller = getController(args, input, config);
            switch (command) {
                case "validate" -> {
                    new CardFileLoader(config.cardFilesFolder()).load(Path.of(args[1]));
                    System.out.println("VALID: " + args[1] + " - 52 unique cards, valid suits and ranks.");
                }
                case "shuffle", "menu" -> controller.shuffleAndPlay();
                case "guaranteed" -> controller.guaranteed(hasCompactOption(args));
                case "random" -> controller.automatic(new ShuffleConfiguration(ShuffleType.RANDOM, false),
                        hasCompactOption(args));
                case "simulate" -> controller.simulate(intArgument(args, 1, config.defaultGameCount()));
                case "compare" -> controller.compare(intArgument(args, 1, config.defaultGameCount()));
                case "manual" -> controller.manual();
                case "help", "--help" -> printUsage();
                default -> throw new IllegalArgumentException("Unknown command: " + command);
            }
            return EXIT_SUCCESS;
        } catch (IOException exception) {
            System.err.println(ConsoleColor.RED.paint("File error: " + exception.getMessage()));
            return EXIT_FAILURE;
        } catch (IllegalArgumentException exception) {
            System.err.println(ConsoleColor.RED.paint("Invalid input: " + exception.getMessage()));
            if (args.length == 0 || !args[0].equals("validate")) {
                printUsage();
            }
            return EXIT_INVALID_INPUT;
        } catch (IllegalStateException exception) {
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
        SolitaireSolver.Limits limits = new SolitaireSolver.Limits(
                intArgument(args, 2, config.maxMoves()),
                numberArgument(args, 3, config.maxMillis(), Long.MAX_VALUE));
        ConsoleInput consoleInput = new ConsoleInput(input, new CardFileLoader(config.cardFilesFolder()),
                config.maxGameCount());
        return new GameController(new VisibleSolver(limits), consoleInput, new ConsoleRenderer());
    }

    /**
     * Checks the command before anything runs: the command must exist, it must not have too many arguments, validate
     * needs exactly one file, and the output option may only be --compact or --verbose. Otherwise, it throws an error.
     */
    private static void validateArguments(String command, String[] args) {
        if (command.equals("validate") && args.length != 2) {
            throw new IllegalArgumentException("Usage: validate <card-file>");
        }
        int maximumArguments = switch (command) {
            case "shuffle", "menu", "manual", "help", "--help" -> 1;
            case "guaranteed", "validate" -> 2;
            case "random", "simulate", "compare" -> 4;
            default -> throw new IllegalArgumentException("Unknown command: " + command);
        };
        if (args.length > maximumArguments) {
            throw new IllegalArgumentException("Too many arguments for " + command);
        }
        int outputOptionIndex = switch (command) {
            case "guaranteed", "random" -> OUTPUT_OPTION_INDEX;
            default -> -1;
        };
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
        return (int) numberArgument(args, index, defaultValue, Integer.MAX_VALUE);
    }

    /**
     * Reads the argument at the given position as a whole number, or returns the default value if it is missing. If it
     * is not a number or is too large, it throws an error that names the command and the wrong value.
     */
    private static long numberArgument(String[] args, int index, long defaultValue, long maximum) {
        if (args.length <= index) {
            return defaultValue;
        }
        String where = args[0] + " argument " + index;
        long value;
        try {
            value = Long.parseLong(args[index]);
        } catch (NumberFormatException notANumber) {
            throw new IllegalArgumentException(where + " must be a whole number, but is '" + args[index] + "'");
        }
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
