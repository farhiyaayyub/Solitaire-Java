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
 * The program settings, such as the solver limits, the game counts, and the card-files folder. It reads them from
 * default.properties and solitaire.properties and checks every value.
 *
 * @author Farhiya Ayyub
 */
public record GameConfig(int maxMoves, long maxMillis, int defaultGameCount, int maxGameCount,
                         Path cardFilesFolder) {

    /** The settings file the player edits, read from the folder the program is started from. */
    public static final Path DEFAULT_FILE = Path.of("solitaire.properties");

    // The packaged defaults also define which setting names exist, so a misspelled name can be rejected.
    private static final String DEFAULTS_RESOURCE = "/default.properties";

    /**
     * Checks the values when a GameConfig is created: the card-files folder must be given, and every limit and game
     * count must be at least 1.
     */
    public GameConfig {
        // The folder must be given, and every number must be at least 1.
        Objects.requireNonNull(cardFilesFolder, "cardFilesFolder");
        requirePositive("solver.maxMoves", maxMoves);
        requirePositive("solver.maxMillis", maxMillis);
        requirePositive("simulation.defaultGameCount", defaultGameCount);
        requirePositive("simulation.maxGameCount", maxGameCount);
    }

    /**
     * Reads the default settings, then the player's solitaire.properties if it exists, and lets the player's values
     * replace the defaults. It rejects unknown setting names, a card-files folder that does not exist, and values that
     * are not whole numbers.
     */
    public static GameConfig load(Path file) throws IOException {
        // Step 1: read the default values that come with the program.
        Properties settings = readDefaults();
        // Step 2: read the player's settings file, if there is one.
        Properties overrides = new Properties();
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                overrides.load(reader);
            }
        }
        // Step 3: every name in the player's file must also exist in the defaults; otherwise it is a typo.
        for (String name : overrides.stringPropertyNames()) {
            if (settings.getProperty(name) == null) {
                throw new IllegalArgumentException(file + ": unknown setting '" + name + "'");
            }
        }
        // Step 4: the player's values replace the default values.
        settings.putAll(overrides);
        // Lets an error name the file the value came from: the player's file or the packaged defaults.
        Set<String> fromFile = overrides.stringPropertyNames();
        // Step 5: the card-files folder must exist.
        Path cardFilesFolder = Path.of(settings.getProperty("cardFiles.folder").trim());
        if (!Files.isDirectory(cardFilesFolder)) {
            throw new IllegalArgumentException(source(file, fromFile, "cardFiles.folder") + ": cardFiles.folder '"
                    + cardFilesFolder + "' is not an existing folder");
        }
        // Step 6: read the four numbers.
        int maxMoves = readInt(settings, file, fromFile, "solver.maxMoves");
        long maxMillis = readLong(settings, file, fromFile, "solver.maxMillis");
        int defaultGameCount = readInt(settings, file, fromFile, "simulation.defaultGameCount");
        int maxGameCount = readInt(settings, file, fromFile, "simulation.maxGameCount");
        // Step 7: build the settings; the record checks that every number is at least 1.
        try {
            return new GameConfig(maxMoves, maxMillis, defaultGameCount, maxGameCount, cardFilesFolder);
        } catch (IllegalArgumentException exception) {
            // The record names the setting; this adds the file it came from.
            String name = exception.getMessage().split(" ")[0];
            throw new IllegalArgumentException(source(file, fromFile, name) + ": " + exception.getMessage());
        }
    }

    /**
     * Reads default.properties from inside the program and returns every setting with its default value. Throws an
     * error if the file is missing.
     */
    private static Properties readDefaults() throws IOException {
        Properties defaults = new Properties();
        // Open the file that is packaged inside the program.
        try (InputStream stream = GameConfig.class.getResourceAsStream(DEFAULTS_RESOURCE)) {
            // null means the file is not in the program.
            if (stream == null) {
                throw new IllegalStateException("default.properties is missing from the program");
            }
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                defaults.load(reader);
            }
        }
        return defaults;
    }

    /** Reads a setting as a whole number and checks that it fits in an int before returning it. */
    private static int readInt(Properties settings, Path file, Set<String> fromFile, String name) {
        // Read it as a long first, then check it is not too big or too small for an int.
        long value = readLong(settings, file, fromFile, name);
        if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
            throw new IllegalArgumentException(source(file, fromFile, name) + ": " + name
                    + " is too large: " + value);
        }
        return (int) value;
    }

    /**
     * Reads a setting's text and converts it to a whole number. If the text is not a number, it throws an error that
     * names the setting and the file.
     */
    private static long readLong(Properties settings, Path file, Set<String> fromFile, String name) {
        String value = settings.getProperty(name).trim();
        // Convert the text to a number; if it is not a number, report the setting and the file.
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(source(file, fromFile, name) + ": " + name
                    + " must be a whole number, but is '" + value + "'");
        }
    }

    /**
     * Returns the name of the file a setting came from (the player's file or default.properties), so an error message
     * can say where to fix it.
     */
    private static String source(Path file, Set<String> fromFile, String name) {
        // If the player's file set this value, name that file; otherwise it came from the defaults.
        return fromFile.contains(name) ? file.toString() : "default.properties";
    }

    /** Throws an error if the value is less than 1. */
    private static void requirePositive(String name, long value) {
        if (value < 1) {
            throw new IllegalArgumentException(name + " must be at least 1, but is " + value);
        }
    }
}
