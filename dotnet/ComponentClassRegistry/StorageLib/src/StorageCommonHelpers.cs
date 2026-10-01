using Microsoft.Win32.SafeHandles;
using System.Runtime.InteropServices;

namespace StorageLib;
internal static class StorageCommonHelpers {
    // Expects unmanaged memory of given len to be allocated to ptr
    public static void ZeroMemory(IntPtr ptr, int len) {
        for (int i = 0; i < len; i++) {
            Marshal.WriteByte(ptr, i, 0);
        }
    }

    public static T CreateStruct<T>() where T : struct {
        int size = Marshal.SizeOf<T>();
        IntPtr ptr = Marshal.AllocHGlobal(size);
        try {
            // Initialize memory to zero  
            ZeroMemory(ptr, size);
            T result = Marshal.PtrToStructure<T>(ptr); // This object becomes managed and doesn't need to be freed later
            return result;
        } finally {
            Marshal.FreeHGlobal(ptr); // The buffer can be safely freed after the structure is created
        }
    }

    public static T CreateStruct<T>(byte[] buffer) where T : struct {
        int size = Marshal.SizeOf<T>();

        // if buffer is larger than size, the extra bytes from buffer are ignored
        // if buffer is smaller than size, the extra bytes in size are zeroed

        IntPtr ptr = Marshal.AllocHGlobal(size);
        try {
            ZeroMemory(ptr, size);
            Marshal.Copy(buffer, 0, ptr, size);
            T result = Marshal.PtrToStructure<T>(ptr); // This object becomes managed and doesn't need to be freed later
            return result;
        } finally {
            Marshal.FreeHGlobal(ptr); // The buffer can be safely freed after the structure is created
        }
    }

    public static byte[] CreateByteArray<T>(T obj) where T : struct {
        int size = Marshal.SizeOf<T>();

        IntPtr ptr = Marshal.AllocHGlobal(size);
        try {
            ZeroMemory(ptr, size);
            Marshal.StructureToPtr(obj, ptr, true);
            byte[] buffer = ConvertIntPtrToByteArray(ptr, size);
            return buffer;
        } finally {
            Marshal.FreeHGlobal(ptr);
        }
    }

    public static byte[] ConvertIntPtrToByteArray(IntPtr ptr, int size) {
        if (ptr == IntPtr.Zero || size == 0) {
            return Array.Empty<byte>();
        }
        byte[] buffer = new byte[size];
        Marshal.Copy(ptr, buffer, 0, size);
        return buffer;
    }

    private static readonly FileAccess[] DeviceAccessOrder = OperatingSystem.IsWindows()
        ? [FileAccess.ReadWrite, FileAccess.Read]
        : [FileAccess.Read, FileAccess.ReadWrite];

    public static SafeFileHandle OpenDevice(string devicePath) {
        foreach (FileAccess access in DeviceAccessOrder) {
            try {
                SafeFileHandle handle = File.OpenHandle(devicePath, FileMode.Open, access, FileShare.ReadWrite);
                if (IsDeviceHandleReady(handle)) {
                    return handle;
                }
            } catch (FileNotFoundException) {
                break; // The device does not exist. Another access mode will not help.
            } catch (Exception e) when (e is IOException or UnauthorizedAccessException) {
                // Try the next access mode
            }
        }

        SafeFileHandle invalid = new();
        invalid.SetHandleAsInvalid();
        return invalid;
    }

    public static bool IsDeviceHandleReady(SafeFileHandle handle) {
        return handle is { IsInvalid: false, IsClosed: false };
    }
}
