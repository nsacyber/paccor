using HardwareManifestPlugin;
using HardwareManifestProto;
using OidsProto;
using PcieLib;

namespace Pcie;

public sealed class PcieHardwareManifestPlugin : HardwareManifestPluginBase {
    public static readonly string TraitDescription = "PCIe-based Component Class Registry";
    public static readonly string TraitDescriptionUri = "https://trustedcomputinggroup.org/wp-content/uploads/TCG_PCIe_Component_Class_Registry_v1_r18_pub10272021.pdf";
    public static readonly string PluginName = "paccor.pcie";
    public static readonly string PluginDescription = "Collect hardware identifiers according to the PCIe Component Class Registry.";

    public PcieHardwareManifestPlugin() {
        Name = PluginName;
        Description = PluginDescription;
        CollectsV2HardwareInformation = true;
        CollectsV3HardwareInformation = false;
    }

    public override bool GatherHardwareIdentifiers() {
        bool result = false;

        Pcie pcie = Pcie.GetPcie();

        if (!pcie.Valid) {
            return result;
        }

        AddComponentsToManifestV2(pcie.Devices, ManifestV2);
        ManifestV3 = HardwareManifestConverter.FromManifestV2(ManifestV2, TraitDescription, TraitDescriptionUri);

        result = pcie.Valid;

        return true;
    }

    public static void AddComponentsToManifestV2(IDictionary<int, IList<PcieDevice>> devices, ManifestV2 manifest) {
        string pcieRegistryOid = OidsUtils.Find(TCG_REGISTRY_COMPONENTCLASS_NODE.TcgRegistryComponentclassPcie).Oid;

        foreach (int type in devices.Keys) {
            foreach (PcieDevice device in devices[type]) {
                ComponentIdentifier component = new() {
                    COMPONENTCLASS = new ComponentClass {
                        COMPONENTCLASSREGISTRY = pcieRegistryOid,
                        COMPONENTCLASSVALUE = "00" + device.ClassCode.Hex
                    },
                    MANUFACTURER = device.VendorId.Hex + ":" + device.SubsystemVendorId.Hex + ":" + device.VpdMn,
                    MODEL = device.DeviceId.Hex + ":" + device.SubsystemId.Hex + ":" + device.VpdPn,
                    REVISION = device.RevisionId.ToString("X2")
                };
                // Don't add SERIAL if both values are empty.
                if (device.DeviceSerialNumber.Length > 0 || !string.IsNullOrEmpty(device.VpdSn)) {
                    component.SERIAL = Convert.ToHexString(device.DeviceSerialNumber) + ":" + device.VpdSn;
                }
                if (device.NetworkMac.Length != 0) {
                    string mac = Convert.ToHexString(device.NetworkMac).PadLeft(12, '0');
                    switch (device.ClassCode.Hex[..4]) {
                        case "0200": // Ethernet
                            component.ADDRESSES.Add(new Address() {
                                ETHERNETMAC = mac
                            });
                            break;
                        case "0D11": // Bluetooth
                            component.ADDRESSES.Add(new Address() {
                                BLUETOOTHMAC = mac
                            });
                            break;
                        case "0280": // Wireless
                            component.ADDRESSES.Add(new Address() {
                                WLANMAC = mac
                            });
                            break;
                    }
                }
                manifest.COMPONENTS.Add(component);
            }
        }
    }
}