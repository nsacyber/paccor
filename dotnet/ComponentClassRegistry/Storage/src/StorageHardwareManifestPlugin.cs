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
                    COMPONENTCLASSVALUE = "000000" + ATA_FormFactor(data.Capabilities.FormFactor)
                },
                MANUFACTURER = ATA_AOI(data.Capabilities.WWN),
                MODEL = ATA_String(data.Strings.MN),
                SERIAL = ATA_String(data.Strings.SN) + ":" + ATA_UNIQUEID(data.Capabilities.WWN),
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

    public static string ATA_FormFactor(byte[] ff) {
        byte[] ffClone = (byte[])ff.Clone();

        string hex = NVMe_Val(ffClone, false);

        string ffStr = "";
        
        // Least significant 4 bits of the 8 byte Form Factor field
        if (hex.Length == 16 && hex.StartsWith("80000000")) {
            ffStr = "0" + hex[-1];
        } else if (hex.Length == 16 && hex.EndsWith("00000080")) {
            ffStr = "0" + hex[1];
        }

        return ffStr;
    }

    public static string ATA_AOI(byte[] wwn) {
        byte[] wwnClone = (byte[])wwn.Clone();
        
        string hex = NVMe_Val(wwnClone, false);

        string wwnStr = "";

        // Word 108 bits 11:0 and word 109 bits 15:4 contain the OUI/AOI
        if (hex.Length == 32 && hex.StartsWith("8000000000000000")) {
            wwnStr = hex[17..23]; 
        } else if (hex.Length == 32 && hex.EndsWith("0000000000000080")) {
            wwnStr = hex[0..16];

            char[] wwnChars = wwnStr.ToCharArray();
            for (int i = 0; i < wwnChars.Length; i += 4) {
                wwnChars[i] = wwnStr[i + 2];
                wwnChars[i + 1] = wwnStr[i + 3];
                wwnChars[i + 2] = wwnStr[i];
                wwnChars[i + 3] = wwnStr[i + 1];
            }
            wwnStr = new string(wwnChars[1..7]);
        }
        Console.WriteLine("XYZ:" + hex);

        return wwnStr;
    }

    public static string ATA_UNIQUEID(byte[] wwn) {
        byte[] wwnClone = (byte[])wwn.Clone();
        
        string hex = NVMe_Val(wwnClone, false);

        string wwnStr = "";

        // Word 109 bits 3:0, word 110:111 contain the UNIQUE ID
        if (hex.Length == 32 && hex.StartsWith("8000000000000000")) {
            wwnStr = hex[23..32]; 
        } else if (hex.Length == 32 && hex.EndsWith("0000000000000080")) {
            wwnStr = hex[0..16];

            char[] wwnChars = wwnStr.ToCharArray();
            for (int i = 0; i < wwnChars.Length; i += 4) {
                wwnChars[i] = wwnStr[i + 2];
                wwnChars[i + 1] = wwnStr[i + 3];
                wwnChars[i + 2] = wwnStr[i];
                wwnChars[i + 3] = wwnStr[i + 1];
            }
            wwnStr = new string(wwnChars[7..16]);
        }
        Console.WriteLine("XYZ:" + hex);

        return wwnStr;
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
