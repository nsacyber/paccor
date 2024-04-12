using HardwareManifestProto;

namespace HardwareManifestPlugin {
    public abstract class HardwareManifest : IHardwareManifest {
        public string Name {
            get;
            protected set;
        } = "";

        public string Description {
            get;
            protected set;
        } = "";

        public bool CollectsV2HardwareInformation {
            get;
            protected set;
        } = false;

        public bool CollectsV3HardwareInformation {
            get;
            protected set;
        } = false;

        public ManifestV2 ManifestV2 {
            get;
            protected set;
        } = new();

        public ManifestV3 ManifestV3 {
            get;
            protected set;
        } = new();

        public abstract bool GatherHardwareIdentifiers();

        public bool GatherHardwareIdentifiers(string[] args) {
            return GatherHardwareIdentifiers();
        }
    }
}
