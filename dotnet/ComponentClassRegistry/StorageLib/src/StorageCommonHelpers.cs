using Microsoft.Win32.SafeHandles;
using System.Runtime.InteropServices;

namespace StorageLib;
public class StorageCommonHelpers {
    // Expects unmanaged memory of given len to be allocated to ptr
    internal static void ZeroMemory(IntPtr ptr, int len) {
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

    public static SafeFileHandle OpenDevice(string devicePath) {
        SafeFileHandle handle = new();
        try {
            handle = File.OpenHandle(devicePath, FileMode.Open, FileAccess.ReadWrite, FileShare.ReadWrite);
        } catch (FileNotFoundException) { // Any error should result in the handle being set to invalid
            handle.SetHandleAsInvalid();
        }

        if (!IsDeviceHandleReady(handle)) { // Also ensure handle is not closed
            handle.SetHandleAsInvalid();
        }
        return handle;
    }

    public static bool IsDeviceHandleReady(SafeFileHandle handle) {
        return handle is { IsInvalid: false, IsClosed: false };
    }

    public static byte[] Reverse(byte[] data) {
        byte[] clone = (byte[])data.Clone();
        Array.Reverse(clone);
        return clone;
    }
}
