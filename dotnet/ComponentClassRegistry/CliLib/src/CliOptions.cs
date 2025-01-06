using CommandLine;
using System.Data;
using System.Runtime.InteropServices;
using System.Security.Principal;

namespace CliLib;
public class CliOptions {
    [Option("print-v2", SetName = "print", Default = false, HelpText = "Print hardware manifest data in JSON format according to Platform Certificate v1.1.")]
    public bool PrintV2 {
        get; set;
    }
    [Option("print-v3", SetName = "print", Default = false, HelpText = "Print hardware manifest data in JSON format according to Platform Certificate v2.0 with component identifiers wrapped in ComponentIdentifierV11Traits.")]
    public bool PrintV3 {
        get; set;
    }

    private static void HandleParseError(IEnumerable<Error> errs) {
        //handle errors
        Console.WriteLine("There was a command line error: " + errs.ToString());
    }

    public static CliOptions? ParseArguments(string[] args) {
        CliOptions? cli = new();
        ParserResult<CliOptions> cliParseResult =
            CommandLine.Parser.Default.ParseArguments<CliOptions>(args)
                .WithParsed(parsed => cli = parsed)
                .WithNotParsed(HandleParseError);
        if (cliParseResult.Tag == ParserResultType.NotParsed) {
            Console.WriteLine("Could not parse command line arguments.");
            cli = null;
        }

        return cli;
    }

    public static string[] SplitArgs(string argString) {
        return argString.SplitArgs(true);
    }

    public static int IsUserPrivileged() {
        bool priv = false;

        if (RuntimeInformation.IsOSPlatform(OSPlatform.Linux)) {
            if (LinuxImports.geteuid() == 0) {
                priv = true;
            }
        } else if (RuntimeInformation.IsOSPlatform(OSPlatform.Windows)) {
            if (new WindowsPrincipal(WindowsIdentity.GetCurrent()).IsInRole(WindowsBuiltInRole.Administrator)) {
                priv = true;
            }
        }

        int result = priv ? (int)ClientExitCodes.SUCCESS : (int)ClientExitCodes.NOT_PRIVILEGED;

        return result;
    }
}
