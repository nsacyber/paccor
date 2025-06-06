﻿using HardwareManifestPlugin;
using HardwareManifestProto;
using System.Runtime.InteropServices;

namespace paccor_scripts {
    public sealed class PaccorComponentScriptsPlugin : HardwareManifestPluginBase {
        public static readonly string Scripts = Path.GetFullPath(Path.Combine(Path.GetDirectoryName(typeof(PaccorComponentScriptsPlugin).Assembly.Location)!, "scripts"));
        public static readonly string LinuxComponents = Path.GetFullPath(Path.Combine(Scripts, "tcg_ccr.sh"));
        public static readonly string WinPath = Path.GetFullPath(Path.Combine(Scripts, "windows"));
        public static readonly string WinTempOutput = Path.GetFullPath(Path.Combine(WinPath, "out.json"));
        public static readonly string WinComponents = Path.GetFullPath(Path.Combine(WinPath, "tcg_ccr.ps1"));

        public static readonly string TraitDescription = "paccor component gathering scripts";
        public static readonly string TraitDescriptionUri = "https://github.com/nsacyber/paccor/scripts";

        public PaccorComponentScriptsPlugin() {
            Name = "paccor_scripts";
            Description = "paccor component gathering scripts";
            CollectsV2HardwareInformation = true;
            CollectsV3HardwareInformation = false;
        }

        public override bool GatherHardwareIdentifiers() {
            bool result = false;
            string json = "";
            if (RuntimeInformation.IsOSPlatform(OSPlatform.Windows)) {
                Task<Tuple<int, string, string>> task = Task.Run(RunWindows);
                Tuple<int, string, string> results = task.Result;
                if (task.Exception != null) {
                    throw task.Exception;
                }
                // The allcomponents powershell script writes output to a file to preserve binary data
                // that can get corrupted during redirection
                if (System.IO.File.Exists(WinTempOutput)) {
                    json = System.IO.File.ReadAllText(WinTempOutput);
                }
            } else if (RuntimeInformation.IsOSPlatform(OSPlatform.Linux)) {
                Task<Tuple<int, string, string>> task = Task.Run(RunLinux);
                Tuple<int, string, string> results = task.Result;
                if (task.Exception != null) {
                    throw task.Exception;
                }
                json = results.Item3;
            }

            if (!string.IsNullOrWhiteSpace(json)) {
                ManifestV2 = ManifestV2.Parser.WithDiscardUnknownFields(true).ParseJson(json);
                ManifestV3 = HardwareManifestConverter.FromManifestV2(ManifestV2, TraitDescription, TraitDescriptionUri);
                result = true;
            }
            return result;
        }

        private async Task<Tuple<int, string, string>> RunWindows() {
            return await Path.GetFullPath(WinComponents).ToString().Powershell(WinComponents);
        }

        private async Task<Tuple<int, string, string>> RunLinux() {
            return await Path.GetFullPath(LinuxComponents).ToString().Bash();
        }
    }
}
