using Microsoft.Win32.SafeHandles;
using System.Runtime.InteropServices;
using System.Runtime.Versioning;

namespace StorageLib.Windows;

[SupportedOSPlatform("windows")]
internal class StorageWinImports {
    public const string kernelDll = "kernel32.dll";

    [DllImport(kernelDll, ExactSpelling = true, SetLastError = true)]
    public static extern bool DeviceIoControl(SafeFileHandle hDevice, uint dwIoControlCode, IntPtr lpInBuffer, int nInBufferSize, IntPtr lpOutBuffer, int nOutBufferSize, ref int lpBytesReturned, ref NativeOverlapped lpOverlapped);
}
