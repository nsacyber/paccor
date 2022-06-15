using HardwareManifestPlugin;
using org.iso.standards.swid;
using Serilog;
using System.Reflection;

namespace HardwareManifestPluginManager {
    public class HardwareManifestPluginManagerUtils {
        private static readonly ILogger log = Log.ForContext<HardwareManifestPluginManagerUtils>();

#pragma warning disable CS8604 // Possible null reference argument.
        public static readonly string pluginsPath = Path.Combine(Path.GetDirectoryName(Environment.ProcessPath), "plugins");
        public static readonly string trustPath = Path.Combine(Path.GetDirectoryName(Environment.ProcessPath), "trust");
#pragma warning restore CS8604 // Possible null reference argument.

        public static List<IHardwareManifest> LoadPlugins(List<string> names, bool swidEnforced) {
            string[] pluginDlls = System.IO.Directory.GetFiles(pluginsPath, "*.dll");
            List<IHardwareManifest> manifests = new();
            List<Tuple<string, string>> namesWithArgs = new();
            foreach(string dllPath in pluginDlls) {
                Assembly pluginAssembly = LoadAssemblyfromDll(dllPath);
                IHardwareManifest? manifest = GatherManifestIfNameSelected(pluginAssembly, names);
                if (manifest != null) {
                    bool trustManifest = !swidEnforced;
                    if (swidEnforced && manifest.ContainsSWID()) {
                        trustManifest = VerifySWIDWithEnvelopedSignature(manifest.SWID!);
                    }
                    if (trustManifest) {
                        manifests.Add(manifest);
                        log.Warning("Loading hardware manifest: " + manifest.Name);
                    }
                }
            }
            if (names.Count > 0) {
                log.Warning("There was no Hardware Manifest plugin with the name " + (names.Count > 1 ? "s" : "") + string.Join(",", names) + ".");
            }
            return manifests;
        }

        private static Assembly LoadAssemblyfromDll(string relativePath) {
            string fullPath = Path.GetFullPath(relativePath).Replace('\\', Path.DirectorySeparatorChar);

            log.Debug($"Seeing if this assembly implements IHardwareManifest: {fullPath}");
            PluginLoadContext loadContext = new(fullPath);
            return loadContext.LoadFromAssemblyName(new AssemblyName(Path.GetFileNameWithoutExtension(fullPath)));
        }

        private static IHardwareManifest? GatherManifestIfNameSelected(Assembly assembly, List<string> names) {
            foreach (Type type in assembly.GetTypes()) {
                if (typeof(IHardwareManifest).IsAssignableFrom(type)) {
                    if (Activator.CreateInstance(type) is IHardwareManifest result && names.Remove(result.Name)) {
                        log.Warning("Found " + result.Name + ".");
                        return result;
                    }
                }
            }
            log.Debug($"Can't find any type which implements IHardwareManifest in {assembly}.\n");
            return null;
        }

        private static bool VerifySWIDWithEnvelopedSignature(SoftwareIdentity SWID) {
            log.Warning("SWID Signature Method not yet tested");
            return true;
        }
    }
}
