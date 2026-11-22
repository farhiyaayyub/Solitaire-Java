# Java Console Klondike Solitaire

A console Klondike Solitaire game in Java 21+. Choose a deck (standard or loaded from a card file), a shuffle
(Random, In-Faro, Out-Faro), and Draw 1 or Draw 3. The built-in solver then plays the game automatically using only
the cards a real player could see, and every move is replayed and checked by the game engine. No external libraries
are used.

## Run

```powershell
./run.ps1                       # interactive menu
./run.ps1 guaranteed --compact  # a deal with a verified winning solution
./run.ps1 random 42 --compact   # seeded random deal
./run.ps1 simulate 100 0        # win rate over 100 random deals
./run.ps1 compare 100 0         # Random vs In-Faro vs Out-Faro on the same seeds
./run.ps1 validate input.txt    # check a card file without playing
./run.ps1 manual                # choose each move yourself
./run.ps1 help
```

With Maven: `mvn package`, then `java -jar target/klondike-solitaire-1.0.0.jar [command]`.

`random`, `simulate`, and `compare` also accept solver limits: `[decisions [moves [ms]]]`
(defaults 15,000 decisions, 3,000 moves, 10,000 ms per game).

Exit codes: `0` success, `1` file or game error, `2` invalid input.

### Interactive menu

1. **Deck:** `[1]` new standard deck, or `[2]` load from a card file (errors are shown and you can retry).
2. **Shuffle:** `[1]` Random or `[2]` Perfect Shuffle, then In-Faro or Out-Faro. File decks also offer `[3]` No
   shuffle.
3. **Draw:** `[1]` Draw 1 or `[3]` Draw 3.
4. **Mode (standard deck only):** watch one game, or test the win rate over many games.

Enter `q` at any prompt to exit. Invalid choices are re-asked; end of input exits safely.

A Faro shuffle is deterministic, so on a standard deck it is applied after one seeded random shuffle (otherwise every
game would be the same deal). On a file deck, the file order is used directly, so a Faro shuffle is applied to exactly
the cards in the file.

### Card files

A card file holds exactly 52 distinct cards separated by spaces or new lines, such as `H-A D-2 C-10 S-K`. Suits are
`H D C S`; ranks are `A 2-10 J Q K`; case is ignored. The first card in the file is the first card dealt. A bare
file name such as `input.txt` is also looked up in `resources/`. See [resources/README.md](resources/README.md) for
the sample valid and invalid files and `./demo-validation.ps1`, which runs all of them.

## Rules implemented

- Deal: tableau columns of 1 to 7 cards, left to right, only the top card face up; the other 24 cards form the stock.
- Tableau: build down in alternating colors; only a King (or a sequence starting with a King) may fill an empty
  column; only face-up sequences move; the newly exposed card is turned face up.
- Foundations: one per suit, Ace to King. A foundation card may be moved back to the tableau.
- Stock: draws 1 or 3 cards (or fewer if fewer remain); the empty stock can be refilled from the waste in the
  original order, without limit. Only the top waste card can be played.
- Every move is checked by `MoveValidator` before it is applied, and after every move the board is checked to still
  hold exactly 52 distinct cards. A game can be marked won only when all 52 cards are on the foundations.

## Design

```text
solitaire/
  Main, SolitaireApplication       entry point and command-line commands
  enums/    Rank, Suit, GameStatus, MoveType
  model/    Card, Deck
  pile/     CardPile (abstract) <- StockPile, WastePile, TableauPile, FoundationPile
  shuffle/  ShuffleStrategy (interface) <- RandomShuffle, NoShuffle, FaroShuffle (abstract) <- InFaroShuffle,
            OutFaroShuffle; ShuffleType, ShuffleConfiguration
  move/     Move, MoveValidator, MoveGenerator
  game/     GameState, SolitaireGame, GameController
  solver/   SolitaireSolver (interface) <- VisibleSolver; ObservableBoard, SolvableDealProvider
  io/       ConsoleInput, ConsoleRenderer, ConsoleColor, CardFileLoader
```

### How to read the code comments

Every comment says what kind of code it is, what it does, and what it calls:

```java
/**
 * Method: Validates and applies one move; returns false, without changing anything, for an illegal move.
 * Calls: MoveValidator.isLegal(), applyLegalMove(), GameState.verify(), GameState.won()
 */
public boolean play(Move move) {
```

| Starts with | Meaning |
| --- | --- |
| `Type:` (class comment) | What kind of type it is: class, abstract class, interface, enum, or record, and what it extends or implements |
| `OOP:` (class comment) | Which OOP principles the class shows |
| `Constructor:` | Runs when an object is created with `new`; `(private)` or `(compact)` when it is a special kind |
| `Method:` | A behavior of the class; `(override)` means it replaces an inherited version, `(static)` means it belongs to the class itself |
| `Abstract method:` | Declared without code; every subclass or implementation must provide it |
| `Constant:` / `Field:` | A fixed value (`static final`) / a value stored in each object |
| `Object:` | The next line creates an object with `new` |
| `Calls:` | The methods this method or constructor uses, written as `Class.method()`, or just `method()` for the same class |

### Object-oriented principles

**Encapsulation**
- All fields are private. Piles, decks, and games return copies of their cards, never the live objects.
- `SolitaireGame` owns its board privately and changes it only through `play(move)`, which validates first.
  `GameState` is the mutable working board that the engine and solver play on; code outside the game only ever
  receives copies of it.
- Records such as `ObservableBoard`, `VerifiedDeal`, and `SolitaireSolver.Result` copy their lists.

**Abstraction**
- `CardPile` defines what every pile can do (`size`, `top`, `place`, `takeTop`, `accepts`) without saying how each
  pile's rule works.
- `ShuffleStrategy` and `SolitaireSolver` are interfaces. `GameController` receives a `SolitaireSolver` in its
  constructor and never depends on `VisibleSolver` directly.
- `ObservableBoard` hides face-down cards, so the solver cannot cheat.

**Inheritance**
- The four piles extend `CardPile` and share its storage and operations.
- `InFaroShuffle` and `OutFaroShuffle` extend `FaroShuffle`, which holds the shared interleaving algorithm
  (template method). Each subclass only says which half goes first.

**Polymorphism**
- `accepts(Card)` is overridden by every pile: a tableau needs one rank lower in the opposite color, a foundation
  needs the same suit one rank higher, and stock and waste accept nothing.
- `TableauPile` overrides the `onTopRemoved()` hook to turn the newly exposed card face up.
- `MoveValidator` and `SolitaireGame` handle every single-card move with the same code,
  `destination.accepts(source.top())` and `destination.place(source.takeTop())`. Java chooses the right rule at run
  time.
- `ShuffleType` maps each menu choice to a `ShuffleStrategy`; `ShuffleConfiguration` calls `shuffle(...)` without
  knowing which one it has.

### Solver

`VisibleSolver` scores each legal move using only visible information:
1. Safe foundation moves score highest.
2. Next come moves that reveal a hidden tableau card.
3. Then waste plays and useful tableau transfers.

Ties keep the generator's fixed order, so results are reproducible. Each decision is final (no undo), and the solver
remembers which moves it already tried in each visible position so it never repeats one.

Play stops when:
- it wins (`WON`),
- no untried move is left (`STUCK`, which does **not** prove the deal is unsolvable), or
- a decision, move, or time limit is reached (`LIMIT_REACHED`).

### Data structures

- `CardPile` uses an `ArrayList` with the top card at the end, so adding or removing the top card is constant time.
- `Deck` stores its next card at the end for the same reason.
- `GameState` keeps foundations in an `EnumMap`.
- The solver uses a `HashMap` from visible positions to the moves already tried there.
- Duplicate checks use `HashSet`.

## Measured win rates

Command: `compare 100 0 15000 3000 3000` (Draw 1, seeds 0 to 99, 3,000 ms limit per game). Every game was
replayed through the engine.

| Method | Games | Won | Not solved | Limit reached | No new moves | Win rate | Avg moves |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Random | 100 | 40 | 60 | 11 | 49 | 40% | 875.43 |
| Random start + In-Faro | 100 | 36 | 64 | 13 | 51 | 36% | 947.21 |
| Random start + Out-Faro | 100 | 35 | 65 | 14 | 51 | 35% | 1016.16 |

The whole comparison took about 20 seconds.

These are results for this sample of 100 deals, not a guarantee. Games that hit the time limit can vary slightly
between machines. Because a Faro shuffle of a random deck is still a random deck, the three rows are expected to be
similar.

## Limits

The solver is a fixed heuristic, not a complete search over hidden cards, so it can miss wins and stop without
proving a loss. Only one constructed guaranteed deal is included.
