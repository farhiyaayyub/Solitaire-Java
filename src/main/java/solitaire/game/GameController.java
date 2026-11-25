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
        // requireNonNull stops with an error if any part is missing.
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
        // Play one menu round, then ask "Play again?"; repeat while the answer is yes.
        do {
            // The player typed q during the round, so leave at once.
            if (!playFromMenu()) {
                return;
            }
        } while (consoleInput.playAgain());
        renderer.showClosingMessage();
    }

    /** Lets the solver play one Draw 1 game on a new standard deck shuffled with the given settings. */
    public void automatic(ShuffleConfiguration configuration, boolean compact) {
        // Use a new standard deck and Draw 1.
        automatic(configuration, new Deck(), GameState.DRAW_ONE, compact);
    }

    /**
     * Shuffles a copy of the given deck with the chosen settings, then lets the solver play it with the chosen draw
     * mode.
     */
    public void automatic(ShuffleConfiguration configuration, Deck initialDeck, int drawCount, boolean compact) {
        // Shuffle the deck, then play it.
        playShuffled(configuration, configuration.prepareDeck(initialDeck), drawCount, compact);
    }

    /**
     * Creates a deal that is known to be winnable and replays its winning moves, so the game always ends in a win. In
     * compact mode only the final board is shown.
     */
    public void guaranteed(boolean compact) {
        // Get the winnable deal and start a game with it.
        SolvableDealProvider.VerifiedDeal deal = new SolvableDealProvider().create();
        SolitaireGame game = new SolitaireGame(deal.state());
        // In verbose mode, show the starting board.
        if (!compact) {
            renderer.render(game);
        }
        // Play the saved winning moves, then mark the game as won and show the result.
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
        // Make gameCount randomly shuffled decks and play them all, showing a line for each game.
        ShuffleConfiguration random = new ShuffleConfiguration(ShuffleType.RANDOM, false);
        simulate(random, shuffledDecks(gameCount, random), GameState.DRAW_ONE, true);
    }

    /**
     * Compares the three shuffles fairly. It creates random decks and plays them as they are, then plays the same decks
     * after an In-Faro shuffle and after an Out-Faro shuffle, and prints a report for each.
     */
    public void compare(int gameCount) {
        // Row 1: play the random decks as they are.
        ShuffleConfiguration random = new ShuffleConfiguration(ShuffleType.RANDOM, false);
        List<Deck> randomDecks = shuffledDecks(gameCount, random);
        simulate(random, randomDecks, GameState.DRAW_ONE, true);
        // Rows 2 and 3: apply In-Faro, then Out-Faro, to the same random decks and play them again.
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
        // Start a game on a randomly shuffled deck.
        Deck deck = new ShuffleConfiguration(ShuffleType.RANDOM, false).prepareDeck();
        SolitaireGame game = new SolitaireGame(GameState.deal(deck));
        MoveGenerator generator = new MoveGenerator();
        // Repeat every turn until the game is won.
        while (!game.state().won()) {
            // Show the board and the list of legal moves.
            renderer.render(game);
            GameState board = game.state();
            List<Move> moves = generator.generate(board);
            renderer.showMoves(moves, board);
            // Ask the player to choose a move; null means q was typed or no move is left, so stop.
            Move selected = consoleInput.selectMove(moves);
            if (selected == null) {
                return;
            }
            // Play the chosen move; it came from the legal list, so a rejection means something is wrong.
            if (!game.play(selected)) {
                throw new IllegalStateException("Selected move is no longer legal");
            }
        }
        // The game is won, so show the final board.
        renderer.render(game);
    }

    /**
     * Runs one round of the menu: it asks for the deck source, the shuffle, and the draw mode, then either plays one
     * automatic game or runs a simulation. Returns false as soon as the player types q.
     */
    private boolean playFromMenu() {
        // Question 1: standard deck or card file? q exits.
        String deckSource = consoleInput.deckSource();
        if (deckSource.equals("q")) {
            return false;
        }
        // If the player chose a card file, load it; null means q was typed.
        Deck fileDeck = null;
        if (deckSource.equals("2")) {
            fileDeck = consoleInput.loadDeck();
            if (fileDeck == null) {
                return false;
            }
        }
        // Question 2: which shuffle? null means q was typed.
        boolean deckFromFile = fileDeck != null;
        ShuffleConfiguration configuration = consoleInput.shuffleConfiguration(deckFromFile);
        if (configuration == null) {
            return false;
        }
        // Question 3: Draw 1 or Draw 3? 0 means q was typed.
        int drawCount = consoleInput.drawCount();
        if (drawCount == 0) {
            return false;
        }
        // A deck from a file is always played as one automatic game.
        if (deckFromFile) {
            automatic(configuration, fileDeck, drawCount, false);
            return true;
        }
        // Question 4: watch one game or test the win rate? q exits.
        String mode = consoleInput.playMode();
        if (mode.equals("q")) {
            return false;
        }
        // Option 1: watch one automatic game.
        if (mode.equals("1")) {
            automatic(configuration, new Deck(), drawCount, false);
            return true;
        }
        // Option 2: ask how many games, then run the simulation. 0 means q was typed.
        int gameCount = consoleInput.gameCount();
        if (gameCount == 0) {
            return false;
        }
        // The decks are kept, so "watch game 1" shows exactly the deal that game 1 of the simulation played.
        List<Deck> decks = shuffledDecks(gameCount, configuration);
        simulate(configuration, decks, drawCount, false);
        // If the player wants, replay game 1 move by move.
        if (consoleInput.wantsToWatchSimulationGame()) {
            renderer.heading("WATCHING SIMULATED GAME 1");
            playShuffled(configuration, decks.getFirst(), drawCount, false);
        }
        return true;
    }

    /** Creates the given number of new standard decks, each shuffled separately with the given settings. */
    private static List<Deck> shuffledDecks(int gameCount, ShuffleConfiguration configuration) {
        // At least one game must be played.
        if (gameCount < 1) {
            throw new IllegalArgumentException("Game count must be positive");
        }
        // Make one shuffled deck for each game.
        List<Deck> decks = new ArrayList<>(gameCount);
        for (int gameIndex = 0; gameIndex < gameCount; gameIndex++) {
            decks.add(configuration.prepareDeck());
        }
        return decks;
    }

    /** Deals the already shuffled deck, lets the solver play it, and prints the final board and the result. */
    private void playShuffled(ShuffleConfiguration configuration, Deck shuffledDeck, int drawCount, boolean compact) {
        // Show which shuffle and draw mode are used.
        renderer.shuffleConfiguration(configuration);
        renderer.playing(drawCount);
        // Deal the board and start the game.
        SolitaireGame game = new SolitaireGame(GameState.deal(shuffledDeck, drawCount));
        // In verbose mode, show the starting board.
        if (!compact) {
            renderer.render(game);
        }
        // Let the solver play, then show the final board and the result.
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
        // Print the report heading and start the timer.
        renderer.simulationStarted();
        renderer.shuffleConfiguration(configuration);
        renderer.playing(drawCount);
        long started = System.nanoTime();
        // Counters for the results.
        int won = 0;
        int limitedGames = 0;
        int stuckGames = 0;
        long totalMoves = 0;
        for (int gameIndex = 0; gameIndex < decks.size(); gameIndex++) {
            // Dealing takes the cards out of a deck, so a copy is dealt and the deck stays whole for later use
            // (the Faro rows of compare, and "watch game 1" in the menu).
            Deck copy = new Deck(decks.get(gameIndex).drawOrder());
            SolitaireGame game = new SolitaireGame(GameState.deal(copy, drawCount));
            // Let the solver play the game without printing each move.
            solveAndReplay(game, true);
            // Add 1 to the counter that matches how the game ended.
            switch (game.status()) {
                // The solver won the game.
                case WON -> won++;
                // The solver ran out of moves or time before winning.
                case LIMIT_REACHED -> limitedGames++;
                // Anything else means STUCK: every possible move had already been tried.
                default -> stuckGames++;
            }
            totalMoves += game.moves();
            // If wanted, print one line for this game.
            if (showEachGame) {
                renderer.simulationGame(gameIndex + 1, game.status(), game.moves());
            }
        }
        // Print the totals and the time taken.
        renderer.simulationSummary(decks.size(), won, limitedGames, stuckGames, totalMoves);
        renderer.elapsed(System.nanoTime() - started);
    }

    /**
     * Lets the solver find its moves on a copy of the board, then replays those moves on the real game so each one is
     * checked again, and saves the solver's final status.
     */
    private SolitaireSolver.Result solveAndReplay(SolitaireGame game, boolean compact) {
        // The solver works on a copy of the board and returns its moves and final status.
        SolitaireSolver.Result result = solver.solve(game.state());
        // Play the same moves on the real game, so every move is checked by the engine again.
        replay(game, result.moves(), compact);
        // Save how the game ended.
        game.finish(result.status());
        return result;
    }

    /**
     * Plays each move in the list on the game and throws an error if one is rejected. Unless compact, it prints each
     * move and the board after it.
     */
    private void replay(SolitaireGame game, List<Move> moves, boolean compact) {
        for (Move move : moves) {
            // In verbose mode, print the move before it is played.
            if (!compact) {
                renderer.nextMove(move, game.state());
            }
            // Play the move; a rejected move means the solution is wrong, so stop with an error.
            if (!game.play(move)) {
                throw new IllegalStateException("Invalid solution path");
            }
            // In verbose mode, print the board after the move.
            if (!compact) {
                renderer.render(game);
            }
        }
    }
}
