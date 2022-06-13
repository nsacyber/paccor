using PlatformCertificateFromProto;
using org.iso.standards.swid;

namespace HardwareManifestPlugin {
    public interface IHardwareManifest {
        string Name {
            get;
        }
        string Description {
            get;
        }
        PlatformConfiguration PlatformConfiguration {
            get;
        }
        PlatformConfigurationV2 PlatformConfigurationV2 {
            get;
        }
        
        NameAttributes NameAttributes {
            get;
        }

        SoftwareIdentity SWID {
            get;
        }

        /// <summary>
        /// Pass arguments to the Hardware Manifest Plugin, if needed.
        /// </summary>
        /// <param name="args">Command-line style arguments to be given to the plugin prior to hardware identifier collection.</param>
        void Configure(string[] args);
        /// <summary>
        /// Will this plugin collect hardware information into structures defined under tcg-at-platformConfiguration-v1?
        /// </summary>
        /// <returns>If true, the PlatformConfiguration property is expected to contain hardware information after GatherHardwareInformation is run. If false, the PlatformConfiguration property is expected to be null.</returns>
        bool WillContainPlatformConfigurationV1();
        /// <summary>
        /// Will this plugin collect hardware information into structures defined under tcg-at-platformConfiguration-v2?
        /// </summary>
        /// <returns>If true, the PlatformConfigurationV2 property is expected to contain hardware information after GatherHardwareInformation is run. If false, the PlatformConfigurationV2 property is expected to be null.</returns>
        bool WillContainPlatformConfigurationV2();
        /// <summary>
        /// Will this plugin collect hardware information into structures intended for the subject alternative name?
        /// </summary>
        /// <returns>If true, the NameAttributes property is expected to contain at least one hardware identifier intended for the subject alternative name after GatherHardwareInformation is run. Individually check each of the sub-properties of NameAttributes. If false, the NameAttributes property is expected to be null.</returns>
        bool WillContainNameAttributes();
        /// <summary>
        /// Was this plugin distributed with a SWID file?
        /// </summary>
        /// <returns>If true, the SWID property is expected to contain a complete SoftwareIdentity structure. The swidtag must provide integrity over That structure may contain a Signature. If false, the SWID property is expected to be empty.</returns>
        bool ContainsSWID() {
            return SWID != null;
        }

        string GatherHardwareManifestAsJsonString();

        /// <summary>
        /// Kick off the hardware collection procedure within the Hardware Manifest Plugin.
        /// </summary>
        /// <returns>True if hardware collection by the plugin was successful. False otherwise.</returns>
        bool GatherHardwareInformation();
        /// <summary>
        /// All PlatformConfiguration, PlatformConfigurationV2, and Name Attributes will be output as a JSON string.
        /// </summary>
        /// <returns></returns>
        string OutputAsJsonString();
        
        
    }
}
