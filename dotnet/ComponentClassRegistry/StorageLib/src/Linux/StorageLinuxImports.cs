using Microsoft.Win32.SafeHandles;
using System.Diagnostics;
using System.Runtime.InteropServices;
using System.Runtime.Versioning;

namespace StorageLib.Linux;

[SupportedOSPlatform("linux")]
public class StorageLinuxImports {
    public const string libcName = "libc";

    [DllImport(libcName, SetLastError = true)]
    public static extern int ioctl(SafeFileHandle fd, uint op, IntPtr data);

    public async static Task<Tuple<int, string, string>> LsblkPhysicalDisks() {
        return await Bash("lsblk -d -n -o NAME,MAJ:MIN -p");
    }

    public async static Task<Tuple<int, string, string>> ListDisksById() {
        return await Bash("find /dev/disk/by-id -type l ! -name \"*-part*\" -a \\( -name \"ata-*\" -o -name \"scsi-*\" -o -name \"nvme-*\" \\) -exec readlink -nf {} ';' -exec echo \",{}\" ';' 2> /dev/null");
    }
    
    private static Task<Tuple<int, string, string>> Bash(string arguments) {
        ProcessStartInfo info = new() {
            FileName = "bash",
            Arguments = $"-c \"{arguments}\"",
            RedirectStandardOutput = true,
            RedirectStandardError = true,
            UseShellExecute = false,
            CreateNoWindow = true
        };
        return StorageCommonHelpers.Execute(info);
    }
}
