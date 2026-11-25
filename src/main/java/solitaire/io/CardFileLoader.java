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
 * Reads a deck from a text file of card codes such as H-A or S-10. It checks every code and makes sure the file has
 * exactly 52 different cards.
 *
 * @author Farhiya Ayyub
 */
public final class CardFileLoader {
    private final Path cardFilesFolder;

    /** Stores the folder where card files are looked for when the player types only a file name, such as input.txt. */
    public CardFileLoader(Path cardFilesFolder) {
        this.cardFilesFolder = Objects.requireNonNull(cardFilesFolder);
    }

    /**
     * Reads the file line by line, splits each line into card codes, and turns each code into a card. It skips blank
     * lines, rejects duplicate cards, and checks that there are exactly 52 cards. Returns the deck in file order, so
     * the first code is drawn first.
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
     * Finds the file to read. If only a file name is given and it is not in the current folder, it looks in the
     * card-files folder. Throws an error if the path is a folder or the file does not exist.
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
     * Turns one code such as H-A (upper or lower case) into a card. The code must be a suit and a rank separated by a
     * dash; otherwise the error names the line.
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

    /** Returns the suit whose code matches (H, D, C, or S), or throws an error that names the line. */
    private Suit parseSuit(String suitCode, int lineNumber) {
        for (Suit suit : Suit.values()) {
            if (suit.code().equals(suitCode)) {
                return suit;
            }
        }
        throw new IllegalArgumentException("Unknown suit '" + suitCode + "' on line " + lineNumber
                + ". Allowed: H, D, C, S.");
    }

    /** Returns the rank whose symbol matches (A, 2 to 10, J, Q, or K), or throws an error that names the line. */
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
