using System.Runtime.InteropServices;
using System.Runtime.Versioning;

namespace Pcie;

[SupportedOSPlatform("linux")]
internal class LinuxImports {
    public const int AF_INET = 2; // socket.h
    public const int SOCK_DGRAM = 2; // socket.h
    public const uint SIOCETHTOOL = 0x8946; // sockios.h
    public const uint ETHTOOL_GPERMADDR = 0x00000020; // ethtool.h
    public const int IFNAMSIZ = 16; // if.h
    public const int IFREQ_SIZE = 40; // if.h, struct ifreq on 64-bit
    public const int IFREQ_DATA_OFFSET = IFNAMSIZ; // ifr_data follows ifr_name
    public const int MAX_ADDR_LEN = 32; // netdevice.h
    public const int ETHTOOL_PERM_ADDR_HEADER_SIZE = 8; // struct ethtool_perm_addr: u32 cmd, u32 size, u8 data[]

    [DllImport("libc", SetLastError = true)]
    internal static extern int socket(int domain, int type, int protocol);

    [DllImport("libc", SetLastError = true)]
    internal static extern int ioctl(int fd, nuint request, IntPtr argp);

    [DllImport("libc", SetLastError = true)]
    internal static extern int close(int fd);

    /// <summary>
    /// This method is imported to query the Linux Kernel whether the program was run with privileges.
    /// </summary>
    /// <returns>The Euid.</returns>
    [DllImport("libc", SetLastError = true)]
    internal static extern uint geteuid();
}
