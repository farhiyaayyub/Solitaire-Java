package solitaire.game;

import java.util.List;
import java.util.Objects;

import solitaire.enums.GameStatus;
import solitaire.io.ConsoleInput;
import solitaire.io.ConsoleRenderer;
import solitaire.model.Deck;
import solitaire.move.Move;
import solitaire.move.MoveGenerator;
import solitaire.shuffle.ShuffleConfiguration;
import solitaire.shuffle.ShuffleType;
import solitaire.solver.SolitaireSolver;
import solitaire.solver.SolvableDealProvider;

/**
 * Connects input, the game engine, the solver, and the display: it sets up each game, lets the solver play it,
 * replays the moves through the engine, and reports the result.
 *
 * <p>Type: final class; the controller.
 * OOP: ABSTRACTION (depends on the SolitaireSolver interface, not on VisibleSolver),
 * ENCAPSULATION (private fields), COMPOSITION (uses renderer, input, and solver objects).
 *
 * @author Farhiya Ayyub
 */
public final class GameController {
    // Field and object: the ConsoleRenderer that prints everything.
    private final ConsoleRenderer renderer = new ConsoleRenderer();
    // Field: the solver, typed as the interface so any solver implementation can be used.
    private final SolitaireSolver solver;
    // Field: reads what the player types.
    private final ConsoleInput consoleInput;

    /**
     * Constructor: Uses the supplied solver (any {@link SolitaireSolver} implementation) and console input.
     * Calls: Objects.requireNonNull()
     */
    public GameController(SolitaireSolver solver, ConsoleInput consoleInput) {
        this.solver = Objects.requireNonNull(solver);
        this.consoleInput = Objects.requireNonNull(consoleInput);
    }

    /**
     * Method: Runs the interactive menu until the player declines another game or cancels.
     * Calls: ConsoleRenderer.heading(), playFromMenu(), ConsoleInput.playAgain(), ConsoleRenderer.goodbye()
     */
    public void shuffleAndPlay() {
        renderer.heading("KLONDIKE SOLITAIRE");
        do {
            if (!playFromMenu()) {
                return;
            }
        } while (consoleInput.playAgain());
        renderer.goodbye();
    }

    /**
     * Method: Plays a Draw 1 game from a standard deck; compact output shows only the final board.
     * Calls: automatic(ShuffleConfiguration, Deck, int, boolean)
     */
    public void automatic(ShuffleConfiguration configuration, boolean compact) {
        // Object: Deck - a new standard deck.
        automatic(configuration, new Deck(), GameState.DRAW_ONE, compact);
    }

    /**
     * Method: Shuffles the deck, lets the solver play, and replays every move through the game engine.
     * Calls: ConsoleRenderer.shuffleConfiguration(), ConsoleRenderer.playing(), ShuffleConfiguration.prepareDeck(),
     * GameState.deal(), ConsoleRenderer.render(), solveAndReplay(), ConsoleRenderer.resultDetail()
     */
    public void automatic(ShuffleConfiguration configuration, Deck initialDeck, int drawCount, boolean compact) {
        renderer.shuffleConfiguration(configuration);
        renderer.playing(drawCount);
        // Object: SolitaireGame - the game engine for this deal.
        SolitaireGame game = new SolitaireGame(GameState.deal(configuration.prepareDeck(initialDeck), drawCount));
        if (!compact) {
            renderer.render(game);
        }
        SolitaireSolver.Result result = solveAndReplay(game, compact);
        renderer.render(game);
        renderer.resultDetail(result.detail());
    }

    /**
     * Method: Replays the constructed practice deal's verified winning moves.
     * Calls: SolvableDealProvider.create(), ConsoleRenderer.render(), replay(), SolitaireGame.finish(),
     * ConsoleRenderer.resultDetail()
     */
    public void guaranteed(boolean compact) {
        // Object: SolvableDealProvider - builds a deal that is known to be winnable.
        SolvableDealProvider.VerifiedDeal deal = new SolvableDealProvider().create();
        // Object: SolitaireGame - the game engine for the practice deal.
        SolitaireGame game = new SolitaireGame(deal.state());
        if (!compact) {
            renderer.render(game);
        }
        replay(game, deal.solution(), compact);
        game.finish(GameStatus.WON);
        renderer.render(game);
        renderer.resultDetail("Practice deal with a verified solution.");
    }

    /**
     * Method: Reports Draw 1 random-shuffle results for consecutive seeds.
     * Calls: simulateBatch()
     */
    public void simulate(int gameCount, long firstSeed) {
        simulateBatch(gameCount, firstSeed, ShuffleType.RANDOM);
    }

    /**
     * Method: Compares Random, In-Faro, and Out-Faro over the same starting seeds.
     * Calls: simulateBatch() (once for each shuffle)
     */
    public void compare(int gameCount, long firstSeed) {
        simulateBatch(gameCount, firstSeed, ShuffleType.RANDOM);
        simulateBatch(gameCount, firstSeed, ShuffleType.IN);
        simulateBatch(gameCount, firstSeed, ShuffleType.OUT);
    }

    /**
     * Method: Lets the player choose moves on a seeded random deal until they win, get stuck, or cancel.
     * Calls: ShuffleConfiguration.prepareDeck(), GameState.deal(), ConsoleRenderer.render(),
     * MoveGenerator.generate(), ConsoleInput.selectMove(), SolitaireGame.play()
     */
    public void manual(long seed) {
        // Object: ShuffleConfiguration - random shuffle with the given seed.
        Deck deck = new ShuffleConfiguration(ShuffleType.RANDOM, false, seed).prepareDeck();
        // Object: SolitaireGame - the game engine for this deal.
        SolitaireGame game = new SolitaireGame(GameState.deal(deck));
        // Object: MoveGenerator - lists the legal moves for the player.
        MoveGenerator generator = new MoveGenerator();
        while (!game.state().won()) {
            renderer.render(game);
            Move selected = consoleInput.selectMove(generator.generate(game.state()), renderer);
            if (selected == null) {
                return;
            }
            if (!game.play(selected)) {
                throw new IllegalStateException("Selected move is no longer legal");
            }
        }
        renderer.render(game);
    }

    /**
     * Method: Runs one round of the interactive menu; returns false if the player cancels.
     * Calls: ConsoleInput.deckSource(), ConsoleInput.loadDeck(), ConsoleInput.shuffleConfiguration(),
     * ConsoleInput.drawCount(), ConsoleInput.playMode(), ConsoleInput.gameCount(),
     * ConsoleInput.wantsToWatchSimulationGame(), automatic(), simulate(), ConsoleRenderer.heading()
     */
    private boolean playFromMenu() {
        String deckSource = consoleInput.deckSource();
        if (deckSource.equals("q")) {
            return false;
        }
        Deck fileDeck = null;
        if (deckSource.equals("2")) {
            fileDeck = consoleInput.loadDeck();
            if (fileDeck == null) {
                return false;
            }
        }
        boolean deckFromFile = fileDeck != null;
        ShuffleConfiguration configuration = consoleInput.shuffleConfiguration(deckFromFile);
        if (configuration == null) {
            return false;
        }
        int drawCount = consoleInput.drawCount();
        if (drawCount == 0) {
            return false;
        }
        if (deckFromFile) {
            automatic(configuration, fileDeck, drawCount, false);
            return true;
        }
        String mode = consoleInput.playMode();
        if (mode.equals("q")) {
            return false;
        }
        if (mode.equals("1")) {
            automatic(configuration, new Deck(), drawCount, false);
            return true;
        }
        int gameCount = consoleInput.gameCount();
        if (gameCount == 0) {
            return false;
        }
        simulate(gameCount, configuration, drawCount, false);
        if (consoleInput.wantsToWatchSimulationGame()) {
            renderer.heading("WATCHING SIMULATED GAME 1");
            automatic(configuration, new Deck(), drawCount, false);
        }
        return true;
    }

    /**
     * Method: Runs a Draw 1 batch for one shuffle type; Faro shuffles start from a randomized deck.
     * Calls: ShuffleType.isFaro(), simulate(int, ShuffleConfiguration, int, boolean)
     */
    private void simulateBatch(int gameCount, long firstSeed, ShuffleType type) {
        // Object: ShuffleConfiguration - the settings for this batch.
        simulate(gameCount, new ShuffleConfiguration(type, type.isFaro(), firstSeed), GameState.DRAW_ONE, true);
    }

    /**
     * Method: Plays games with consecutive seeds and reports their outcomes.
     * Calls: ConsoleRenderer.simulationStarted(), ConsoleRenderer.shuffleConfiguration(), ConsoleRenderer.playing(),
     * ShuffleConfiguration.withSeedOffset(), ShuffleConfiguration.prepareDeck(), GameState.deal(),
     * solveAndReplay(), ConsoleRenderer.simulationGame(), ConsoleRenderer.simulationSummary(),
     * ConsoleRenderer.elapsed()
     */
    private void simulate(int gameCount, ShuffleConfiguration configuration, int drawCount, boolean showEachGame) {
        if (gameCount < 1) {
            throw new IllegalArgumentException("Game count must be positive");
        }
        renderer.simulationStarted();
        renderer.shuffleConfiguration(configuration);
        renderer.playing(drawCount);
        long started = System.nanoTime();
        int won = 0;
        int limitedGames = 0;
        int stuckGames = 0;
        long totalMoves = 0;
        long totalDecisions = 0;
        for (int gameIndex = 0; gameIndex < gameCount; gameIndex++) {
            Deck deck = configuration.withSeedOffset(gameIndex).prepareDeck();
            // Object: SolitaireGame - the game engine for one simulated deal.
            SolitaireGame game = new SolitaireGame(GameState.deal(deck, drawCount));
            SolitaireSolver.Result result = solveAndReplay(game, true);
            switch (game.status()) {
                case WON -> won++;
                case LIMIT_REACHED -> limitedGames++;
                default -> stuckGames++;
            }
            totalMoves += game.moves();
            totalDecisions += result.decisions();
            if (showEachGame) {
                renderer.simulationGame(gameIndex + 1, game.status(), game.moves());
            }
        }
        renderer.simulationSummary(gameCount, won, limitedGames, stuckGames, totalMoves, totalDecisions);
        renderer.elapsed(System.nanoTime() - started);
    }

    /**
     * Method: Lets the solver play from the game's board, replays its moves on the game, and records the result.
     * Calls: SolitaireSolver.solve() (runs VisibleSolver), SolitaireGame.state(), replay(), SolitaireGame.finish()
     */
    private SolitaireSolver.Result solveAndReplay(SolitaireGame game, boolean compact) {
        SolitaireSolver.Result result = solver.solve(game.state());
        replay(game, result.moves(), compact);
        game.finish(result.status());
        return result;
    }

    /**
     * Method: Applies each move through the game engine, showing every step unless compact.
     * Calls: ConsoleRenderer.nextMove(), SolitaireGame.play(), ConsoleRenderer.render()
     */
    private void replay(SolitaireGame game, List<Move> moves, boolean compact) {
        for (Move move : moves) {
            if (!compact) {
                renderer.nextMove(move);
            }
            if (!game.play(move)) {
                throw new IllegalStateException("Invalid solution path");
            }
            if (!compact) {
                renderer.render(game);
            }
        }
    }
}
