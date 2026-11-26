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
 * Runs every mode of the program: the menu, autoplay, simulations, the shuffle comparison, and manual play. It connects
 * the solver, the keyboard input, and the screen output.
 *
 * @author Farhiya Ayyub
 */
public final class GameController {
    private final SolitaireSolver solver;
    private final ConsoleInput consoleInput;
    private final ConsoleRenderer renderer;

    /** Stores the solver, the input reader, and the renderer this controller uses. None of them may be null. */
    public GameController(SolitaireSolver solver, ConsoleInput consoleInput, ConsoleRenderer renderer) {
        this.solver = Objects.requireNonNull(solver);
        this.consoleInput = Objects.requireNonNull(consoleInput);
        this.renderer = Objects.requireNonNull(renderer);
    }

    /**
     * Shows the welcome title and runs menu rounds for as long as the player wants to play again. If the player types
     * q it stops right away; otherwise it ends with a thank-you message.
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

    /** Lets the solver play one Draw 1 game on a new standard deck shuffled with the given settings. */
    public void automatic(ShuffleConfiguration configuration, boolean compact) {
        automatic(configuration, new Deck(), GameState.DRAW_ONE, compact);
    }

    /**
     * Shuffles a copy of the given deck with the chosen settings, then lets the solver play it with the chosen draw
     * mode.
     */
    public void automatic(ShuffleConfiguration configuration, Deck initialDeck, int drawCount, boolean compact) {
        playShuffled(configuration, configuration.prepareDeck(initialDeck), drawCount, compact);
    }

    /**
     * Creates a deal that is known to be winnable and replays its winning moves, so the game always ends in a win. In
     * compact mode only the final board is shown.
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
     * Plays the given number of automatic Draw 1 games, each on a new random deal, and prints one line per game and a
     * summary with the win rate.
     */
    public void simulate(int gameCount) {
        ShuffleConfiguration random = new ShuffleConfiguration(ShuffleType.RANDOM, false);
        simulate(random, shuffledDecks(gameCount, random), GameState.DRAW_ONE, true);
    }

    /**
     * Compares the three shuffles fairly. It creates random decks and plays them as they are, then plays the same decks
     * after an In-Faro shuffle and after an Out-Faro shuffle, and prints a report for each.
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
     * Lets the player play a random Draw 1 game. Each turn it shows the board and the numbered legal moves, reads the
     * player's choice, and plays it, until the game is won, no move is left, or the player types q.
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
            game.play(selected);
        }
        renderer.render(game);
    }

    /**
     * Runs one round of the menu: it asks for the deck source, the shuffle, and the draw mode, then either plays one
     * automatic game or runs a simulation. Returns false as soon as the player types q.
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
        List<Deck> decks = shuffledDecks(gameCount, configuration);
        simulate(configuration, decks, drawCount, false);
        if (consoleInput.wantsToWatchSimulationGame()) {
            renderer.heading("WATCHING SIMULATED GAME 1");
            playShuffled(configuration, decks.getFirst(), drawCount, false);
        }
        return true;
    }

    /** Creates the given number of new standard decks, each shuffled separately with the given settings. */
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

    /** Deals the already shuffled deck, lets the solver play it, and prints the final board and the result. */
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
     * Plays one automatic game for each deck and counts how many were won, stopped at the limit, or got stuck. Then it
     * prints the totals, the win rate, the average number of moves, and the time taken.
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
     * Lets the solver find its moves on a copy of the board, then replays those moves on the real game so each one is
     * checked again, and saves the solver's final status.
     */
    private SolitaireSolver.Result solveAndReplay(SolitaireGame game, boolean compact) {
        SolitaireSolver.Result result = solver.solve(game.state());
        replay(game, result.moves(), compact);
        game.finish(result.status());
        return result;
    }

    /**
     * Plays each move in the list on the game and throws an error if one is rejected. Unless compact, it prints each
     * move and the board after it.
     */
    private void replay(SolitaireGame game, List<Move> moves, boolean compact) {
        for (Move move : moves) {
            if (!compact) {
                renderer.nextMove(move, game.state());
            }
            game.play(move);
            if (!compact) {
                renderer.render(game);
            }
        }
    }
}
