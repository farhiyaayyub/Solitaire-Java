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
import java.util.Set;

/**
 * Reads a complete deck from whitespace-separated suit-rank tokens such as H-A or S-10.
 *
 * <p>Type: final class; reads and validates card files.
 * OOP: ABSTRACTION (callers just call load(path) and get a Deck), ENCAPSULATION (private parsing helpers).
 *
 * @author Farhiya Ayyub
 */
public final class CardFileLoader {
    /**
     * Method: Returns a deck in file order, with the first token drawn first.
     * Calls: resolveFile(), Files.newBufferedReader(), parseCard(), Set.add() (finds duplicates)
     *
     * @throws IOException if the file cannot be read
     * @throws IllegalArgumentException if a token, duplicate, or card count is invalid
     */
    public Deck load(Path filePath) throws IOException {
        Path resolvedPath = resolveFile(filePath);
        List<Card> cards = new ArrayList<>();
        Set<Card> uniqueCards = new HashSet<>();
        // The reader is closed automatically at the end of this block, even if an error happens.
        try (BufferedReader reader = Files.newBufferedReader(resolvedPath, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
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
        // Object: Deck - built from the cards in file order.
        return new Deck(cards);
    }

    /**
     * Method: Finds the requested file; a bare file name is also looked up in the resources folder.
     * Calls: Files.exists(), Files.isDirectory(), Path.resolve()
     */
    private Path resolveFile(Path requestedPath) throws IOException {
        Path resolvedPath = requestedPath;
        if (!Files.exists(requestedPath) && !requestedPath.isAbsolute()
                && requestedPath.getNameCount() == 1) {
            resolvedPath = Path.of("resources").resolve(requestedPath);
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
     * Method: Converts one suit-rank token, such as H-A, into a card.
     * Calls: parseSuit(), parseRank()
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
        // Object: Card - the card for this token.
        return new Card(suit, rank);
    }

    /**
     * Method: Looks up a suit code or reports its line-specific error.
     * Calls: Suit.code()
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
     * Method: Looks up a rank symbol or reports its line-specific error.
     * Calls: Rank.symbol()
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
