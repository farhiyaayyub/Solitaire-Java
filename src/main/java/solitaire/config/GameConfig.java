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
 * The settings that can change without rebuilding the program: the solver limits, the simulation defaults, and the
 * folder where card files are looked up. No setting value is written in the Java code. The defaults come from
 * {@code default.properties}, which is packaged with the program, and any value in {@code solitaire.properties} (in
 * the folder the program is started from) replaces its default.
 *
 * <p>It is a record, so the settings cannot change after they are loaded. Encapsulation: every value is checked
 * when the record is created, so the rest of the program only ever receives valid settings.
 *
 * @param maxMoves         the most moves the solver may play in one game
 * @param maxMillis        the most time the solver may use for one game, in milliseconds
 * @param defaultGameCount games played by {@code simulate} and {@code compare} when no count is given
 * @param maxGameCount     the highest game count the menu accepts
 * @param defaultSeed      the seed used by {@code random}, {@code simulate}, {@code compare}, and {@code manual} when
 *                         no seed is given
 * @param cardFilesFolder      the folder searched when the player types only a card file name, such as input.txt
 * @author Farhiya Ayyub
 */
public record GameConfig(int maxMoves, long maxMillis, int defaultGameCount, int maxGameCount,
                         long defaultSeed, Path cardFilesFolder) {

    /** The settings file the player edits, read from the folder the program is started from. */
    public static final Path DEFAULT_FILE = Path.of("solitaire.properties");

    // The default settings, packaged with the program (src/main/resources). They also define which names exist.
    private static final String DEFAULTS_RESOURCE = "/default.properties";

    /**
     * Checks every value when the record is created.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@code requirePositive()} - rejects a limit or count below 1</li>
     * </ul>
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
     * Reads the default settings, replaces them with the values in the settings file, checks them, and returns them.
     * If the settings file does not exist, the defaults are used, so the program also runs without it.
     *
     * <p>Algorithm: defaults first, then the player's values on top.
     * <ol>
     *   <li>Read {@code default.properties} from the program; every setting and its default is there.</li>
     *   <li>If the settings file exists, read it as {@code name=value} lines (lines starting with # are comments).</li>
     *   <li>Reject any name in the settings file that is not in the defaults, so a misspelled name is reported.</li>
     *   <li>Let every value in the settings file replace its default.</li>
     *   <li>Check that the card-files folder exists, convert the numbers, and create the record (which checks
     *     them).</li>
     * </ol>
     *
     * <p>Calls:
     * <ul>
     *   <li>{@code readDefaults()} - reads default.properties from the program</li>
     *   <li>{@code readInt()} / {@code readLong()} - convert each value to a number</li>
     * </ul>
     *
     * <p>Called by: {@code SolitaireApplication.run()}.
     *
     * @param file the settings file to read
     * @return the settings, with the defaults for anything the file leaves out
     * @throws IOException if a file exists but cannot be read
     * @throws IllegalArgumentException for an unknown setting, a value that is not a whole number, a value below 1,
     *     or a card-files folder that does not exist; the message names the file and the setting
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
        // Names an error after the file the value came from: the player's file or the packaged defaults.
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
        long defaultSeed = readLong(settings, file, fromFile, "simulation.defaultSeed");
        try {
            return new GameConfig(maxMoves, maxMillis, defaultGameCount, maxGameCount, defaultSeed,
                    cardFilesFolder);
        } catch (IllegalArgumentException exception) {
            // The record names the setting; this adds the file it came from.
            String name = exception.getMessage().split(" ")[0];
            throw new IllegalArgumentException(source(file, fromFile, name) + ": " + exception.getMessage());
        }
    }

    /**
     * Reads {@code default.properties} from the program.
     *
     * <p>Called by: {@link #load}.
     *
     * @return every setting with its default value
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
     * Returns the setting as an int.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@code source()} - names the file in the error message</li>
     * </ul>
     *
     * <p>Called by: {@link #load}.
     *
     * @param settings the defaults with the player's values on top
     * @param file the player's settings file
     * @param fromFile the names that the player's file sets
     * @param name the setting name, such as "solver.maxMoves"
     * @return the setting's value as an int
     * @throws IllegalArgumentException if the value is not a whole number
     */
    private static int readInt(Properties settings, Path file, Set<String> fromFile, String name) {
        String value = settings.getProperty(name).trim();
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(source(file, fromFile, name) + ": " + name
                    + " must be a whole number, but is '" + value + "'");
        }
    }

    /**
     * Returns the setting as a long.
     *
     * <p>Calls:
     * <ul>
     *   <li>{@code source()} - names the file in the error message</li>
     * </ul>
     *
     * <p>Called by: {@link #load}.
     *
     * @param settings the defaults with the player's values on top
     * @param file the player's settings file
     * @param fromFile the names that the player's file sets
     * @param name the setting name, such as "solver.maxMillis"
     * @return the setting's value as a long
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
     * Returns the name of the file a setting's value came from, for error messages.
     *
     * <p>Called by: {@link #load}, {@code readInt()}, {@code readLong()}.
     *
     * @param file the player's settings file
     * @param fromFile the names that the player's file sets
     * @param name the setting name
     * @return the player's file if it sets the value, otherwise "default.properties"
     */
    private static String source(Path file, Set<String> fromFile, String name) {
        return fromFile.contains(name) ? file.toString() : "default.properties";
    }

    /**
     * Checks that a limit or count is at least 1.
     *
     * <p>Called by: {@link #GameConfig}.
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
