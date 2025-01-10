using CliLib;
using Smbios;
using System.Reflection;
using System.Runtime.InteropServices;

namespace SmbiosCli;
public class SmbiosCli {
    public static int Main(string[] args) {
        int returnCode = (int)ClientExitCodes.SUCCESS;

        Version? version = Assembly.GetExecutingAssembly().GetName().Version;

        if (RuntimeInformation.IsOSPlatform(OSPlatform.Linux)) {
            // Linux requires sudo
            int result = CliOptions.IsUserPrivileged();
            if (result != (int)ClientExitCodes.SUCCESS) {
                Console.WriteLine("SMBIOS data retrieval on Linux requires admin privileges. Please run as root.");
                return result;
            }
        }

        CliOptions? cli = CliOptions.ParseArguments(args);

        if (cli == null) {
            return (int)ClientExitCodes.CLI_PARSE_ERROR;
        }

        SmbiosHardwareManifestPlugin plugin = new();
        if (!plugin.GatherHardwareIdentifiers()) {
            Console.WriteLine("SMBIOS hardware information gathered was not valid.");
            return (int)ClientExitCodes.GATHER_HW_MANIFEST_FAIL;
        }

        // All smbios data should be validated at this point.
        if (cli.PrintV2 || (!cli.PrintV2 && !cli.PrintV3)) { // V2 should be printed by default not matter what
            Console.WriteLine(plugin.ManifestV2.ToString());
        }
        if (cli.PrintV3) {
            Console.WriteLine(plugin.ManifestV3.ToString());
        }

        return returnCode;
    }
}