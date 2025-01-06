using HardwareManifestPlugin;
using HardwareManifestProto;
using OidsProto;
using StorageLib;
using StorageNvme;
using System.Data.Common;
using static System.Runtime.InteropServices.JavaScript.JSType;

namespace Storage;
public class StorageHardwareManifestPlugin : HardwareManifestPluginBase {
    public static readonly string TraitDescription = "Storage Component Class Registry";
    public static readonly string TraitDescriptionUri = "https://trustedcomputinggroup.org/wp-content/uploads/Storage-Component-Class-Registry-Version-1.0-Revision-22_pub.pdf";
    public static readonly string PluginName = "paccor.componentclassregistry.storage";
    public static readonly string PluginDescription = "Collect hardware identifiers according to the Storage Component Class Registry.";

    public StorageHardwareManifestPlugin() {
        Name = PluginName;
        Description = PluginDescription;
        CollectsV2HardwareInformation = true;
        CollectsV3HardwareInformation = false;
    }

    public override bool GatherHardwareIdentifiers() {
        bool nvmeValid = StorageNvmeHelpers.CollectNvmeData(out List<StorageNvmeData> nvmeData);
       
        if (!nvmeValid) {
            return false;
        }

        List<byte> ataData = new();
        List<byte> scsiData = new();
        AddComponentsToManifestV2(nvmeData, ataData, scsiData, ManifestV2);
        ManifestV3 = HardwareManifestConverter.FromManifestV2(ManifestV2, TraitDescription, TraitDescriptionUri);

        return true;
    }

    public static void AddComponentsToManifestV2(List<StorageNvmeData> nvmeData, List<byte> ataData, List<byte> scsiData, ManifestV2 manifest) {
        string storageRegistryOid = OidsUtils.Find(TCG_REGISTRY_COMPONENTCLASS_NODE.TcgRegistryComponentclassDisk).Oid;
        foreach (StorageNvmeData data in nvmeData) {
            ComponentIdentifier component = new() {
                COMPONENTCLASS = new ComponentClass {
                    COMPONENTCLASSREGISTRY = storageRegistryOid,
                    COMPONENTCLASSVALUE = "02" + data.ClassCode.Hex
                },
                MANUFACTURER = NVMe_Val(data.NvmeCtrl.VID, true) + ":" + NVMe_Val(data.NvmeCtrl.SSVID, true) + ":" + NVMe_OUI(data.NvmeCtrl.IEEE),
                MODEL = NVMe_String(data.NvmeCtrl.MN),
                SERIAL = NVMe_Val(data.NvmeCtrl.FGUID, false) + ":" + NVMe_String(data.NvmeCtrl.SN) + ":" + NVMe_String(data.NvmeCtrl.SUBNQN),
                REVISION = NVMe_Val(data.NvmeCtrl.VER, true) + ":" + NVMe_String(data.NvmeCtrl.FR)
            };
            manifest.COMPONENTS.Add(component);
        }
    }

    public static string NVMe_Val(byte val) {
        return NVMe_Val([val], false);
    }

    public static string NVMe_Val(byte[] val, bool littleEndianField) {
        if (littleEndianField) {
            Array.Reverse(val);
        }
        return Convert.ToHexString(val).PadLeft(val.Length / 2 + val.Length % 2, '0');
    }

    public static string NVMe_OUI(byte[] val) {
        // These fields are specified to be little endian
        return NVMe_Val(val, true);
    }

    public static string NVMe_String(byte[] val) {
        return System.Text.Encoding.ASCII.GetString(val).TrimEnd(' ', '\0');
    }
}
