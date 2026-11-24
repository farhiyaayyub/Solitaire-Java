package solitaire;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

import solitaire.config.GameConfig;
import solitaire.game.GameController;
import solitaire.io.CardFileLoader;
import solitaire.io.ConsoleColor;
import solitaire.io.ConsoleInput;
import solitaire.shuffle.ShuffleConfiguration;
import solitaire.shuffle.ShuffleType;
import solitaire.solver.SolitaireSolver;
import solitaire.solver.VisibleSolver;

/**
 * Reads the command-line arguments, creates the main objects, runs the requested command, and converts each kind of
 * failure into an exit code. With no arguments it runs the interactive menu ("shuffle").
 *
 * <p>All methods are static, because the class only connects the other objects and has no state of its own.
 *
 * @author Farhiya Ayyub
 */
public final class SolitaireApplication {
    // Exit codes. The solver limits, game counts, and seed are no longer fixed here: they come from
    // solitaire.properties through GameConfig, so they can change without rebuilding.
    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_FAILURE = 1;
    private static final int EXIT_INVALID_INPUT = 2;

    /** Prevents objects of this class; all its methods are static. */
    private SolitaireApplication() {
    }

    /**
     * Reads the settings, validates the arguments, builds the controller, and runs one command. Errors are caught here
     * and printed in red.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link GameConfig#load} - reads solitaire.properties, or uses the defaults</li>
     *   <li>{@code validateArguments()} - rejects bad commands before any work starts</li>
     *   <li>{@code getController()} - builds the controller with the solver limits</li>
     *   <li>{@code intArgument()} - reads a whole-number argument or its default</li>
     *   <li>{@code longArgument()} - reads a seed or time argument or its default</li>
     *   <li>{@link CardFileLoader#load} - checks the card file for the validate command</li>
     *   <li>{@link GameController#shuffleAndPlay} - runs the interactive menu</li>
     *   <li>{@code hasCompactOption()} - checks whether only the final board is wanted</li>
     *   <li>{@link GameController#guaranteed} - plays the known-winnable deal</li>
     *   <li>{@link GameController#automatic} - plays one automatic game</li>
     *   <li>{@link GameController#simulate(int, long)} - plays many games and prints a summary</li>
     *   <li>{@link GameController#compare} - compares the three shuffle types</li>
     *   <li>{@link GameController#manual} - lets the player choose every move</li>
     *   <li>{@code printUsage()} - shows the list of commands</li>
     *   <li>{@link ConsoleColor#paint} - prints each error message in red</li>
     * </ul>
     *
     * <p>Called by: {@link Main#main}.
     *
     * @param args the command-line arguments; the first one is the command
     * @return 0 on success, 1 for a file or game error, or 2 for invalid input or invalid settings (including a
     *     number that cannot be parsed)
     */
    public static int run(String[] args) {
        // The settings are read first, so a broken configuration file is reported before anything else happens.
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
                case "guaranteed" -> controller.guaranteed(hasCompactOption(args, 1));
                case "random" -> controller.automatic(new ShuffleConfiguration(ShuffleType.RANDOM, false,
                        longArgument(args, 1, config.defaultSeed())), hasCompactOption(args, 2));
                case "simulate" -> controller.simulate(intArgument(args, 1, config.defaultGameCount()),
                        longArgument(args, 2, config.defaultSeed()));
                case "compare" -> controller.compare(intArgument(args, 1, config.defaultGameCount()),
                        longArgument(args, 2, config.defaultSeed()));
                case "manual" -> controller.manual(config.defaultSeed());
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
     * Creates the controller and the objects it needs. The solver limits come from the settings, unless arguments 4
     * and 5 (indexes 3 and 4) give them on the command line.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@code intArgument()} / {@code longArgument()} - read a limit from the command line or the settings</li>
     * </ul>
     *
     * <p>Called by: {@link #run}.
     *
     * @param args the command-line arguments; the first one is the command
     * @param input reads the player's answers
     * @param config the settings from solitaire.properties
     * @return a controller with a VisibleSolver and a ConsoleInput
     */
    private static GameController getController(String[] args, Scanner input, GameConfig config) {
        SolitaireSolver.Limits limits = new SolitaireSolver.Limits(
                intArgument(args, 3, config.maxMoves()),
                longArgument(args, 4, config.maxMillis()));
        // Each object receives what it needs from outside (dependency injection): the solver is passed in through
        // the SolitaireSolver interface, and ConsoleInput gets its file loader and game limit from the settings.
        ConsoleInput consoleInput = new ConsoleInput(input, new CardFileLoader(config.cardFilesFolder()),
                config.maxGameCount());
        return new GameController(new VisibleSolver(limits), consoleInput);
    }

    /**
     * Checks the command and its arguments before any work starts.
     *
     * <p>Called by: {@link #run}.
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
            case "random", "simulate", "compare" -> 5;
            default -> throw new IllegalArgumentException("Unknown command: " + command);
        };
        if (args.length > maximumArguments) {
            throw new IllegalArgumentException("Too many arguments for " + command);
        }
        // Position of the output option for the commands that accept one; -1 means the command has none.
        int outputOptionIndex = switch (command) {
            case "guaranteed" -> 1;
            case "random" -> 2;
            default -> -1;
        };
        if (outputOptionIndex >= 0 && args.length > outputOptionIndex
                && !args[outputOptionIndex].equals("--compact") && !args[outputOptionIndex].equals("--verbose")) {
            throw new IllegalArgumentException("Output option must be --compact or --verbose");
        }
    }

    /**
     * Returns {@code true} only if the argument at the index exists and is exactly --compact.
     *
     * <p>Called by: {@link #run}.
     *
     * @param args the command-line arguments; the first one is the command
     * @param index the position of the output option
     * @return {@code true} only if the argument at the index exists and is exactly --compact
     */
    private static boolean hasCompactOption(String[] args, int index) {
        return args.length > index && args[index].equals("--compact");
    }

    /**
     * Returns the argument at the index as an int, or the default when the argument is missing.
     *
     * <p>Called by: {@link #run}.
     *
     * @param args the command-line arguments; the first one is the command
     * @param index the position of the argument
     * @param defaultValue the value to use when the argument is missing
     * @return the argument, or the default
     * @throws NumberFormatException if the argument is not a whole number
     */
    private static int intArgument(String[] args, int index, int defaultValue) {
        return args.length > index ? Integer.parseInt(args[index]) : defaultValue;
    }

    /**
     * Returns the argument at the index as a long, or the default when the argument is missing.
     *
     * <p>Called by: {@link #run}.
     *
     * @param args the command-line arguments; the first one is the command
     * @param index the position of the argument
     * @param defaultValue the value to use when the argument is missing
     * @return the argument, or the default
     * @throws NumberFormatException if the argument is not a whole number
     */
    private static long longArgument(String[] args, int index, long defaultValue) {
        return args.length > index ? Long.parseLong(args[index]) : defaultValue;
    }

    /**
     * Prints every command with its optional arguments in brackets.
     *
     * <p>Called by: {@link #run}.
     */
    private static void printUsage() {
        System.out.println("""
                Commands:
                  shuffle | menu        Interactive setup (standard deck or card file).
                  validate <card-file>  Check a deck file without starting a game.
                  guaranteed [--compact|--verbose]
                  random [seed [--compact|--verbose [moves [ms]]]]
                  simulate [count [firstSeed [moves [ms]]]]
                  compare [count [firstSeed [moves [ms]]]]
                  manual
                  help""");
    }
}
