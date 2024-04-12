using System.Reflection;
using System.Runtime.Loader;

namespace HardwareManifestPluginManager {
    class PluginLoadContext : AssemblyLoadContext {
        private readonly AssemblyDependencyResolver _resolver;

        public PluginLoadContext(string pluginPath) {
            _resolver = new AssemblyDependencyResolver(pluginPath);
        }

        protected override Assembly? Load(AssemblyName assemblyName) {
                string? assemblyPath = _resolver.ResolveAssemblyToPath(assemblyName);
                return assemblyPath != null ? LoadFromAssemblyPath(assemblyPath) : null;
        }

        protected override IntPtr LoadUnmanagedDll(string unmanagedDllName) {
            string? libraryPath = _resolver.ResolveUnmanagedDllToPath(unmanagedDllName);
            return libraryPath != null ? LoadUnmanagedDllFromPath(libraryPath) : IntPtr.Zero;
        }
    }
}
