using HardwareManifestPlugin;
using org.iso.standards.swid;
using Serilog;
using System.Reflection;

namespace HardwareManifestPluginManager {
    public class HardwareManifestPluginManager {
        public static readonly string pluginsPath = Path.Combine(Path.GetDirectoryName(Environment.ProcessPath), "plugins");
        public static readonly string trustPath = Path.Combine(Path.GetDirectoryName(Environment.ProcessPath), "trust");
        public HardwareManifestPluginManager(ILogger logger) {
            Log.Logger = logger;
        }
        
        public List<IHardwareManifest> LoadPlugins(List<string> names, bool swidEnforced) {
            string[] pluginDlls = System.IO.Directory.GetFiles(pluginsPath, "*.dll");
            List<IHardwareManifest> manifests = new();
            foreach(string dllPath in pluginDlls) {
                Assembly pluginAssembly = LoadAssemblyfromDll(dllPath);
                IHardwareManifest? manifest = GatherManifestIfNameSelected(pluginAssembly, names);
                if (manifest != null) {
                    bool trustManifest = !swidEnforced;
                    if (swidEnforced) {
                        trustManifest = VerifySWIDWithEnvelopedSignature(manifest.SWID);
                    }
                    if (trustManifest) {
                        manifests.Add(manifest);
                        Log.Warning("Loading hardware manifest: " + manifest.Name);
                    }
                }
            }
            if (names.Count > 0) {
                Log.Warning("There was no Hardware Manifest plugin with the name " + (names.Count > 1 ? "s" : "") + string.Join(",", names) + ".");
            }
            return manifests;
        }

        private static Assembly LoadAssemblyfromDll(string relativePath) {
            string fullPath = Path.GetFullPath(relativePath).Replace('\\', Path.DirectorySeparatorChar);

            Log.Warning($"Seeing if this assembly implements IHardwareManifest: {fullPath}");
            PluginLoadContext loadContext = new(fullPath);
            return loadContext.LoadFromAssemblyName(new AssemblyName(Path.GetFileNameWithoutExtension(fullPath)));
        }

        private static IHardwareManifest? GatherManifestIfNameSelected(Assembly assembly, List<string> names) {
            foreach (Type type in assembly.GetTypes()) {
                if (typeof(IHardwareManifest).IsAssignableFrom(type)) {
                    if (Activator.CreateInstance(type) is IHardwareManifest result && names.Remove(result.Name)) {
                        Log.Warning("Found " + result.Name + ".");
                        return result;
                    }
                }
            }
            Log.Warning($"Can't find any type which implements IHardwareManifest in {assembly}.\n");
            return null;
        }

        private static bool VerifySWIDWithEnvelopedSignature(SoftwareIdentity SWID) {
            Log.Warning("SWID Signature Method not yet tested");
            return false;
        }
    }
}
