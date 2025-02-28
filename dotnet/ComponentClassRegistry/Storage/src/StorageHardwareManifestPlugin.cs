using HardwareManifestPlugin;
using HardwareManifestProto;
using OidsProto;
using StorageAta;
using StorageLib;
using StorageNvme;
using StorageScsi;
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
        bool scsiValid = StorageScsiHelpers.CollectScsiData(out List<StorageScsiData> scsiData);
       
        if (!nvmeValid || !ataValid || !scsiValid) {
            return false;
        }

        AddComponentsToManifestV2(nvmeData, ataData, scsiData, ManifestV2);
        ManifestV3 = HardwareManifestConverter.FromManifestV2(ManifestV2, TraitDescription, TraitDescriptionUri);

        return true;
    }

    public static void AddComponentsToManifestV2(List<StorageNvmeData> nvmeData, List<StorageAtaData> ataData, List<StorageScsiData> scsiData, ManifestV2 manifest) {
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

        foreach (StorageScsiData data in scsiData) {
            ComponentIdentifier component = new() {
                COMPONENTCLASS = new ComponentClass {
                    COMPONENTCLASSREGISTRY = storageRegistryOid,
                    COMPONENTCLASSVALUE = "010000" + SPC_INQUIRY_Class(data.Inquiry.PeripheralQualifierAndDeviceType)
                },
                MANUFACTURER = SPC_INQUIRY_String(data.Inquiry.T10VendorIdentification),
                MODEL = SPC_INQUIRY_String(data.Inquiry.ProductIdentification),
                SERIAL = SPC_VPD_DI_UNIQUEID_String(data.Vpd83) + ":" + SPC_VPD_SN_String(data.Vpd80),
                REVISION = SPC_INQUIRY_String(data.Inquiry.ProductRevisionLevel)
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

    public static string SPC_INQUIRY_Class(byte val) {
        return Convert.ToHexString([val]).PadLeft(2, '0');
    }

    public static string SPC_INQUIRY_String(byte[] val) {
        return NVMe_String(val);
    }

    public static string SPC_VPD_SN_String(byte[] vpdPage80) {
        return vpdPage80.Length < 5 ? string.Empty : NVMe_String(vpdPage80[4..]);
    }

    public static string SPC_VPD_DI_UNIQUEID_String(byte[] vpdPage83) {
        if (vpdPage83.Length < 5) {
            return string.Empty;
        }

        byte[] ddList = vpdPage83[4..];
        int pos = 0;
        byte[] dd1List = [];
        byte[] dd2List = [];
        List<byte[]> dd3List = [];
        while (pos < ddList.Length) {
            if (pos + 3 > ddList.Length) {
                break;
            }

            pos += 1;
            byte dType = (byte)(ddList[pos] & 0x0F);
            pos += 2;
            byte dLen = ddList[pos];

            if (dLen == 0) {
                continue;
            }

            pos += 1;
            byte[] d = ddList[pos..(pos += dLen)];

            switch (dType) {
                case 1:
                    dd1List = d;
                    break;
                case 2:
                    dd2List = d;
                    break;
                case 3:
                    dd3List.Add(d);
                    break;
                default:
                    break;
            }
        }

        if (dd3List.Count == 0 && dd2List.Length != 8 && dd1List.Length <= 8) {
            return string.Empty;
        } else if (dd3List.Count > 0) {
            byte[] naaTypes = {0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF}; // Length 7, initialized to 0xFFs
            for(byte i = 0; i < dd3List.Count; i++) {
                if (dd3List[i].Length < 8) {
                    continue;
                }
                byte naa = (byte)((dd3List[i][0] & 0xF0) >> 4);
                if (naa > 6) {
                    continue;
                }

                naaTypes[naa] = i;
            }

            if (naaTypes[0x06] >= 0 && dd3List.Count > naaTypes[0x06] && dd3List[naaTypes[0x06]].Length == 16) {
                string result = NVMe_Val(dd3List[naaTypes[0x06]], false);
                return result[7..32];
            } else if (naaTypes[0x05] >= 0 && dd3List.Count >= naaTypes[0x05] && dd3List[naaTypes[0x05]].Length == 8) {
                string result = NVMe_Val(dd3List[naaTypes[0x05]], false);
                return result[7..16];
            } else if (naaTypes[0x02] >= 0 && dd3List.Count >= naaTypes[0x02] && dd3List[naaTypes[0x02]].Length == 8) {
                string result = NVMe_Val(dd3List[naaTypes[0x02]], false);
                return result[1..4] + result[10..16];
            } else if (naaTypes[0x03] >= 0 && dd3List.Count >= naaTypes[0x03] && dd3List[naaTypes[0x03]].Length == 8) {
                string result = NVMe_Val(dd3List[naaTypes[0x03]], false);
                return result[1..];
            } else {
                return string.Empty;
            }
        } else if (dd2List.Length == 8) {
            return NVMe_Val(dd2List[3..8], false);
        } else if (dd1List.Length > 8) { // VENDOR SPECIFIC IDENTIFIER byte 8 to end
            return NVMe_String(dd1List[8..]);
        }
        
        return string.Empty;
    }
}
