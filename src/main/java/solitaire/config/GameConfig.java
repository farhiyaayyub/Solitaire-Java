package solitaire.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

/**
 * Program settings loaded from properties files.
 *
 * <p>Kind: record.
 * <p>Purpose: holds the values that can change without rebuilding the program: the solver limits, the simulation
 * defaults, and the card-files folder. The defaults come from {@code default.properties} (packaged with the program),
 * and any value in {@code solitaire.properties} (in the start folder) replaces its default.
 * <p>Data: the five record fields below. load() uses a Properties object with the defaults, overridden by the
 * player's file, and a Set of the names the player's file sets, so an error can say which file a bad value came from.
 * <p>OOP: Encapsulation - a record cannot change after it is created, and every value is checked in the constructor,
 * so the rest of the program only ever receives valid settings.
 *
 * @param maxMoves         the most moves the solver may play in one game
 * @param maxMillis        the most time the solver may use for one game, in milliseconds
 * @param defaultGameCount games played by {@code simulate} and {@code compare} when no count is given
 * @param maxGameCount     the highest game count the menu accepts
 * @param cardFilesFolder  the folder searched when the player types only a card file name, such as input.txt
 * @author Farhiya Ayyub
 */
public record GameConfig(int maxMoves, long maxMillis, int defaultGameCount, int maxGameCount,
                         Path cardFilesFolder) {

    /** The settings file the player edits, read from the folder the program is started from. */
    public static final Path DEFAULT_FILE = Path.of("solitaire.properties");

    // The packaged defaults also define which setting names exist, so a misspelled name can be rejected.
    private static final String DEFAULTS_RESOURCE = "/default.properties";

    /**
     * Rejects a missing card-files folder and any limit or game count below 1.
     *
     * <p>Called by: load().
     *
     * @throws NullPointerException if cardFilesFolder is {@code null}
     * @throws IllegalArgumentException if a limit or game count is less than 1
     */
    public GameConfig {
        Objects.requireNonNull(cardFilesFolder, "cardFilesFolder");
        requirePositive("solver.maxMoves", maxMoves);
        requirePositive("solver.maxMillis", maxMillis);
        requirePositive("simulation.defaultGameCount", defaultGameCount);
        requirePositive("simulation.maxGameCount", maxGameCount);
    }

    /**
     * Reads and checks the settings. If the settings file does not exist, the defaults are used, so the program also
     * runs without it.
     *
     * <p>Algorithm: defaults first, then the player's values on top.
     * <ol>
     *   <li>Read {@code default.properties}; every setting and its default is there.</li>
     *   <li>If the settings file exists, read its {@code name=value} lines (# starts a comment).</li>
     *   <li>Reject any name that is not in the defaults, so a misspelled name is reported.</li>
     *   <li>Let every value in the settings file replace its default.</li>
     *   <li>Check that the card-files folder exists, convert the numbers, and create the record.</li>
     * </ol>
     *
     * <p>Called by: SolitaireApplication.run().
     *
     * @param file the settings file to read
     * @return the settings, with the defaults for anything the file leaves out
     * @throws IOException if a file exists but cannot be read
     * @throws IllegalArgumentException for an unknown setting, a value that is not a whole number, a value below 1,
     *     a number too large for its setting, or a card-files folder that does not exist; the message names the file and the setting
     */
    public static GameConfig load(Path file) throws IOException {
        Properties settings = readDefaults();
        Properties overrides = new Properties();
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                overrides.load(reader);
            }
        }
        for (String name : overrides.stringPropertyNames()) {
            if (settings.getProperty(name) == null) {
                throw new IllegalArgumentException(file + ": unknown setting '" + name + "'");
            }
        }
        settings.putAll(overrides);
        // Lets an error name the file the value came from: the player's file or the packaged defaults.
        Set<String> fromFile = overrides.stringPropertyNames();
        Path cardFilesFolder = Path.of(settings.getProperty("cardFiles.folder").trim());
        if (!Files.isDirectory(cardFilesFolder)) {
            throw new IllegalArgumentException(source(file, fromFile, "cardFiles.folder") + ": cardFiles.folder '"
                    + cardFilesFolder + "' is not an existing folder");
        }
        int maxMoves = readInt(settings, file, fromFile, "solver.maxMoves");
        long maxMillis = readLong(settings, file, fromFile, "solver.maxMillis");
        int defaultGameCount = readInt(settings, file, fromFile, "simulation.defaultGameCount");
        int maxGameCount = readInt(settings, file, fromFile, "simulation.maxGameCount");
        try {
            return new GameConfig(maxMoves, maxMillis, defaultGameCount, maxGameCount, cardFilesFolder);
        } catch (IllegalArgumentException exception) {
            // The record names the setting; this adds the file it came from.
            String name = exception.getMessage().split(" ")[0];
            throw new IllegalArgumentException(source(file, fromFile, name) + ": " + exception.getMessage());
        }
    }

    /**
     * {@return every setting with its default value, read from {@code default.properties} inside the program}
     *
     * <p>Called by: load().
     *
     * @throws IOException if the file is packaged but cannot be read
     * @throws IllegalStateException if the file is not packaged with the program
     */
    private static Properties readDefaults() throws IOException {
        Properties defaults = new Properties();
        try (InputStream stream = GameConfig.class.getResourceAsStream(DEFAULTS_RESOURCE)) {
            if (stream == null) {
                throw new IllegalStateException("default.properties is missing from the program");
            }
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                defaults.load(reader);
            }
        }
        return defaults;
    }

    /**
     * {@return the setting's value as an int}
     *
     * <p>Called by: load().
     *
     * @param settings the defaults with the player's values on top
     * @param file the player's settings file
     * @param fromFile the names that the player's file sets
     * @param name the setting name, such as "solver.maxMoves"
     * @throws IllegalArgumentException if the value is not a whole number or does not fit in an int
     */
    private static int readInt(Properties settings, Path file, Set<String> fromFile, String name) {
        long value = readLong(settings, file, fromFile, name);
        if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
            throw new IllegalArgumentException(source(file, fromFile, name) + ": " + name
                    + " is too large: " + value);
        }
        return (int) value;
    }

    /**
     * {@return the setting's value as a long}
     *
     * <p>Called by: load(), readInt().
     *
     * @param settings the defaults with the player's values on top
     * @param file the player's settings file
     * @param fromFile the names that the player's file sets
     * @param name the setting name, such as "solver.maxMillis"
     * @throws IllegalArgumentException if the value is not a whole number
     */
    private static long readLong(Properties settings, Path file, Set<String> fromFile, String name) {
        String value = settings.getProperty(name).trim();
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(source(file, fromFile, name) + ": " + name
                    + " must be a whole number, but is '" + value + "'");
        }
    }

    /**
     * {@return the player's file name if it sets this setting, otherwise "default.properties"}, for error messages.
     *
     * <p>Called by: load(), readInt(), readLong().
     *
     * @param file the player's settings file
     * @param fromFile the names that the player's file sets
     * @param name the setting name
     */
    private static String source(Path file, Set<String> fromFile, String name) {
        return fromFile.contains(name) ? file.toString() : "default.properties";
    }

    /**
     * Checks that a limit or count is at least 1.
     *
     * <p>Called by: the GameConfig constructor.
     *
     * @param name the setting name, used in the error message
     * @param value the value to check
     * @throws IllegalArgumentException if the value is less than 1
     */
    private static void requirePositive(String name, long value) {
        if (value < 1) {
            throw new IllegalArgumentException(name + " must be at least 1, but is " + value);
        }
    }
}
