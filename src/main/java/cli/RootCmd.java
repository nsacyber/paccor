package cli;

import java.util.logging.Level;
import java.util.logging.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine;

@Command(name = "paccor", description = "Platform Certificate Creator CLI", mixinStandardHelpOptions = true,
    subcommands = {
        CertGenCmd.class,
        AssembleCmd.class,
        ValidateCmd.class,
        ViewCmd.class
    }
)
public class RootCmd implements Runnable, HasCommonOptions {
    @Mixin
    private CommonOptions common;

    @Override
    public CommonOptions commonOptions() {
        return common;
    }

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }

    public static void main(String[] args) {
        RootCmd rootCmd = new RootCmd();
        CommandLine cmd = new CommandLine(rootCmd);
        cmd.setExecutionStrategy(new RootCmdStrategy(new CommandLine.RunLast()));
        cmd.setExecutionExceptionHandler(new RootCmdExceptionHandler());
        System.exit(cmd.execute(args));
    }

    public static final CommonOptions extractCommonOptions(CommandLine.ParseResult pr) {
        if (pr != null && !pr.asCommandLineList().isEmpty()) {
            Object leaf = pr.asCommandLineList().get(pr.asCommandLineList().size() - 1).getCommand();
            if (leaf instanceof HasCommonOptions h) {
                return h.commonOptions();
            }
        }
        return null;
    }

    public static final CommonOptions extractCommonOptions(CommandLine cmd, CommandLine.ParseResult pr) {
        Object commandObj = cmd.getCommand();
        if (commandObj instanceof HasCommonOptions h) {
            return h.commonOptions();
        }
        return extractCommonOptions(pr);
    }
}
