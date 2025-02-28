using Microsoft.Win32.SafeHandles;
using System.Runtime.InteropServices;
using System.Runtime.Versioning;

namespace StorageLib.Linux;

[SupportedOSPlatform("linux")]
public class StorageLinuxImports {
    public const string libcName = "libc";
    public const string libudevName = "libudev";

    [DllImport(libcName, SetLastError = true)]
    public static extern int ioctl(SafeFileHandle fd, uint op, IntPtr data);
}
