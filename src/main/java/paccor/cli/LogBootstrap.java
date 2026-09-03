package paccor.cli;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

/**
 * Configures Java Util Logging for the application by setting levels and handlers for console and optional rotating file output.
 */
public final class LogBootstrap {
    public static final String AVAILABLE_LEVELS =
            "ALL, FINEST, FINER, FINE, CONFIG, INFO, WARNING, SEVERE, OFF";

    private LogBootstrap() {}

    /**
     * Initializes the root logger with the requested level, optional rotating file handler, and optional console output.
     *
     * @param levelString JUL level name (FINE, INFO, WARNING, ...). Defaults to INFO.
     * @param logFile Base file path for rotating logs. If null, file logging is disabled.
     * @param quiet If true, suppresses console logging.
     */
    public static void init(String levelString, File logFile, boolean quiet) {
        Logger root = LogManager.getLogManager().getLogger("");
        for (Handler h : root.getHandlers()) {
            root.removeHandler(h);
            h.close();
        }
        Level level = parseLevel(levelString);
        root.setLevel(level);
        if (!quiet) {
            SimpleFormatter formatter = new SimpleFormatter();
            StreamHandler stdout = new StreamHandler(System.out, formatter) {
                @Override
                public void publish(LogRecord record) {
                    if (isLoggable(record)) {
                        super.publish(record);
                        flush();
                    }
                }
            };
            stdout.setLevel(level);
            stdout.setFilter(record -> record.getLevel().intValue() < Level.WARNING.intValue());
            root.addHandler(stdout);

            ConsoleHandler stderr = new ConsoleHandler();
            stderr.setLevel(level);
            stderr.setFilter(record -> record.getLevel().intValue() >= Level.WARNING.intValue());
            stderr.setFormatter(formatter);
            root.addHandler(stderr);
        }
        if (logFile != null) {
            try {
                String pattern = logFile.getAbsolutePath() + ".%g";
                int limit = 10 * 1024 * 1024;
                int fileCount = 5;
                boolean append = true;
                FileHandler fh = new FileHandler(pattern, limit, fileCount, append);
                fh.setLevel(level);
                fh.setFormatter(new SimpleFormatter());
                root.addHandler(fh);
            } catch (IOException ioe) {
                System.err.println("Warning: cannot open log file " + logFile + ": " + ioe.getMessage());
            }
        }
    }

    public static Level parseLevel(String s) {
        return Optional.ofNullable(s)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> value.toUpperCase(Locale.ROOT))
                .map(Level::parse)
                .orElse(Level.INFO);
    }
}
