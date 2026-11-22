package solitaire;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

import solitaire.game.GameController;
import solitaire.io.CardFileLoader;
import solitaire.io.ConsoleInput;
import solitaire.shuffle.ShuffleConfiguration;
import solitaire.shuffle.ShuffleType;
import solitaire.solver.SolitaireSolver;
import solitaire.solver.VisibleSolver;

/**
 * Reads the command-line arguments, runs the matching command, and turns failures into exit codes.
 *
 * <p>Type: final utility class with only static methods; it creates the main objects and connects them.
 *
 * @author Farhiya Ayyub
 */
public final class SolitaireApplication {
    // Constants: default solver limits and settings, and the exit codes.
    private static final int DEFAULT_MAX_DECISIONS = 15_000;
    private static final int DEFAULT_MAX_MOVES = 3_000;
    private static final long DEFAULT_MAX_MILLIS = 10_000;
    private static final int DEFAULT_GAME_COUNT = 100;
    private static final long DEFAULT_SEED = 0;
    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_FAILURE = 1;
    private static final int EXIT_INVALID_INPUT = 2;

    /** Constructor (private): Prevents creating an object of this command-line utility. */
    private SolitaireApplication() {
    }

    /**
     * Method (static): Runs a command; returns 0 on success, 2 for invalid input, or 1 for a file or game error.
     * Calls: validateArguments(), intArgument(), longArgument(), CardFileLoader.load(),
     * GameController.shuffleAndPlay(), GameController.guaranteed(), GameController.automatic(),
     * GameController.simulate(), GameController.compare(), GameController.manual(), hasCompactOption(), printUsage()
     */
    public static int run(String[] args) {
        // Object: Scanner - reads the keyboard; closed automatically at the end of this block.
        try (Scanner input = new Scanner(System.in)) {
            String command = args.length == 0 ? "shuffle" : args[0];
            validateArguments(command, args);
            // Object: Limits - the solver's decision, move, and time limits.
            SolitaireSolver.Limits limits = new SolitaireSolver.Limits(
                    intArgument(args, 3, DEFAULT_MAX_DECISIONS),
                    intArgument(args, 4, DEFAULT_MAX_MOVES),
                    longArgument(args, 5, DEFAULT_MAX_MILLIS));
            // Objects: GameController, with a VisibleSolver (used through the SolitaireSolver interface)
            // and a ConsoleInput.
            GameController controller = new GameController(new VisibleSolver(limits), new ConsoleInput(input));
            switch (command) {
                case "validate" -> {
                    new CardFileLoader().load(Path.of(args[1]));
                    System.out.println("VALID: " + args[1] + " - 52 unique cards, valid suits and ranks.");
                }
                case "shuffle", "menu" -> controller.shuffleAndPlay();
                case "guaranteed" -> controller.guaranteed(hasCompactOption(args, 1));
                case "random" -> controller.automatic(
                        new ShuffleConfiguration(ShuffleType.RANDOM, false, longArgument(args, 1, DEFAULT_SEED)),
                        hasCompactOption(args, 2));
                case "simulate" -> controller.simulate(intArgument(args, 1, DEFAULT_GAME_COUNT),
                        longArgument(args, 2, DEFAULT_SEED));
                case "compare" -> controller.compare(intArgument(args, 1, DEFAULT_GAME_COUNT),
                        longArgument(args, 2, DEFAULT_SEED));
                case "manual" -> controller.manual(DEFAULT_SEED);
                case "help", "--help" -> printUsage();
                default -> throw new IllegalArgumentException("Unknown command: " + command);
            }
            return EXIT_SUCCESS;
        // Each kind of error gets its own message and exit code.
        } catch (IOException exception) {
            System.err.println("File error: " + exception.getMessage());
            return EXIT_FAILURE;
        } catch (IllegalArgumentException exception) {
            System.err.println("Invalid input: " + exception.getMessage());
            if (args.length == 0 || !args[0].equals("validate")) {
                printUsage();
            }
            return EXIT_INVALID_INPUT;
        } catch (IllegalStateException exception) {
            System.err.println("Game error: " + exception.getMessage());
            return EXIT_FAILURE;
        }
    }

    /** Method: Checks the argument count and the output option for the command. */
    private static void validateArguments(String command, String[] args) {
        if (command.equals("validate") && args.length != 2) {
            throw new IllegalArgumentException("Usage: validate <card-file>");
        }
        int maximumArguments = switch (command) {
            case "shuffle", "menu", "manual", "help", "--help" -> 1;
            case "guaranteed", "validate" -> 2;
            case "random", "simulate", "compare" -> 6;
            default -> throw new IllegalArgumentException("Unknown command: " + command);
        };
        if (args.length > maximumArguments) {
            throw new IllegalArgumentException("Too many arguments for " + command);
        }
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

    /** Method: Reports whether the argument at the index is --compact. */
    private static boolean hasCompactOption(String[] args, int index) {
        return args.length > index && args[index].equals("--compact");
    }

    /**
     * Method: Parses an int argument, or returns the default when it is absent.
     * Calls: Integer.parseInt()
     */
    private static int intArgument(String[] args, int index, int defaultValue) {
        return args.length > index ? Integer.parseInt(args[index]) : defaultValue;
    }

    /**
     * Method: Parses a long argument, or returns the default when it is absent.
     * Calls: Long.parseLong()
     */
    private static long longArgument(String[] args, int index, long defaultValue) {
        return args.length > index ? Long.parseLong(args[index]) : defaultValue;
    }

    /** Method: Prints the supported commands and their argument formats. */
    private static void printUsage() {
        System.out.println("Commands:\n"
                + "  shuffle | menu        Interactive setup (standard deck or card file).\n"
                + "  validate <card-file>  Check a deck file without starting a game.\n"
                + "  guaranteed [--compact|--verbose]\n"
                + "  random [seed [--compact|--verbose [decisions [moves [ms]]]]]\n"
                + "  simulate [count [firstSeed [decisions [moves [ms]]]]]\n"
                + "  compare [count [firstSeed [decisions [moves [ms]]]]]\n"
                + "  manual\n"
                + "  help");
    }
}
