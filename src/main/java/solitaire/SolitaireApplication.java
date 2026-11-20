package solitaire;

import solitaire.game.GameController;
import solitaire.solver.SolitaireSolver;
import solitaire.io.ConsoleInput;
import solitaire.shuffle.ShuffleConfiguration;
import solitaire.enums.ShuffleType;
import java.util.Scanner;
import java.io.IOException;
import java.nio.file.Path;
import solitaire.io.CardFileLoader;

/**
 * Runs the command-line application and routes user commands.
 *
 * @author Farhiya Ayyub
 */
public final class SolitaireApplication {
    private static final int DEFAULT_MAX_STATES = 15_000;
    private static final int DEFAULT_MAX_MOVES = 3000;
    private static final long DEFAULT_TIMEOUT_MILLIS = 10_000;
    private static final int DEFAULT_GAME_COUNT = 100;
    private static final long DEFAULT_SEED = 0;
    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_GAME_ERROR = 1;
    private static final int EXIT_INVALID_INPUT = 2;

    /** Prevents creating an instance of this command-line utility. */
    private SolitaireApplication() { }

    /** Dispatches command-line input; returns 0 on completion, 2 for invalid input, or 1 for a game-state failure. */
    public static int run(String[] args) {
        try (Scanner input = new Scanner(System.in)) {
            String command = args.length == 0 ? "shuffle" : args[0];
            validateArguments(command, args);
            int maxDecisions = args.length > 3 ? Integer.parseInt(args[3]) : DEFAULT_MAX_STATES;
            int maxMoves = args.length > 4 ? Integer.parseInt(args[4]) : DEFAULT_MAX_MOVES;
            long timeoutMillis = args.length > 5 ? Long.parseLong(args[5]) : DEFAULT_TIMEOUT_MILLIS;
            GameController controller = new GameController(new SolitaireSolver.Limits(maxDecisions, maxMoves, timeoutMillis), new ConsoleInput(input));
            switch (command) {
                case "validate" -> {
                    new CardFileLoader().load(Path.of(args[1]));
                    System.out.println("VALID: " + args[1] + " - 52 unique cards, valid suits and ranks.");
                }
                case "shuffle", "menu" -> controller.shuffleAndPlay();

                case "guaranteed" -> controller.guaranteed( args.length == 2 && args[1].equals("--compact"));
                case "random" -> controller.automatic(new ShuffleConfiguration(ShuffleType.RANDOM, 1, false,
                        args.length > 1 ? Long.parseLong(args[1]) : DEFAULT_SEED),
                        args.length > 2 && args[2].equals("--compact"));
                case "compare" -> controller.compare(args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_GAME_COUNT,
                        args.length > 2 ? Long.parseLong(args[2]) : DEFAULT_SEED);
                case "manual" -> controller.manual(DEFAULT_SEED);
                case "simulate" -> controller.simulate(
                        args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_GAME_COUNT,
                        args.length > 2 ? Long.parseLong(args[2]) : DEFAULT_SEED);
                case "help", "--help" -> printUsage();
                default -> throw new IllegalArgumentException("Unknown command: " + command);
            }
            return EXIT_SUCCESS;
        } catch (IOException exception) {
            System.err.println("File error: " + exception.getMessage());
            return EXIT_GAME_ERROR;
        } catch (IllegalArgumentException exception) {
            System.err.println("Invalid input: " + exception.getMessage());
            if (args.length == 0 || !args[0].equals("validate")) {
                printUsage();
            }
            return EXIT_INVALID_INPUT;
        } catch (IllegalStateException exception) {
            System.err.println("Game error: " + exception.getMessage());
            return EXIT_GAME_ERROR;
        }
    }

    /** Checks the command's argument count and supported output option. */
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
        if (args.length > maximumArguments) throw new IllegalArgumentException("Too many arguments for " + command);
        int outputOptionIndex = command.equals("guaranteed") ? 1 : command.equals("random") ? 2 : -1;
        if (outputOptionIndex >= 0 && args.length > outputOptionIndex
                && !args[outputOptionIndex].equals("--compact") && !args[outputOptionIndex].equals("--verbose"))
            throw new IllegalArgumentException("Output option must be --compact or --verbose");
    }

    /** Prints the supported commands and their argument formats. */
    private static void printUsage() {
        System.out.println("  validate <card-file>  Check a deck file without starting a game.");
        System.out.println("Commands:\n  shuffle\n  menu\n  guaranteed [--compact|--verbose]\n"
                + "  random [seed [--compact|--verbose [states [moves [ms]]]]]\n"
                + "  simulate [count [firstSeed [states [moves [ms]]]]]\n  compare [count [firstSeed [states [moves [ms]]]]]\n  manual\n  help");
    }
}
