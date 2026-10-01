using Microsoft.Win32.SafeHandles;
using System.Runtime.InteropServices;
using System.Runtime.Versioning;

namespace StorageLib.Linux;

[SupportedOSPlatform("linux")]
internal static class StorageLinuxImports {
    private const string LibcName = "libc";

    [DllImport(LibcName, SetLastError = true)]
    public static extern int ioctl(SafeFileHandle fd, uint op, IntPtr data);
}
