using System.Runtime.InteropServices;
using System.Runtime.Versioning;

namespace Pcie;

[SupportedOSPlatform("windows")]
internal class WindowsImports {
    public const int MIB_IF_ROW2_SIZE = 1352;
    public const int MIB_IF_ROW2_INTERFACE_LUID_OFFSET = 0;
    public const int MIB_IF_ROW2_INTERFACE_GUID_OFFSET = 12;
    public const int MIB_IF_ROW2_PHYSICAL_ADDRESS_LENGTH_OFFSET = 1056;
    public const int MIB_IF_ROW2_PERMANENT_PHYSICAL_ADDRESS_OFFSET = 1092;
    public const int IF_MAX_PHYS_ADDRESS_LENGTH = 32;
    public const uint NO_ERROR = 0;

    public const string iphlpapiDll = "iphlpapi.dll";
    [DllImport(iphlpapiDll)]
    internal static extern uint ConvertInterfaceGuidToLuid(in Guid interfaceGuid, out ulong interfaceLuid);

    [DllImport(iphlpapiDll)]
    internal static extern uint GetIfEntry2(IntPtr row);
}
