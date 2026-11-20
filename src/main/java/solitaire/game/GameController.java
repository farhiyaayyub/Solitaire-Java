package solitaire.game;

import java.util.Objects;
import java.util.List;

import solitaire.enums.GameStatus;
import solitaire.enums.ShuffleType;
import solitaire.io.ConsoleInput;
import solitaire.io.ConsoleRenderer;
import solitaire.model.Deck;
import solitaire.move.Move;
import solitaire.move.MoveGenerator;
import solitaire.shuffle.ShuffleConfiguration;
import solitaire.solver.SolvableDealProvider;
import solitaire.solver.SolitaireSolver;
import solitaire.solver.VisibleSolver;

/**
 * Coordinates setup, solver replay, manual play, and simulation reports.
 *
 * @author Farhiya Ayyub
 */
public final class GameController {
    private final ConsoleRenderer renderer = new ConsoleRenderer();
    private final ConsoleInput consoleInput;
    private final SolitaireSolver.Limits limits;

    /** Stores execution limits and input without starting a game. */
    /** Stores the solver limits and console input used by this controller. */
    public GameController(SolitaireSolver.Limits limits, ConsoleInput consoleInput) {
        this.limits = Objects.requireNonNull(limits);
        this.consoleInput = Objects.requireNonNull(consoleInput);
    }

    /** Creates a reproducible Draw 1 board from a random shuffle. */
    /** Creates a reproducible random deal from the supplied seed. */
    public static GameState random(long seed) {
        Deck deck = new Deck();
        deck.shuffle(seed);
        return GameState.deal(deck);
    }

    /** Reads the two setup choices and starts play unless cancelled. */
    public void shuffleAndPlay() {
        ConsoleRenderer.heading("KLONDIKE SOLITAIRE");
        do {
            ShuffleConfiguration configuration = consoleInput.shuffleConfiguration();
            if (configuration == null) return;
            int drawCount = consoleInput.drawCount();
            if (drawCount == 0) return;
            String mode = consoleInput.playMode();
            if (mode.equals("q")) return;
            if (mode.equals("2")) {
                int count = consoleInput.gameCount();
                if (count == 0) return;
                simulate(count, configuration, drawCount, false);
                if (consoleInput.wantsToWatchSimulationGame()) watchSimulationGame(configuration, drawCount, 0);
            } else {
                automatic(configuration, drawCount, false);
            }
        } while (consoleInput.playAgain());
        System.out.println("Thank you for playing!");
    }

    /** Plays Draw 1; compact output omits intermediate boards. */
    public void automatic(ShuffleConfiguration configuration, boolean compact) {
        automatic(configuration, 1, compact);
    }

    /** Solves and legally replays the selected shuffle and draw mode. */
    public void automatic(ShuffleConfiguration configuration, int drawCount, boolean compact) {
        automatic(configuration, new Deck(), drawCount, compact);
    }

    /** Runs automatic play from a validated supplied deck. */
    public void automatic(ShuffleConfiguration configuration, Deck initialDeck, int drawCount, boolean compact) {
        renderer.shuffleConfiguration(configuration);
        System.out.println("Playing... (Draw " + drawCount + ")");
        SolitaireGame game = new SolitaireGame(GameState.deal(configuration.prepareDeck(initialDeck), drawCount));
        if (!compact) renderer.render(game);
        SolitaireSolver solver = new VisibleSolver(limits);
        SolitaireSolver.Result result = solver.solve(game.state());
        replay(game, result.moves(), compact);
        game.finish(result.status());
        renderer.render(game);
    }
    /** Replays the constructed deal's verified winning sequence. */
    public void guaranteed(boolean compact) {
        SolvableDealProvider.VerifiedDeal deal = new SolvableDealProvider().create();
        SolitaireGame game = new SolitaireGame(deal.state());
        if (!compact) renderer.render(game);
        replay(game, deal.solution(), compact);
        game.finish(GameStatus.WON);
        renderer.render(game);
        renderer.solverDetail("Practice deal with a verified solution.");
    }
    /** Applies a move path to the game and displays each step when requested. */
    private void replay(SolitaireGame game, List<Move> moves, boolean compact) {
        for (Move move : moves) {
            if (!compact) renderer.nextMove(move);
            if (!game.play(move)) throw new IllegalStateException("Invalid solution path");

            if (!compact) renderer.render(game);
        }
    }

    /** Reports random-game results for consecutive seeds. */
    public void simulate(int gameCount, long firstSeed) {
        simulate(gameCount, firstSeed, ShuffleType.RANDOM, 1);
    }

    /** Compares shuffle methods over matching starting seeds. */
    public void compare(int gameCount, long firstSeed) {
        simulate(gameCount, firstSeed, ShuffleType.RANDOM, 1);
        simulate(gameCount, firstSeed, ShuffleType.IN, 1);
        simulate(gameCount, firstSeed, ShuffleType.OUT, 1);

    }

    /** Runs a batch of games using the selected shuffle strategy. */
    private void simulate(int gameCount, long firstSeed, ShuffleType type, int repetitions) {
        simulate(gameCount, new ShuffleConfiguration(type, repetitions, type != ShuffleType.RANDOM, firstSeed), 1, true);
    }

    /** Runs games for consecutive seeds and reports their outcomes. */
    private void simulate(int gameCount, ShuffleConfiguration configuration, int drawCount, boolean showEachGame) {
        if (gameCount < 1) throw new IllegalArgumentException("Game count must be positive");
        renderer.simulationStarted();
        renderer.shuffleConfiguration(configuration);
        System.out.println("Playing... (Draw " + drawCount + ")");
        long started = System.nanoTime();
        int won = 0, limitedGames = 0, stuckGames = 0;
        long moves = 0, totalDecisions = 0;
        for (int gameIndex = 0; gameIndex < gameCount; gameIndex++) {
            SolitaireGame game = new SolitaireGame(GameState.deal(new ShuffleConfiguration(
                    configuration.type(), configuration.repetitions(), configuration.randomizedInitial(),
                    configuration.seed() + gameIndex).prepareDeck(), drawCount));
            var result = new VisibleSolver(limits).solve(game.state());
            replay(game, result.moves(), true);
            game.finish(result.status());
            if (game.status() == GameStatus.WON) won++;
            else if (game.status() == GameStatus.SEARCH_LIMIT_REACHED) limitedGames++;
            else stuckGames++;
            moves += game.moves();
            totalDecisions += result.explored();
            if (showEachGame) renderer.simulationGame(gameIndex + 1, game.status(), game.moves());
        }
        renderer.simulationSummary(gameCount, won, limitedGames, stuckGames, moves, totalDecisions);
        System.out.printf(java.util.Locale.ROOT, "  Elapsed: %.1f seconds%n", (System.nanoTime() - started) / 1_000_000_000.0);
    }

    /** Replays one deal from a completed simulation with every move and board visible. */
    private void watchSimulationGame(ShuffleConfiguration configuration, int drawCount, int gameIndex) {
        ShuffleConfiguration selectedDeal = new ShuffleConfiguration(configuration.type(), configuration.repetitions(),
                configuration.randomizedInitial(), configuration.seed() + gameIndex);
        renderer.heading("WATCHING SIMULATED GAME " + (gameIndex + 1));
        renderer.shuffleConfiguration(selectedDeal);
        System.out.println("Playing... (Draw " + drawCount + ")");
        SolitaireGame game = new SolitaireGame(GameState.deal(selectedDeal.prepareDeck(), drawCount));
        renderer.render(game);
        SolitaireSolver.Result result = new VisibleSolver(limits).solve(game.state());
        replay(game, result.moves(), false);
        game.finish(result.status());
        renderer.render(game);
    }

    /** Accepts legal move selections until victory or cancellation. */
    public void manual(long seed) {
        SolitaireGame game = new SolitaireGame(random(seed));
        MoveGenerator generator = new MoveGenerator();
        while (!game.state().won()) {
            renderer.render(game);
            List<Move> moves = generator.generate(game.state());
            Move selected = consoleInput.selectMove(moves, renderer);
            if (selected == null) return;
            if (!game.play(selected)) throw new IllegalStateException("Selected move is no longer legal");
        }
        renderer.render(game);
    }
}
