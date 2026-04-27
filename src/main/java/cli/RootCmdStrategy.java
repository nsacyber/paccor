package cli;

import lombok.AllArgsConstructor;
import picocli.CommandLine;

@AllArgsConstructor
public final class RootCmdStrategy implements CommandLine.IExecutionStrategy {
    private final CommandLine.IExecutionStrategy delegate;

    @Override
    public int execute(CommandLine.ParseResult pr) {
        CommonOptions opts = RootCmd.extractCommonOptions(pr);

        if (opts != null) {
            LogBootstrap.init(opts.logLevel, opts.logFile, opts.quiet);
        }

        return delegate.execute(pr);
    }
}
