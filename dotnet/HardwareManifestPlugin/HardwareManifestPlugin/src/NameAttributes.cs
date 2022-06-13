using PlatformCertificateFromProto;

namespace HardwareManifestPlugin {
    public class NameAttributes {
        public PlatformManufacturerStr? PlatformManufacturerStr {
            get; private set;
        }
        public PlatformModel? PlatformModel {
            get; private set;
        }
        public PlatformSerial? PlatformSerial {
            get; private set;
        }
        public PlatformVersion? PlatformVersion {
            get; private set;
        }
        public PlatformManufacturerId? PlatformManufacturerId {
            get; private set;
        }
        public NameAttributes(PlatformManufacturerStr ven, PlatformModel mn, PlatformSerial sn, PlatformVersion ver, PlatformManufacturerId venId) {
            PlatformManufacturerStr = ven;
            PlatformModel = mn;
            PlatformSerial = sn;
            PlatformVersion = ver;
            PlatformManufacturerId = venId;
        }
    }
}
