using Microsoft.Win32.SafeHandles;
using System.Runtime.InteropServices;

namespace StorageLib.Linux;

public class StorageLinuxImports {
    public const string libName = "libc";

    [DllImport(libName, SetLastError = true)]
    public static extern int ioctl(SafeFileHandle fd, uint op, IntPtr data);
}
