using Microsoft.Win32.SafeHandles;
using System.Diagnostics;
using System.Runtime.InteropServices;

namespace StorageLib;
public static class StorageCommonHelpers {
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

    internal static Task<Tuple<int, string, string>> Execute(ProcessStartInfo info) {
        TaskCompletionSource<Tuple<int, string, string>> source = new();

        using Process process = new();
        process.StartInfo = info;
        process.EnableRaisingEvents = true;

        try {
            process.Start();

            Task<string> outputTask = process.StandardOutput.ReadToEndAsync();

            Task<string> errorTask = process.StandardError.ReadToEndAsync();

            process.WaitForExit();

            string output = outputTask.GetAwaiter().GetResult();
            string error = errorTask.GetAwaiter().GetResult();

            int exitCode = process.ExitCode;

            if (exitCode == 0) {
                source.SetResult(new Tuple<int, string, string>(exitCode, error, output));
            } else {
                if (error.IsWhiteSpace()) {
                    error = "<empty>";
                }
                error = "Error message: " + error;
                source.SetException(new Exception($"Command `{info.FileName} {info.Arguments}` failed with exit code `{exitCode}`. {error}"));
            }
        } catch (Exception e) {
            source.SetException(e);
        }

        return source.Task;
    }
}
