using HardwareManifestPlugin;
using HardwareManifestProto;
using OidsProto;

namespace Smbios;

public sealed class SmbiosHardwareManifestPlugin : HardwareManifestPluginBase {
    public static readonly string TraitDescription = "SMBIOS-based Component Class Registry";
    public static readonly string TraitDescriptionUri = "https://trustedcomputinggroup.org/wp-content/uploads/SMBIOS-Component-Class-Registry_v1.01_finalpublication.pdf";
    public static readonly string PluginName = "paccor.smbios";
    public static readonly string PluginDescription = "Collect hardware identifiers according to the SMBIOS Component Class Registry.";

    public SmbiosHardwareManifestPlugin() {
        Name = PluginName;
        Description = PluginDescription;
        CollectsV2HardwareInformation = true;
        CollectsV3HardwareInformation = false;
    }

    public override bool GatherHardwareIdentifiers() {
        bool result = false;

        Smbios smbios = Smbios.GetSmbios();

        if (!smbios.Valid) {
            return result;
        }

        AddComponentsToManifestV2(smbios.Structures, ManifestV2);
        ManifestV3 = HardwareManifestConverter.FromManifestV2(ManifestV2, TraitDescription, TraitDescriptionUri);

        result = smbios.Valid;

        return result;
    }

    public static void AddComponentsToManifestV2(IDictionary<int, IList<SmbiosTable>> structures, ManifestV2 manifest) {
        string dmtfRegistryOid = OidsUtils.Find(TCG_REGISTRY_COMPONENTCLASS_NODE.TcgRegistryComponentclassDmtf).Oid;

        foreach (int type in structures.Keys) {
            foreach (SmbiosTable table in structures[type]) {
                ComponentIdentifier component = new() { COMPONENTCLASS = new ComponentClass() };
                bool addComponent = false;

                switch (type) {
                    case 0x0002: // BASEBOARD
                        component.COMPONENTCLASS.COMPONENTCLASSREGISTRY = dmtfRegistryOid;
                        component.COMPONENTCLASS.COMPONENTCLASSVALUE = "00" + Value(table, 0x00) + "00" + Value(table, 0x0D);
                        component.MANUFACTURER = Strref(table, 0x04);
                        component.MODEL = Strref(table, 0x05);
                        component.SERIAL = Strref(table, 0x07);
                        component.REVISION = Strref(table, 0x06);
                        component.FIELDREPLACEABLE = BitField(table, 0x09, 0x1C, 0) ? "true" : "false";
                        addComponent = true;
                        break;
                    case 0x0000: // BIOS
                        component.COMPONENTCLASS.COMPONENTCLASSREGISTRY = dmtfRegistryOid;
                        component.COMPONENTCLASS.COMPONENTCLASSVALUE = "00" + Value(table, 0x00) + Value(table, 0x12, 2);
                        component.MANUFACTURER = Strref(table, 0x04);
                        component.MODEL = Strref(table, 0x05);
                        component.SERIAL = "";
                        component.REVISION = Value(table, 0x14, 2);
                        component.FIELDREPLACEABLE = "";
                        addComponent = true;
                        break;
                    case 0x0003: // CHASSIS
                        component.COMPONENTCLASS.COMPONENTCLASSREGISTRY = dmtfRegistryOid;
                        component.COMPONENTCLASS.COMPONENTCLASSVALUE = "00" + Value(table, 0x00) + "00" + Value(table, 0x05);
                        component.MANUFACTURER = Strref(table, 0x04);
                        component.MODEL = Value(table, 0x05);
                        component.SERIAL = Strref(table, 0x07);
                        component.REVISION = Strref(table, 0x06);
                        component.FIELDREPLACEABLE = "";
                        addComponent = true;
                        break;
                    case 0x0004: // PROCESSOR
                        component.COMPONENTCLASS.COMPONENTCLASSREGISTRY = dmtfRegistryOid;
                        component.COMPONENTCLASS.COMPONENTCLASSVALUE = "00" + Value(table, 0x00) + "00" + Value(table, 0x05);
                        component.MANUFACTURER = Strref(table, 0x07);
                        component.MODEL = Value(table, 0x06);
                        component.SERIAL = Strref(table, 0x20);
                        component.REVISION = Strref(table, 0x10);
                        component.FIELDREPLACEABLE = BitField(table, 0x19, 0x06) ? "true" : "false";
                        addComponent = true;
                        break;
                    case 0x0011: // RAM
                        component.COMPONENTCLASS.COMPONENTCLASSREGISTRY = dmtfRegistryOid;
                        component.COMPONENTCLASS.COMPONENTCLASSVALUE = "00" + Value(table, 0x00) + "00" + Value(table, 0x12);
                        component.MANUFACTURER = Strref(table, 0x17);
                        component.MODEL = Strref(table, 0x1A);
                        component.SERIAL = Strref(table, 0x18);
                        component.REVISION = Strref(table, 0x2B);
                        component.FIELDREPLACEABLE = "";
                        addComponent = !Value(table, 0x0C, 2).Equals("0000"); // Size of the memory device; If the value is 0, no memory device is installed in the socket.
                        break;
                    case 0x0001: // SYSTEM
                        component.COMPONENTCLASS.COMPONENTCLASSREGISTRY = dmtfRegistryOid;
                        component.COMPONENTCLASS.COMPONENTCLASSVALUE = "00" + Value(table, 0x00) + "0000";
                        component.MANUFACTURER = Strref(table, 0x04);
                        component.MODEL = Strref(table, 0x05);
                        component.SERIAL = Strref(table, 0x07);
                        component.REVISION = Strref(table, 0x06);
                        component.FIELDREPLACEABLE = "";
                        addComponent = true;
                        break;
                    case 0x0027: // POWER SUPPLY
                        component.COMPONENTCLASS.COMPONENTCLASSREGISTRY = dmtfRegistryOid;
                        component.COMPONENTCLASS.COMPONENTCLASSVALUE = "00" + Value(table, 0x00) + "0000";
                        component.MANUFACTURER = Strref(table, 0x07);
                        component.MODEL = Strref(table, 0x0A);
                        component.SERIAL = Strref(table, 0x08);
                        component.REVISION = Strref(table, 0x0B);
                        component.FIELDREPLACEABLE = BitField(table, 0x0E, 0x01, 0) ? "true" : "false";
                        addComponent = true;
                        break;
                    case 0x002B: // TPM
                        component.COMPONENTCLASS.COMPONENTCLASSREGISTRY = dmtfRegistryOid;
                        component.COMPONENTCLASS.COMPONENTCLASSVALUE = "00" + Value(table, 0x00) + "0000";
                        component.MANUFACTURER = Value(table, 0x04, 4);
                        component.MODEL = Value(table, 0x08, 2);
                        component.SERIAL = "";
                        component.REVISION = Value(table, 0x0A, 8);
                        component.FIELDREPLACEABLE = "";
                        addComponent = true;
                        break;

                }

                if (addComponent) {
                    manifest.COMPONENTS.Add(component);
                }
            }
        }
    }

    public static string Strref(SmbiosTable table, int offset) {
        if (offset >= table.Data.Length) {
            return "";
        }
        int index = table.Data[offset];
        return index > 0 ? table.Strings[table.Data[offset]-1] : "";
    }

    public static string Value(SmbiosTable table, int offset, int length = 1) {
        int end = offset + length;
        byte[] data;
        if (end <= table.Data.Length) {
            data = table.Data[offset..end];
        } else {
            data = new byte[length];
            Array.Fill<byte>(data, 0x00);
        }
        return Convert.ToHexString(data);
    }

    public static bool BitField(SmbiosTable table, int offset, int testValue) {
        return offset < table.Data.Length && table.Data[offset] != testValue;
    }

    public static bool BitField(SmbiosTable table, int offset, int mask, int testValue) {
        return offset < table.Data.Length && (table.Data[offset] & mask) != testValue;
    }
}
