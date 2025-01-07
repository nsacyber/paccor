using Microsoft.Win32.SafeHandles;
using System.Runtime.InteropServices;
using System.Runtime.Versioning;

namespace StorageLib.Linux;

[SupportedOSPlatform("linux")]
public class StorageLinuxImports {
    public const string libName = "libc";

    [DllImport(libName, SetLastError = true)]
    public static extern int ioctl(SafeFileHandle fd, uint op, IntPtr data);
}
