package paccor.cli;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Common options for commands.
 */
@Command
public class CommonOptions {
    private static final Logger LOGGER = Logger.getLogger(CommonOptions.class.getName());

    @Option(names = CliOptionNames.LOG_LEVEL_LONG, description = "Options: " + LogBootstrap.AVAILABLE_LEVELS, defaultValue = "INFO")
    String logLevel;
    @Option(names = CliOptionNames.LOG_FILE_LONG, description = "Path to save rotating logs. If null or omitted, file logging is disabled.")
    File logFile;
    @Option(names = {CliOptionNames.QUIET_SHORT, CliOptionNames.QUIET_LONG}, description = "Suppress console logging.")
    boolean quiet;

    /**
     * Logs an informational message. Console visibility is controlled by LogBootstrap.
     * @param msg Message to log.
     */
    public void printInfo(String msg){
        LOGGER.log(Level.INFO, msg);
    }

    /**
     * Logs an error message. Console visibility is controlled by LogBootstrap.
     * @param msg Message to log.
     */
    public void printError(String msg){
        LOGGER.log(Level.SEVERE, msg);
    }
}
