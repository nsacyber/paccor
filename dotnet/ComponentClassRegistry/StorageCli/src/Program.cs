﻿using CliLib;
using Storage;
using System.Runtime.InteropServices;

namespace StorageCli;
public class StorageCli {
    public static int Main(string[] args) {
        int returnCode = (int)ClientExitCodes.SUCCESS;

        if (RuntimeInformation.IsOSPlatform(OSPlatform.Linux) || RuntimeInformation.IsOSPlatform(OSPlatform.Windows)) {
            // Both Linux and Windows require sudo
            int result = CliOptions.IsUserPrivileged();
            if (result != (int)ClientExitCodes.SUCCESS) {
                Console.WriteLine("Storage data retrieval requires admin privileges. Please run as root.");
                return result;
            }
        }

        CliOptions? cli = CliOptions.ParseArguments(args);

        if (cli == null) {
            return (int)ClientExitCodes.CLI_PARSE_ERROR;
        }

        StorageHardwareManifestPlugin plugin = new();
        if (!plugin.GatherHardwareIdentifiers()) {
            Console.WriteLine("Storage hardware information gathered was not valid.");
            return (int)ClientExitCodes.GATHER_HW_MANIFEST_FAIL;
        }

        // All storage data should be validated at this point.
        if (cli.PrintV2 || (!cli.PrintV2 && !cli.PrintV3)) {
            // V2 should be printed by default not matter what
            Console.WriteLine(cli.ComponentsOnly
                ? plugin.ManifestV2.COMPONENTS.ToString().Trim('[', ']', ' ')
                : plugin.ManifestV2.ToString());
        }
        if (cli.PrintV3) {
            Console.WriteLine(cli.ComponentsOnly
                ? plugin.ManifestV3.PlatformConfiguration.PlatformComponents.ToString().Trim('[', ']', ' ')
                : plugin.ManifestV3.ToString());
        }

        return returnCode;
    }
}