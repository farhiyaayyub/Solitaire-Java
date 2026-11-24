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
 * Command-line front end that wires the program together.
 *
 * <p>Kind: final class with only static methods, uses GameConfig, GameController, CardFileLoader, ConsoleInput,
 * ConsoleRenderer, and VisibleSolver.
 * <p>Purpose: reads the command, builds the main objects, runs the command, and turns every kind of failure into an
 * exit code and a red error message. With no arguments it runs the interactive menu ("shuffle").
 * <p>Data: none; it only holds the three exit-code constants (0, 1, 2) and creates the objects inside run().
 * <p>OOP: Abstraction - the controller receives its solver as a SolitaireSolver, so another solver could be plugged
 * in here without changing GameController. Composition - this class creates the objects and passes each one what it
 * needs (dependency injection).
 *
 * @author Farhiya Ayyub
 */
public final class SolitaireApplication {
    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_FAILURE = 1;
    private static final int EXIT_INVALID_INPUT = 2;
    // Position of --compact/--verbose for the guaranteed and random commands.
    private static final int OUTPUT_OPTION_INDEX = 1;

    /** Prevents objects of this class; all its methods are static. */
    private SolitaireApplication() {
    }

    /**
     * Loads the settings first, so a broken configuration file is reported before anything else, then checks the
     * arguments and runs the command. A card-file error from validate is not followed by the usage text.
     *
     * <p>Called by: Main.main().
     *
     * @param args the command-line arguments; the first one is the command
     * @return 0 on success, 1 for a file or game error, or 2 for invalid input or invalid settings (including a
     *     number that cannot be parsed)
     */
    public static int run(String[] args) {
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
            // The usage text is not relevant to a card-file problem, so validate skips it.
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
     * Builds the controller and its parts. The solver limits come from the settings, unless arguments 3 and 4
     * (indexes 2 and 3) give them on the command line.
     *
     * <p>Called by: run().
     *
     * @param args the command-line arguments; the first one is the command
     * @param input reads the player's answers
     * @param config the settings from solitaire.properties
     * @return a controller with a VisibleSolver, a ConsoleInput, and a ConsoleRenderer
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
     * Checks the argument count for each command and the --compact/--verbose option, so no game starts with a typo.
     *
     * <p>Called by: run().
     *
     * @param command the command name, such as "random"
     * @param args all command-line arguments, including the command
     * @throws IllegalArgumentException for an unknown command, too many arguments, a validate command without exactly
     *     one file, or an output option other than --compact or --verbose
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
        // -1 means the command takes no output option.
        int outputOptionIndex = switch (command) {
            case "guaranteed", "random" -> OUTPUT_OPTION_INDEX;
            default -> -1;
        };
        if (outputOptionIndex >= 0 && args.length > outputOptionIndex
                && !args[outputOptionIndex].equals("--compact") && !args[outputOptionIndex].equals("--verbose")) {
            throw new IllegalArgumentException("Output option must be --compact or --verbose");
        }
    }

    /**
     * {@return {@code true} only if the output option (argument index 1) exists and is exactly --compact}
     *
     * <p>Called by: run().
     *
     * @param args the command-line arguments; the first one is the command
     */
    private static boolean hasCompactOption(String[] args) {
        return args.length > OUTPUT_OPTION_INDEX && args[OUTPUT_OPTION_INDEX].equals("--compact");
    }

    /**
     * {@return the argument at the index as an int, or the default when the argument is missing}
     *
     * <p>Called by: run(), getController().
     *
     * @param args the command-line arguments; the first one is the command
     * @param index the position of the argument
     * @param defaultValue the value to use when the argument is missing
     * @throws IllegalArgumentException if the argument is not a whole number; the message names the argument
     */
    private static int intArgument(String[] args, int index, int defaultValue) {
        return (int) numberArgument(args, index, defaultValue, Integer.MAX_VALUE);
    }

    /**
     * {@return the argument at the index as a whole number, or the default when it is missing}
     *
     * <p>Key idea: Java's own parse error ("For input string: ...") is replaced by a message that names the command,
     * the argument, and the wrong value, so the player knows what to fix.
     *
     * <p>Called by: intArgument(), getController().
     *
     * @param args the command-line arguments; the first one is the command
     * @param index the position of the argument
     * @param defaultValue the value to use when the argument is missing
     * @param maximum the largest accepted value
     * @throws IllegalArgumentException if the argument is not a whole number or is larger than the maximum
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

    /** Prints every command with its optional arguments in brackets. Called by: run(). */
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
