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
        public const int PluginMinorVersion = 2;
        public const int PluginRevision = 0;

        /// <summary>
        /// Will this plugin collect hardware information into structures defined under tcg-at-platformConfiguration-v2?
        /// </summary>
        /// <returns>If true, the ManifestV2 property is expected to contain hardware information after GatherHardwareInformation is run. If false, the ManifestV2 property is not expected to be initialized.</returns>
        bool CollectsV2HardwareInformation {
            get; 
        }

        ManifestV2 ManifestV2 {
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

        /// <summary>
        /// Can this plugin prove that components in its manifest possess a key?
        /// </summary>
        /// <returns>If true, GatherComponentEvidence returns evidence for one or more components.</returns>
        bool SupportsComponentEvidence => false;

        /// <summary>
        /// Ask components to prove possession of their keys by signing evidence that includes the verifier's nonce.
        /// Call after GatherHardwareIdentifiers so ComponentIndex refers to the collected ManifestV2.COMPONENTS.
        /// </summary>
        /// <param name="nonce">Nonce issued by the verifier. Some formats require a specific length.</param>
        /// <returns>Evidence for each component that could respond. Empty if none could.</returns>
        IReadOnlyList<ComponentEvidence> GatherComponentEvidence(byte[] nonce) => [];
    }
}
