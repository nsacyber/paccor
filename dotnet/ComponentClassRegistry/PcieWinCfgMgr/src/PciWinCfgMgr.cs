using System.Runtime.InteropServices;
using System.Text.RegularExpressions;

namespace PcieWinCfgMgr;

public class PciWinCfgMgr {
    public static bool GetAllPciDeviceInstanceIds(out List<string> pciDeviceInstanceIdsW) {
        string filter = CfgConstants.PCI_DEVICEID_PREFIX;
        uint flags = CfgConstants.CM_GETIDLIST_FILTER_ENUMERATOR;
        return GetDeviceInstanceIds(out pciDeviceInstanceIdsW, filter, flags);
    }

    public static bool GetDeviceInstanceIds(out List<string> deviceInstanceIdsW, string filter, uint flags) {
        deviceInstanceIdsW = Array.Empty<string>().ToList();

        // Get the buffer size required to store all the device instance IDs
        CfgConstants.ConfigRet response = CfgImports.CM_Get_Device_ID_List_Size(out uint dataLength, filter, flags);

        if (response != CfgConstants.ConfigRet.CR_SUCCESS) {
            return false;
        }

        IntPtr dataPtr = IntPtr.Zero;

        try {
            dataPtr = Marshal.AllocHGlobal((int)dataLength);

            response = CfgImports.CM_Get_Device_ID_List(filter, dataPtr, dataLength, flags);

            if (response != CfgConstants.ConfigRet.CR_SUCCESS) {
                return false;
            }

            // Response was successful
            string data = Marshal.PtrToStringUTF8(dataPtr, (int)dataLength);
            string[] split = data.Split('\0');
            deviceInstanceIdsW.AddRange(split);
            deviceInstanceIdsW.RemoveAll(string.IsNullOrWhiteSpace);
        } finally {
            Marshal.FreeHGlobal(dataPtr);
        }

        return true;
    }

    public static bool GetDiskDevInterfaces(out List<string> diskDeviceInterfaceIdsW) {
        Guid guid = CfgConstants.GUID_DEVINTERFACE_DISK;
        return GetDevInterfaces(out diskDeviceInterfaceIdsW, guid);
    }

    // Public so other libraries can use these ids as handles
    public static bool GetDevInterfaces(out List<string> deviceInterfaceIdsW, Guid guid) {
        deviceInterfaceIdsW = [];
        
        IntPtr pDeviceId = IntPtr.Zero; // List all Interfaces
        uint flags = CfgConstants.CM_GET_DEVICE_INTERFACE_LIST_ALL_DEVICES;

        // Get the buffer size required to store all the device interface IDs
        CfgConstants.ConfigRet response = CfgImports.CM_Get_Device_Interface_List_SizeW(out uint dataLength, ref guid, pDeviceId, flags);
        
        if (response != CfgConstants.ConfigRet.CR_SUCCESS) {
            return false;
        }
        
        IntPtr dataPtr = IntPtr.Zero;
        try {
            // Allocate memory for the device interface IDs given the previous dataLength response
            dataPtr = Marshal.AllocHGlobal((int)dataLength*2); // double because of wide strings

            // Query the system for the device interface IDs
            response = CfgImports.CM_Get_Device_Interface_ListW(ref guid, pDeviceId, dataPtr, dataLength, flags);

            if (response != CfgConstants.ConfigRet.CR_SUCCESS) {
                return false;
            }
           
            // Response was successful
            string dataString = Marshal.PtrToStringUni(dataPtr, (int)dataLength);
            string[] split = dataString.Split('\0');
            deviceInterfaceIdsW.AddRange(split);
            deviceInterfaceIdsW.RemoveAll(string.IsNullOrWhiteSpace);
        } finally {
            // Free allocated memory
            Marshal.FreeHGlobal(dataPtr);
        }

        return true;
    }

    // Both lists should be the same size
    public static bool GetDeviceInterfaceInstanceIds(out List<string> deviceInterfaceInstanceIds, List<string> deviceInterfaceIdsW) {
        deviceInterfaceInstanceIds = [];

        foreach (string deviceInterfaceIdW in deviceInterfaceIdsW) {
            if (!GetDeviceInterfaceInstanceId(out string deviceInterfaceInstanceId, deviceInterfaceIdW)) {
                return false;
            }

            deviceInterfaceInstanceIds.Add(deviceInterfaceInstanceId);
        }

        return true;
    }

    public static bool GetDeviceInterfaceInstanceId(out string deviceInterfaceInstanceId, string deviceInterfaceIdW) {
        deviceInterfaceInstanceId = "";

        CfgStructs.DevPropKey instanceIdKey = CfgConstants.DEVPKEY_Device_InstanceId;
        uint expectedPropertyType = CfgConstants.DEVPROP_TYPE_STRING;
        uint bufferLength = CfgConstants.DEVPROP_BUFFER_SIZE;

        IntPtr deviceInterfaceIdWPtr = Marshal.StringToHGlobalUni(deviceInterfaceIdW); // Need a pointer to the string

        try {
            byte[] buffer = new byte[bufferLength];

            CfgConstants.ConfigRet response = CfgImports.CM_Get_Device_Interface_PropertyW(deviceInterfaceIdWPtr, in instanceIdKey, out uint propertyType, buffer, ref bufferLength, 0);

            if (response != CfgConstants.ConfigRet.CR_SUCCESS || propertyType != expectedPropertyType) {
                return false;
            }

            deviceInterfaceInstanceId = System.Text.Encoding.Unicode.GetString(buffer, 0, (int)bufferLength);
        } finally {
            Marshal.FreeHGlobal(deviceInterfaceIdWPtr);
        }

        return true;
    }

    public static bool CreateMockConfigBufferFromPciDeviceInstanceId(out byte[] config, out bool isLittleEndian, string deviceInstanceId) {
        config = [];
        isLittleEndian = BitConverter.IsLittleEndian;

        CfgConstants.ConfigRet response = CfgImports.CM_Locate_DevNode(out IntPtr devNodePtr, deviceInstanceId,
            CfgConstants.CM_LOCATE_DEVNODE_NORMAL);

        if (response != CfgConstants.ConfigRet.CR_SUCCESS) {
            return false;
        }

        return CreateMockConfigBufferFromPciDevNode(out config, out isLittleEndian, devNodePtr);
    }

    // The dev node is expected to be for a PCI device
    public static bool CreateMockConfigBufferFromPciDevNode(out byte[] config, out bool isLittleEndian, IntPtr devNodePtr) {
        config = [];
        isLittleEndian = BitConverter.IsLittleEndian;

        bool gotIds = ParseHardwareIdsFromDevNode(out ushort VendorId, out ushort DeviceId, out ushort SubsystemVendorId, out ushort SubsystemId, out byte Revision, out byte Class, out byte SubClass, out byte ProgrammingInterface, devNodePtr);
        bool gotDsn = GetPciDeviceSerialNumberFromDevNode(out byte[] dsnBytes, devNodePtr);

        if (!gotIds) {
            return false;
        }

        if (Class == 0 && SubClass == 0 && ProgrammingInterface == 0) {
            // Try another way to pull the class code from cfgmgr32
            GetPciClassCodeFromDevNode(out Class, out SubClass, out ProgrammingInterface, devNodePtr);
        }

        byte[] ccBytes = [Class, SubClass, ProgrammingInterface];
        if (isLittleEndian) {
            Array.Reverse(ccBytes);
        }

        config = new byte[gotDsn ? CfgConstants.PCIE_CONFIG_SIZE : CfgConstants.PCI_CONFIG_SIZE];
        Array.Fill<byte>(config, 0);
        Array.Copy(BitConverter.GetBytes(VendorId), 0, config, 0x0, 2);
        Array.Copy(BitConverter.GetBytes(DeviceId), 0, config, 0x2, 2);
        config[0x8] = Revision;
        Array.Copy(ccBytes, 0, config, 0x9, 3);
        Array.Copy(BitConverter.GetBytes(SubsystemVendorId), 0, config, 0x2C, 2);
        Array.Copy(BitConverter.GetBytes(SubsystemId), 0, config, 0x2E, 2);

        if (gotDsn && config.Length >= 0x10C && dsnBytes.Length == 8) {
            config[0x100] = 0x03;
            config[0x101] = 0x00;
            config[0x102] = 0x00;
            config[0x103] = 0x00;
            if (isLittleEndian) {
                Array.Reverse(dsnBytes);
            }
            Array.Copy(dsnBytes, 0, config, 0x104, dsnBytes.Length);
        }

        Array.Copy(BitConverter.GetBytes(CfgConstants.PCIEWINCFGMGR_CONFIG_SIG), 0, config, config.Length-5, 4);

        return true;
    }

    public static bool GetDeviceNodeOfParent(out IntPtr parentNodePtr, string deviceInstanceId) {
        parentNodePtr = IntPtr.Zero;

        CfgConstants.ConfigRet response = CfgImports.CM_Locate_DevNode(out IntPtr devNodePtr, deviceInstanceId, CfgConstants.CM_LOCATE_DEVNODE_NORMAL);

        if (response != CfgConstants.ConfigRet.CR_SUCCESS) {
            return false;
        }

        CfgStructs.DevPropKey parentKey = CfgConstants.DEVPKEY_Device_Parent;
        uint expectedPropertyType = CfgConstants.DEVPROP_TYPE_STRING;
        uint bufferLength = CfgConstants.DEVPROP_BUFFER_SIZE;
        byte[] buffer = new byte[bufferLength];

        response = CfgImports.CM_Get_DevNode_PropertyW(devNodePtr, in parentKey, out uint propertyType, buffer, ref bufferLength, 0);

        if (response != CfgConstants.ConfigRet.CR_SUCCESS || propertyType != expectedPropertyType) {
            return false;
        }

        string parentDeviceId = System.Text.Encoding.Unicode.GetString(buffer, 0, (int)bufferLength);

        response = CfgImports.CM_Locate_DevNode(out parentNodePtr, parentDeviceId, CfgConstants.CM_LOCATE_DEVNODE_NORMAL);

        return response == CfgConstants.ConfigRet.CR_SUCCESS;
    }

    public static bool GetUint32FromDevNodeProperty(out uint propertyValue, CfgStructs.DevPropKey devpkey, IntPtr devNodePtr) {
        propertyValue = 0;

        bool result = GetByteArrayFromDevNodeProperty(out byte[] buffer, devpkey, CfgConstants.DevpropTypeFixed.DEVPROP_TYPE_UINT32, devNodePtr);

        if (!result) {
            return false;
        }

        propertyValue = BitConverter.ToUInt32(buffer);

        return true;
    }

    private static bool GetByteArrayFromDevNodeProperty(out byte[] propertyValue, CfgStructs.DevPropKey devpkey, CfgConstants.DevpropTypeFixed expectedPropertyType, IntPtr devNodePtr) {
        propertyValue = [];

        uint expectedBufferLength = 0;
        uint bufferLength = CfgConstants.DEVPROP_BUFFER_SIZE;

        expectedBufferLength = expectedPropertyType switch {
            CfgConstants.DevpropTypeFixed.DEVPROP_TYPE_BYTE => 1,
            CfgConstants.DevpropTypeFixed.DEVPROP_TYPE_UINT16 => 2,
            CfgConstants.DevpropTypeFixed.DEVPROP_TYPE_UINT32 => 4,
            CfgConstants.DevpropTypeFixed.DEVPROP_TYPE_UINT64 => 8,
            _ => expectedBufferLength
        };

        byte[] buffer = new byte[bufferLength]; // DEVPROP_BUFFER_SIZE

        CfgConstants.ConfigRet response = CfgImports.CM_Get_DevNode_PropertyW(devNodePtr, in devpkey, out uint propertyType, buffer, ref bufferLength, 0);

        if (response != CfgConstants.ConfigRet.CR_SUCCESS || propertyType != (uint)expectedPropertyType || bufferLength != expectedBufferLength) {
            return false;
        }

        propertyValue = new byte[bufferLength]; // expected length
        Array.Copy(buffer, propertyValue, bufferLength);

        return true;
    }

    public static bool GetStringListFromDevNodeProperty(out List<string> propertyValue, CfgStructs.DevPropKey devpkey, IntPtr devNodePtr) {
        propertyValue = [];

        uint expectedPropertyType = CfgConstants.DEVPROP_TYPE_STRING_LIST;
        uint bufferLength = CfgConstants.DEVPROP_BUFFER_SIZE;
        byte[] buffer = new byte[bufferLength];

        CfgConstants.ConfigRet response = CfgImports.CM_Get_DevNode_PropertyW(devNodePtr, in devpkey, out uint propertyType, buffer, ref bufferLength, 0);

        if (response != CfgConstants.ConfigRet.CR_SUCCESS || propertyType != expectedPropertyType || bufferLength == 0) {
            return false;
        }

        string conv = System.Text.Encoding.Unicode.GetString(buffer, 0, (int)bufferLength); // buffer contains null-terminated UTF-16 strings
        string[] split = conv.Split('\0');
        propertyValue.AddRange(split);

        return true;
    }

    public static bool GetPciClassCodeFromDevNode(out byte Class, out byte SubClass, out byte ProgrammingInterface, IntPtr devNodePtr) {
        bool resultBaseClass = GetUint32FromDevNodeProperty(out uint baseClassInt, CfgConstants.DEVPKEY_PciDevice_BaseClass, devNodePtr);

        bool resultSubClass = GetUint32FromDevNodeProperty(out uint subClassInt, CfgConstants.DEVPKEY_PciDevice_SubClass, devNodePtr);

        bool resultProgIf = GetUint32FromDevNodeProperty(out uint progIfInt, CfgConstants.DEVPKEY_PciDevice_ProgIf, devNodePtr);

        // The casts here will truncate the 3 MSBs
        Class = (byte)baseClassInt;
        SubClass = (byte)subClassInt;
        ProgrammingInterface = (byte)progIfInt;

        return resultBaseClass && resultSubClass && resultProgIf;
    }

    public static bool ParseHardwareIdsFromDevNode(out ushort VendorId, out ushort DeviceId, out ushort SubsystemVendorId, out ushort SubsystemId, out byte Revision, out byte Class, out byte SubClass, out byte ProgrammingInterface, IntPtr devNodePtr) {
        VendorId = 0;
        DeviceId = 0;
        SubsystemVendorId = 0;
        SubsystemId = 0;
        Revision = 0;
        Class = 0;
        SubClass = 0;
        ProgrammingInterface = 0;

        CfgStructs.DevPropKey key = CfgConstants.DEVPKEY_Device_HardwareIds;

        bool result = GetStringListFromDevNodeProperty(out List<string> hardwareIds, key, devNodePtr);

        foreach (string hardwareId in hardwareIds) {
            if (string.IsNullOrWhiteSpace(hardwareId) || !hardwareId.StartsWith(@"PCI\")) {
                continue;
            }

            Match match = CfgConstants.HARDWAREID_REGEX.Match(hardwareId);

            while (match.Success) {
                try {
                    if (match.Groups[CfgConstants.HARDWAREID_GROUPNAME_VEN].Captures.Count > 0 && VendorId == 0) {
                        VendorId = Convert.ToUInt16(match.Groups[CfgConstants.HARDWAREID_GROUPNAME_VEN].Value, 16);
                    }

                    if (match.Groups[CfgConstants.HARDWAREID_GROUPNAME_DEV].Captures.Count > 0 && DeviceId == 0) {
                        DeviceId = Convert.ToUInt16(match.Groups[CfgConstants.HARDWAREID_GROUPNAME_DEV].Value, 16);
                    }

                    if (match.Groups[CfgConstants.HARDWAREID_GROUPNAME_SUBSYSVEN].Captures.Count > 0 && SubsystemVendorId == 0) {
                        SubsystemVendorId = Convert.ToUInt16(match.Groups[CfgConstants.HARDWAREID_GROUPNAME_SUBSYSVEN].Value, 16);
                    }

                    if (match.Groups[CfgConstants.HARDWAREID_GROUPNAME_SUBSYSDEV].Captures.Count > 0 && SubsystemId == 0) {
                        SubsystemId = Convert.ToUInt16(match.Groups[CfgConstants.HARDWAREID_GROUPNAME_SUBSYSDEV].Value, 16);
                    }

                    if (match.Groups[CfgConstants.HARDWAREID_GROUPNAME_REV].Captures.Count > 0 && Revision == 0) {
                        Revision = Convert.ToByte(match.Groups[CfgConstants.HARDWAREID_GROUPNAME_REV].Value, 16);
                    }

                    if (match.Groups[CfgConstants.HARDWAREID_GROUPNAME_CC_CLASS].Captures.Count > 0 && Class == 0) {
                        Class = Convert.ToByte(match.Groups[CfgConstants.HARDWAREID_GROUPNAME_CC_CLASS].Value, 16);
                    }

                    if (match.Groups[CfgConstants.HARDWAREID_GROUPNAME_CC_SUBCLASS].Captures.Count > 0 && SubClass == 0) {
                        SubClass = Convert.ToByte(match.Groups[CfgConstants.HARDWAREID_GROUPNAME_CC_SUBCLASS].Value, 16);
                    }

                    if (match.Groups[CfgConstants.HARDWAREID_GROUPNAME_CC_PROGIF].Captures.Count > 0 && ProgrammingInterface == 0) {
                        ProgrammingInterface = Convert.ToByte(match.Groups[CfgConstants.HARDWAREID_GROUPNAME_CC_PROGIF].Value, 16);
                    }
                } finally {
                    match = match.NextMatch();
                }
            }
        }
        return result;
    }

    public static bool GetPciDeviceSerialNumberFromDevNode(out byte[] dsnBytes, IntPtr devNodePtr) {
        dsnBytes = [];

        bool result = GetByteArrayFromDevNodeProperty(out dsnBytes, CfgConstants.DEVPKEY_PciDevice_SerialNumber, CfgConstants.DevpropTypeFixed.DEVPROP_TYPE_UINT64, devNodePtr);

        return result;
    }
}
