package paccor.cli;

import paccor.exception.PaccorException;
import java.io.FileNotFoundException;
import java.nio.file.AccessDeniedException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import picocli.CommandLine;

public class RootCmdExceptionHandler implements CommandLine.IExecutionExceptionHandler {
    @Override
    public int handleExecutionException(Exception ex, CommandLine cmd, CommandLine.ParseResult pr) {
        CommonOptions opts = RootCmd.extractCommonOptions(cmd, pr);

        Throwable cause = (ex instanceof CommandLine.ExecutionException && ex.getCause() != null)
                ? ex.getCause()
                : ex;

        if (cause instanceof PaccorException pe) {
            if (opts != null) {
                LogBootstrap.init(opts.logLevel, opts.logFile, opts.quiet);
                opts.printError("Error: " + toUserMessage(pe));
            }
            Logger logger = Logger.getLogger(cmd.getCommandName());
            if (shouldLogThrowable(opts)) {
                logger.log(Level.SEVERE, "Command failed", pe);
            } else {
                logger.log(Level.SEVERE, "Command failed: {0}", toUserMessage(pe));
            }
            return pe.getExitCode().code();
        }

        if (opts != null) {
            LogBootstrap.init(opts.logLevel, opts.logFile, opts.quiet);
            opts.printError("Unexpected error: " + cause.getMessage());
        }
        Logger logger = Logger.getLogger(cmd.getCommandName());
        if (shouldLogThrowable(opts)) {
            logger.log(Level.SEVERE, "Unexpected error", cause);
        } else {
            logger.log(Level.SEVERE, "Unexpected error: {0}", cause.getMessage());
        }
        return ClientExitCodes.RUNTIME_ERROR.code();
    }

    private static boolean shouldLogThrowable(CommonOptions opts) {
        return opts != null && opts.logFile != null;
    }

    private static String toUserMessage(PaccorException pe) {
        Throwable root = rootCause(pe);
        if (isPermissionDenied(root)) {
            return "Permission denied while reading input file: " + root.getMessage();
        }
        return pe.getUserMessage();
    }

    private static Throwable rootCause(Throwable t) {
        Throwable curr = t;
        while (curr.getCause() != null && curr.getCause() != curr) {
            curr = curr.getCause();
        }
        return curr;
    }

    private static boolean isPermissionDenied(Throwable t) {
        if (t instanceof AccessDeniedException) {
            return true;
        }
        if (t instanceof FileNotFoundException && t.getMessage() != null) {
            return t.getMessage().toLowerCase(Locale.ROOT).contains("permission denied");
        }
        return false;
    }
}
