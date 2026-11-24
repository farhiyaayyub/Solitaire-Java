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
 * Runs games from start to finish. It connects the input, the game engine, the solver, and the display: it sets up
 * a deal, lets the solver or the player choose moves, applies them through SolitaireGame, and prints the result.
 * It contains no game rules and no printing code of its own.
 *
 * <p>Abstraction: it depends on the SolitaireSolver interface, not on VisibleSolver, so another solver can be used
 * without changing this class. Composition: it is built from a renderer, an input reader, and a solver.
 *
 * @author Farhiya Ayyub
 */
public final class GameController {
    private final ConsoleRenderer renderer = new ConsoleRenderer();
    // Typed as the SolitaireSolver interface, so any solver passed to the constructor can be used.
    private final SolitaireSolver solver;
    private final ConsoleInput consoleInput;

    /**
     * Stores the solver and input that this controller will use (dependency injection: they are created outside and
     * passed in).
     *
     * @param solver the automatic player
     * @param consoleInput reads the player's answers
     * @throws NullPointerException if solver or consoleInput is {@code null}
     */
    public GameController(SolitaireSolver solver, ConsoleInput consoleInput) {
        this.solver = Objects.requireNonNull(solver);
        this.consoleInput = Objects.requireNonNull(consoleInput);
    }

    /**
     * Shows the title, then repeats one menu round while the player answers yes to "play again". Prints the closing
     * message only after a normal finish; returns at once, without it, if the player enters q.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link ConsoleRenderer#heading} - prints the game title</li>
     *   <li>{@code playFromMenu()} - runs one round of the menu</li>
     *   <li>{@link ConsoleInput#playAgain} - asks whether to play another round</li>
     *   <li>{@link ConsoleRenderer#showClosingMessage} - prints the thank-you message</li>
     * </ul>
     *
     * <p>Called by: {@code SolitaireApplication.run()}.
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
     * Plays one automatic Draw 1 game from a new standard deck. With compact {@code true}, only the final board and
     * result are printed; otherwise every move and board is printed.
     *
     * <p>Called by: {@code SolitaireApplication.run()}.
     *
     * @param configuration the shuffle settings
     * @param compact {@code true} to print only the final board and result
     */
    public void automatic(ShuffleConfiguration configuration, boolean compact) {
        automatic(configuration, new Deck(), GameState.DRAW_ONE, compact);
    }

    /**
     * Plays one automatic game: shuffles a copy of the deck, deals it, lets the solver choose all moves, replays them
     * through the game engine, then prints the final board and the result line.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link ConsoleRenderer#shuffleConfiguration} - prints the chosen shuffle</li>
     *   <li>{@link ConsoleRenderer#playing} - prints the draw mode</li>
     *   <li>{@link ShuffleConfiguration#prepareDeck} - shuffles a copy of the deck</li>
     *   <li>{@link GameState#deal} - lays the cards out on a new board</li>
     *   <li>{@link ConsoleRenderer#render} - prints the board</li>
     *   <li>{@code solveAndReplay()} - lets the solver play and applies its moves</li>
     *   <li>{@link ConsoleRenderer#resultDetail} - prints the Result line</li>
     * </ul>
     *
     * <p>Called by: {@link #automatic}, {@code playFromMenu()}.
     *
     * @param configuration the shuffle settings
     * @param initialDeck the deck to start from; it is not changed
     * @param drawCount cards turned per draw: 1 or 3
     * @param compact {@code true} to print only the final board and result
     */
    public void automatic(ShuffleConfiguration configuration, Deck initialDeck, int drawCount, boolean compact) {
        renderer.shuffleConfiguration(configuration);
        renderer.playing(drawCount);
        SolitaireGame game = new SolitaireGame(GameState.deal(configuration.prepareDeck(initialDeck), drawCount));
        if (!compact) {
            renderer.render(game);
        }
        SolitaireSolver.Result result = solveAndReplay(game, compact);
        renderer.render(game);
        renderer.resultDetail(result.detail());
    }

    /**
     * Plays a demonstration deal that is known to be winnable, using its already-checked winning moves instead of the
     * solver, so it always ends in a win.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link SolvableDealProvider#create} - gets a winnable deal and its moves</li>
     *   <li>{@link SolvableDealProvider.VerifiedDeal#state()} - gets a copy of the winnable board</li>
     *   <li>{@link ConsoleRenderer#render} - prints the board</li>
     *   <li>{@code replay()} - plays the winning moves</li>
     *   <li>{@link SolitaireGame#finish} - records the game as WON</li>
     *   <li>{@link ConsoleRenderer#resultDetail} - prints the Result line</li>
     * </ul>
     *
     * <p>Called by: {@code SolitaireApplication.run()}.
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
     * Plays gameCount Draw 1 games with a random shuffle, using seeds firstSeed, firstSeed + 1, and so on, and prints
     * one line per game plus a summary.
     *
     * <p>Called by: {@code SolitaireApplication.run()}.
     *
     * @param gameCount how many games to play
     * @param firstSeed the seed of the first game; game n uses firstSeed + n
     */
    public void simulate(int gameCount, long firstSeed) {
        simulateBatch(gameCount, firstSeed, ShuffleType.RANDOM);
    }

    /**
     * Runs the same simulation three times, with Random, In-Faro, and Out-Faro shuffles, using the same seeds, so the
     * three win rates can be compared fairly.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@code simulateBatch()} - runs one batch per shuffle type</li>
     * </ul>
     *
     * <p>Called by: {@code SolitaireApplication.run()}.
     *
     * @param gameCount how many games to play
     * @param firstSeed the seed of the first game; game n uses firstSeed + n
     */
    public void compare(int gameCount, long firstSeed) {
        simulateBatch(gameCount, firstSeed, ShuffleType.RANDOM);
        simulateBatch(gameCount, firstSeed, ShuffleType.IN);
        simulateBatch(gameCount, firstSeed, ShuffleType.OUT);
    }

    /**
     * Lets the player choose every move on a random deal made from the seed. Ends when the board is won, when no legal
     * move is left, or when the player enters q.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link ShuffleConfiguration#prepareDeck} - shuffles a deck from the seed</li>
     *   <li>{@link GameState#deal} - lays the cards out on a new board</li>
     *   <li>{@link SolitaireGame#state()} - gets the board for the move list</li>
     *   <li>{@link GameState#won} - stops the loop when the game is won</li>
     *   <li>{@link ConsoleRenderer#render} - prints the board before each choice</li>
     *   <li>{@link MoveGenerator#generate} - lists the moves the player may choose</li>
     *   <li>{@link ConsoleInput#selectMove} - asks the player to pick a move</li>
     *   <li>{@link SolitaireGame#play} - plays the chosen move</li>
     * </ul>
     *
     * <p>Called by: {@code SolitaireApplication.run()}.
     *
     * @param seed the seed for the random deal
     * @throws IllegalStateException if a chosen move is rejected by the engine
     */
    public void manual(long seed) {
        Deck deck = new ShuffleConfiguration(ShuffleType.RANDOM, false, seed).prepareDeck();
        SolitaireGame game = new SolitaireGame(GameState.deal(deck));
        MoveGenerator generator = new MoveGenerator();
        while (!game.state().won()) {
            renderer.render(game);
            GameState board = game.state();
            Move selected = consoleInput.selectMove(generator.generate(board), board, renderer);
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
     * Runs one round of the menu: deck source, shuffle, draw mode, then auto-play or simulation (a deck from a file
     * always uses auto-play).
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link ConsoleInput#deckSource} - asks: standard deck or card file</li>
     *   <li>{@link ConsoleInput#loadDeck} - loads the card file</li>
     *   <li>{@link ConsoleInput#shuffleConfiguration} - asks which shuffle to use</li>
     *   <li>{@link ConsoleInput#drawCount} - asks Draw 1 or Draw 3</li>
     *   <li>{@link #automatic} - plays one game move by move</li>
     *   <li>{@link ConsoleInput#playMode} - asks: watch one game or test win rate</li>
     *   <li>{@link ConsoleInput#gameCount} - asks how many games to simulate</li>
     *   <li>{@code simulate()} - plays many games for the win rate</li>
     *   <li>{@link ConsoleInput#wantsToWatchSimulationGame} - asks whether to watch game 1</li>
     *   <li>{@link ConsoleRenderer#heading} - prints the title before game 1</li>
     * </ul>
     *
     * <p>Called by: {@link #shuffleAndPlay}.
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
        simulate(gameCount, configuration, drawCount, false);
        if (consoleInput.wantsToWatchSimulationGame()) {
            renderer.heading("WATCHING SIMULATED GAME 1");
            automatic(configuration, new Deck(), drawCount, false);
        }
        return true;
    }

    /**
     * Runs a compact Draw 1 simulation for one shuffle type. Faro shuffles randomize the deck first, because a perfect
     * shuffle of the same sorted deck would give the same deal every game.
     *
     * <p>Called by: {@link #simulate(int, long)}, {@link #compare}.
     *
     * @param gameCount how many games to play
     * @param firstSeed the seed of the first game; game n uses firstSeed + n
     * @param type the shuffle to test
     */
    private void simulateBatch(int gameCount, long firstSeed, ShuffleType type) {
        simulate(gameCount, new ShuffleConfiguration(type, type.isFaro(), firstSeed), GameState.DRAW_ONE, true);
    }

    /**
     * Plays gameCount automatic games and prints the totals, win rate, and time taken. Prints a line per game only if
     * showEachGame is {@code true}.
     *
     * <p>Algorithm: repeated trials.
     * <ol>
     *   <li>For game n = 0 to gameCount - 1: shuffle with seed + n, deal, and let the solver play.</li>
     *   <li>Add 1 to won, limitedGames, or stuckGames according to the final status; add up the moves.</li>
     *   <li>Win rate = 100.0 * won / gameCount (calculated in ConsoleRenderer.simulationSummary()).</li>
     * </ol>
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link ConsoleRenderer#simulationStarted} - prints the report title</li>
     *   <li>{@link ConsoleRenderer#shuffleConfiguration} - prints the chosen shuffle</li>
     *   <li>{@link ConsoleRenderer#playing} - prints the draw mode</li>
     *   <li>{@link ShuffleConfiguration#withSeedOffset} - gives each game its own seed</li>
     *   <li>{@link ShuffleConfiguration#prepareDeck} - shuffles the deck for one game</li>
     *   <li>{@link GameState#deal} - lays the cards out on a new board</li>
     *   <li>{@code solveAndReplay()} - lets the solver play one game</li>
     *   <li>{@link SolitaireGame#status()} - counts the game as won, limited, or stuck</li>
     *   <li>{@link SolitaireGame#moves()} - adds up the moves for the average</li>
     *   <li>{@link ConsoleRenderer#simulationGame} - prints one line per game</li>
     *   <li>{@link ConsoleRenderer#simulationSummary} - prints the totals and win rate</li>
     *   <li>{@link ConsoleRenderer#elapsed} - prints how long it took</li>
     * </ul>
     *
     * <p>Called by: {@code playFromMenu()}, {@code simulateBatch()}.
     *
     * @param gameCount how many games to play
     * @param configuration the shuffle settings
     * @param drawCount cards turned per draw: 1 or 3
     * @param showEachGame {@code true} to print one line per game
     * @throws IllegalArgumentException if gameCount is less than 1
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
        for (int gameIndex = 0; gameIndex < gameCount; gameIndex++) {
            Deck deck = configuration.withSeedOffset(gameIndex).prepareDeck();
            SolitaireGame game = new SolitaireGame(GameState.deal(deck, drawCount));
            SolitaireSolver.Result result = solveAndReplay(game, true);
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
        renderer.simulationSummary(gameCount, won, limitedGames, stuckGames, totalMoves);
        renderer.elapsed(System.nanoTime() - started);
    }

    /**
     * Gives the solver a copy of the board, replays the moves it returns on the real game so each is checked again by
     * play(), then records the solver's final status on the game and returns the solver's result.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link SolitaireGame#state()} - gives the solver a copy of the board</li>
     *   <li>{@link SolitaireSolver#solve} - asks the solver for its moves</li>
     *   <li>{@code replay()} - applies the moves to the real game</li>
     *   <li>{@link SolitaireGame#finish} - records how the game ended</li>
     * </ul>
     *
     * <p>Called by: {@link #automatic}, {@code simulate()}.
     *
     * @param game the game to play; its board is given to the solver as a copy
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
     * Plays each move through SolitaireGame.play(). Unless compact, prints each move and the board after it.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@link SolitaireGame#state()} - the board before the move, to name the card</li>
     *   <li>{@link ConsoleRenderer#nextMove} - prints the move before it is played</li>
     *   <li>{@link SolitaireGame#play} - checks and plays the move</li>
     *   <li>{@link ConsoleRenderer#render} - prints the board after the move</li>
     * </ul>
     *
     * <p>Called by: {@link #guaranteed}, {@code solveAndReplay()}.
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
