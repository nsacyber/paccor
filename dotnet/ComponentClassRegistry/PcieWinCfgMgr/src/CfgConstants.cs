using System.Text.RegularExpressions;

namespace PcieWinCfgMgr;
public class CfgConstants {
    public static readonly Guid GUID_DEVINTERFACE_DISK = new(0x53f56307, 0xb6bf, 0x11d0, 0x94, 0xf2, 0x00, 0xa0, 0xc9, 0x1e, 0xfb, 0x8b); // ntddstor.h
    public static readonly Guid GUID_PCI_DEVICE_DEVPKEY = new(0x3ab22e31, 0x8264, 0x4b4e, 0x9a, 0xf5, 0xa8, 0xd2, 0xd8, 0xe3, 0x3e, 0x62); // pciprop.h
    public static readonly CfgStructs.DevPropKey DEVPKEY_PciDevice_BaseClass = new() { DEVPROPGUID = GUID_PCI_DEVICE_DEVPKEY, DEVPROPID = 0x03 }; // pciprop.h
    public static readonly CfgStructs.DevPropKey DEVPKEY_PciDevice_SubClass = new() { DEVPROPGUID = GUID_PCI_DEVICE_DEVPKEY, DEVPROPID = 0x04 }; // pciprop.h
    public static readonly CfgStructs.DevPropKey DEVPKEY_PciDevice_ProgIf = new() { DEVPROPGUID = GUID_PCI_DEVICE_DEVPKEY, DEVPROPID = 0x05 }; // pciprop.h, DEVPROP_TYPE_UINT32
    public static readonly CfgStructs.DevPropKey DEVPKEY_PciDevice_SerialNumber = new() { DEVPROPGUID = GUID_PCI_DEVICE_DEVPKEY, DEVPROPID = 0x28 }; // pciprop.h
    public static readonly Guid GUID_SPDRP_DEVICE_PROP_DEVPKEY = new(0xa45c254e, 0xdf1c, 0x4efd, 0x80, 0x20, 0x67, 0xd1, 0x46, 0xa8, 0x50, 0xe0); // devpkey.h
    public static readonly CfgStructs.DevPropKey DEVPKEY_Device_HardwareIds = new() { DEVPROPGUID = GUID_SPDRP_DEVICE_PROP_DEVPKEY, DEVPROPID = 0x03 }; // devpkey.h, DEVPROP_TYPE_STRING_LIST
    public static readonly Guid GUID_DEVICE_RELATIONS_DEVPKEY = new(0x4340a6c5, 0x93fa, 0x4706, 0x97, 0x2c, 0x7b, 0x64, 0x80, 0x08, 0xa5, 0xa7); // devpkey.h
    public static readonly CfgStructs.DevPropKey DEVPKEY_Device_Parent = new() { DEVPROPGUID = GUID_DEVICE_RELATIONS_DEVPKEY, DEVPROPID = 0x08 }; // devpkey.h
    public static readonly Guid GUID_DEVICE_OTHER_DEVPKEY = new(0x540b947e, 0x8b40, 0x45bc, 0xa8, 0xa2, 0x6a, 0x0b, 0x89, 0x4c, 0xbd, 0xa2); // devpkey.h
    public static readonly CfgStructs.DevPropKey DEVPKEY_Device_PhysicalDeviceLocation = new() { DEVPROPGUID = GUID_DEVICE_OTHER_DEVPKEY, DEVPROPID = 0x09 }; // devpkey.h
    public static readonly Guid GUID_DEVICE_COMMON_DEVPKEY = new(0x78c34fc8, 0x104a, 0x4aca, 0x9e, 0xa4, 0x52, 0x4d, 0x52, 0x99, 0x6e, 0x57); // devpkey.h
    public static readonly CfgStructs.DevPropKey DEVPKEY_Device_InstanceId = new() { DEVPROPGUID = GUID_DEVICE_COMMON_DEVPKEY, DEVPROPID = 0x0100 }; // devpkey.h
    public static readonly uint PCI_CONFIG_SIZE = 256;
    public static readonly uint PCIE_CONFIG_SIZE = 4096;
    public static readonly uint DEVPROP_BUFFER_SIZE = 2048;
    public static readonly uint DEVPROP_TYPEMOD_ARRAY = 0x00001000; // devpropdef.h
    public static readonly uint DEVPROP_TYPEMOD_LIST = 0x00002000;  // devpropdef.h
    public static readonly uint DEVPROP_TYPE_BYTE = 0x00000003; // devpropdef.h
    public static readonly uint DEVPROP_TYPE_UINT16 = 0x00000005; // devpropdef.h
    public static readonly uint DEVPROP_TYPE_UINT32 = 0x00000007; // devpropdef.h
    public static readonly uint DEVPROP_TYPE_UINT64 = 0x00000009; // devpropdef.h
    public static readonly uint DEVPROP_TYPE_STRING = 0x00000012; // devpropdef.h
    public static readonly uint DEVPROP_TYPE_STRING_LIST = DEVPROP_TYPE_STRING | DEVPROP_TYPEMOD_LIST; // devpropdef.h
    public static readonly uint DEVPROP_TYPE_BINARY = DEVPROP_TYPE_BYTE | DEVPROP_TYPEMOD_ARRAY; // devpropdef.h
    public static readonly uint CM_GETIDLIST_FILTER_ENUMERATOR = 0x00000001; // cfgmfg32.h
    public static readonly uint CM_GET_DEVICE_INTERFACE_LIST_PRESENT = 0x00000000; // cfgmfg32.h
    public static readonly uint CM_GET_DEVICE_INTERFACE_LIST_ALL_DEVICES = 0x00000001; // cfgmfg32.h
    public static readonly uint CM_GET_DEVICE_INTERFACE_LIST_BITS = 0x00000001; // cfgmfg32.h
    public static readonly uint CM_LOCATE_DEVNODE_NORMAL = 0x00000000; // cfgmfg32.h
    public static readonly uint CM_LOCATE_DEVNODE_PHANTOM = 0x00000001; // cfgmfg32.h
    public static readonly uint CM_LOCATE_DEVNODE_CANCELREMOVE = 0x00000002; // cfgmfg32.h
    public static readonly uint CM_LOCATE_DEVNODE_NOVALIDATION = 0x00000004; // cfgmfg32.h
    public static readonly uint CM_LOCATE_DEVNODE_BITS = 0x00000007; // cfgmfg32.h

    public static readonly uint CM_LOCATE_DEVINST_NORMAL = CM_LOCATE_DEVNODE_NORMAL; // cfgmfg32.h
    public static readonly uint CM_LOCATE_DEVINST_PHANTOM = CM_LOCATE_DEVNODE_PHANTOM; // cfgmfg32.h
    public static readonly uint CM_LOCATE_DEVINST_CANCELREMOVE = CM_LOCATE_DEVNODE_CANCELREMOVE; // cfgmfg32.h
    public static readonly uint CM_LOCATE_DEVINST_NOVALIDATION = CM_LOCATE_DEVNODE_NOVALIDATION; // cfgmfg32.h
    public static readonly uint CM_LOCATE_DEVINST_BITS = CM_LOCATE_DEVNODE_BITS; // cfgmfg32.h

    public enum DevpropTypeFixed : uint { // devpropdef.h
        DEVPROP_TYPE_BYTE = 0x00000003,
        DEVPROP_TYPE_UINT16 = 0x00000005,
        DEVPROP_TYPE_UINT32 = 0x00000007,
        DEVPROP_TYPE_UINT64 = 0x00000009,
    }

    public enum ConfigRet : uint { // cfgmfg32.h
        CR_SUCCESS = 0x00000000,
        CR_DEFAULT = 0x00000001,
        CR_OUT_OF_MEMORY = 0x00000002,
        CR_INVALID_POINTER = 0x00000003,
        CR_INVALID_FLAG = 0x00000004,
        CR_INVALID_DEVNODE = 0x00000005,
        CR_INVALID_DEVINST = CR_INVALID_DEVNODE,
        CR_INVALID_RES_DES = 0x00000006,
        CR_INVALID_LOG_CONF = 0x00000007,
        CR_INVALID_ARBITRATOR = 0x00000008,
        CR_INVALID_NODELIST = 0x00000009,
        CR_DEVNODE_HAS_REQS = 0x0000000A,
        CR_DEVINST_HAS_REQS = CR_DEVNODE_HAS_REQS,
        CR_INVALID_RESOURCEID = 0x0000000B,
        CR_DLVXD_NOT_FOUND = 0x0000000C, // WIN 95 ONLY
        CR_NO_SUCH_DEVNODE = 0x0000000D,
        CR_NO_SUCH_DEVINST = CR_NO_SUCH_DEVNODE,
        CR_NO_MORE_LOG_CONF = 0x0000000E,
        CR_NO_MORE_RES_DES = 0x0000000F,
        CR_ALREADY_SUCH_DEVNODE = 0x00000010,
        CR_ALREADY_SUCH_DEVINST = CR_ALREADY_SUCH_DEVNODE,
        CR_INVALID_RANGE_LIST = 0x00000011,
        CR_INVALID_RANGE = 0x00000012,
        CR_FAILURE = 0x00000013,
        CR_NO_SUCH_LOGICAL_DEV = 0x00000014,
        CR_CREATE_BLOCKED = 0x00000015,
        CR_NOT_SYSTEM_VM = 0x00000016, // WIN 95 ONLY
        CR_REMOVE_VETOED = 0x00000017,
        CR_APM_VETOED = 0x00000018,
        CR_INVALID_LOAD_TYPE = 0x00000019,
        CR_BUFFER_SMALL = 0x0000001A,
        CR_NO_ARBITRATOR = 0x0000001B,
        CR_NO_REGISTRY_HANDLE = 0x0000001C,
        CR_REGISTRY_ERROR = 0x0000001D,
        CR_INVALID_DEVICE_ID = 0x0000001E,
        CR_INVALID_DATA = 0x0000001F,
        CR_INVALID_API = 0x00000020,
        CR_DEVLOADER_NOT_READY = 0x00000021,
        CR_NEED_RESTART = 0x00000022,
        CR_NO_MORE_HW_PROFILES = 0x00000023,
        CR_DEVICE_NOT_THERE = 0x00000024,
        CR_NO_SUCH_VALUE = 0x00000025,
        CR_WRONG_TYPE = 0x00000026,
        CR_INVALID_PRIORITY = 0x00000027,
        CR_NOT_DISABLEABLE = 0x00000028,
        CR_FREE_RESOURCES = 0x00000029,
        CR_QUERY_VETOED = 0x0000002A,
        CR_CANT_SHARE_IRQ = 0x0000002B,
        CR_NO_DEPENDENT = 0x0000002C,
        CR_SAME_RESOURCES = 0x0000002D,
        CR_NO_SUCH_REGISTRY_KEY = 0x0000002E,
        CR_INVALID_MACHINENAME = 0x0000002F, // NT ONLY
        CR_REMOTE_COMM_FAILURE = 0x00000030, // NT ONLY
        CR_MACHINE_UNAVAILABLE = 0x00000031, // NT ONLY
        CR_NO_CM_SERVICES = 0x00000032, // NT ONLY
        CR_ACCESS_DENIED = 0x00000033, // NT ONLY
        CR_CALL_NOT_IMPLEMENTED = 0x00000034,
        CR_INVALID_PROPERTY = 0x00000035,
        CR_DEVICE_INTERFACE_ACTIVE = 0x00000036,
        CR_NO_SUCH_DEVICE_INTERFACE = 0x00000037,
        CR_INVALID_REFERENCE_STRING = 0x00000038,
        CR_INVALID_CONFLICT_LIST = 0x00000039,
        CR_INVALID_INDEX = 0x0000003A,
        CR_INVALID_STRUCTURE_SIZE = 0x0000003B,
        CR_RESULTS = 0x0000003C
    }

    // String parsing functions for CM HardwareIds
    public static readonly string PCI_DEVICEID_PREFIX = "PCI";
    public static readonly string HARDWAREID_GROUPNAME_VEN = "ven";
    public static readonly string HARDWAREID_GROUPNAME_DEV = "dev";
    public static readonly string HARDWAREID_GROUPNAME_SUBSYSVEN = "subsysVen";
    public static readonly string HARDWAREID_GROUPNAME_SUBSYSDEV = "subsysDev";
    public static readonly string HARDWAREID_GROUPNAME_REV = "rev";
    public static readonly string HARDWAREID_GROUPNAME_CC_CLASS = "cClass";
    public static readonly string HARDWAREID_GROUPNAME_CC_SUBCLASS = "cSubClass";
    public static readonly string HARDWAREID_GROUPNAME_CC_PROGIF = "cProgIf";

    public static readonly string HARDWAREID_PATTERN_VEN = @"VEN_(?<" + HARDWAREID_GROUPNAME_VEN + @">[0-9A-Fa-f]{4})";
    public static readonly string HARDWAREID_PATTERN_DEV = @"DEV_(?<" + HARDWAREID_GROUPNAME_DEV + @">[0-9A-Fa-f]{4})";
    public static readonly string HARDWAREID_PATTERN_SUBSYS = @"SUBSYS_(?<" + HARDWAREID_GROUPNAME_SUBSYSDEV + @">[0-9A-Fa-f]{4})(?<" + HARDWAREID_GROUPNAME_SUBSYSVEN + @">[0-9A-Fa-f]{4})";
    public static readonly string HARDWAREID_PATTERN_REV = @"REV_(?<" + HARDWAREID_GROUPNAME_REV + @">[0-9A-Fa-f]{2})";
    public static readonly string HARDWAREID_PATTERN_CC = @"CC_(?<" + HARDWAREID_GROUPNAME_CC_CLASS + @">[0-9A-Fa-f]{2})(?<" + HARDWAREID_GROUPNAME_CC_SUBCLASS + @">[0-9A-Fa-f]{2})(?<" + HARDWAREID_GROUPNAME_CC_PROGIF + @">[0-9A-Fa-f]{2})";
    public static readonly string HARDWAREID_PATTERN = $"({HARDWAREID_PATTERN_VEN})|({HARDWAREID_PATTERN_DEV})|({HARDWAREID_PATTERN_SUBSYS})|({HARDWAREID_PATTERN_REV})|({HARDWAREID_PATTERN_CC})";

    public static readonly Regex HARDWAREID_REGEX = new Regex(HARDWAREID_PATTERN, RegexOptions.Compiled);

    // Settings to identify config buffers regenerated by this library
    public static readonly uint PCIEWINCFGMGR_CONFIG_SIG = 0x3909C18E;
}
