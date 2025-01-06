using Microsoft.Win32.SafeHandles;
using System.Runtime.InteropServices;

namespace StorageWin;
public class StorageWinHelpers {
    public static uint CTL_CODE(uint deviceType, uint function, uint method, uint access) {
        return ((deviceType << 16) | (access << 14) | (function << 2) | method);
    }
    public static uint CTL_CODE(uint deviceType, uint function, StorageWinConstants.IoctlMethodCodes method, StorageWinConstants.IoctlFileAccess access) {
        return CTL_CODE(deviceType, function, (uint)method, (uint)access);
    }

    public static SafeFileHandle OpenDevice(string devicePath) {
        //SafeFileHandle hDevice = MyWorkingKernelImports.CreateFile(devicePath, (uint)FileAccess.ReadWrite, (uint)FileShare.ReadWrite,
        //    IntPtr.Zero, (uint)FileMode.Open, (uint)FileAttributes.Normal, IntPtr.Zero);
        SafeFileHandle hDevice = new();
        try {
            hDevice = File.OpenHandle(devicePath, FileMode.Open, FileAccess.Read, FileShare.Read);
        } catch (Exception e) { // Any error should result in the handle being set to invalid
            hDevice.SetHandleAsInvalid();
        }
        return hDevice;
    }
}
