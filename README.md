# Java Console Klondike Solitaire

## File input and helpers

You can enter just `input.txt`: if a bare filename is not in the working directory, the loader checks `resources/`. Explicit relative and absolute paths are also accepted. Enter a file name, not the `resources` folder itself.

Run `./run.ps1` and enter `resources/input.txt` at the card-file prompt, then choose the shuffle and Draw 1/3. Relative paths are resolved from the working directory; absolute paths also work. Enter `q` to cancel. Other CLI commands continue to use generated or constructed decks.

Files contain exactly 52 distinct whitespace-separated cards, such as `H-A D-2 C-10 S-K`. Suits are H, D, C, S; ranks are A, 2 through 10, J, Q, K. Letter case is ignored. The first token is the first card in the initial draw order, before the chosen shuffle. Invalid files report a reason and prompt again.

Sample files: `resources/input.txt` (valid), `resources/duplicate.txt` (duplicate), `resources/invalid.txt` (invalid card), and `resources/incomplete.txt` (wrong count).

Console input, output, and file validation live in `solitaire.io`: `ConsoleInput`, `ConsoleRenderer`, and `CardFileLoader`. Game rules remain in their domain packages. The loader closes its reader, throws IOException for read failures and IllegalArgumentException for invalid card data; the console handles these separately. Existing historical reports refer to earlier layouts.

An object-oriented Klondike Solitaire project with selectable Faro/random shuffles and automatic visible-information play. Requires Java 21 or newer. The project uses Maven and has no runtime dependencies or preview features.

See [the visible solver planning document](docs/klondike-planning.md) for the algorithm work plan and completion criteria.
See [the Solitaire design document](docs/solitaire-design.md) for class responsibilities, ownership, and game flow.

## Run

Build and run with Maven:

```powershell
mvn package
java -jar target/klondike-solitaire-1.0.0.jar
```

Pass command-line arguments after the jar name, such as `java -jar target/klondike-solitaire-1.0.0.jar random 42 --compact`.

The optional `run.ps1` script runs the application directly with `javac` on Windows.

Or compile and run directly:

```powershell
$sources = @(Get-ChildItem src/main/java -Recurse -Filter *.java | ForEach-Object FullName)
javac -encoding UTF-8 -Xlint:all -d out/app-classes $sources
java -cp out/app-classes solitaire.Main
```

Interactive setup asks for only two choices: shuffle method ([1] Random, [2] In-Faro, [3] Out-Faro), then Draw 1 or Draw 3. Enter q to exit; invalid choices retry and end-of-input exits safely. Each selection applies one shuffle operation; Faro uses a randomized initial deck automatically. Seeds are generated internally. The board displays the selected draw mode. CLI commands retain Draw 1 by default.

After configuration, the application prepares the deck, displays the initial board, computes a committed automatic path, then displays each move and resulting board. No move selection is required. Normal output uses `[XX]` for hidden cards, `[H-A]`, `[S-10]`, `[D-2]`, etc. for face-up cards, and a blank bracketed slot for an empty pile.

```powershell
./run.ps1 random 42 --compact
./run.ps1 simulate 100 0 15000 3000 3000
./run.ps1 compare 100 0 15000 3000 3000
./run.ps1 guaranteed --compact
./run.ps1 help
```

- `shuffle` and `menu` open the automatic shuffle setup.
- `random [seed [--compact|--verbose [states [moves [ms]]]]]` plays a seeded random deal. The CLI seed defaults to 0.
- `simulate [count [firstSeed [states [moves [ms]]]]]` measures the random baseline.
- `compare` accepts the same arguments and runs baseline, one In-Faro, and one Out-Faro on matching initial seeds.
- Defaults are 100 simulation games, first seed 0, 15,000 decision states, 3,000 committed moves, and 10,000 milliseconds per game.
- `guaranteed` preserves the constructed, verified-solution demonstration. It is explicitly excluded from random benchmarks.
- `manual` preserves the earlier optional manual mode for demonstrations. It is not part of the default automatic flow.
- Exit codes: 0 for normal completion (including an unsuccessful bounded game), 2 for invalid arguments, 1 for a detected game invariant failure.

`SolitaireSolver.Limits` groups the decision, move, and time limits used by the automatic solver.

## Deck order and shuffle semantics

All shuffle strategies receive cards in **top-first draw order**. The existing ordered deck is preserved: Spades K through A, Clubs K through A, Diamonds K through A, then Hearts K through A. Internally Deck stores the reverse so removing its last ArrayList element draws the top in constant time.

- **In-Faro:** split into 26 + 26 and interleave second-half top, first-half top, second-half next, first-half next, etc.
- **Out-Faro:** interleave first-half top, second-half top, etc. Original top and bottom remain in place.
- **Interactive Faro:** choose In-Faro or Out-Faro directly. One operation is applied after initial randomization; there are no repetition or initial-deck prompts.
- **Random:** uses `Collections.shuffle` with `new Random(seed)` on top-first order. Random mode applies one random operation to the ordered deck, not an additional hidden shuffle.

For Faro operations, an ordered start applies the chosen permutation directly. A randomized start applies one seeded random shuffle before the Faro operations. Comparisons default to randomized starts. The random baseline uses that same initial seeded deck without additional Faro operations. Deterministic Faro shuffles do not independently generate random distributions.

The same seed, starting order, and configuration reproduce the exact deal. The older `Deck.shuffle(long)` now delegates to the common top-first random strategy; seeded layouts from versions that shuffled reverse storage directly may differ.

## Initialization and rules

1. Read and validate the shuffle method and Draw 1/3 selection in ConsoleInput.
2. Generate the standard ordered deck.
3. Optionally apply the seeded initial randomization.
4. Apply the selected ShuffleType strategy.
5. Verify 52 distinct card identities.
6. Deal tableau columns completely, left to right, with sizes 1 through 7, preserving the project's existing column-by-column dealing convention.
7. Place the remaining 24 cards into stock in draw order; waste and four foundations start empty.
8. Reveal only each tableau column's top card.
9. Display the board and run automatic play. GameState.deal never shuffles.

Tableau transfers require descending ranks and alternating colors. Only a King or a valid sequence beginning with a King can enter an empty column. All transferred tableau cards must be face-up. Foundations accept face-up cards of the matching suit in ascending order from Ace. Only the top waste card can move. Removing a tableau suffix reveals its newly exposed top. Foundation-to-tableau moves remain supported.

Stock draws one or three cards per action according to the selected mode, or all remaining cards when fewer remain. Only the top waste card can be played. The draw count is preserved in board copies and solver observations. Unlimited redeals restore the original remaining draw order and conceal the stock. MoveValidator checks moves before mutation; SolitaireGame.play verifies conservation after every accepted move. All piles together must hold exactly 52 distinct cards. A win requires all 52 cards in foundations and empty stock, waste, and tableau. An unfinished board cannot be marked won.

## Automatic algorithm and hidden information

`VisibleSolver` implements `SolitaireSolver`. It creates a working game and repeatedly obtains legal proposals from MoveGenerator. Its decision method `choose` receives only an immutable `ObservableBoard`, legal move descriptions, and previously tried actions. ObservableBoard includes draw mode, stock count, revealed waste cards, foundation heights, and tableau cards with hidden identities replaced by rank 0 and no suit. No hidden stock identity reaches the policy.

The policy prioritizes safe foundation moves, revealing hidden tableau cards, using waste cards, and useful tableau transfers. Foundation safety considers opposite-color and same-color foundation progress. Unsafe foundation moves rank below productive transfers; foundation reversals and whole-column King relocation rank low. Equal scores use deterministic move-generation order.

Each decision is committed legally before a newly exposed card can be inspected. There are no speculative draws, undo operations, or abandoned branches from which the policy can retain hidden knowledge. Legal proposals are generated using exposed cards and pile sizes; changing unrevealed identities cannot influence their ordering or the policy's first choice.

A map from observable positions to attempted moves prevents repeating the same action in the same observed position. On revisits, another untried legal action may be chosen. Decision count, move count, and elapsed-time limits also bound execution. `STUCK` means this policy has no untried action in the observed position; it is **not proof of unsolvability**. `SEARCH_LIMIT_REACHED` means a configured budget ended play. Explored states in these reports counts committed decision evaluations, including a final no-action evaluation where applicable.

Only the visible-information solver is retained. The unused diagnostic SearchSolver and its HeuristicEvaluator were removed to keep the project focused. Full imperfect-information backtracking is not implemented.

## Packages and class responsibilities

```text
src/main/java/solitaire/
  Main.java, SolitaireApplication.java
  enums/ Rank.java, Suit.java, GameStatus.java, MoveType.java, ShuffleType.java, ConsoleColor.java
  model/ Card.java, Deck.java
  shuffle/ ShuffleConfiguration.java
  pile/ StockPile.java, WastePile.java, TableauPile.java, FoundationPile.java
  game/ GameState.java,
        SolitaireGame.java, GameController.java
  move/ Move.java, MoveValidator.java, MoveGenerator.java
  solver/ SolitaireSolver.java, VisibleSolver.java, ObservableBoard.java,
          SolvableDealProvider.java
  io/ CardFileLoader.java, ConsoleInput.java, ConsoleRenderer.java
src/test/java/solitaire/
  SolitaireSmokeTest.java, ShuffleAndVisibleTest.java
```

Main only delegates and returns the process exit code. SolitaireApplication owns the Scanner in try-with-resources, dispatches commands, and reports argument/game failures. ConsoleInput (renamed from ConsoleMenu) owns interactive prompts and input validation. ConsoleRenderer handles output without separate formatting-helper classes. GameController coordinates preparation, solving, replay, and statistics.

ShuffleType provides meaningful strategy polymorphism independent of Solitaire rules. ShuffleConfiguration validates settings and explicitly prepares a deck; constructors do not shuffle, deal, print, or start gameplay. Rank keeps explicit values and symbols; Suit keeps its code, symbol, and color. Deck.CARD_COUNT, GameState.TABLEAU_COLUMNS, and Suit.FOUNDATION_COUNT name fixed rules.

Stock and waste use ArrayDeque for top/bottom behavior; tableau and foundations use ordered ArrayLists for sequence and rank access. Deck uses an ArrayList with end removal. GameState uses an EnumMap for foundations. The visible solver uses a map of observations to attempted actions. GameState.key returns a String for snapshot comparisons in tests; no extra wrapper class is needed.

Deck.drawOrder and pile cards/top methods return detached card snapshots. Collections are unmodifiable. SolitaireGame copies constructor input and returns a deep board snapshot, so callers cannot mutate the live game through getters. GameState itself is intentionally mutable for initialization and isolated solver work. VerifiedDeal copies its board and solution; solver Result copies its move list. Pile mutation methods are behavior operations, not collection setters.

There is no artificial inheritance hierarchy. ShuffleType owns shuffle behavior; SolitaireSolver supplies the solver interface. Public types have concise Javadoc with author tags, and public API contracts are documented where behavior needs explanation. Card equality uses suit and rank independently of visibility; duplicate checks use sets. ConsoleColor owns ANSI foreground styling.

## Tests

```powershell
$tests = @(Get-ChildItem src/test/java -Recurse -Filter *.java | ForEach-Object FullName)
javac -encoding UTF-8 -Xlint:all -cp out/app-classes -d out/test-classes $tests
java -cp "out/app-classes;out/test-classes" solitaire.SolitaireSmokeTest
java -cp "out/app-classes;out/test-classes" solitaire.ShuffleAndVisibleTest
```

On 2026-11-13, Microsoft OpenJDK 25 compilation with `-Xlint:all` succeeded. The suites passed 211 core checks and 13,240 shuffle/visible-information checks. These include exact interleaving, 8 Out-Faro and 52 In-Faro permutation cycles, reproducible seeds, no duplicate/lost cards, no second shuffle, initial pile counts, snapshot isolation, hidden-card permutation invariance, legal replay, recycle order, win detection, bounded cycles, invalid input, and EOF handling. These were historical results before simplification; current tests replay and solve the verified deal with the production VisibleSolver.

Earlier end-to-end checks exercised the shuffle menu choices, invalid command handling, help, and the guaranteed demonstration. See `docs/reports/verification-2026-11-13.txt`.

## Measured win rates

Command: `compare 100 0 15000 3000 3000`, Microsoft OpenJDK 25, 768 MB maximum heap. Each method used starting seeds 0 through 99; limits were 15,000 decisions, 3,000 moves, and 3,000 ms per game. No deals were replaced after failures. All results were replayed through the game engine. This historical run also included IN,OUT. Custom shuffle sequences have since been removed; compare now runs the three supported methods only.

| Method | Games | Won | Not solved | Limit reached | Policy stopped | Win rate | Avg moves | Avg explored |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Seeded random baseline | 100 | 40 | 60 | 11 | 49 | 40% | 875.43 | 875.92 |
| Randomized start + In-Faro | 100 | 36 | 64 | 13 | 51 | 36% | 947.21 | 947.72 |
| Randomized start + Out-Faro | 100 | 35 | 65 | 14 | 51 | 35% | 1016.16 | 1016.67 |

Win rate is 100 × won / total. Averages include unsuccessful games. The random baseline exceeded the 20% objective on this measured sample, not as a population guarantee. Timing-limited outcomes can vary by machine. The full per-game report is `docs/reports/visible-shuffle-comparison-2026-11-13.txt`. Older reports in that directory are historical and must not be attributed to the current visible solver.

## Remaining limits

The visible solver is a deterministic heuristic policy rather than an optimal imperfect-information search. It can make poor choices, explore many legal but unproductive actions, and stop without proving a loss. Observationally indistinguishable positions are deliberately treated alike rather than using hidden identities to distinguish them. Unlimited redeals are legal, but execution budgets prevent unbounded play. Only one constructed guaranteed deal is included as a separate demonstration. The 100-seed comparison is a measured sample, not evidence that Faro improves random-deal win rates.
After removing Custom Sequence, Java 25 compilation and 13,176 shuffle/visible checks passed. The former third submenu choice is rejected; repetition and deck-conservation checks remain. Historical reports above retain their original results.

## Simpler implementation

Removed SearchSolver, HeuristicEvaluator, and StateKey. Limits now belongs to SolitaireSolver and uses the name moves. The controller exposes guaranteed(compact) directly instead of a boolean switch between unrelated modes. Existing manual and guaranteed commands remain functional. ShuffleType and SolitaireSolver remain interfaces with real implementations, and records/enums remain where they represent useful data or choices. No placeholder classes were added.

After this cleanup, Java 25 compilation passed; the production-solver core suite passed 209 checks and the shuffle/visible suite passed 13,176 checks.
