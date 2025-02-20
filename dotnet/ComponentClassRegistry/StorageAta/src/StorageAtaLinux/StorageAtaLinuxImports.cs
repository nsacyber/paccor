using Microsoft.Win32.SafeHandles;
using System.Runtime.InteropServices;
using System.Runtime.Versioning;

namespace StorageLib.Linux;

[SupportedOSPlatform("linux")]
public class StorageAtaLinuxImports {
    public const string libName = "libata";

    [DllImport(libName, SetLastError = true)]
    public static extern int ioctl(SafeFileHandle fd, uint op, IntPtr data);

    [DllImport(libName, SetLastError = true)]
    public static extern int ata_read_log_page(SafeFileHandle fd, byte log, byte page, IntPtr buf, uint sectors);
}