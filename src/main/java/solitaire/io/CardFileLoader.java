package solitaire.io;

import solitaire.enums.Rank;
import solitaire.enums.Suit;
import solitaire.model.Card;
import solitaire.model.Deck;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Card file reader.
 *
 * <p>Kind: final class, uses Deck, Card, Suit, and Rank.
 * <p>Purpose: reads a deck from a text file of suit-rank codes such as H-A or S-10, separated by spaces or new lines,
 * and reports every problem with its line number so the player can fix the file.
 * <p>Data: the Path of the card-files folder. load() collects cards in an ArrayList to keep the file order and in a
 * HashSet for a fast duplicate check.
 * <p>OOP: Abstraction - callers only call load(path) and get a Deck; the parsing steps are private. Encapsulation -
 * the card-files folder is passed in once (from solitaire.properties) and kept private.
 *
 * @author Farhiya Ayyub
 */
public final class CardFileLoader {
    private final Path cardFilesFolder;

    /**
     * Creates a loader that looks for bare file names, such as input.txt, in the given folder.
     *
     * <p>Called by: SolitaireApplication.run(), SolitaireApplication.getController().
     *
     * @param cardFilesFolder the folder searched when only a file name is given
     * @throws NullPointerException if cardFilesFolder is {@code null}
     */
    public CardFileLoader(Path cardFilesFolder) {
        this.cardFilesFolder = Objects.requireNonNull(cardFilesFolder);
    }

    /**
     * Reads the file and returns a deck in file order (the first code is drawn first). Lowercase codes are accepted.
     *
     * <p>Algorithm: line-by-line parsing.
     * <ol>
     *   <li>Find the file and read it one line at a time, counting line numbers for error messages.</li>
     *   <li>Skip blank lines and remove an invisible byte-order mark from line 1.</li>
     *   <li>Split each line on spaces and turn each code into a card.</li>
     *   <li>Stop at the first duplicate, found with a HashSet.</li>
     *   <li>After the last line, require exactly 52 cards.</li>
     * </ol>
     *
     * <p>Called by: ConsoleInput.loadDeck(), SolitaireApplication.run().
     *
     * @param filePath the card file to read; a bare file name is also looked for in the card-files folder
     * @return a deck of the 52 cards in file order
     * @throws IOException if the file does not exist, is a folder, or cannot be read
     * @throws IllegalArgumentException for an invalid code, a duplicate card, or a card count other than 52
     */
    public Deck load(Path filePath) throws IOException {
        Path resolvedPath = resolveFile(filePath);
        List<Card> cards = new ArrayList<>();
        Set<Card> uniqueCards = new HashSet<>();
        // try-with-resources closes the file even if an error is thrown while reading it.
        try (BufferedReader reader = Files.newBufferedReader(resolvedPath, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                // Some editors (for example Notepad) save an invisible byte-order mark at the start of the file.
                if (lineNumber == 1 && line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }
                if (line.isBlank()) {
                    continue;
                }
                for (String token : line.trim().split("\\s+")) {
                    Card card = parseCard(token, lineNumber);
                    if (!uniqueCards.add(card)) {
                        throw new IllegalArgumentException("Duplicate card " + token + " on line " + lineNumber);
                    }
                    cards.add(card);
                }
            }
        }
        if (cards.size() != Deck.CARD_COUNT) {
            throw new IllegalArgumentException("Expected " + Deck.CARD_COUNT + " cards, found " + cards.size());
        }
        return new Deck(cards);
    }

    /**
     * {@return the path to read} If the path does not exist and is only a file name, such as input.txt, it is looked up
     * in the card-files folder instead.
     *
     * <p>Called by: load().
     *
     * @param requestedPath the path the player typed
     * @throws IOException if the result is a folder or does not exist
     */
    private Path resolveFile(Path requestedPath) throws IOException {
        Path resolvedPath = requestedPath;
        if (!Files.exists(requestedPath) && !requestedPath.isAbsolute()
                && requestedPath.getNameCount() == 1) {
            resolvedPath = cardFilesFolder.resolve(requestedPath);
        }
        if (Files.isDirectory(resolvedPath)) {
            throw new IOException("'" + requestedPath + "' is a folder. Enter a card file path.");
        }
        if (!Files.exists(resolvedPath)) {
            throw new IOException("File not found: " + requestedPath
                    + ". Enter a valid card file path.");
        }
        return resolvedPath;
    }

    /**
     * {@return the card for one code, such as H-A or h-a}
     *
     * <p>Called by: load().
     *
     * @param token one card code from the file, such as "H-A"
     * @param lineNumber the line in the file, used in error messages
     * @throws IllegalArgumentException unless the code is exactly one suit and one rank joined by a single hyphen
     */
    private Card parseCard(String token, int lineNumber) {
        String normalizedToken = token.toUpperCase(Locale.ROOT);
        String[] parts = normalizedToken.split("-", -1);
        if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
            throw new IllegalArgumentException("Invalid format '" + token + "' on line " + lineNumber
                    + ". Use suit-rank, for example D-2.");
        }
        Suit suit = parseSuit(parts[0], lineNumber);
        Rank rank = parseRank(parts[1], lineNumber);
        return new Card(suit, rank);
    }

    /**
     * {@return the suit with this code (H, D, C, or S)}
     *
     * <p>Called by: parseCard().
     *
     * @param suitCode the suit part of a card code, such as "H"
     * @param lineNumber the line in the file, used in error messages
     * @throws IllegalArgumentException with the line number for any other code
     */
    private Suit parseSuit(String suitCode, int lineNumber) {
        for (Suit suit : Suit.values()) {
            if (suit.code().equals(suitCode)) {
                return suit;
            }
        }
        throw new IllegalArgumentException("Unknown suit '" + suitCode + "' on line " + lineNumber
                + ". Allowed: H, D, C, S.");
    }

    /**
     * {@return the rank with this symbol (A, 2 to 10, J, Q, or K)}
     *
     * <p>Called by: parseCard().
     *
     * @param rankSymbol the rank part of a card code, such as "10"
     * @param lineNumber the line in the file, used in error messages
     * @throws IllegalArgumentException with the line number for any other symbol
     */
    private Rank parseRank(String rankSymbol, int lineNumber) {
        for (Rank rank : Rank.values()) {
            if (rank.symbol().equals(rankSymbol)) {
                return rank;
            }
        }
        throw new IllegalArgumentException("Unknown rank '" + rankSymbol + "' on line " + lineNumber
                + ". Allowed: A, 2-10, J, Q, K.");
    }
}
