using PcieWinCfgMgr;
using System.Runtime.InteropServices;
using System.Runtime.Versioning;
using System.Text;

namespace Pcie;

/// <summary>
/// Reads the permanent (factory) MAC address of a network adapter directly.
/// </summary>
internal static class NetAdapterMac {
    [SupportedOSPlatform("windows")]
    public static bool GetPermanentAddressWindows(out byte[] mac, string pciDeviceInstanceId) {
        mac = [];

        if (!PciWinCfgMgr.GetNetAdapterInterfaceGuid(out Guid interfaceGuid, pciDeviceInstanceId)) {
            return false;
        }

        if (WindowsImports.ConvertInterfaceGuidToLuid(in interfaceGuid, out ulong interfaceLuid) != WindowsImports.NO_ERROR) {
            return false;
        }

        IntPtr row = Marshal.AllocHGlobal(WindowsImports.MIB_IF_ROW2_SIZE);
        try {
            byte[] zeros = new byte[WindowsImports.MIB_IF_ROW2_SIZE];
            Marshal.Copy(zeros, 0, row, zeros.Length);
            Marshal.WriteInt64(row, WindowsImports.MIB_IF_ROW2_INTERFACE_LUID_OFFSET, (long)interfaceLuid);

            if (WindowsImports.GetIfEntry2(row) != WindowsImports.NO_ERROR) {
                return false;
            }

            mac = ReadPermanentAddressFromRow(row, interfaceGuid);
        } finally {
            Marshal.FreeHGlobal(row);
        }

        return IsSet(mac);
    }

    /// <summary>
    /// An empty or all-zero permanent address means the adapter has none (e.g. SR-IOV virtual functions).
    /// </summary>
    private static bool IsSet(byte[] mac) {
        return mac.Any(b => b != 0);
    }

    [SupportedOSPlatform("windows")]
    private static byte[] ReadPermanentAddressFromRow(IntPtr row, Guid expectedInterfaceGuid) {
        byte[] guidBytes = new byte[16];
        Marshal.Copy(row + WindowsImports.MIB_IF_ROW2_INTERFACE_GUID_OFFSET, guidBytes, 0, guidBytes.Length);
        int length = Marshal.ReadInt32(row, WindowsImports.MIB_IF_ROW2_PHYSICAL_ADDRESS_LENGTH_OFFSET);

        // Guard against reading the wrong row or a layout mismatch
        if (new Guid(guidBytes) != expectedInterfaceGuid || length <= 0 || length > WindowsImports.IF_MAX_PHYS_ADDRESS_LENGTH) {
            return [];
        }

        byte[] mac = new byte[length];
        Marshal.Copy(row + WindowsImports.MIB_IF_ROW2_PERMANENT_PHYSICAL_ADDRESS_OFFSET, mac, 0, length);
        return mac;
    }

    [SupportedOSPlatform("linux")]
    public static bool GetPermanentAddressLinux(out byte[] mac, string interfaceName) {
        mac = [];
        byte[] name = Encoding.ASCII.GetBytes(interfaceName);

        if (name.Length == 0 || name.Length >= LinuxImports.IFNAMSIZ) {
            return false;
        }

        int fd = LinuxImports.socket(LinuxImports.AF_INET, LinuxImports.SOCK_DGRAM, 0);
        if (fd < 0) {
            return false;
        }

        IntPtr ifreq = Marshal.AllocHGlobal(LinuxImports.IFREQ_SIZE);
        IntPtr permAddr = Marshal.AllocHGlobal(LinuxImports.ETHTOOL_PERM_ADDR_HEADER_SIZE + LinuxImports.MAX_ADDR_LEN);
        try {
            Marshal.Copy(new byte[LinuxImports.IFREQ_SIZE], 0, ifreq, LinuxImports.IFREQ_SIZE);
            Marshal.Copy(name, 0, ifreq, name.Length);
            Marshal.WriteIntPtr(ifreq, LinuxImports.IFREQ_DATA_OFFSET, permAddr);
            Marshal.WriteInt32(permAddr, 0, (int)LinuxImports.ETHTOOL_GPERMADDR);
            Marshal.WriteInt32(permAddr, 4, LinuxImports.MAX_ADDR_LEN);

            if (LinuxImports.ioctl(fd, LinuxImports.SIOCETHTOOL, ifreq) < 0) {
                return false;
            }

            int length = Marshal.ReadInt32(permAddr, 4);
            if (length <= 0 || length > LinuxImports.MAX_ADDR_LEN) {
                return false;
            }

            mac = new byte[length];
            Marshal.Copy(permAddr + LinuxImports.ETHTOOL_PERM_ADDR_HEADER_SIZE, mac, 0, length);
        } finally {
            Marshal.FreeHGlobal(permAddr);
            Marshal.FreeHGlobal(ifreq);
            LinuxImports.close(fd);
        }

        return IsSet(mac);
    }
}
