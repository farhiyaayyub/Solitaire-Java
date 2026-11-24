# Java Console Klondike Solitaire

A console Klondike Solitaire game in Java 21+. Choose a deck (standard or loaded from a card file), a shuffle
(Random, In-Faro, Out-Faro), and Draw 1 or Draw 3. The built-in solver then plays the game automatically using only
the cards a real player could see, and every move is replayed and checked by the game engine. Settings live in an
external configuration file, so they can change without rebuilding. No external libraries are used.

## Run

```powershell
./run.ps1                       # interactive menu
./run.ps1 guaranteed --compact  # a deal with a verified winning solution
./run.ps1 random 7 --compact    # seeded random deal (seed 7 is a win)
./run.ps1 simulate 100 0        # win rate over 100 random deals
./run.ps1 compare 100 0         # Random vs In-Faro vs Out-Faro on the same seeds
./run.ps1 validate input.txt    # check a card file without playing
./demo-validation.ps1           # run every valid and invalid card file
./run.ps1 manual                # choose each move yourself
./run.ps1 help
```

With Maven: `mvn package`, then `java -jar target/klondike-solitaire-1.0.0.jar [command]`.

`random`, `simulate`, and `compare` also accept solver limits for one run: `[moves [ms]]`, for example
`./run.ps1 random 7 --compact 5000 20000`.

Exit codes: `0` success, `1` file or game error, `2` invalid input or invalid settings.

### Interactive menu

1. **Deck:** `[1]` new standard deck, or `[2]` card file (errors are shown in red and you can retry).
2. **Shuffle:** `[1]` Random or `[2]` Perfect Shuffle, then In-Faro or Out-Faro. Card files also offer `[3]` No
   shuffle.
3. **Draw:** `[1]` Draw 1 or `[3]` Draw 3.
4. **Mode (standard deck only):** watch one game, or test the win rate over 1 to 100 games.

Enter `q` at any prompt to exit. Invalid choices are re-asked; end of input exits safely.

### Card files

A card file holds exactly 52 distinct cards separated by spaces or new lines, such as `H-A D-2 C-10 S-K`. Suits are
`H D C S`; ranks are `A 2-10 J Q K`; case is ignored. The first card in the file is the first card dealt. A bare
file name such as `input.txt` is looked up in `card-files/`. See [card-files/README.md](card-files/README.md) for the
sample valid and invalid files.

### Settings

| File | Role |
| --- | --- |
| `src/main/resources/default.properties` | Default value of every setting. Packaged with the program. Also defines which setting names exist. |
| `solitaire.properties` (project folder) | Your settings. Every line starts commented out (`#`); remove the `#` and edit the value to change it. No rebuild needed. |

| Setting | Default | Meaning |
| --- | ---: | --- |
| `solver.maxMoves` | 3000 | Most moves the solver plays in one game |
| `solver.maxMillis` | 10000 | Time limit for one game, in milliseconds |
| `simulation.defaultGameCount` | 100 | Games for `simulate` and `compare` when no count is given |
| `simulation.maxGameCount` | 100 | Highest game count the menu accepts |
| `simulation.defaultSeed` | 0 | Seed for `random`, `simulate`, `compare`, and `manual` when none is given |
| `cardFiles.folder` | card-files | Folder searched when only a card file name is typed |

`GameConfig` reads the defaults, lets `solitaire.properties` replace them, and checks every value. An unknown name,
a value that is not a whole number, a value below 1, or a folder that does not exist stops the program with a red
`Configuration error` that names the file and the setting. A missing `solitaire.properties` simply means "use the
defaults".

## Rules implemented

- Deal: tableau columns of 1 to 7 cards, left to right, only the top card face up; the other 24 cards form the stock.
- Tableau: build down in alternating colors; only a King (or a sequence starting with a King) may fill an empty
  column; only face-up sequences move; the newly exposed card is turned face up.
- Foundations: one per suit, Ace to King, one card at a time. A foundation card may be moved back to the tableau.
- Stock: draws 1 or 3 cards (or fewer if fewer remain); the empty stock can be refilled from the waste in the
  original order, without limit. Only the top waste card can be played.
- Every move is checked by `MoveValidator` before it is applied, and after every move the board is checked to still
  hold exactly 52 distinct cards. A game can be marked won only when all 52 cards are on the foundations.

## Algorithms

### 1. Solver: greedy search with memory (`VisibleSolver`)

The solver plays like a careful human: it sees only face-up cards, follows the real rules, and never takes a move
back. Each turn:

1. If all 52 cards are on the foundations, stop with **WON**.
2. Build the visible position (`ObservableBoard`): every face-down card is replaced by a hidden marker.
3. List every legal move (`MoveGenerator`).
4. Give each move a score and pick the highest-scoring move **not yet tried in this visible position**.
5. If every legal move was already tried here, stop with **STUCK**.
6. Remember the move as tried in this position, then play it.

The loop also stops with **LIMIT_REACHED** after 3,000 moves or 10 seconds, whichever comes first, so every game
ends. Choosing the best move is one pass over the legal moves, O(number of moves); on a tie the move listed first
wins, so results are reproducible.

**Scores**

| Move | Score | Why |
| --- | ---: | --- |
| Safe move to a foundation | 500 | The card can never be needed in the tableau again |
| Move that uncovers a face-down card | +300 + cards still under it | Seeing new cards is the most useful progress; deeper columns first |
| Waste to tableau | 80 | Brings a waste card into play |
| Column move that empties the source column | 30 | Makes room for a King |
| Other column move | 20 + 0.1 per card | Prefers moving longer sequences |
| Unsafe move to a foundation | 12 | Allowed, but the card may still be needed |
| Draw | 5 | Only when nothing better is available |
| Recycle the waste | 1 | Last resort |
| Foundation back to tableau | -100 | Usually a step backwards |
| Whole column onto an empty column | -200 | Changes nothing useful |

**Safe foundation move.** Aces and 2s are always safe. A higher card is safe when both opposite-color foundations
are at least one rank lower and the other same-color foundation is at least two ranks lower, because then no
tableau card can still need it to build on. The check stops at the first foundation that is too low.

**Loop prevention.** A `HashMap<ObservableBoard, Set<Move>>` maps each visible position to the moves already tried
there. Because `ObservableBoard` and `Move` are records, Java generates `equals()` and `hashCode()`, so the same
position and the same move are recognised again. This stops endless back-and-forth moves.

**Replay check.** The solver plays on its own copy of the board. `GameController` then replays every chosen move on
the real game through `SolitaireGame.play()`, which validates each move again.

### 2. Move generation: generate and test (`MoveGenerator`)

For every move type, every source pile, and every destination pile, build the candidate move and keep it only if
`MoveValidator` says it is legal. Only column-to-column moves try more than one card (every count up to the column
size); every other type moves exactly one card. The order is fixed, so the same board always gives the same list.

### 3. Shuffling (`ShuffleStrategy`, Strategy pattern)

- **Random:** Fisher-Yates through `Collections.shuffle(list, new Random(seed))`, O(n). The same seed always gives the
  same order, like a game number.
- **Faro (perfect shuffle):** split the 52 cards into two halves of 26 and interleave them one card at a time, O(n).
  **In-Faro** takes the second half first (the top card moves to position 2); **Out-Faro** takes the first half first
  (the top and bottom cards stay in place). The two classes differ only in `startsWithSecondHalf()`.
- **No shuffle:** keeps the card file's order.
- A Faro shuffle is not random, so on a standard deck it is applied after one seeded random shuffle; otherwise every
  game would be the same deal. On a card file, the file order is used directly.
- After any shuffle, a new `Deck` is built and checked again (no duplicates, exactly 52 cards).

### 4. Deal (`GameState.deal`)

Column 1 gets 1 card, column 2 gets 2, up to column 7 with 7 (28 cards); only the last card of each column is face
up. The remaining 24 cards go to the stock face down. The board is then checked to hold 52 distinct cards.

### 5. Validation

| What | Where | How |
| --- | --- | --- |
| Settings | `GameConfig.load()` | Known names only, whole numbers, at least 1, existing folder |
| Command and arguments | `SolitaireApplication` | Known command, argument count, `--compact`/`--verbose` |
| Card file | `CardFileLoader` | Line by line: `suit-rank` format, known suit and rank, no duplicate (`HashSet`), exactly 52; every error names its line |
| Deck after a shuffle | `Deck`, `ShuffleConfiguration` | No duplicate, exactly 52 |
| Each move | `MoveValidator`, each pile's `accepts()` | The rules above; `count` must be 1 except between columns |
| Board after each move | `GameState.verify()` | Exactly 52 distinct cards, O(n) with a `HashSet` |
| Menu answers | `ConsoleInput` | Re-asks until valid; `try/catch` for numbers; range 1 to the maximum game count |

### 6. Win rate (`GameController.simulate`)

Game *n* uses seed *first seed + n*. Each game is counted as won, limit reached, or stuck, and the win rate is
`100.0 * won / games`.

## Design

```text
solitaire/
  Main, SolitaireApplication       entry point and command-line commands
  config/   GameConfig                reads and checks the settings files
  enums/    Rank, Suit, GameStatus, MoveType
  model/    Card, Deck
  pile/     CardPile (abstract) <- StockPile, WastePile, TableauPile, FoundationPile
  shuffle/  ShuffleStrategy (interface) <- RandomShuffle, NoShuffle, FaroShuffle (abstract) <- InFaroShuffle,
            OutFaroShuffle; ShuffleType, ShuffleConfiguration
  move/     Move, MoveValidator, MoveGenerator
  game/     GameState, SolitaireGame, GameController
  solver/   SolitaireSolver (interface) <- VisibleSolver; ObservableBoard, SolvableDealProvider
  io/       ConsoleInput, ConsoleRenderer, ConsoleColor, CardFileLoader

card-files/                         sample card files (user input, not part of the program)
src/main/resources/                 default.properties (packaged with the program)
solitaire.properties                your settings (all commented out by default)
```

### How to read the code comments

Every class and method has standard Javadoc. Press **Ctrl+Q** on a name in IntelliJ to see it formatted.

| Part | Meaning |
| --- | --- |
| First sentence | What the method does |
| `Algorithm:` | The steps, as a numbered list (methods with a real algorithm) |
| `Key idea:` | Which data structure or technique is used, and why |
| `Implements` / `Overrides` / `Implemented by` | Which interface or parent method it implements, or which classes implement it |
| `Calls:` | The project methods it calls, each with a short reason |
| `Called by:` | The methods that call it; "(through X)" means the call goes through interface or parent type X |
| `@param`, `@return`, `@throws` | Inputs, result, and errors |

### Object-oriented principles

**Encapsulation**
- All fields are private. Piles, decks, and games return copies of their cards, never the live objects.
- `SolitaireGame` owns its board privately and changes it only through `play(move)`, which validates first.
- Records such as `Move`, `GameConfig`, `ObservableBoard`, and `SolitaireSolver.Result` cannot change after creation;
  `GameConfig` checks every setting when it is created.

**Abstraction**
- `CardPile` defines what every pile can do without saying how each pile's rule works.
- `ShuffleStrategy` and `SolitaireSolver` are interfaces. `GameController` receives a `SolitaireSolver` in its
  constructor and never depends on `VisibleSolver` directly (dependency injection).
- `ObservableBoard` hides face-down cards, so the solver cannot cheat.

**Inheritance**
- The four piles extend `CardPile` and share its storage and operations.
- `InFaroShuffle` and `OutFaroShuffle` extend `FaroShuffle`, which holds the shared interleaving algorithm. Each
  subclass only says which half goes first.

**Polymorphism**
- `accepts(Card)` is implemented by every pile: a tableau needs one rank lower in the opposite color, a foundation
  needs the same suit one rank higher, and stock and waste accept nothing.
- `TableauPile` overrides the `onTopRemoved()` hook to turn the newly exposed card face up.
- `MoveValidator` and `SolitaireGame` handle every single-card move with the same code,
  `destination.accepts(source.top())` and `destination.place(source.takeTop())`. Java chooses the right rule at run
  time.
- `ShuffleConfiguration` calls `type.strategy().shuffle(...)` without knowing which shuffle it has.

### Data structures

- `CardPile` uses an `ArrayList` with the top card at the end, so adding or removing the top card is O(1), the same as
  a `Deque`. An `ArrayList` is used instead of a `Deque` because the tableau needs cards in the middle of a column
  (`get(index)`): to check a moving sequence, to find its lowest card, to see whether a hidden card lies under it,
  and to print each row.
- `Deck` stores its next card at the end for the same reason.
- `GameState` keeps foundations in an `EnumMap` keyed by suit.
- The solver uses a `HashMap` from visible positions to a `HashSet` of moves already tried there.
- Duplicate checks use `HashSet`.

## Measured win rates

Command: `compare 100 0` (Draw 1, seeds 0 to 99, default limits of 3,000 moves and 10 seconds per game). Every game
was replayed through the engine.

| Method | Games | Won | Not solved | Limit reached | No new moves | Win rate | Avg moves |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Random | 100 | 40 | 60 | 11 | 49 | 40% | 875.43 |
| Random start + In-Faro | 100 | 36 | 64 | 13 | 51 | 36% | 947.21 |
| Random start + Out-Faro | 100 | 35 | 65 | 14 | 51 | 35% | 1016.16 |

The whole comparison takes about 7 seconds. Draw 3 is much harder: about 10% over 100 random deals.

These are results for this sample of 100 deals, not a guarantee. Because a Faro shuffle of a random deck is still a
random deck, the three rows are expected to be similar.

## Limits

The solver is a fixed heuristic, not a complete search over hidden cards, so it can miss wins and stop without
proving a loss. Some Klondike deals cannot be won at all. Only one constructed guaranteed deal is included.
