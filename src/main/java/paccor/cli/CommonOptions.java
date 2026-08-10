package paccor.cli;

import java.io.File;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Common options for commands.
 */
@Command
public class CommonOptions {
    @Option(names = CliOptionNames.LOG_LEVEL_LONG, defaultValue = "info")
    String logLevel;
    @Option(names = CliOptionNames.LOG_FILE_LONG)
    File logFile;
    @Option(names = {CliOptionNames.QUIET_SHORT, CliOptionNames.QUIET_LONG})
    boolean quiet;

    /**
     * Print info message if not in quiet mode.
     * @param msg Message to print.
     */
    public void printInfo(String msg){
        if (!quiet) System.out.println(msg);
    }

    /**
     * Print an error message if not in quiet mode.
     * @param msg Message to print.
     */
    public void printError(String msg){
        if (!quiet) System.err.println(msg);
    }
}
