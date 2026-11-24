package solitaire.game;

import java.util.ArrayList;
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
 * Game runner that ties input, engine, solver, and display together.
 *
 * <p>Kind: final class, uses SolitaireSolver, ConsoleInput, ConsoleRenderer, and SolitaireGame.
 * <p>Purpose: it runs every program mode (menu, autoplay, manual, simulation, compare) from deal to result. It holds
 * no game rules and no printing code of its own.
 * <p>Data: three final fields (solver, consoleInput, renderer) set once in the constructor; no collections are kept
 * between games.
 * <p>OOP: Abstraction - it depends on the SolitaireSolver interface, not on VisibleSolver. Composition - it is built
 * from a solver, an input reader, and a renderer that are passed in (dependency injection).
 *
 * @author Farhiya Ayyub
 */
public final class GameController {
    private final SolitaireSolver solver;
    private final ConsoleInput consoleInput;
    private final ConsoleRenderer renderer;

    /**
     * Stores the parts this controller works with.
     *
     * <p>Called by: SolitaireApplication.getController().
     *
     * @param solver the automatic player
     * @param consoleInput reads the player's answers
     * @param renderer prints the boards, moves, and results
     * @throws NullPointerException if any argument is {@code null}
     */
    public GameController(SolitaireSolver solver, ConsoleInput consoleInput, ConsoleRenderer renderer) {
        this.solver = Objects.requireNonNull(solver);
        this.consoleInput = Objects.requireNonNull(consoleInput);
        this.renderer = Objects.requireNonNull(renderer);
    }

    /**
     * Runs menu rounds while the player answers yes to "play again". The closing message is printed only after a
     * normal finish; entering q returns at once without it.
     *
     * <p>Called by: SolitaireApplication.run().
     */
    public void shuffleAndPlay() {
        renderer.heading("WELCOME TO KLONDIKE SOLITAIRE");
        do {
            if (!playFromMenu()) {
                return;
            }
        } while (consoleInput.playAgain());
        renderer.showClosingMessage();
    }

    /**
     * Plays one automatic Draw 1 game from a new standard deck.
     *
     * <p>Called by: SolitaireApplication.run().
     *
     * @param configuration the shuffle settings
     * @param compact {@code true} to print only the final board and result
     */
    public void automatic(ShuffleConfiguration configuration, boolean compact) {
        automatic(configuration, new Deck(), GameState.DRAW_ONE, compact);
    }

    /**
     * Shuffles a copy of the deck with the chosen settings, then lets the solver play it.
     *
     * <p>Called by: automatic(ShuffleConfiguration, boolean), playFromMenu().
     *
     * @param configuration the shuffle settings
     * @param initialDeck the deck to start from; it is not changed
     * @param drawCount cards turned per draw: 1 or 3
     * @param compact {@code true} to print only the final board and result
     */
    public void automatic(ShuffleConfiguration configuration, Deck initialDeck, int drawCount, boolean compact) {
        playShuffled(configuration, configuration.prepareDeck(initialDeck), drawCount, compact);
    }

    /**
     * Plays a deal that is known to be winnable, replaying its already-checked winning moves instead of asking the
     * solver, so it always ends in a win.
     *
     * <p>Called by: SolitaireApplication.run().
     *
     * @param compact {@code true} to print only the final board; {@code false} to print every move
     */
    public void guaranteed(boolean compact) {
        SolvableDealProvider.VerifiedDeal deal = new SolvableDealProvider().create();
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
     * Plays gameCount Draw 1 games, each on a new random deal, and prints one line per game plus a summary.
     *
     * <p>Called by: SolitaireApplication.run().
     *
     * @param gameCount how many games to play
     * @throws IllegalArgumentException if gameCount is less than 1
     */
    public void simulate(int gameCount) {
        ShuffleConfiguration random = new ShuffleConfiguration(ShuffleType.RANDOM, false);
        simulate(random, shuffledDecks(gameCount, random), GameState.DRAW_ONE, true);
    }

    /**
     * Plays gameCount Draw 1 games with each of the Random, In-Faro, and Out-Faro shuffles and prints one report per
     * shuffle.
     *
     * <p>Key idea: a fair comparison. The Random row plays new random decks as they are; each Faro row applies its
     * perfect shuffle to those same decks first, so only the shuffle method differs between the rows.
     *
     * <p>Called by: SolitaireApplication.run().
     *
     * @param gameCount how many games each shuffle plays
     * @throws IllegalArgumentException if gameCount is less than 1
     */
    public void compare(int gameCount) {
        ShuffleConfiguration random = new ShuffleConfiguration(ShuffleType.RANDOM, false);
        List<Deck> randomDecks = shuffledDecks(gameCount, random);
        simulate(random, randomDecks, GameState.DRAW_ONE, true);
        for (ShuffleType faro : List.of(ShuffleType.IN, ShuffleType.OUT)) {
            ShuffleConfiguration configuration = new ShuffleConfiguration(faro, false);
            List<Deck> faroDecks = new ArrayList<>(gameCount);
            for (Deck randomDeck : randomDecks) {
                faroDecks.add(configuration.prepareDeck(randomDeck));
            }
            simulate(configuration, faroDecks, GameState.DRAW_ONE, true);
        }
    }

    /**
     * Lets the player choose every move on a new random Draw 1 deal. Ends when the board is won, when no legal move
     * is left, or when the player enters q.
     *
     * <p>Called by: SolitaireApplication.run().
     *
     * @throws IllegalStateException if a chosen move is rejected by the engine
     */
    public void manual() {
        Deck deck = new ShuffleConfiguration(ShuffleType.RANDOM, false).prepareDeck();
        SolitaireGame game = new SolitaireGame(GameState.deal(deck));
        MoveGenerator generator = new MoveGenerator();
        while (!game.state().won()) {
            renderer.render(game);
            GameState board = game.state();
            List<Move> moves = generator.generate(board);
            renderer.showMoves(moves, board);
            Move selected = consoleInput.selectMove(moves);
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
     * Runs one menu round: deck source, shuffle, draw mode, then autoplay or simulation. A deck from a file always
     * uses autoplay; after a simulation the player may watch game 1.
     *
     * <p>Called by: shuffleAndPlay().
     *
     * @return {@code false} as soon as the player enters q at any question, {@code true} otherwise
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
        // The decks are kept, so "watch game 1" shows exactly the deal that game 1 of the simulation played.
        List<Deck> decks = shuffledDecks(gameCount, configuration);
        simulate(configuration, decks, drawCount, false);
        if (consoleInput.wantsToWatchSimulationGame()) {
            renderer.heading("WATCHING SIMULATED GAME 1");
            playShuffled(configuration, decks.getFirst(), drawCount, false);
        }
        return true;
    }

    /**
     * {@return gameCount new standard decks, each shuffled separately} With a random shuffle, every game is a
     * different deal.
     *
     * <p>Called by: simulate(int), compare(), playFromMenu().
     *
     * @param gameCount how many decks to make
     * @param configuration the shuffle settings
     * @throws IllegalArgumentException if gameCount is less than 1
     */
    private static List<Deck> shuffledDecks(int gameCount, ShuffleConfiguration configuration) {
        if (gameCount < 1) {
            throw new IllegalArgumentException("Game count must be positive");
        }
        List<Deck> decks = new ArrayList<>(gameCount);
        for (int gameIndex = 0; gameIndex < gameCount; gameIndex++) {
            decks.add(configuration.prepareDeck());
        }
        return decks;
    }

    /**
     * Deals an already shuffled deck, lets the solver play it, and prints the final board and the result line.
     *
     * <p>Called by: automatic(ShuffleConfiguration, Deck, int, boolean), playFromMenu().
     *
     * @param configuration the shuffle settings, printed above the game
     * @param shuffledDeck the deck to deal, already shuffled
     * @param drawCount cards turned per draw: 1 or 3
     * @param compact {@code true} to print only the final board and result
     */
    private void playShuffled(ShuffleConfiguration configuration, Deck shuffledDeck, int drawCount, boolean compact) {
        renderer.shuffleConfiguration(configuration);
        renderer.playing(drawCount);
        SolitaireGame game = new SolitaireGame(GameState.deal(shuffledDeck, drawCount));
        if (!compact) {
            renderer.render(game);
        }
        SolitaireSolver.Result result = solveAndReplay(game, compact);
        renderer.render(game);
        renderer.resultDetail(result.detail());
    }

    /**
     * Plays one automatic game per deck and prints the totals, win rate, and time taken; a line per game is printed
     * only if showEachGame is {@code true}.
     *
     * <p>Algorithm: repeated trials.
     * <ol>
     *   <li>For each deck: deal a copy and let the solver play.</li>
     *   <li>Count the game as won, limited, or stuck by its final status; add up the moves.</li>
     *   <li>Pass the totals to ConsoleRenderer.simulationSummary(), which prints the win rate as
     *   100.0 * won / games.</li>
     * </ol>
     *
     * <p>Called by: simulate(int), compare(), playFromMenu().
     *
     * @param configuration the shuffle settings, printed in the report
     * @param decks the decks to play, already shuffled; one game per deck
     * @param drawCount cards turned per draw: 1 or 3
     * @param showEachGame {@code true} to print one line per game
     */
    private void simulate(ShuffleConfiguration configuration, List<Deck> decks, int drawCount,
                          boolean showEachGame) {
        renderer.simulationStarted();
        renderer.shuffleConfiguration(configuration);
        renderer.playing(drawCount);
        long started = System.nanoTime();
        int won = 0;
        int limitedGames = 0;
        int stuckGames = 0;
        long totalMoves = 0;
        for (int gameIndex = 0; gameIndex < decks.size(); gameIndex++) {
            // Dealing takes the cards out of a deck, so a copy is dealt and the deck stays whole for later use
            // (the Faro rows of compare, and "watch game 1" in the menu).
            Deck copy = new Deck(decks.get(gameIndex).drawOrder());
            SolitaireGame game = new SolitaireGame(GameState.deal(copy, drawCount));
            solveAndReplay(game, true);
            switch (game.status()) {
                case WON -> won++;
                case LIMIT_REACHED -> limitedGames++;
                default -> stuckGames++;
            }
            totalMoves += game.moves();
            if (showEachGame) {
                renderer.simulationGame(gameIndex + 1, game.status(), game.moves());
            }
        }
        renderer.simulationSummary(decks.size(), won, limitedGames, stuckGames, totalMoves);
        renderer.elapsed(System.nanoTime() - started);
    }

    /**
     * Gives the solver a copy of the board, then replays its moves on the real game so play() checks each one again,
     * and records the solver's final status.
     *
     * <p>Called by: playShuffled(), simulate().
     *
     * @param game the game to play
     * @param compact {@code true} to print only the final board; {@code false} to print every move
     * @return the solver's result: final status and moves
     */
    private SolitaireSolver.Result solveAndReplay(SolitaireGame game, boolean compact) {
        SolitaireSolver.Result result = solver.solve(game.state());
        replay(game, result.moves(), compact);
        game.finish(result.status());
        return result;
    }

    /**
     * Plays each move through SolitaireGame.play(); unless compact, prints each move and the board after it.
     *
     * <p>Called by: guaranteed(), solveAndReplay().
     *
     * @param game the game to play the moves on
     * @param moves the moves to play, in order
     * @param compact {@code true} to print only the final board; {@code false} to print every move
     * @throws IllegalStateException if the engine rejects a move
     */
    private void replay(SolitaireGame game, List<Move> moves, boolean compact) {
        for (Move move : moves) {
            if (!compact) {
                renderer.nextMove(move, game.state());
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
