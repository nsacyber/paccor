using HardwareManifestPlugin;
using org.iso.standards.swid;
using PlatformCertificateFromProto;
using System.Reflection;
using System.Runtime.InteropServices;

namespace paccor_scripts {
    public class PaccorComponentScriptsPlugin : IHardwareManifest {
        public static readonly string scripts = Path.GetFullPath(Path.Combine(Path.GetDirectoryName(typeof(PaccorComponentScriptsPlugin).Assembly.Location)!, "scripts"));
        public static readonly string linux_components = Path.GetFullPath(Path.Combine(scripts, "allcomponents.sh"));
        public static readonly string win_path = Path.GetFullPath(Path.Combine(scripts, "windows"));
        public static readonly string win_temp_output = Path.GetFullPath(Path.Combine(win_path, "out.json"));
        public static readonly string win_components = Path.GetFullPath(Path.Combine(win_path, "allcomponents.ps1"));
        public string Name {
            get; private set;
        }

        public string Description {
            get; private set;
        }
        public SoftwareIdentity? SWID {
            get; private set;
        }

        PlatformConfiguration IHardwareManifest.PlatformConfiguration => throw new NotImplementedException();

        PlatformConfigurationV2 IHardwareManifest.PlatformConfigurationV2 => throw new NotImplementedException();

        NameAttributes IHardwareManifest.NameAttributes => throw new NotImplementedException();


        public PaccorComponentScriptsPlugin() {
            Name = "paccor_scripts";
            Description = "paccor 1.1.4r6 component gathering scripts";
            SWID = null;
        }

        string IHardwareManifest.GatherHardwareManifestAsJsonString() {
            string json = "";
            if (RuntimeInformation.IsOSPlatform(OSPlatform.Windows)) {
                Task<Tuple<int, string, string>> task = Task.Run(RunWindows);
                Tuple<int, string, string> results = task.Result;
                if (task.Exception != null) {
                    throw task.Exception;
                }
                // The allcomponents powershell script writes output to a file to preserve binary data
                // that can get corrupted during redirection
                if (System.IO.File.Exists(win_temp_output)) {
                    json = System.IO.File.ReadAllText(win_temp_output);
                    //System.IO.File.Delete(win_temp_output);
                }
            } else if (RuntimeInformation.IsOSPlatform(OSPlatform.Linux)) {
                //await $"scripts/00magic.sh --param {arg}".Bash(this.logger);
                Task<Tuple<int, string, string>> task = Task.Run(RunLinux);
                Tuple<int, string, string> results = task.Result;
                if (task.Exception != null) {
                    throw task.Exception;
                }
                json = results.Item3;
            }
            return json;
        }

        private async Task<Tuple<int, string, string>> RunWindows() {
            return await Path.GetFullPath(win_components).ToString().Powershell(win_temp_output);
        }

        private async Task<Tuple<int, string, string>> RunLinux() {
            return await Path.GetFullPath(linux_components).ToString().Bash();
        }

        void IHardwareManifest.Configure(string[] args) {
            // does nothing
        }

        bool IHardwareManifest.WillContainPlatformConfigurationV1() {
            return false;
        }

        bool IHardwareManifest.WillContainPlatformConfigurationV2() {
            return false;
        }

        bool IHardwareManifest.WillContainNameAttributes() {
            return false;
        }
    }
}
