using HardwareManifestPlugin;
using HardwareManifestProto;
using OidsProto;
using StorageAta;
using StorageLib;
using StorageNvme;
using System.Data.Common;
using static System.Runtime.InteropServices.JavaScript.JSType;

namespace Storage;
public class StorageHardwareManifestPlugin : HardwareManifestPluginBase {
    public static readonly string TraitDescription = "Storage Component Class Registry";
    public static readonly string TraitDescriptionUri = "https://trustedcomputinggroup.org/wp-content/uploads/Storage-Component-Class-Registry-Version-1.0-Revision-22_pub.pdf";
    public static readonly string PluginName = "paccor.storage";
    public static readonly string PluginDescription = "Collect hardware identifiers according to the Storage Component Class Registry.";

    public StorageHardwareManifestPlugin() {
        Name = PluginName;
        Description = PluginDescription;
        CollectsV2HardwareInformation = true;
        CollectsV3HardwareInformation = false;
    }

    public override bool GatherHardwareIdentifiers() {
        bool nvmeValid = StorageNvmeHelpers.CollectNvmeData(out List<StorageNvmeData> nvmeData);
        bool ataValid = StorageAtaHelpers.CollectAtaData(out List<StorageAtaData> ataData);
       
        if (!nvmeValid || !ataValid) {
            return false;
        }

        List<byte> scsiData = new();
        AddComponentsToManifestV2(nvmeData, ataData, scsiData, ManifestV2);
        ManifestV3 = HardwareManifestConverter.FromManifestV2(ManifestV2, TraitDescription, TraitDescriptionUri);

        return true;
    }

    public static void AddComponentsToManifestV2(List<StorageNvmeData> nvmeData, List<StorageAtaData> ataData, List<byte> scsiData, ManifestV2 manifest) {
        string storageRegistryOid = OidsUtils.Find(TCG_REGISTRY_COMPONENTCLASS_NODE.TcgRegistryComponentclassDisk).Oid;
        foreach (StorageAtaData data in ataData) {
            ComponentIdentifier component = new() {
                COMPONENTCLASS = new ComponentClass {
                    COMPONENTCLASSREGISTRY = storageRegistryOid,
                    COMPONENTCLASSVALUE = "000000" + ATA_FormFactor(data.Capabilities.FormFactor, true)
                },
                MANUFACTURER = ATA_AOI(data.Capabilities.WWN, true),
                MODEL = ATA_String(data.Strings.MN),
                SERIAL = ATA_String(data.Strings.SN) + ":" + ATA_UNIQUEID(data.Capabilities.WWN, true),
                REVISION = ATA_String(data.Strings.FR)
            };
            manifest.COMPONENTS.Add(component);
        }
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

    public static string ATA_FormFactor(byte[] val, bool littleEndianField) {
        byte[] valClone = (byte[])val.Clone();

        if (littleEndianField) {
            Array.Reverse(valClone);
        }

        string ff = "";

        if (valClone.Length == 8 && valClone[0] == 0x80) {
            ff = NVMe_Val(valClone[7]);
        }

        return ff;
    }

    public static string ATA_AOI(byte[] wwn, bool littleEndianField) {
        byte[] wwnClone = (byte[])wwn.Clone();

        if (littleEndianField) {
            Array.Reverse(wwnClone);
        }

        string result = "";
        
        if (wwnClone.Length == 16 && wwnClone[0] == 0x80) {
            string hex = NVMe_Val(wwnClone, false);
            result = hex[17..23]; // Word 108 bits 11:0 and word 109 bits 15:4 contain the OUI/AOI
        }

        return result;
    }

    public static string ATA_UNIQUEID(byte[] wwn, bool littleEndianField) {
        byte[] wwnClone = (byte[])wwn.Clone();

        if (littleEndianField) {
            Array.Reverse(wwnClone);
        }

        string result = "";
        
        if (wwn.Length == 16 && wwnClone[0] == 0x80) {
            string hex = NVMe_Val(wwnClone, false);
            result = hex[23..32]; // Word 109 bits 3:0, word 110:111 contain the UNIQUE ID
        }

        return result;
    }

    public static string ATA_String(byte[] val) {
        byte[] valClone = (byte[])val.Clone();

        int len = valClone.Length - valClone.Length % 2;
        for (int i = 0; i < len; i+=2) {
            byte swap = valClone[i];
            valClone[i] = valClone[i+1];
            valClone[i+1] = swap;
        }
        return System.Text.Encoding.ASCII.GetString(valClone).Trim(' ', '\0');
    }

    public static string NVMe_Val(byte val) {
        byte valClone = val;
        return NVMe_Val([valClone], false);
    }

    public static string NVMe_Val(byte[] val, bool littleEndianField) {
        byte[] valClone = (byte[])val.Clone();

        if (littleEndianField) {
            Array.Reverse(valClone);
        }
        return Convert.ToHexString(valClone).PadLeft(valClone.Length / 2 + valClone.Length % 2, '0');
    }

    public static string NVMe_OUI(byte[] val) {
        byte[] valClone = (byte[])val.Clone();

        // These fields are specified to be little endian
        return NVMe_Val(valClone, true);
    }

    public static string NVMe_String(byte[] val) {
        byte[] valClone = (byte[])val.Clone();
        return System.Text.Encoding.ASCII.GetString(valClone).TrimEnd(' ', '\0');
    }
}
