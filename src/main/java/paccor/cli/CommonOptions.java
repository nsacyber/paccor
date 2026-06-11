package paccor.cli;

import java.io.File;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Common options for commands.
 */
@Command
public class CommonOptions {
    @Option(names = "--log-level", defaultValue = "info")
    String logLevel;
    @Option(names = "--log-file")
    File logFile;
    @Option(names = { "-q", "--quiet" })
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
