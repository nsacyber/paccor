using System.Runtime.InteropServices;
using static PcieWinCfgMgr.CfgConstants;

namespace PcieWinCfgMgr;

internal class CfgImports {
    public const string cfgmgrDll = "cfgmgr32.dll";

    [DllImport(cfgmgrDll, SetLastError = true)]
    internal static extern ConfigRet CM_Locate_DevNode(out IntPtr pdnDevInst, string pDeviceID, uint ulFlags);

    [DllImport(cfgmgrDll, SetLastError = true)]
    internal static extern ConfigRet CM_Get_Device_ID_List_Size(out uint pulLen, string pszFilter, uint ulFlags);

    [DllImport(cfgmgrDll, SetLastError = true)]
    internal static extern ConfigRet CM_Get_Device_ID_List(string pszFilter, IntPtr Buffer, uint BufferLen, uint ulFlags);

    [DllImport(cfgmgrDll, SetLastError = true)]
    internal static extern ConfigRet CM_Get_Device_Interface_List_SizeW(out uint pulLen, ref Guid interfaceClassGuid, IntPtr pDeviceID, uint ulFlags);

    // Strings are UTF-16
    [DllImport(cfgmgrDll, SetLastError = true)]
    internal static extern ConfigRet CM_Get_Device_Interface_ListW(ref Guid interfaceClassGuid, IntPtr pDeviceID, IntPtr buffer, uint bufferLen, uint ulFlags);

    // Strings are UTF-16
    [DllImport(cfgmgrDll, SetLastError = true)]
    internal static extern ConfigRet CM_Get_DevNode_PropertyW(IntPtr dnDevInst, in CfgStructs.DevPropKey propertyKey, out uint propertyType, byte[]? propertyBuffer, ref uint propertyBufferSize, uint mustBeZero);

    // Strings are UTF-16
    [DllImport(cfgmgrDll, SetLastError = true)]
    internal static extern ConfigRet CM_Get_Device_Interface_Property_KeysW(IntPtr pszDeviceInterface, IntPtr propertyKeyArray, ref int propertyKeyCount, uint mustBeZero);

    // Strings are UTF-16
    [DllImport(cfgmgrDll, SetLastError = true)]
    internal static extern ConfigRet CM_Get_Device_Interface_PropertyW(IntPtr pszDeviceInterface, in CfgStructs.DevPropKey propertyKey, out uint propertyType, byte[]? propertyBuffer, ref uint propertyBufferSize, uint mustBeZero);
}
