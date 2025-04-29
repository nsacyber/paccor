using Microsoft.Win32.SafeHandles;
using System.Diagnostics;
using System.Runtime.InteropServices;
using System.Runtime.Versioning;

namespace StorageLib.Windows;

[SupportedOSPlatform("windows")]
public class StorageWinImports {
    public const string kernelDll = "kernel32.dll";

    [DllImport(kernelDll, ExactSpelling = true, SetLastError = true)]
    public static extern bool DeviceIoControl(SafeFileHandle hDevice, uint dwIoControlCode, IntPtr lpInBuffer, int nInBufferSize, IntPtr lpOutBuffer, int nOutBufferSize, ref int lpBytesReturned, ref NativeOverlapped lpOverlapped);

    public async static Task<Tuple<int, string, string>> PowershellNumPhysicalDisks() {
        return await Powershell("((Get-PhysicalDisk).DeviceId).Count");
    }
    private static Task<Tuple<int, string, string>> Powershell(string arguments) {
        const char ch = '"'; // couldn't get escaping to work properly without this method
        ProcessStartInfo info = new() {
            FileName = "powershell.exe",
            Arguments = "-NoProfile -ExecutionPolicy Bypass -Command " + ch + arguments + ch,
            RedirectStandardOutput = true,
            RedirectStandardError = true,
            UseShellExecute = false,
            CreateNoWindow = true
        };
        return StorageCommonHelpers.Execute(info);
    }
}
