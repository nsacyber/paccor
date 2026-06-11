package paccor.cli;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Configures Java Util Logging for the application by setting levels and handlers for console and optional rotating file output.
 */
public final class LogBootstrap {
    /**
     * Log levels.
     */
    public enum LogLevel {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR;
    }

    private LogBootstrap() {}

    /**
     * Initializes the root logger with the requested level, optional rotating file handler, and optional console output.
     *
     * @param levelString Log level name (trace, debug, info, warn, error). Defaults to info if null or unknown.
     * @param logFile Base file path for rotating logs. If null, file logging is disabled.
     * @param quiet If true, suppresses console logging.
     */
    public static void init(String levelString, File logFile, boolean quiet) {
        Logger root = LogManager.getLogManager().getLogger("");
        for (Handler h : root.getHandlers()) {
            root.removeHandler(h);
        }
        Level level = toLevel(levelString);
        root.setLevel(level);
        if (!quiet) {
            ConsoleHandler ch = new ConsoleHandler();
            ch.setLevel(level);
            ch.setFormatter(new SimpleFormatter());
            root.addHandler(ch);
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

    private static Level toLevel(String s) {
        String v = Objects.toString(s, "INFO").toUpperCase(Locale.ROOT).trim();
        return toLevel(LogLevel.valueOf(v));
    }

    private static Level toLevel(LogLevel l) {
        return switch (l) {
            case TRACE -> Level.FINER;
            case DEBUG -> Level.FINE;
            case WARN -> Level.WARNING;
            case ERROR -> Level.SEVERE;
            default -> Level.INFO;
        };
    }
}
