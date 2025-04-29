using System.Runtime.InteropServices;

namespace Smbios;
public class LinuxImports {
    /// <summary>
    /// This method is imported to query the Linux Kernel whether the program was run with privileges.
    /// </summary>
    /// <returns>The Euid.</returns>
    [DllImport("libc", SetLastError = true)]
    internal static extern uint geteuid();
}