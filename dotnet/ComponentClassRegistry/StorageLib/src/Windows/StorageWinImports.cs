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

    public static Task<Tuple<int, string, string>> Powershell(string arguments) {
        char ch = '"'; // couldn't get escaping to work properly without this method
        ProcessStartInfo info = new() {
            FileName = "powershell.exe",
            Arguments = "-NoProfile -ExecutionPolicy Bypass -Command " + ch + arguments + ch,
            RedirectStandardOutput = true,
            RedirectStandardError = true,
            UseShellExecute = false,
            CreateNoWindow = true
        };
        return Execute(info);
    }

    private static Task<Tuple<int, string, string>> Execute(ProcessStartInfo info) {
        TaskCompletionSource<Tuple<int, string, string>> source = new();
        Process process = new() {
            StartInfo = info,
            EnableRaisingEvents = true
        };

        process.Exited += (sender, args) => {
            source.SetResult(new Tuple<int, string, string>(process.ExitCode, process.StandardError.ReadToEnd(), process.StandardOutput.ReadToEnd()));
            if (process.ExitCode != 0) {
                source.SetException(new Exception($"Command `{process.StartInfo.FileName} {process.StartInfo.Arguments}` failed with exit code `{process.ExitCode}`"));
            }

            process.Dispose();
        };

        try {
            process.Start();
            process.WaitForExit();
        } catch (Exception e) {
            source.SetException(e);
        } finally {
            process.Dispose();
        }

        return source.Task;
    }
}
