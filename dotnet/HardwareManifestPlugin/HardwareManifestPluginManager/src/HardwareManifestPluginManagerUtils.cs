using HardwareManifestPlugin;
using Serilog;
using System.Reflection;
using System.Xml;

namespace HardwareManifestPluginManager {
    public class HardwareManifestPluginManagerUtils {
        private static readonly ILogger Log = Serilog.Log.ForContext<HardwareManifestPluginManagerUtils>();

#pragma warning disable CS8604 // Possible null reference argument.
        public static readonly string PluginsPath = Path.Combine(Path.GetDirectoryName(Environment.ProcessPath), "plugins");
        public static readonly string TrustPath = Path.Combine(Path.GetDirectoryName(Environment.ProcessPath), "trust");
#pragma warning restore CS8604 // Possible null reference argument.

        public static List<IHardwareManifest> LoadPlugins(List<string> names, bool sbomExpected) {
            string[] pluginDlls = System.IO.Directory.GetFiles(PluginsPath, "*.dll");
            List<IHardwareManifest> manifests = new();
            List<Tuple<string, string>> namesWithArgs = new();
            foreach(string dllPath in pluginDlls) {
                Assembly pluginAssembly = LoadAssemblyFromDll(dllPath);
                IHardwareManifest? manifest = GatherManifestIfNameSelected(pluginAssembly, names);
                if (manifest != null) {
                    bool trustManifest = !sbomExpected;
                    if (sbomExpected) {
                        trustManifest = VerifySbom(manifest.Name);
                    }
                    if (trustManifest) {
                        manifests.Add(manifest);
                        Log.Debug("Loading hardware manifest: " + manifest.Name);
                    }
                }
            }
            if (names.Count > 0) {
                Log.Debug("There was no Hardware Manifest plugin with the name " + (names.Count > 1 ? "s" : "") + string.Join(",", names) + ".");
            }
            return manifests;
        }

        private static Assembly LoadAssemblyFromDll(string relativePath) {
            string fullPath = Path.GetFullPath(relativePath).Replace('\\', Path.DirectorySeparatorChar);

            Log.Debug($"Seeing if this assembly implements IHardwareManifest: {fullPath}");
            PluginLoadContext loadContext = new(fullPath);
            return loadContext.LoadFromAssemblyName(new AssemblyName(Path.GetFileNameWithoutExtension(fullPath)));
        }

        private static IHardwareManifest? GatherManifestIfNameSelected(Assembly assembly, List<string> names) {
            foreach (Type type in assembly.GetTypes()) {
                if (typeof(IHardwareManifest).IsAssignableFrom(type)) {
                    if (Activator.CreateInstance(type) is IHardwareManifest result && names.Remove(result.Name)) {
                        Log.Debug("Found " + result.Name + ".");
                        return result;
                    }
                }
            }
            Log.Debug($"Can't find any type which implements IHardwareManifest in {assembly}.\n");
            return null;
        }

        private static bool VerifySbom(string manifestName) {
            Log.Debug("Sbom verification method not yet tested");
            return true;
        }
    }
}
