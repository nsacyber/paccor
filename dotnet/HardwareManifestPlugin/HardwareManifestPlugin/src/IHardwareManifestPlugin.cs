using HardwareManifestProto;

namespace HardwareManifestPlugin {
    public interface IHardwareManifestPlugin {
        string Name {
            get;
        }
        string Description {
            get;
        }

        public const int PluginMajorVersion = 2;
        public const int PluginMinorVersion = 0;
        public const int PluginRevision = 1;

        /// <summary>
        /// Will this plugin collect hardware information into structures defined under tcg-at-platformConfiguration-v2?
        /// </summary>
        /// <returns>If true, the ManifestV2 property is expected to contain hardware information after GatherHardwareInformation is run. If false, the ManifestV2 property is not expected to be initialized.</returns>
        bool CollectsV2HardwareInformation {
            get; 
        }
        /// <summary>
        /// Will this plugin collect hardware information into structures defined under tcg-at-platformConfiguration-v3?
        /// </summary>
        /// <returns>If true, the ManifestV3 property is expected to contain hardware information after GatherHardwareInformation is run. If false, the ManifestV3 property is not expected to be initialized.</returns>
        bool CollectsV3HardwareInformation {
            get;
        }

        ManifestV2 ManifestV2 {
            get;
        }

        ManifestV3 ManifestV3 {
            get;
        }

        /// <summary>
        /// Kick off the hardware collection procedure within the Hardware Manifest Plugin.
        /// </summary>
        /// <returns>True if collection completed successfully. False otherwise.</returns>
        bool GatherHardwareIdentifiers();

        /// <summary>
        /// Kick off the hardware collection procedure within the Hardware Manifest Plugin.
        /// </summary>
        /// <param name="args">Arguments can be passed to the function.</param>
        /// <returns>True if collection completed successfully. False otherwise.</returns>
        bool GatherHardwareIdentifiers(string[] args);
    }
}
